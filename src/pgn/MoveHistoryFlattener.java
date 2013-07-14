package pgn;

import java.util.List;

import domain.*;

/**
 * Takes a MoveHistory and produces a list of tokens describing the game.
 */
public class MoveHistoryFlattener {	
	public interface MoveHistoryVisitor {
		void beginVariation();
		void endVariation();
		void move(String text, MoveHistoryNode move, boolean currentPosition);
		void end();
	}

	private MoveHistory moveHistory;
	
	public MoveHistoryFlattener(MoveHistory moveHistory) {
		this.moveHistory = moveHistory;
	}
	
	public void getMoveTokens(MoveHistoryVisitor visitor) {
		addMoveTokens(moveHistory.getInitialPosition(), true, visitor, moveHistory.getRootNode());
		visitor.end();
	}

	private void addMoveTokens(Board board, boolean firstMove, MoveHistoryVisitor visitor, MoveHistoryNode node) {
		List<? extends MoveHistoryNode> nextNodes = node.getNextNodes();
		
		if(nextNodes.size() == 0) {
			return;
		}
		
		String moveNumberText = "";
		
		int moveNumber = board.getFullMoves();
		if(board.getSideToPlay() == Side.WHITE) {
			moveNumberText = moveNumber + ". ";
		} else if(firstMove) {
			moveNumberText = moveNumber + "... ";
		}
		
		boolean firstNode = true;
		for(MoveHistoryNode next:nextNodes) {
			if(!firstNode) {
				visitor.beginVariation();
				if(board.getSideToPlay() == Side.WHITE) {
					moveNumberText = moveNumber + ". ";
				} else {
					moveNumberText = moveNumber + "... ";
				}
			}
			
			try {
				Move move = next.getMove();
				String pgn = board.getMoveAsPgn(move);
				visitor.move(moveNumberText + pgn, next, moveHistory.isCurrentPosition(next));
				
				if(!(nextNodes.size() > 1 && firstNode)) {
					addMoveTokens(board.makeMove(move), false, visitor, next);
				}
			} catch(IllegalMoveException e) {
				throw new IllegalStateException("Invalid Move", e);
			}

			if(!firstNode) {
				visitor.endVariation();
			}
			firstNode = false;
			
			moveNumberText = "";
		}
		
		if(nextNodes.size() > 1) {
			try {
				MoveHistoryNode next = nextNodes.get(0);
				addMoveTokens(board.makeMove(next.getMove()), false, visitor, next);
			} catch(IllegalMoveException e) {
				throw new IllegalStateException("Invalid Move", e);
			}
		}
	}
}
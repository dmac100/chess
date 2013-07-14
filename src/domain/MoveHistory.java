package domain;

import java.util.*;

public class MoveHistory {
	private MutableMoveHistoryNode root = new MutableMoveHistoryNode(null, null);
	private MutableMoveHistoryNode position = root;
	private Board initialPosition = new Board();

	public void setMoves(Board initialPosition, MoveHistoryNode moves) {
		this.root = new MutableMoveHistoryNode(moves);
		this.position = root;
		this.initialPosition = initialPosition;
	}
	
	public void setMoves(MoveHistoryNode moves) {
		this.root = new MutableMoveHistoryNode(moves);
		this.position = root;
	}
	
	public void setInitialPosition(Board initialPosition) {
		this.initialPosition = initialPosition;
		this.root = new MutableMoveHistoryNode(null, null);
		this.position = root;
	}
	
	private int getPositionIndex() {
		int index = 0;
		for(MoveHistoryNode node = position; node.getMove() != null; node = node.getParent()) {
			index++;
		}
		return index;
	}
	
	public Board getCurrentPosition() {
		Board board = initialPosition;
		for(Move move:getMoves().subList(0, getPositionIndex())) {
			try {
				board = board.makeMove(move);
			} catch(IllegalMoveException e) {
				throw new RuntimeException("Invalid move in move list", e);
			}
			
		}
		return board;
	}
	
	public boolean isCurrentPosition(MoveHistoryNode node) {
		return position == node;
	}
	
	public List<Move> getMoves() {
		List<Move> moves = new ArrayList<Move>();
		MoveHistoryNode node = position;
		while(node.getMove() != null) {
			moves.add(node.getMove());
			node = node.getParent();
		}
		Collections.reverse(moves);

		node = position;
		while(node.getNextNodes().size() > 0) {
			node = node.getNextNodes().get(0);
			moves.add(node.getMove());
		}
		
		return moves;
	}
	
	public void makeMove(Move move) throws IllegalMoveException {
		for(MutableMoveHistoryNode node:position.getNextNodes()) {
			if(node.getMove().equals(move)) {
				position = node;
				return;
			}
		}
		
		Board board = getCurrentPosition();
		if(!board.isPossibleMove(move)) {
			throw new IllegalMoveException();
		}
		getCurrentPosition().makeMove(move);
		
		MutableMoveHistoryNode nextNode = new MutableMoveHistoryNode(move, position);
		position.getNextNodes().add(nextNode);
		position = nextNode;
	}
	
	public void next() {
		if(position.getNextNodes().size() > 0) {
			position = position.getNextNodes().get(0);
		}
	}
	
	public void prev() {
		if(position.getParent() != null) {
			position = position.getParent();
		}
	}

	public void first() {
		position = root;
	}

	public void last() {
		position = root;
		while(position.getNextNodes().size() > 0) {
			position = position.getNextNodes().get(0);
		}
	}

	public void setPosition(MoveHistoryNode position) {
		// Find indexes of each variation up to position.
		List<Integer> indexes = new ArrayList<Integer>();
		for(MoveHistoryNode current = position; current.getParent() != null; current = current.getParent()) {
			int index = current.getParent().getNextNodes().indexOf(current);
			indexes.add(index);
		}
		Collections.reverse(indexes);
		
		// Follow indexes starting from the root position.
		this.position = root;
		for(Integer index:indexes) {
			this.position = this.position.getNextNodes().get(index);
		}
	}

	public boolean blackHasFirstMove() {
		return (initialPosition.getSideToPlay() == Side.BLACK);
	}

	public Board getInitialPosition() {
		return initialPosition;
	}
	
	public MoveHistoryNode getRootNode() {
		return root;
	}

	public List<Move> getVarations() {
		List<Move> moves = new ArrayList<Move>();
		for(MoveHistoryNode node:position.getNextNodes()) {
			moves.add(node.getMove());
		}
		return moves;
	}

	public void deleteCurrentVariation() {
		MutableMoveHistoryNode current = position;
		while(current.getParent() != null) {
			List<MutableMoveHistoryNode> siblings = current.getParent().getNextNodes();
			if(siblings.size() > 1) {
				if(siblings.indexOf(current) == 0) {
					// Don't delete main variation.
					return;
				}
				siblings.remove(current);
				return;
			}
			current = current.getParent();
		}
	}
	
	public void promoteCurrentVariation() {
		MutableMoveHistoryNode current = position;
		while(current.getParent() != null) {
			List<MutableMoveHistoryNode> siblings = current.getParent().getNextNodes();
			if(siblings.indexOf(current) > 0) {
				siblings.remove(current);
				siblings.add(0, current);
				return;
			}
			current = current.getParent();
		}
	}
	
	public void trimVariation() {
		position.clearNextNodes();
	}

	public void setCurrentMoveComment(String comment) {
		position.setComment(comment);
	}

	public String getCurrentMoveComment() {
		return position.getComment();
	}
}

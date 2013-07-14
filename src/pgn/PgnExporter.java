package pgn;

import pgn.MoveHistoryFlattener.MoveHistoryVisitor;
import domain.*;

public class PgnExporter {
	public String exportPgn(Board initialPosition, MoveHistoryNode moves) throws IllegalMoveException {
		MoveHistory moveHistory = new MoveHistory();
		moveHistory.setInitialPosition(initialPosition);
		moveHistory.setMoves(moves);
		return exportPgn(moveHistory);
	}
	
	public String exportPgn(MoveHistory moveHistory) throws IllegalMoveException {
		final StringBuilder pgn = new StringBuilder();
		
		Board initialPosition = moveHistory.getInitialPosition();
		if(!initialPosition.equals(new Board())) {
			pgn.append("[FEN \"" + initialPosition.getFen() + "\"]\n");
			pgn.append("[SetUp \"1\"]\n");
			pgn.append("\n");
		}
		
		new MoveHistoryFlattener(moveHistory).getMoveTokens(new MoveHistoryVisitor() {
			@Override
			public void move(String text, MoveHistoryNode move, boolean currentPosition) {
				pgn.append(text);
				if(move.getAnnotation() != null) {
					pgn.append(convertAnnotation(move.getAnnotation()));
				}
				if(move.getComment() != null) {
					pgn.append(" {");
					pgn.append(move.getComment());
					pgn.append("}");
				}
				pgn.append(" ");
			}
			
			@Override
			public void beginVariation() {
				pgn.append("(");
			}
			
			@Override
			public void endVariation() {
				pgn.deleteCharAt(pgn.length() - 1);
				pgn.append(") ");
			}
			
			@Override
			public void end() {
			}
		});
		
		return pgn.toString().trim();
	}

	private String convertAnnotation(String annotation) {
		if(annotation.equals(" (=)")) return " $10";
		if(annotation.equals(" (+-)")) return " $18";
		if(annotation.equals(" (-+)")) return " $19";
		return annotation;
	}
}

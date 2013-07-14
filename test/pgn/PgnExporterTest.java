package pgn;

import static org.junit.Assert.*;

import org.junit.Test;

import domain.*;

public class PgnExporterTest {
	private static MoveHistoryNode getMovesFromPgn(Board initialPosition, String... pgnMoves) throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		history.setInitialPosition(initialPosition);
		
		for(String pgnMove:pgnMoves) {
			history.makeMove(history.getCurrentPosition().getPgnMove(pgnMove));
		}
		
		return history.getRootNode();
	}
	
	@Test
	public void exportPgn() throws IllegalMoveException {
		Board board = new Board();
		
		MoveHistoryNode moves = getMovesFromPgn(board, "e4", "e6", "e5");
		
		String pgn = new PgnExporter().exportPgn(new Board(), moves);
		
		assertEquals("1. e4 e6 2. e5", pgn);
	}
	
	@Test
	public void exportPgn_newInitialPosition() throws IllegalMoveException {
		String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
		
		Board board = new Board(fen);
		
		MoveHistoryNode moves = getMovesFromPgn(board, "e5", "d4", "Nf6");
		
		String pgn = new PgnExporter().exportPgn(board, moves);
		
		assertEquals("[FEN \""+fen+"\"]\n[SetUp \"1\"]\n\n1... e5 2. d4 Nf6", pgn);
	}
	
	@Test
	public void exportPgn_variation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		history.makeMove(history.getCurrentPosition().getPgnMove("e4"));
		history.makeMove(history.getCurrentPosition().getPgnMove("e5"));
		history.makeMove(history.getCurrentPosition().getPgnMove("d4"));
		history.makeMove(history.getCurrentPosition().getPgnMove("d5"));
		history.prev();
		history.makeMove(history.getCurrentPosition().getPgnMove("d6"));
		history.prev();
		history.makeMove(history.getCurrentPosition().getPgnMove("c6"));
		history.prev();
		history.prev();
		history.makeMove(history.getCurrentPosition().getPgnMove("d3"));
		history.makeMove(history.getCurrentPosition().getPgnMove("d5"));
		
		String pgn = new PgnExporter().exportPgn(history);
		
		assertEquals("1. e4 e5 2. d4 (2. d3 d5) d5 (2... d6) (2... c6)", pgn);
	}
}

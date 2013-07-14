package domain;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import pgn.ParseException;
import pgn.PgnImporter;

public class MoveHistoryTest {
	/**
	 * Makes a sequence of pgn moves and adds them to the history.
	 */
	private static void makePgnMoves(MoveHistory history, String... pgnMoves) throws IllegalMoveException {
		for(String pgnMove:pgnMoves) {
			Board board = history.getCurrentPosition();
			history.makeMove(board.getPgnMove(pgnMove));
		}
	}
	
	/**
	 * Makes a sequence of pgn moves and returns the resulting position.
	 */
	private static Board getBoard(String... pgnMoves) throws IllegalMoveException {
		Board board = new Board();
		for(String pgnMove:pgnMoves) {
			board = board.makePgnMove(pgnMove);
		}
		return board;
	}
	
	@Test
	public void makeMoves() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		assertEquals(history.getCurrentPosition(), getBoard("e4", "e5", "Nf3", "Nc6"));
	}
	
	@Test
	public void prevMove() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		
		assertEquals(history.getCurrentPosition(), getBoard("e4", "e5", "Nf3"));
	}
	
	@Test
	public void nextMove() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.next();
		
		assertEquals(history.getCurrentPosition(), getBoard("e4", "e5", "Nf3", "Nc6"));
	}
	
	@Test
	public void overrideMove() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.makeMove(history.getCurrentPosition().getPgnMove("Nf6"));
		
		assertEquals(history.getCurrentPosition(), getBoard("e4", "e5", "Nf3", "Nf6"));
	}
	
	@Test
	public void addVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.makeMove(history.getCurrentPosition().getPgnMove("Nf6"));
		
		history.prev();
		List<Move> variations = history.getVarations();
		
		assertEquals(2, variations.size());
		assertEquals("Nc6", history.getCurrentPosition().getMoveAsPgn(variations.get(0)));
		assertEquals("Nf6", history.getCurrentPosition().getMoveAsPgn(variations.get(1)));
	}
	
	@Test
	public void followMainVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.prev();
		
		makePgnMoves(history, "d4", "d5");
		
		assertEquals(getBoard("e4", "e5", "d4", "d5"), history.getCurrentPosition());
		
		history.prev();
		history.prev();
		
		history.makeMove(history.getCurrentPosition().getPgnMove("Nf3"));
		history.next();

		assertEquals(getBoard("e4", "e5", "Nf3", "Nc6"), history.getCurrentPosition());
	}
	
	@Test
	public void followVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.prev();
		
		makePgnMoves(history, "d4", "d5");
		
		assertEquals(getBoard("e4", "e5", "d4", "d5"), history.getCurrentPosition());
		
		history.prev();
		history.prev();
		
		history.makeMove(history.getCurrentPosition().getPgnMove("d4"));
		history.next();

		assertEquals(getBoard("e4", "e5", "d4", "d5"), history.getCurrentPosition());
	}
	
	@Test
	public void promoteVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.prev();
		
		makePgnMoves(history, "d4", "d5");
		
		history.promoteCurrentVariation();
		
		history.prev();
		history.prev();
		
		List<Move> variations = history.getVarations();
		
		assertEquals("d4", history.getCurrentPosition().getMoveAsPgn(variations.get(0)));
		assertEquals("Nf3", history.getCurrentPosition().getMoveAsPgn(variations.get(1)));
		
		history.next();
		history.next();
		
		assertEquals(getBoard("e4", "e5", "d4", "d5"), history.getCurrentPosition());
	}
	
	@Test
	public void deleteVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.prev();
		
		makePgnMoves(history, "d4", "d5");
		
		history.deleteCurrentVariation();
		
		history.prev();
		history.prev();
		
		List<Move> variations = history.getVarations();
		assertEquals(1, variations.size());
		assertEquals("Nf3", history.getCurrentPosition().getMoveAsPgn(variations.get(0)));
		
		history.next();
		history.next();
		
		assertEquals(getBoard("e4", "e5", "Nf3", "Nc6"), history.getCurrentPosition());
	}
	
	@Test
	public void deleteVariation_mainVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.prev();
		
		makePgnMoves(history, "d4", "d5");
		
		history.prev();
		history.prev();
		
		history.next();
		
		history.deleteCurrentVariation();
		
		history.prev();
		
		assertEquals(2, history.getVarations().size());
	}
	
	@Test
	public void trimVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		history.prev();
		history.prev();
		
		history.trimVariation();
		
		assertEquals(2, history.getMoves().size());
	}
	
	@Test
	public void setPosition() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		
		MoveHistoryNode node = history.getRootNode();
		node = node.getNextNodes().get(0);
		node = node.getNextNodes().get(0);
		node = node.getNextNodes().get(0);

		history.setPosition(node);
		
		assertEquals(getBoard("e4", "e5", "Nf3"), history.getCurrentPosition());
		assertEquals(4, history.getMoves().size());
	}
	
	@Test
	public void setMoves() throws ParseException {
		MoveHistory history = new MoveHistory();
		
		MoveHistoryNode treeNode = new PgnImporter().importPgn("1. e4 e5 2. Nf3 Nc6").getMoves();
		history.setMoves(treeNode);
		
		history.next();
		history.next();
		
		assertEquals("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2", history.getCurrentPosition().getFen());
	}
	
	@Test
	public void editComment() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();

		makePgnMoves(history, "e4", "e5");
		
		history.next();
		history.setCurrentMoveComment("Comment");
		
		assertEquals(null, history.getRootNode().getNextNodes().get(0).getComment());
		assertEquals("Comment", history.getRootNode().getNextNodes().get(0).getNextNodes().get(0).getComment());
	}
}
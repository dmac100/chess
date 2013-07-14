package domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class PiecesTest {
	@Test
	public void pawnMoves() {
		Board board = new Board("K1k5/8/8/8/8/8/P7/8 w - - 0 1");
		assertEquals(5, board.getPossibleMoves().size());
	}
	
	@Test
	public void pawnCaptures() {
		Board board = new Board("K1k5/8/8/8/8/1r6/P7/8 w - - 0 1");
		assertEquals(6, board.getPossibleMoves().size());
	}
	
	@Test
	public void rookMoves() {
		Board board = new Board("K1k5/8/8/8/8/8/R7/8 w - - 0 1");
		assertEquals(16, board.getPossibleMoves().size());
	}
	
	@Test
	public void bishopMoves() {
		Board board = new Board("K1k5/8/8/8/8/8/B7/8 w - - 0 1");
		assertEquals(10, board.getPossibleMoves().size());
	}
	
	@Test
	public void knightMoves() {
		Board board = new Board("K1k5/8/8/8/8/8/N7/8 w - - 0 1");
		assertEquals(6, board.getPossibleMoves().size());
	}
	
	@Test
	public void queenMoves() {
		Board board = new Board("K1k5/8/8/8/8/8/Q7/8 w - - 0 1");
		assertEquals(23, board.getPossibleMoves().size());
	}
	
	@Test
	public void kingMoves() {
		Board board = new Board("k7/8/8/8/8/8/K7/8 w - - 0 1");
		assertEquals(5, board.getPossibleMoves().size());
	}
}

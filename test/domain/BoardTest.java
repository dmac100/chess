package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoardTest {
	@Test
	public void initialPosition() {
		Board board = new Board();
		String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void setFen() {
		String fen = "rnbqk2r/ppppppp1/8/8/8/8/PPPPP2P/RNBQK1NR w Kq g3 3 2";
		Board board = new Board(fen);
		assertEquals(fen, board.getFen());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setFen_invalidPosition() {
		Board board = new Board("8/8/8/8/8/8/8/8 b kqKQ - 0 0");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setFen_InvalidColumnCount() {
		String fen = "4/ppppppp1/8/8/8/8/PPPPP2P/RNBQK1NR w Kq g3 3 2";
		Board board = new Board(fen);
		assertEquals(fen, board.getFen());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setFen_InvalidPiece() {
		String fen = "8/pppppppO/8/8/8/8/PPPPP2P/RNBQK1NR w Kq g3 3 2";
		Board board = new Board(fen);
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void makeInitialMove() throws IllegalMoveException {
		Board board = new Board();
		board = board.makeMove(new Move(new Square("e2"), new Square("e4")));
		String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void makeInitialPgnMove() throws IllegalMoveException {
		Board board = new Board();
		board = board.makePgnMove("e4");
		String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
		
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void makeOpeningPgnMoves() throws IllegalMoveException {
		Board board = new Board();
		board = board.makePgnMove("e4");
		board = board.makePgnMove("c5");
		board = board.makePgnMove("Nf3");
		String fen = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2";
		
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void makeAmbiguousMove_rank() throws IllegalMoveException {
		Board board = new Board("k1K5/8/8/8/N7/8/N7/8 w - - 0 1");
		board = board.makePgnMove("N2c3");
		String fen = "k1K5/8/8/8/N7/2N5/8/8 b - - 1 1";
		
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void makeAmbiguousMove_file() throws IllegalMoveException {
		Board board = new Board("k1K5/8/8/8/8/8/8/N1N5 w - - 0 1");
		board = board.makePgnMove("Nab3");
		String fen = "k1K5/8/8/8/8/1N6/8/2N5 b - - 1 1";
		
		assertEquals(fen, board.getFen());
	}
	
	@Test
	public void checkmate_notInCheck() {
		Board board = new Board("k7/ppp5/7R/8/8/8/8/7K b - - 0 1");
		assertFalse(board.isCheckmate());
	}
	
	@Test
	public void checkmate_backRank() {
		Board board = new Board("k6R/ppp5/8/8/8/8/8/7K b - - 0 1");
		assertTrue(board.isCheckmate());
	}
	
	@Test
	public void checkmate_escape() {
		Board board = new Board("k6R/1pp5/8/8/8/8/8/7K b - - 0 1");
		assertFalse(board.isCheckmate());
	}
	
	@Test
	public void checkmate_block() {
		Board board = new Board("k6R/ppp5/5r2/8/8/8/8/7K b - - 0 1");
		assertFalse(board.isCheckmate());
	}
	
	@Test
	public void checkmate_capture() {
		Board board = new Board("k6R/ppp5/5b2/8/8/8/8/7K b - - 0 1");
		assertFalse(board.isCheckmate());
	}
	
	@Test
	public void checkmate_blockCaptureFriendly() {
		Board board = new Board("k6R/ppp5/5BR1/8/8/8/8/7K b - - 0 1");
		assertTrue(board.isCheckmate());
	}
	
	@Test
	public void castle_whiteKingSide() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/8/R3K2R w K - 0 1");
		board = board.makePgnMove("0-0");
		assertEquals("1k6/8/8/8/8/8/8/R4RK1 b - - 1 1", board.getFen());
	}

	@Test(expected=IllegalMoveException.class)
	public void castle_notAllowed() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/8/R3K2R w - - 0 1");
		board = board.makePgnMove("0-0");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void castle_inCheck() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/4r3/R3K2R w K - 0 1");
		board = board.makePgnMove("0-0");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void castle_through() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/5r2/R3K2R w K - 0 1");
		board = board.makePgnMove("0-0");
	}

	@Test(expected=IllegalMoveException.class)
	public void castle_toOccupied() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/8/3RK1NR w K - 0 1");
		board = board.makePgnMove("0-0");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void castle_throughOccupied() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/8/3RKB1R w K - 0 1");
		board = board.makePgnMove("0-0");
	}
	
	@Test
	public void promote() throws IllegalMoveException {
		Board board = new Board("8/7P/8/8/8/8/8/k1K5 w - - 0 1");
		board = board.makePgnMove("h8=Q");
		assertEquals("7Q/8/8/8/8/8/8/k1K5 b - - 0 1", board.getFen());
	}
	
	@Test(expected=IllegalMoveException.class)
	public void promote_noPromotion() throws IllegalMoveException {
		Board board = new Board("8/7P/8/8/8/8/8/k1K5 w - - 0 1");
		board = board.makePgnMove("h8");
	}
	
	@Test
	public void getMoveAsPgn() throws IllegalMoveException {
		String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
		Board board = new Board(fen);
		assertEquals("e4", board.getMoveAsPgn(new Move(new Square("e2"), new Square("e4"))));
		assertEquals("Nf3", board.getMoveAsPgn(new Move(new Square("g1"), new Square("f3"))));
	}
	
	@Test
	public void getMoveAsPgn_ambiguousRank() throws IllegalMoveException {
		Board board = new Board("8/8/8/8/N7/8/N7/k1K5 w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("a4"), new Square("c3")));
		assertEquals("N4c3", pgn);
	}
	
	@Test
	public void getMoveAsPgn_ambiguousFile() throws IllegalMoveException {
		Board board = new Board("k1K5/8/8/8/8/8/8/N1N5 w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("a1"), new Square("b3")));
		assertEquals("Nab3", pgn);
	}
	
	@Test
	public void getMoveAsPgn_ambiguousRowOrFile() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/7R/8/K4R2 w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("f1"), new Square("h1")));
		assertEquals("Rfh1", pgn);
	}
	
	@Test
	public void getMoveAsPgn_ambiguousRowAndFile() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/6R1/8/K4R1R w - - 0 1");
		
		String pgn = board.getMoveAsPgn(new Move(new Square("f1"), new Square("g1")));
		assertEquals("Rf1g1", pgn);
	}
	
	@Test
	public void getMoveAsPgn_ambiguousDifferentPiece() throws IllegalMoveException {
		Board board = new Board("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2");
		String pgn = board.getMoveAsPgn(new Move(new Square("g1"), new Square("f3")));
		assertEquals("Nf3", pgn);
	}
	
	@Test
	public void getMoveAsPgn_capture() throws IllegalMoveException {
		Board board = new Board("k1K5/8/8/4p3/3P4/8/8/8 w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("d4"), new Square("e5")));
		assertEquals("dxe5", pgn);
	}

	
	@Test
	public void getMoveAsPgn_checkmate() throws IllegalMoveException {
		Board board = new Board("k7/ppp5/8/8/8/8/8/6KR w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("h1"), new Square("h8")));
		assertEquals("Rh8#", pgn);
	}
	
	@Test
	public void getMoveAsPgn_check() throws IllegalMoveException {
		Board board = new Board("k7/1pp5/8/8/8/8/8/6KR w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("h1"), new Square("h8")));
		assertEquals("Rh8+", pgn);
	}
	
	@Test
	public void getMoveAsPgn_promotion() throws IllegalMoveException {
		Board board = new Board("8/7P/8/8/8/8/8/1k1K4 w - - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("h7"), new Square("h8"), PromotionChoice.QUEEN));
		assertEquals("h8=Q", pgn);
	}
	
	@Test
	public void getMoveAsPgn_castle() throws IllegalMoveException {
		Board board = new Board("1k6/8/8/8/8/8/8/R3K2R w K - 0 1");
		String pgn = board.getMoveAsPgn(new Move(new Square("e1"), new Square("g1"), true));
		assertEquals("O-O", pgn);
	}
	
	@Test
	public void isPossibleMove_yes() {
		Board board = new Board();
		assertTrue(board.isPossibleMove(new Move(new Square("e2"), new Square("e4"))));
	}
	
	@Test
	public void isPossibleMove_no() {
		Board board = new Board();
		assertFalse(board.isPossibleMove(new Move(new Square("e2"), new Square("e5"))));
	}
	
	@Test
	public void makeMove_kingNotInCheckByPawnMovingForward() throws IllegalMoveException {
		Board board = new Board("8/8/6k1/8/4P1P1/8/8/K7 b - - 1 0");
		board.makePgnMove("Kg5");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void makeMove_pawnCannotCaptureByMovingForward() throws IllegalMoveException {
		Board board = new Board("k7/8/p7/P7/8/8/8/K7 w - - 1 0");
		board.makePgnMove("a6");
	}
	
	@Test
	public void makeMove_enPassantRemovesPiece() throws IllegalMoveException {
		Board board = new Board("k7/8/8/Pp6/8/8/8/K7 w - b6 1 0");
		
		board = board.makePgnMove("axb6");
		String fen = board.getFen();
		
		assertEquals("k7/8/1P6/8/8/8/8/K7 b - - 0 0", fen);
	}
	
	@Test
	public void makeMove_kingNotInCheckByCastling() throws IllegalMoveException {
		Board board = new Board("8/8/8/8/4k1K1/8/7P/8 w - - 1 0");
		
		board = board.makePgnMove("h4");
	}
	
	@Test
	public void makeMove_captureAndPromote() throws IllegalMoveException {
		Board board = new Board("rn2kb1r/ppp1qppp/8/8/4n1Q1/6P1/PPPP2pP/RNB3KR b kq - 0 10");
		
		board = board.makePgnMove("gxh1=Q+");
	}
	
	@Test
	public void getPgnMove_noParseException() throws IllegalMoveException {
		Board board = new Board();
		board.getPgnMove("O-O");
		board.getPgnMove("O-O!");
		board.getPgnMove("O-O-O+");
		board.getPgnMove("O-O-O!!");
		board.getPgnMove("O-O-O!?");
		board.getPgnMove("O-O-O#");
	}
	
	@Test
	public void makePgnMove_ambiguousPinned() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/1b6/2N5/8/4K1N1 w - - 0 0");
		
		board.makePgnMove("Ne2");
	}
	
	@Test
	public void makePgnMove_castleWithCheck() throws IllegalMoveException {
		Board board = new Board("5k2/8/8/8/8/8/8/4K2R w K - 0 0");
		
		board.makePgnMove("O-O+");
	}
	
	@Test
	public void makePgnMove_castleQueensideWithCheck() throws IllegalMoveException {
		Board board = new Board("3k4/8/8/8/8/8/8/R3K3 w Q - 0 0");
		
		board.makePgnMove("O-O-O+");
	}
	
	@Test
	public void getPgnMove_ambiguousEnPassant() throws IllegalMoveException {
		Board board = new Board("6k1/8/8/4PpP1/8/8/8/7K w - f6 0 0");
		
		String pgnMove = board.getMoveAsPgn(new Move(new Square("g5"), new Square("f6")));
		
		assertEquals("gxf6", pgnMove);
	}
	
	@Test
	public void isCheckmate_inconsistentFen() throws IllegalMoveException {
		Board board = new Board("8/8/3Q4/4k3/5Q2/8/8/7K b kqKQ - 0 0");
		
		assertTrue(board.isCheckmate());
	}
	
	@Test(expected=IllegalMoveException.class)
	public void makeMove_castleWrongRank() throws IllegalMoveException {
		Board board = new Board("8/4k2r/8/8/8/8/8/6K1 b kqKQ - 0 0");
		
		board.makePgnMove("O-O");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void makeMove_castleOppositeRank() throws IllegalMoveException {
		Board board = new Board("8/K7/8/8/8/8/8/4k2r b kqKQ - 0 0");
		
		board.makePgnMove("O-O");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void makeMove_castleWithKnight() throws IllegalMoveException {
		Board board = new Board("4k2n/8/8/8/8/8/8/6K1 b kqKQ - 0 0");
		
		board.makePgnMove("O-O");
	}
	
	@Test(expected=IllegalMoveException.class)
	public void makeMove_castleWithMovedKing() throws IllegalMoveException {
		Board board = new Board("5k1r/8/8/8/8/8/8/6K1 b - - 0 0");
		
		board.makePgnMove("O-O");
	}
	
	@Test
	public void castleKingside960() throws IllegalMoveException {
		Board board = new Board("7k/8/8/8/8/8/8/5KR1 w K - 0 0");
		board = board.makePgnMove("O-O");
		assertEquals("7k/8/8/8/8/8/8/5RK1 b - - 1 0", board.getFen());
	}
	
	@Test
	public void castleQueenside960() throws IllegalMoveException {
		Board board = new Board("7k/8/8/8/8/8/8/R4K2 w Q - 0 0");
		board = board.makePgnMove("O-O-O");
		assertEquals("7k/8/8/8/8/8/8/2KR4 b - - 1 0", board.getFen());
	}
	
	@Test
	public void castleKingside_noKingMove960() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/8/6KR w K - 0 0");
		board = board.makePgnMove("O-O");
		assertEquals("k7/8/8/8/8/8/8/5RK1 b - - 1 0", board.getFen());
	}
	
	@Test
	public void cancelKingsideCastling960() throws IllegalMoveException {
		Board board = new Board("7k/8/8/8/8/8/8/1R2KR2 w KQ - 0 0");
		board = board.makePgnMove("Rh1");
		assertEquals("7k/8/8/8/8/8/8/1R2K2R b Q - 1 0", board.getFen());
	}
	
	@Test
	public void cancelQueensideCastling960() throws IllegalMoveException {
		Board board = new Board("7k/8/8/8/8/8/8/1R2KR2 w KQ - 0 0");
		board = board.makePgnMove("Ra1");
		assertEquals("7k/8/8/8/8/8/8/R3KR2 b K - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_canCastleInnerRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/7R/4K1R1 w K - 0 0");
		board = board.makePgnMove("Rhh1");
		assertEquals("k7/8/8/8/8/8/8/4K1RR b G - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_canCastleOuterRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/5R2/4K1R1 w K - 0 0");
		board = board.makePgnMove("Rff1");
		assertEquals("k7/8/8/8/8/8/8/4KRR1 b K - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_castleInnerRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/8/4K1RR w G - 0 0");
		board = board.makePgnMove("O-O");
		assertEquals("k7/8/8/8/8/8/8/5RKR b - - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_moveInnerRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/8/4K1RR w K - 0 0");
		board = board.makePgnMove("Rgg2");
		assertEquals("k7/8/8/8/8/8/6R1/4K2R b K - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_moveOuterRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/8/4K1RR w G - 0 0");
		board = board.makePgnMove("Rhh2");
		assertEquals("k7/8/8/8/8/8/7R/4K1R1 b K - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_cancelMoveOuterRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/8/4K1RR w K - 0 0");
		board = board.makePgnMove("Rhh2");
		assertEquals("k7/8/8/8/8/8/7R/4K1R1 b - - 1 0", board.getFen());
	}
	
	@Test
	public void ambiguousCastling960_cancelMoveInnerRook() throws IllegalMoveException {
		Board board = new Board("k7/8/8/8/8/8/8/4K1RR w G - 0 0");
		board = board.makePgnMove("Rgg2");
		assertEquals("k7/8/8/8/8/8/6R1/4K2R b - - 1 0", board.getFen());
	}
}
package domain;

import domain.pieces.Piece;
import domain.pieces.PieceType;


public class Castling {	
	private boolean whiteKingSide;
	private boolean whiteQueenSide;
	private boolean blackKingSide;
	private boolean blackQueenSide;

	public Castling() {
		whiteKingSide = true;
		whiteQueenSide = true;
		blackKingSide = true;
		blackQueenSide = true;
	}
	
	public Castling(String castling) {
		whiteKingSide = castling.contains("K");
		whiteQueenSide = castling.contains("Q");
		blackKingSide = castling.contains("k");
		blackQueenSide = castling.contains("q");
	}
	
	public Castling(Castling castling) {
		whiteKingSide = castling.whiteKingSide;
		whiteQueenSide = castling.whiteQueenSide;
		blackKingSide = castling.blackKingSide;
		blackQueenSide = castling.blackQueenSide;
	}
	
	public String toString() {
		String castle = "";
		if(whiteKingSide) castle += "K";
		if(whiteQueenSide) castle += "Q";
		if(blackKingSide) castle += "k";
		if(blackQueenSide) castle += "q";
		return (castle.equals("")) ? "-" : castle;
	}
	
	public Castling nextCastling(Board board, Move move) {
		Castling next = new Castling(this);
		Square from = move.getFrom();

		Square kingSquare = getPiece(board, PieceType.KING, board.getSideToPlay());

		Piece piece = board.getPiece(move.getFrom());
		
		if(piece.getPieceType() == PieceType.ROOK) {
			if(board.getSideToPlay() == Side.WHITE) {
				if(piece.getSquare().getX() < kingSquare.getX()) next.whiteQueenSide = false;
				if(piece.getSquare().getX() > kingSquare.getX()) next.whiteKingSide = false;
			} else {
				if(piece.getSquare().getX() < kingSquare.getX()) next.blackQueenSide = false;
				if(piece.getSquare().getX() > kingSquare.getX()) next.blackKingSide = false;
			}
		}

		if(from.equals(kingSquare)) {
			if(board.getSideToPlay() == Side.WHITE) {
				next.whiteKingSide = false;
				next.whiteQueenSide = false;
			} else {
				next.blackKingSide = false;
				next.blackQueenSide = false;
			}
		}
		
		return next;
	}

	private Square getPiece(Board board, PieceType type, Side side) {
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				Square square = new Square(x, y);
				Piece piece = board.getPiece(square);
				if(piece != null && piece.getPieceType() == type && piece.getSide() == side) {
					return square;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns whether a side is allowed to make a castling move.
	 */
	public boolean allowedCastle(Side side, Move move) {
		boolean kingSide = (move.getTo().getX() == 6);
		
		if(side == Side.WHITE) {
			if(kingSide && !whiteKingSide) return false;
			if(!kingSide && !whiteQueenSide) return false;
		} else {
			if(kingSide && !blackKingSide) return false;
			if(!kingSide && !blackQueenSide) return false;
		}
		return true;
	}
}

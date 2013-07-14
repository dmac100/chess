package domain;


public class Castling {
	private static Square a1 = new Square("a1");
	private static Square h1 = new Square("h1");
	private static Square a8 = new Square("a8");
	private static Square h8 = new Square("h8");
	private static Square e1 = new Square("e1");
	private static Square e8 = new Square("e8");
	
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
	
	public Castling nextCastling(Move move) {
		Castling next = new Castling(this);
		Square from = move.getFrom();
		
		if(from.equals(a1)) next.whiteQueenSide = false;
		if(from.equals(h1)) next.whiteKingSide = false;
		if(from.equals(a8)) next.blackQueenSide = false;
		if(from.equals(h8)) next.blackKingSide = false;

		if(from.equals(e1)) {
			next.whiteKingSide = false;
			next.whiteQueenSide = false;
		}
		
		if(from.equals(e8)) {
			next.blackKingSide = false;
			next.blackQueenSide = false;
		}
		
		return next;
	}

	/**
	 * Returns whether a side is allowed to make a castling move.
	 */
	public boolean allowedCastle(Side side, Move move) {
		boolean kingSide = (move.getTo().getX() > move.getFrom().getX());
		
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

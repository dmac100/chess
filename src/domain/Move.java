package domain;


/**
 * Represents a chess move, as the squares the piece has moved from and to, and promotion choice.
 * This class is immutable.
 */
public final class Move {
	private Square from;
	private Square to;
	private PromotionChoice promote;
	
	public Move(Square from, Square to) {
		this(from, to, null);
	}
	
	public Move(Square from, Square to, PromotionChoice promote) {
		this.from = from;
		this.to = to;
		this.promote = promote;
	}
	
	public Move(String from, String to) {
		this.from = new Square(from);
		this.to = new Square(to);
	}

	public Move(String from, String to, PromotionChoice promote) {
		this(from, to);
		this.promote = promote;
	}

	public Square getFrom() {
		return from;
	}
	
	public Square getTo() {
		return to;
	}
	
	public PromotionChoice getPromote() {
		return promote;
	}
	
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other.getClass() != Move.class) return false;
		
		Move otherMove = (Move)other;
		
		if(!from.equals(otherMove.from)) return false;
		if(!to.equals(otherMove.to)) return false;
		if(promote == null && otherMove.promote != null) return false;
		if(promote != null && !promote.equals(otherMove.promote)) return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return from + "-" + to;
	}
}
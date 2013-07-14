package domain;

public enum Side {
	WHITE, BLACK;
	
	public Side otherSide() {
		return (this == WHITE) ? BLACK : WHITE;
	}
	
	public String toString() {
		return (this == WHITE) ? "w" : "b";
	}
}

package domain;

/**
 * Represents a square on a chess board. This class is immutable.
 */
public final class Square {
	private int x;
	private int y;
	
	public static boolean inBounds(int x, int y) {
		return (x >= 0 && x < 8 && y >= 0 && y < 8);
	}
	
	/**
	 * Create a square using two integer coordinates.
	 * @param x integer 0-7 where 0 is the leftmost file (from white's perspective).
	 * @param y integer 0-7 where 0 is the back rank (from white's perspective).
	 */
	public Square(int x, int y) {
		if(!inBounds(x, y)) throw new IllegalArgumentException("Invalid square: " + x+","+y);
		
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o.getClass() != Square.class) return false;
		Square other = (Square)o;
		
		return (x == other.x && y == other.y);
	}
	
	public int hashCode() {
		return y*8 + x;
	}
	
	/**
	 * Create a square from the algebraic notation. For example, A1 or h8.
	 */
	public Square(String s) {
		if(s == null || !s.matches("[a-hA-H][1-8]")) throw new IllegalArgumentException("Invalid square: " + s);
		
		this.x = s.toLowerCase().charAt(0) - 'a';
		this.y = 8 - (s.charAt(1) - '0');
	}

	/**
	 * Returns the file as an integer 0-7 where 0 is the leftmost file (from white's perspective).
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Returns the rank as an integer 0-7 where 0 is the back rank (from white's perspective).
	 */
	public int getY() {
		return y;
	}
	
	public char getRank() {
		return (char)('0' + (8 - y));
	}
	
	public char getFile() {
		return "abcdefgh".toCharArray()[x];
	}
	
	public String toString() {
		return String.valueOf(getFile()) + String.valueOf(getRank());
	}
}

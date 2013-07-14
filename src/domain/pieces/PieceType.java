package domain.pieces;

public enum PieceType {
	PAWN('p'), ROOK('r'), KNIGHT('n'), BISHOP('b'), QUEEN('q'), KING('k');
	
	char c;
	PieceType(char c) {
		this.c = c;
	}
	
	public char getAlgebraic() {
		return c;
	}
}

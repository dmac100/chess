package domain;

public enum PromotionChoice {
	QUEEN('q'), ROOK('r'), BISHOP('b'), KNIGHT('n');
	
	char c;
	PromotionChoice(char c) {
		this.c = c;
	}
	
	public char getAlgebraic() {
		return c;
	}
}

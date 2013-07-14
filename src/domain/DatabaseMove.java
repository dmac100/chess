package domain;

public class DatabaseMove {
	private Move move;
	private int win;
	private int draw;
	private int loss;
	
	public DatabaseMove(Move move, int win, int draw, int loss) {
		this.move = move;
		this.win = win;
		this.draw = draw;
		this.loss = loss;
	}

	public Move getMove() {
		return move;
	}
	
	public int getWin() {
		return win;
	}
	
	public int getDraw() {
		return draw;
	}
	
	public int getLoss() {
		return loss;
	}

	public int getTotal() {
		return win + draw + loss;
	}
	
	public String toString() {
		return win + "/" + draw + "/" + loss;
	}
}

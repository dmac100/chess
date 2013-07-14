package domain;

public class EngineMove {
	private Board position;
	private Move move;
	private String score;
	private String pgnMove;
	private int scoreNum;
	
	public EngineMove(Board position, Move move, String score, int scoreNum) throws IllegalMoveException {
		this.position = position;
		this.move = move;
		this.score = score;
		this.scoreNum = scoreNum;
		
		this.pgnMove = position.getMoveAsPgn(move);
	}

	public Board getPosition() {
		return position;
	}
	
	public Move getMove() {
		return move;
	}

	public String getScore() {
		return score;
	}
	
	public int getScoreNum() {
		return scoreNum;
	}
	
	public String getPgnMove() {
		return pgnMove;
	}
	
	public String toString() {
		return pgnMove + " (" + score + ")";
	}
}
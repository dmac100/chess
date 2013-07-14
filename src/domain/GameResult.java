package domain;

public enum GameResult {
	WHITE_WIN("1-0"), BLACK_WIN("0-1"), DRAW("1/2-1/2"), OTHER("*");
	
	private String string;

	GameResult(String string) {
		this.string = string;
	}
	
	public String toString() {
		return string;
	}
}

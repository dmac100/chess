package pgn;

import java.util.*;

import domain.*;

public class PgnGame {
	private String pgn;
	private Map<String, String> tags;
	private MoveHistoryNode moves;
	private GameResult result;
	private Board initialPosition;

	public PgnGame(String pgn, Map<String, String> tags, MoveHistoryNode moves, GameResult result, Board initialPosition) {
		this.pgn = pgn;
		this.tags = tags;
		this.moves = moves;
		this.result = result;
		this.initialPosition = initialPosition;
	}

	public String getPgn() {
		return pgn;
	}
	
	public Map<String, String> getTags() {
		return tags;
	}

	public MoveHistoryNode getMoves() {
		return moves;
	}
	
	public Board getInitialPosition() {
		return initialPosition;
	}
	
	public List<Move> getMainLine() {
		List<Move> list = new ArrayList<Move>();
		
		MoveHistoryNode current = moves;
		while(current.getNextNodes().size() > 0) {
			current = current.getNextNodes().get(0);
			list.add(current.getMove());
		}
		
		return list;
	}
	
	public GameResult getResult() {
		return result;
	}
	
	public String getWhite() {
		return getTag("White");
	}
	
	public String getBlack() {
		return getTag("Black");
	}
	
	public String getDate() {
		return getTag("Date");
	}
	
	public String getSite() {
		return getTag("Site");
	}

	private String getTag(String name) {
		String value = tags.get(name);
		return (value == null) ? "Unknown" : value;
	}
	
	public String toString() {
		return String.format("[Site: %s, Date: %s, White: %s, Black: %s (%s)]", getSite(), getDate(), getWhite(), getBlack(), getResult());
	}
}

package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pgn.PgnGame;
import pgn.PgnImporter;
import util.FileUtil;

/**
 * Classifies openings by ECO code.
 */
public class EcoClassifier {
	private Map<String, List<Move>> openings = new HashMap<>();

	public EcoClassifier() {
		try {
			String[] lines = FileUtil.readResource("/resource/eco.parsed").split("\n");
			int line = 0;
			while(line < lines.length) {
				String openingName = lines[line++];
				int moves = Integer.parseInt(lines[line++]);
				List<Move> moveList = new ArrayList<>();
				for(int move = 0; move < moves; move++) {
					String[] moveText = lines[line++].split(", ");
					Square from = new Square(moveText[0]);
					Square to = new Square(moveText[1]);
					boolean castling = moveText[2].equals("true");
					PromotionChoice promote = (moveText[3].equals("null")) ? null : PromotionChoice.valueOf(moveText[3]);
					moveList.add(new Move(from, to, castling, promote));
				}
				openings.put(openingName, moveList);
			}
		} catch(Exception e) {
			System.err.println("Error loading eco database: " + e);
		}
	}
	
	private void parsePgn() throws Exception {
		String ecoPgn = FileUtil.readResource("/resource/eco.pgn");
		List<PgnGame> openings = new PgnImporter().importCollection(ecoPgn);
		
		openings.forEach(pgnGame -> {
			System.out.println(getOpeningName(pgnGame));
			System.out.println(pgnGame.getMainLine().size());
			for(Move move:pgnGame.getMainLine()) {
				Square from = move.getFrom();
				Square to = move.getTo();
				boolean castling = move.getCastling();
				PromotionChoice promote = move.getPromote();
				System.out.println(from + ", " + to + ", " + castling + ", " + promote);
			}
		});
	}
	
	/**
	 * Returns the formatted opening name by reading the pgn tags.
	 */
	private String getOpeningName(PgnGame game) {
		if(openings == null) return "Unknown opening";
		
		String opening = game.getTags().get("Opening");
		String variation = game.getTags().get("Variation");
		String eco = game.getTags().get("ECO");
		
		if(variation == null) {
			return String.format("%s %s", eco, opening);
		} else {
			return String.format("%s %s (%s)", eco, opening, variation);
		}
	}

	/**
	 * Returns the opening name that matches the game specified.
	 */
	public String classify(List<Move> game) {
		if(openings == null) return "Unknown opening";
		
		List<Move> found = null;
		String openingName = "Unknown opening";
		
		for(String name:openings.keySet()) {
			List<Move> moves = openings.get(name);
			if(match(game, moves)) {
				if(found == null || moves.size() > found.size()) {
					found = moves;
					openingName = name;
				}
			}
		}
		
		return openingName;
	}

	/**
	 * Returns whether the opening is playing in the game.
	 */
	private boolean match(List<Move> game, List<Move> openingMoves) {
		if(openings == null) return false;
		
		if(openingMoves.size() > game.size()) {
			return false;
		}
		
		for(int i = 0; i < openingMoves.size(); i++) {
			if(!game.get(i).equals(openingMoves.get(i))) {
				return false;
			}
		}
		
		return true;
	}
}

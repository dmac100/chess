package domain;

import java.util.List;

import pgn.PgnGame;
import pgn.PgnImporter;
import util.FileUtil;

/**
 * Classifies openings by ECO code.
 */
public class EcoClassifier {
	// List of games representing each opening. Initialized in background to improve startup time.
	private volatile List<PgnGame> openings;

	public EcoClassifier() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					String ecoPgn = FileUtil.readResource("/resource/eco.pgn");
					openings = new PgnImporter().importCollection(ecoPgn);
				} catch(Exception e) {
					System.err.println("Error loading eco database: " + e);
				}
			}
		});
		thread.start();
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
		
		PgnGame found = null;
		
		for(PgnGame opening:openings) {
			if(match(game, opening)) {
				if(found == null || opening.getMainLine().size() > found.getMainLine().size()) {
					found = opening;
				}
			}
		}
		
		if(found == null) {
			return "Unknown opening";
		}
		
		return getOpeningName(found);
	}

	/**
	 * Returns whether the opening is playing in the game.
	 */
	private boolean match(List<Move> game, PgnGame opening) {
		if(openings == null) return false;
		
		List<Move> openingMoves = opening.getMainLine();
		
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

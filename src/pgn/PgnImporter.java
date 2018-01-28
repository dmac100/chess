package pgn;

import java.util.*;

import org.apache.commons.collections.map.CaseInsensitiveMap;

import domain.*;

public class PgnImporter {
	public PgnGame importPgn(String pgn) throws ParseException {
		Map<String, String> tags = new HashMap<String, String>();
		
		for(String section:pgn.split("(\r?\n|\n){2}")) {
			section = section.trim();
			
			if(section.startsWith("[")) {
				tags = parseTags(section);
			} else if(section.startsWith("1")) {
				Board initialPosition = getInitialPosition(tags);
				
				MoveHistoryNode moves = new MoveTextParser().parseMoveText(initialPosition, section);
				GameResult result = getResult(section);
				
				return new PgnGame(pgn, tags, moves, result, initialPosition);
			}
		}
		
		throw new ParseException("Can't find movetext");
	}
	
	public List<PgnGame> importCollection(String pgn) throws ParseException {
		List<PgnGame> games = new ArrayList<PgnGame>();
		
		Map<String, String> tags = new HashMap<String, String>();
		for(String section:pgn.split("(\r?\n|\n){2}")) {
			try {
				section = section.trim();
				
				if(section.startsWith("[")) {
					tags = parseTags(section);
				} else if(section.startsWith("1")) {
					if(tags.containsKey("Variant")) {
						System.out.println("Skipping variant: " + tags.get("Variant"));
						continue;
					}
					
					Board initialPosition = getInitialPosition(tags);
					
					MoveHistoryNode moves = new MoveTextParser().parseMoveText(initialPosition, section);
					GameResult result = getResult(section);
					
					games.add(new PgnGame(pgn, tags, moves, result, initialPosition));
					
					if(games.size() % 100 == 0) {
						System.out.println("Loaded: " + games.size() + " games.");
					}
					
					tags = new HashMap<String, String>();
				}
			} catch(ParseException e) {
				throw new ParseException("Error reading game: " + section, e);
			}
		}
		
		return games;
	}
	
	/**
	 * Returns the initial position if it is specified in the tags, or the default position if not.
	 */
	private Board getInitialPosition(Map<String, String> tags) throws ParseException {
		String setup = tags.get("SetUp");
		String fen = tags.get("FEN");

		// If setup is missing but fen isn't, then assume setup is set. This is against the
		// pgn spec but some programs still export in this format.
		if(setup == null && fen != null) {
			tags.put("SetUp", "1");
			try {
				return new Board(fen);
			} catch(IllegalArgumentException e) {
				throw new ParseException("Invalid initial position: " + fen);
			}
		}
		
		// If setup is 1, then the initial position is given by fen.
		if(setup != null && setup.equals("1")) {
			if(fen == null) {
				throw new ParseException("SetUp is 1, but not FEN tag found");
			}
			try {
				return new Board(fen);
			} catch(IllegalArgumentException e) {
				throw new ParseException("Invalid initial position: " + fen);
			}
		}
		
		return new Board();
	}

	private GameResult getResult(String moveText) {
		String result = moveText.replaceAll("(?s).* ", "");
		
		if(result.equals("0-1")) return GameResult.BLACK_WIN;
		if(result.equals("1-0")) return GameResult.WHITE_WIN;
		if(result.equals("1/2-1/2")) return GameResult.DRAW;
		
		return GameResult.OTHER;
	}

	private Map<String, String> parseTags(String tags) throws ParseException {
		// Make tags case insensitive, against the pgn spec but more tolerant of import formats.
		Map<String, String> tagMap = new CaseInsensitiveMap();
		
		for(String line:tags.split("\r?\n|\n")) {
			if(!line.matches("\\[.+ \".+\"\\]")) throw new ParseException("Can't parse tag: " + line);
			
			line = line.replaceAll("^\\[", "");
			line = line.replaceAll("\\]$", "");
			
			int space = line.indexOf(" ");
			String name = line.substring(0, space);
			String value = line.substring(space + 1, line.length());
			
			value = value.replaceAll("\"", "");
			
			tagMap.put(name, value);
		}
		
		return tagMap;
	}
}

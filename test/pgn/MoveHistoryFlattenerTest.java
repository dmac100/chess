package pgn;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import pgn.MoveHistoryFlattener.MoveHistoryVisitor;
import domain.*;

public class MoveHistoryFlattenerTest {
	/**
	 * Makes a sequence of pgn moves and adds them to the history.
	 */
	private static void makePgnMoves(MoveHistory history, String... pgnMoves) throws IllegalMoveException {
		for(String pgnMove:pgnMoves) {
			Board board = history.getCurrentPosition();
			history.makeMove(board.getPgnMove(pgnMove));
		}
	}
	
	private static List<String> getMoveTokens(MoveHistory history) {
		final List<String> moves = new ArrayList<String>();
		
		new MoveHistoryFlattener(history).getMoveTokens(new MoveHistoryVisitor() {
			public void move(String text, MoveHistoryNode move, boolean currentPosition) {
				moves.add(text);
			}
			
			public void beginVariation() {
				moves.add("(");
			}
			
			public void endVariation() {
				moves.add(")");
			}

			public void end() {
			}
		});
		
		return moves;
	}
	
	private void assertTokensEquals(String[] expected, List<String> tokens) {
		assertEquals(Arrays.toString(expected), Arrays.toString(tokens.toArray()));
	}
	
	@Test
	public void longerVariation() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5", "Nf3", "Nc6");
		history.prev();
		history.prev();
		makePgnMoves(history, "d4", "d5");
		
		List<String> tokens = getMoveTokens(history);
		
		assertTokensEquals(new String[] {
			"1. e4", "e5", "2. Nf3", "(", "2. d4", "d5", ")", "Nc6"
		}, tokens);
	}
	
	@Test
	public void ellipses() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4", "e5");
		history.prev();
		makePgnMoves(history, "e6");
		
		List<String> tokens = getMoveTokens(history);
		
		assertTokensEquals(new String[] {
			"1. e4", "e5", "(", "1... e6", ")"
		}, tokens);
	}
	
	@Test
	public void multipleVariations() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		
		makePgnMoves(history, "e4");
		history.prev();
		makePgnMoves(history, "d4");
		history.prev();
		makePgnMoves(history, "c4");
		history.prev();
		
		List<String> tokens = getMoveTokens(history);
		
		assertTokensEquals(new String[] {
			"1. e4", "(", "1. d4", ")", "(", "1. c4", ")"
		}, tokens);
	}
	
	@Test
	public void setInitialPosition_firstMoveBlack() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		history.setInitialPosition(new Board().makePgnMove("e4"));
		makePgnMoves(history, "e5");
		
		List<String> tokens = getMoveTokens(history);
		
		assertTokensEquals(new String[] {
			"1... e5"
		}, tokens);
	}
	
	@Test
	public void setInitialPosition_secondMove() throws IllegalMoveException {
		MoveHistory history = new MoveHistory();
		history.setInitialPosition(new Board().makePgnMove("e4").makePgnMove("e5"));
		makePgnMoves(history, "d4");
		
		List<String> tokens = getMoveTokens(history);
		
		assertTokensEquals(new String[] {
			"2. d4"
		}, tokens);
	}
}

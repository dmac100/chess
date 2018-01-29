package domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class MoveDatabaseTest {
	@Test
	public void addMove() {
		MoveDatabase database = new MoveDatabase();
		
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4"), 1, 1, 1));
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e3"), 2, 1, 1));
		
		List<DatabaseMove> moves = database.getMoves(new Board());
		
		assertEquals(2, moves.size());
	}
	
	@Test
	public void addMove_twoPositions() throws IllegalMoveException {
		MoveDatabase database = new MoveDatabase();
		
		Board otherBoard = new Board().makeMove(new Move("e2", "e4"));
		
		database.addMove(1, otherBoard, new DatabaseMove(new Move("e2", "e4"), 3, 1, 1));
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e3"), 2, 1, 1));
		
		List<DatabaseMove> moves = database.getMoves(otherBoard);
		
		assertEquals(1, moves.size());
		assertEquals(3, moves.get(0).getWin());
	}
	
	@Test
	public void addMove_duplicateMove() {
		MoveDatabase database = new MoveDatabase();
		
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4"), 1, 1, 1));
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4"), 1, 1, 1));
		
		List<DatabaseMove> moves = database.getMoves(new Board());
		
		assertEquals(1, moves.size());
		assertEquals(2, moves.get(0).getWin());
	}
	
	@Test
	public void addMove_duplicateMoveWithPromote() {
		MoveDatabase database = new MoveDatabase();
		
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4", PromotionChoice.ROOK), 1, 1, 1));
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4", PromotionChoice.ROOK), 1, 1, 1));
		
		List<DatabaseMove> moves = database.getMoves(new Board());
		
		assertEquals(1, moves.size());
		assertEquals(2, moves.get(0).getWin());
	}
	
	@Test
	public void addMove_duplicateMoveWithDifferentPromote() {
		MoveDatabase database = new MoveDatabase();
		
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4", PromotionChoice.ROOK), 1, 1, 1));
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4", PromotionChoice.QUEEN), 1, 1, 1));
		
		List<DatabaseMove> moves = database.getMoves(new Board());
		
		assertEquals(2, moves.size());
		assertEquals(1, moves.get(0).getWin());
	}
	
	@Test
	public void addGame() {
		MoveDatabase database = new MoveDatabase();
		assertEquals(0, database.addGame("abc"));
		assertEquals(1, database.addGame("abc"));
		assertEquals("abc", database.getGame(1));
	}
	
	@Test
	public void getGames() {
		MoveDatabase database = new MoveDatabase();
		database.addGame("abc");
		database.addGame("def");
		
		database.addMove(0, new Board(), new DatabaseMove(new Move("e2", "e4"), 1, 0, 0));
		database.addMove(1, new Board(), new DatabaseMove(new Move("e2", "e4"), 1, 0, 0));
		
		assertEquals(Arrays.asList("abc", "def"), database.getGames(new Board()));
		
	}
}

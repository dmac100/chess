package domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import pgn.PgnGame;

/**
 * A move database storing the win/draw/loss counts for the moves played at each position.
 */
public class MoveDatabase {
	private SessionFactory sessionFactory;

	public Connection getConnection() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			return DriverManager.getConnection("jdbc:hsqldb:mem:movedb");
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public MoveDatabase() {
		try {
			createTables();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createTables() throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.createStatement().execute("drop schema public cascade");
			connection.createStatement().execute("create table PositionMove ( id int identity primary key," +
				"positionText varchar(100), moveFrom char(2), moveTo char(2), castling boolean, promote char(1), win int, draw int, loss int )");
			connection.createStatement().execute("create index positionTextIndex on PositionMove ( positionText )");
		} finally {
			close(connection);
		}
	}
	
	/**
	 * Add a move for the given position, updating any already existing move for the position.
	 */
	public void addMove(Board board, DatabaseMove move) {
		Connection connection = null;
		try {
			connection = getConnection();
			addMove(connection, board, move);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection);
		}
	}
	
	public void addMove(Connection connection, Board board, DatabaseMove move) throws SQLException {
		int[] winDrawLoss = getWinDrawLoss(connection, board, move);
		
		if(winDrawLoss == null) {
			PreparedStatement statement = connection.prepareStatement("insert into PositionMove values ( NULL, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			statement.setString(1, board.getPositionDatabaseString());
			statement.setString(2, move.getMove().getFrom().toString());
			statement.setString(3, move.getMove().getTo().toString());
			statement.setBoolean(4, move.getMove().getCastling());
			if(move.getMove().getPromote() == null) {
				statement.setString(5, "");
			} else {
				statement.setString(5, String.valueOf(move.getMove().getPromote().getAlgebraic()));
			}
			
			statement.setInt(6, move.getWin());
			statement.setInt(7, move.getDraw());
			statement.setInt(8, move.getLoss());
			
			statement.execute();
		} else {
			PreparedStatement statement = connection.prepareStatement("update PositionMove set win=?, draw=?, loss=? where positionText=? and moveFrom=? and moveTo=? and promote=?");
			
			statement.setInt(1, move.getWin() + winDrawLoss[0]);
			statement.setInt(2, move.getDraw() + winDrawLoss[1]);
			statement.setInt(3, move.getLoss() + winDrawLoss[2]);
			
			statement.setString(4, board.getPositionDatabaseString());
			statement.setString(5, move.getMove().getFrom().toString());
			statement.setString(6, move.getMove().getTo().toString());
			if(move.getMove().getPromote() == null) {
				statement.setString(7, "");
			} else {
				statement.setString(7, String.valueOf(move.getMove().getPromote().getAlgebraic()));
			}
			
			statement.execute();
		}
	}
	
	private int[] getWinDrawLoss(Connection connection, Board board, DatabaseMove move) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select win,draw,loss from PositionMove where positionText=? and moveFrom=? and moveTo=? and castling=? and promote=?");
		
		statement.setString(1, board.getPositionDatabaseString());
		statement.setString(2, move.getMove().getFrom().toString());
		statement.setString(3, move.getMove().getTo().toString());
		statement.setBoolean(4, move.getMove().getCastling());
		if(move.getMove().getPromote() == null) {
			statement.setString(5, "");
		} else {
			statement.setString(5, String.valueOf(move.getMove().getPromote().getAlgebraic()));
		}
		
		statement.execute();
		ResultSet resultSet = statement.getResultSet();
		
		if(resultSet.next()) {
			return new int[] {
				resultSet.getInt(1),
				resultSet.getInt(2),
				resultSet.getInt(3)
			};
		} else {
			return null;
		}
	}
	
	/**
	 * Returns a list of moves played at the given position.
	 */
	public List<DatabaseMove> getMoves(Board board) {
		Connection connection = null;
		try {
			connection = getConnection();
			return getMoves(connection, board);
		} catch(SQLException e) {
			e.printStackTrace();
			return new ArrayList<DatabaseMove>();
		} finally {
			close(connection);
		}
	}
	
	public List<DatabaseMove> getMoves(Connection connection, Board board) throws SQLException {
		List<DatabaseMove> moves = new ArrayList<DatabaseMove>();
	
		PreparedStatement statement = connection.prepareStatement("select * from PositionMove where positionText=? order by (win+draw+loss) desc");
		statement.setString(1, board.getPositionDatabaseString());
		statement.execute();
		
		ResultSet resultSet = statement.getResultSet();
		
		while(resultSet.next()) {
			String moveFrom = resultSet.getString(3);
			String moveTo = resultSet.getString(4);
			boolean castling = resultSet.getBoolean(5);
			String promote = resultSet.getString(6);
			int win = resultSet.getInt(7);
			int draw = resultSet.getInt(8);
			int loss = resultSet.getInt(9);
			
			Square fromSquare = new Square(moveFrom);
			Square toSquare = new Square(moveTo);
			PromotionChoice promotePiece = null;
			if(promote.equals("r")) promotePiece = PromotionChoice.ROOK;
			if(promote.equals("q")) promotePiece = PromotionChoice.QUEEN;
			if(promote.equals("n")) promotePiece = PromotionChoice.KNIGHT;
			if(promote.equals("b")) promotePiece = PromotionChoice.BISHOP;
			moves.add(new DatabaseMove(new Move(fromSquare, toSquare, castling, promotePiece), win, draw, loss));
		}
		
		return moves;
	}
	
	private void close(Connection connection) {
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void importPgnGames(List<PgnGame> games) {
		long startTime = System.currentTimeMillis();
		System.out.println("Starting...");
		int moves = 0;
		
		Connection connection = null;
		try {
			connection = getConnection();
			
			for(PgnGame game:games) {
				System.out.println("Importing: " + game);
				
				try {
					int win = 0;
					int draw = 0;
					int loss = 0;
					if(game.getResult().equals(GameResult.BLACK_WIN)) loss++;
					if(game.getResult().equals(GameResult.WHITE_WIN)) win++;
					if(game.getResult().equals(GameResult.DRAW)) draw++;
					if(game.getResult().equals(GameResult.OTHER)) {
						System.out.println("Skipping game with unknown result: " + game.toString());
						continue;
					}
						
					Board board = new Board();
					for(Move move:game.getMainLine()) {
						moves++;
						
						addMove(connection, board, new DatabaseMove(move, win, draw, loss));
						
						board = board.makeMove(move);
					}
				} catch(IllegalMoveException e) {
					System.out.println("Skipping game with illegal move: " + game + " - " + e.getMessage());
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection);
		}
		
		long time = System.currentTimeMillis() - startTime;
		double perSecond = moves * 1000 / (double)time;
		double gamesPerSecond = perSecond / (35 * 2);
		
		System.out.printf("Done in: %dms (%.2f moves/sec) (%.2f games/sec)\n", time, perSecond, gamesPerSecond);
	}
}

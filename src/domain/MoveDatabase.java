package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import pgn.PgnGame;
import util.FileUtil;

/**
 * A move database storing the win/draw/loss counts for the moves played at each position.
 */
public class MoveDatabase {
	public MoveDatabase() {
		clearDatabase();
		createTables();
	}
	
	public Connection getConnection() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			return DriverManager.getConnection("jdbc:hsqldb:mem:movedb");
		} catch(Exception e) {
			throw new RuntimeException("Error getting connection", e);
		}
	}
	
	private void clearDatabase() {
		try(Connection connection = getConnection()) {
			connection.createStatement().execute("drop schema public cascade");
		} catch(SQLException e) {
			throw new RuntimeException("Error clearing database", e);
		}
	}

	public void saveDatabase(File file) {
		try(Writer writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
			try(Connection connection = getConnection()) {
				try(ResultSet resultSet = connection.createStatement().executeQuery("select * from Game")) {
					while(resultSet.next()) {
						writer.append(String.format("insert into Game values ( %s, '%s' );\n",
							resultSet.getObject(1),
							resultSet.getString(2).replace("'", "''").replace("\n", "\\u000a")
						));
					}
				}
				
				try(ResultSet resultSet = connection.createStatement().executeQuery("select * from PositionGame")) {
					while(resultSet.next()) {
						writer.append(String.format("insert into PositionGame values ( %s, '%s', %s );\n",
							resultSet.getObject(1),
							resultSet.getString(2).replace("'", "''"),
							resultSet.getObject(3)
						));
					}
				}
				
				try(ResultSet resultSet = connection.createStatement().executeQuery("select * from PositionMove")) {
					while(resultSet.next()) {
						writer.append(String.format("insert into PositionMove values ( %s, '%s', '%s', '%s', %s, '%s', %s, %s, %s );\n",
							resultSet.getObject(1),
							resultSet.getString(2).replace("'", "''").replace("\n", "\\u000a"),
							resultSet.getString(3).replace("'", "''").replace("\n", "\\u000a"),
							resultSet.getString(4).replace("'", "''").replace("\n", "\\u000a"),
							resultSet.getObject(5),
							resultSet.getString(6).replace("'", "''").replace("\n", "\\u000a"),
							resultSet.getObject(7),
							resultSet.getObject(8),
							resultSet.getObject(9)
						));
					}
				}
			}
		} catch(Exception e) {
			throw new RuntimeException("Error saving database", e);
		}
	}
	
	public void importDatabase(File file) {
		clearDatabase();
		createTables();
		try(Connection connection = getConnection()) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
				String line;
				while((line = reader.readLine()) != null) {
					connection.createStatement().execute(line.replace("\\u000a", "\n"));
				}
			}
		} catch(Exception e) {
			throw new RuntimeException("Error importing database", e);
		}
	}
	
	private void createTables() {
		try(Connection connection = getConnection()) {
			connection.createStatement().execute("drop schema public cascade");
			connection.createStatement().execute("create table Game ( id int identity primary key, pgn varchar(10000) )");
			connection.createStatement().execute("create table PositionGame ( id int identity primary key, positionText varchar(100), gameId int )");
			connection.createStatement().execute("create table PositionMove ( id int identity primary key," +
					"positionText varchar(100), moveFrom char(2), moveTo char(2), castling boolean, promote char(1), win int, draw int, loss int )");
			connection.createStatement().execute("create index GamePositionTextIndex on PositionMove ( positionText )");
			connection.createStatement().execute("create index MovePositionTextIndex on PositionGame ( positionText )");
		} catch(SQLException e) {
			throw new RuntimeException("Error creating tables", e);
		}
	}
	
	/**
	 * Adds a pgn game to the database and returns its id.
	 */
	public int addGame(String pgn) {
		try(Connection connection = getConnection()) {
			PreparedStatement statement = connection.prepareStatement("insert into Game values ( NULL, ? )",  Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, pgn);
			statement.executeUpdate();
			
			try(ResultSet resultSet = statement.getGeneratedKeys()) {
				resultSet.next();
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			throw new RuntimeException("Error adding game", e);
		}
	}
	
	/**
	 * Returns the pgn for a game given its id.
	 */
	public String getGame(int gameId) {
		try(Connection connection = getConnection()) {
			PreparedStatement statement = connection.prepareStatement("select pgn from Game where id = ?");
			statement.setInt(1, gameId);
			try(ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getString(1);
			}
		} catch(SQLException e) {
			throw new RuntimeException("Error getting game", e);
		}
	}
	
	/**
	 * Add a move for the given position, updating any already existing move for the position.
	 */
	public void addMove(int gameId, Board board, DatabaseMove move) {
		try(Connection connection = getConnection()) {
			addMove(connection, gameId, board, move);
		} catch(SQLException e) {
			throw new RuntimeException("Error adding move", e);
		}
	}
	
	public void addMove(Connection connection, int gameId, Board board, DatabaseMove move) throws SQLException {
		int[] winDrawLoss = getWinDrawLoss(connection, board, move);
		
		try(PreparedStatement statement = connection.prepareStatement("insert into PositionGame values ( NULL, ?, ? )")) {
			statement.setString(1, board.getPositionDatabaseString());
			statement.setInt(2, gameId);
			
			statement.execute();
		}
		
		if(winDrawLoss == null) {
			try(PreparedStatement statement = connection.prepareStatement("insert into PositionMove values ( NULL, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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
			}
		} else {
			try(PreparedStatement statement = connection.prepareStatement("update PositionMove set win=?, draw=?, loss=? where positionText=? and moveFrom=? and moveTo=? and promote=?")) {
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
	}
	
	private int[] getWinDrawLoss(Connection connection, Board board, DatabaseMove move) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement("select win,draw,loss from PositionMove where positionText=? and moveFrom=? and moveTo=? and castling=? and promote=?")) {
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
			try(ResultSet resultSet = statement.getResultSet()) {
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
		}
	}
	
	/**
	 * Returns a list of games that reached the given position.
	 */
	public List<String> getGames(Board board) {
		List<String> games = new ArrayList<>();
		
		try(Connection connection = getConnection()) {
			try(PreparedStatement statement = connection.prepareStatement("select pgn from PositionGame inner join Game on Game.id = PositionGame.gameId where positionText=?")) {
				statement.setString(1, board.getPositionDatabaseString());
				statement.execute();
				
				try(ResultSet resultSet = statement.getResultSet()) {
					while(resultSet.next()) {
						games.add(resultSet.getString(1));
					}
				}
			}
		} catch(SQLException e) {
			throw new RuntimeException("Error getting games", e);
		}
		
		return games;
	}
	
	/**
	 * Returns a list of moves played at the given position.
	 */
	public List<DatabaseMove> getMoves(Board board) {
		try(Connection connection = getConnection()) {
			return getMoves(connection, board);
		} catch(SQLException e) {
			throw new RuntimeException("Error getting moves", e);
		}
	}
	
	public List<DatabaseMove> getMoves(Connection connection, Board board) throws SQLException {
		List<DatabaseMove> moves = new ArrayList<DatabaseMove>();
	
		try(PreparedStatement statement = connection.prepareStatement("select * from PositionMove where positionText=? order by (win+draw+loss) desc")) {
			statement.setString(1, board.getPositionDatabaseString());
			statement.execute();
			
			try(ResultSet resultSet = statement.getResultSet()) {
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
		}
	}
	
	public void importPgnGames(List<PgnGame> games) {
		long startTime = System.currentTimeMillis();
		System.out.println("Starting...");
		int moves = 0;
		
		try(Connection connection = getConnection()) {
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
					
					int gameId = addGame(game.getPgn());
						
					Board board = new Board();
					for(Move move:game.getMainLine()) {
						moves++;
						
						addMove(connection, gameId, board, new DatabaseMove(move, win, draw, loss));
						
						board = board.makeMove(move);
					}
				} catch(IllegalMoveException e) {
					System.out.println("Skipping game with illegal move: " + game + " - " + e.getMessage());
				}
			}
		} catch(SQLException e) {
			throw new RuntimeException("Error importing pgn games");
		}
		
		long time = System.currentTimeMillis() - startTime;
		double perSecond = moves * 1000 / (double)time;
		double gamesPerSecond = perSecond / (35 * 2);
		
		System.out.printf("Done in: %dms (%.2f moves/sec) (%.2f games/sec)\n", time, perSecond, gamesPerSecond);
	}
}

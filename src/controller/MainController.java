package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import domain.Board;
import domain.EcoClassifier;
import domain.EngineMove;
import domain.IllegalMoveException;
import domain.Move;
import domain.MoveDatabase;
import domain.MoveHistory;
import domain.MoveHistoryNode;
import domain.PromotionChoice;
import domain.Side;
import domain.Square;
import pgn.ParseException;
import pgn.PgnExporter;
import pgn.PgnGame;
import pgn.PgnImporter;
import ui.BoardCanvas;
import ui.BoardDragHandler;
import ui.BoardPositionChangedHandler;
import ui.DatabaseGameSelectedHandler;
import ui.DatabaseMoveSelectedHandler;
import ui.DatabaseView;
import ui.EngineItemSelectedHandler;
import ui.EngineMovesTable;
import ui.HistoryItemSelectedHandler;
import ui.MoveHistoryTree;
import util.FileUtil;

public class MainController implements BoardDragHandler, HistoryItemSelectedHandler, DatabaseMoveSelectedHandler, DatabaseGameSelectedHandler, EngineItemSelectedHandler, BoardPositionChangedHandler {
	private static final String TOGA = "/home/david/opt/toga/src/fruit";
	
	private final BoardCanvas boardCanvas;
	private final MoveHistoryTree moveHistoryTree;
	private final MoveHistory history = new MoveHistory();
	private final DatabaseView databaseView;
	private final EngineMovesTable engineMovesTable;
	
	private MoveDatabase moveDatabase = new MoveDatabase();
	
	private final EcoClassifier ecoClassifier = new EcoClassifier();

	private AnalysisEngine analysisEngine;
	private final EnginePlayController enginePlayController;
	
	private boolean flipped = false;
	private boolean editPosition = false;
	private Side editToPlay = Side.WHITE;
	private boolean showMoveArrows = false; 
	
	public MainController(BoardCanvas boardCanvas, MoveHistoryTree moveHistoryTree, DatabaseView databaseView, EngineMovesTable engineMovesTable) {
		this.boardCanvas = boardCanvas;
		this.moveHistoryTree = moveHistoryTree;
		this.databaseView = databaseView;
		this.engineMovesTable = engineMovesTable;
		
		boardCanvas.addDragHandler(this);
		boardCanvas.addPositionChangedHandler(this);
		moveHistoryTree.addHistoryItemSelectedHandler(this);
		databaseView.addMoveSelectedHandler(this);
		databaseView.addGameSelectedHandler(this);
		engineMovesTable.addHistoryItemSelectedHandler(this);
		
		try {
			this.analysisEngine = new AnalysisEngine(TOGA, engineMovesTable, boardCanvas);
		} catch (IOException e) {
			System.err.println("Error creating engine: " + e.getMessage());
		}
		
		this.enginePlayController = new EnginePlayController(this, analysisEngine);
		
		updateView();
	}

	public void dispose() {
		if(analysisEngine != null) {
			analysisEngine.dispose();
		}
	}
	
	private void updateView() {
		final Board board = history.getCurrentPosition();
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				moveHistoryTree.setMoves(history);
				
				boardCanvas.setBoard(board);
				boardCanvas.setEditPosition(editPosition);
				databaseView.setMoves(board, moveDatabase.getMoves(board));
				databaseView.setGames(moveDatabase.getGames(board));
				databaseView.setOpening(ecoClassifier.classify(history.getMoves()));
			}
		});
		
		if(analysisEngine != null) {
			analysisEngine.setPosition(board);
		}
		
		List<BoardCanvas.BoardArrow> arrows = new ArrayList<BoardCanvas.BoardArrow>();
		if(showMoveArrows) {
			for(Move move:history.getVarations()) {
				arrows.add(new BoardCanvas.BoardArrow(move.getFrom(), move.getTo(), 0));
			}
		}
		boardCanvas.setMoveArrows(arrows);
		
		engineMovesTable.setPlayerMoves(history.getVarations());
		
		enginePlayController.madeMove();
	}

	@Override
	public void onHistoryItemSelected(MoveHistoryNode selected) {
		history.setPosition(selected);
		updateView();
	}
	
	@Override
	public void onPositionChanged(Board board) {
		history.setInitialPosition(board);
		setToPlay(editToPlay);
		updateView();
	}
	
	@Override
	public void onEngineItemSelected(EngineMove move) {
		try {
			makeMove(move.getMove());
		} catch (IllegalMoveException e) {
			// Don't make move.
		}
	}
	
	public void makeMove(Move move) throws IllegalMoveException {
		history.makeMove(move);
		updateView();
	}
	
	public void makeEngineMove() {
		if(analysisEngine == null) return;
		
		try {
			makeMove(analysisEngine.getBestMove());
		} catch (IllegalMoveException e) {
			// Don't make move.
		}
	}
	
	@Override
	public void onDrag(Square start, Square end, boolean castling, PromotionChoice promote) {
		try {
			makeMove(new Move(start, end, castling, promote));
		} catch(IllegalMoveException e) {
			// Don't make move.
		}
	}
	
	@Override
	public void onDatabaseMoveSelected(Move move) {
		try {
			Board board = history.getCurrentPosition();
			history.makeMove(move);
			updateView();
		} catch (IllegalMoveException e) {
			throw new RuntimeException("Illegal move in database", e);
		}
	}
	
	@Override
	public void onDatabaseGameSelected(String pgn) {
		try {
			setPgn(pgn);
		} catch(ControllerException e) {
			throw new RuntimeException("Error loading database game", e);
		}
	}

	public void setPgn(String pgnText) throws ControllerException {
		try {
			PgnGame game = new PgnImporter().importPgn(pgnText);
			history.setMoves(game.getInitialPosition(), game.getMoves());
			updateView();
		} catch (ParseException e) {
			throw new ControllerException("Error parsing pgn:\n" + e.getMessage(), e);
		}
	}
	
	public void openFile(String path) throws ControllerException {
		try {
			String pgnText = FileUtil.readFile(path);
			
			setPgn(pgnText);
		} catch(IOException e) {
			throw new ControllerException("Error loading file:\n" + e.getMessage(), e);
		}
	}

	public void importDatabase(String path) throws ControllerException {
		try {
			String pgnText = FileUtil.readFile(path);
			
			List<PgnGame> games = new PgnImporter().importCollection(pgnText);
			
			createMoveDatabase(games);
			
			updateView();
		} catch(ParseException e) {
			throw new ControllerException("Error loading file:\n" + e.getMessage(), e);
		} catch (IOException e) {
			throw new ControllerException("Error parsing file:\n" + e.getMessage(), e);
		}
	}
	
	private void createMoveDatabase(List<PgnGame> games) {
		this.moveDatabase = new MoveDatabase();
		
		moveDatabase.importPgnGames(games);
		
		System.out.printf("Imported %d games.", games.size());
	}

	public void prevMove() {
		history.prev();
		updateView();
	}
	
	public void nextMove() {
		history.next();
		updateView();
	}

	public void firstMove() {
		history.first();
		updateView();
	}

	public void lastMove() {
		history.last();
		updateView();
	}
	
	public void randomMove() {
		history.nextRandom();
		updateView();
	}

	public void flipBoard() {
		flipped = !flipped;
		boardCanvas.setFlipped(flipped);
	}

	public void setFen(String fen) {
		Board board = new Board(fen);
		history.setInitialPosition(board);
		updateView();
	}

	public void newGame() {
		history.setInitialPosition(new Board());
		updateView();
	}

	public String getPgn() throws IllegalMoveException {
		String pgn = new PgnExporter().exportPgn(history);
		
		return pgn;
	}
	
	public void savePgn(String location) throws IOException, IllegalMoveException {
		FileUtil.writeFile(location, getPgn());
	}

	public String getFen() {
		return history.getCurrentPosition().getFen();
	}

	public void enginePlayWhite() {
		enginePlayController.enginePlayWhite();
	}

	public void enginePlayBlack() {
		enginePlayController.enginePlayBlack();
	}

	public void enginePlayBoth() {
		enginePlayController.enginePlayBoth();
	}

	public void enginePlayNone() {
		enginePlayController.enginePlayNone();
	}
	
	public Board getCurrentPosition() {
		return history.getCurrentPosition();
	}
	
	public void showMoveArrows() {
		showMoveArrows = true;
		updateView();
	}
	
	public void hideMoveArrows() {
		showMoveArrows = false;
		updateView();
	}
	
	public boolean areMoveArrowsShown() {
		return showMoveArrows;
	}
	
	public void showEngineArrows() {
		analysisEngine.showArrows();
	}

	public void hideEngineArrows() {
		analysisEngine.hideArrows();
	}

	public void editPosition() {
		editPosition = true;
		
		Board board = new Board(getCurrentPosition());
		board = board.setSideToPlay(editToPlay);
		// TODO: No UI to edit castling and en-passant rights.
		board = board.resetCounters();
		
		history.setInitialPosition(board);
		
		updateView();
	}

	public void playPosition() {
		editPosition = false;
		updateView();
	}
	
	public void setToPlay(Side side) {
		editToPlay = side;
		history.setInitialPosition(getCurrentPosition().setSideToPlay(editToPlay));
		updateView();
	}

	public void clearPosition() {
		history.setInitialPosition(new Board("k7/8/8/8/8/8/8/7K w KQkq - 0 1"));
		updateView();
	}

	public boolean areEngineArrowsShown() {
		if(analysisEngine == null) return false;
		
		return analysisEngine.areArrowsShown();
	}

	public boolean isEnginePlaying() {
		return enginePlayController.isEnginePlaying();
	}

	public Side getToPlay() {
		return editToPlay;
	}

	public boolean isEditingPosition() {
		return editPosition;
	}

	public void promoteVariation() {
		history.promoteCurrentVariation();
		updateView();
	}

	public void deleteVariation() {
		history.deleteCurrentVariation();
		updateView();
	}
	
	public void setCurrentMoveComment(String comment) {
		history.setCurrentMoveComment(comment);
		updateView();
	}

	public String getCurrentMoveComment() {
		return history.getCurrentMoveComment();
	}

	public void trimVariation() {
		history.trimVariation();
		updateView();
	}
}

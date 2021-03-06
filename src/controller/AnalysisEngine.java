package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Display;

import domain.Board;
import domain.EngineMove;
import domain.IllegalMoveException;
import domain.Move;
import domain.PromotionChoice;
import domain.Side;
import domain.Square;
import domain.pieces.Piece;
import domain.pieces.PieceType;
import ui.BoardCanvas;
import ui.BoardCanvas.BoardArrow;
import ui.EngineMovesTable;

public class AnalysisEngine {
	private Process process;
	private BufferedReader reader;
	private BufferedReader errReader;
	private PrintWriter writer;

	private String exePath;
	private EngineMovesTable engineView;
	private BoardCanvas boardCanvas;
	
	private Board currentPosition = new Board();
	private boolean enabled = true;
	
	private EngineMove[] engineMoves = new EngineMove[10];
	private Thread readThread;
	private Thread readErrThread;
	private Thread updateViewThread;
	
	private boolean showArrows = false;
	
	public AnalysisEngine(String exePath, EngineMovesTable engineView, BoardCanvas boardCanvas) throws IOException {
		this.exePath = exePath;
		this.engineView = engineView;
		this.boardCanvas = boardCanvas;
		
		engineView.addEnabledSelectedHandler(enabled -> setEnabled(enabled));
		
		startEngine();
	}
	
	private synchronized void startEngine() throws IOException {
		if(process == null) {
			ProcessBuilder processBuilder = new ProcessBuilder(exePath);
			
			this.process = processBuilder.start();
			this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			this.errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			this.writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
			
			readLoop();
			readErrLoop();
			updateViewLoop();
		}
	}
	
	private synchronized void exitEngine() {
		if(process != null) {
			readThread.interrupt();
			readErrThread.interrupt();
			updateViewThread.interrupt();
			process.destroy();
			process = null;
		}
	}
	
	public Move getBestMove() {
		if(engineMoves[0] == null) return null;
		
		return engineMoves[0].getMove();
	}
	
	public EngineMove[] getAllMoves() {
		return engineMoves;
	}
	
	public void setPosition(Board board) {
		if(currentPosition == board) return;
		
		// Ignore invalid positions because the engine can't handle them.
		if(!board.validPosition()) return;
		
		try {
			if(process == null) {
				startEngine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		this.currentPosition = board;
		
		engineMoves = new EngineMove[10];
		
		syncEngine();
	}
	
	private void syncEngine() {
		writer.println("stop");
		writer.println("uci");
		writer.println("setoption name MultiPV value 10");
		writer.println("isready");
		writer.println("ucinewgame");
		writer.println("position fen " + currentPosition.getFen());
		writer.println(enabled ? "go movetime 60000" : "stop");
		writer.flush();
	}
	
	public void setEnabled(boolean running) {
		this.enabled = running;
		syncEngine();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	private void handleEngineOutput(String line) {
		if(line.startsWith("info") && line.contains("multipv")) {
			// Example: info multipv 10 depth 10 seldepth 40 score cp -56 time 3956 nodes 3366368 pv a7a6 b1c3 b8c6 d2d4 d7d6 g1f3 g8f6 c1f4 b7b5 f1d3
			// Example: info multipv 6 depth 1 seldepth 10 score mate -2 time 1 nodes 691 pv f1g2 d8h4 e1f1 h4f2
			
			String rank = extractGroup(line, "multipv (\\d*)");
			String score = extractGroup(line, "score (\\S* \\S*)");
			String move = extractGroup(line, " pv ([a-h0-9]{4}[rbnq]?)");
			
			if(rank == null || score == null || move == null) {
				System.err.println("Can't extract move from engine. UCI line: " + line);
				return;
			}
			
			int rankInt = Integer.parseInt(rank) - 1;
			Square fromSquare = new Square(move.substring(0, 2));
			Square toSquare = new Square(move.substring(2, 4));
			
			PromotionChoice promote = null;
			if(move.length() == 5) {
				char p = move.charAt(4);
				if(p == 'r') promote = PromotionChoice.ROOK;
				if(p == 'b') promote = PromotionChoice.BISHOP;
				if(p == 'n') promote = PromotionChoice.KNIGHT;
				if(p == 'q') promote = PromotionChoice.QUEEN;
			}
			
			try {
				int scoreNum = Integer.parseInt(score.split(" ")[1]);
				if(score.contains("mate")) {
					if(scoreNum > 0) {
						scoreNum += 100000;
					} else {
						scoreNum -= 100000;
					}
				}
				
				if(currentPosition.getSideToPlay() == Side.WHITE) {
					score = "White " + score;
				} else {
					score = "Black " + score;
				}
				
				Piece piece = currentPosition.getPiece(fromSquare);
				boolean castling = (piece != null && piece.getPieceType() == PieceType.KING && Math.abs(fromSquare.getX() - toSquare.getX()) > 1);
				
				engineMoves[rankInt] = new EngineMove(currentPosition, new Move(fromSquare, toSquare, castling, promote), score, scoreNum);
			} catch(IllegalMoveException e) {
				System.err.println("IllegalMove from engine. UCI line: " + line);
				System.err.println(fromSquare + "-" + toSquare + " -- " + currentPosition.getFen());
			}
		}
	}
	
	private void displayEngineMoves() {
		if(!currentPosition.validPosition()) {
			// Don't display anything for invalid positions.
			engineView.setEngineMoves(new ArrayList<EngineMove>());
			boardCanvas.setEngineArrows(new ArrayList<BoardArrow>());
			return;
		}
		
		int maxScore = Integer.MIN_VALUE;
		int minScore = Integer.MAX_VALUE;
		for(EngineMove engineMove:engineMoves) {
			if(engineMove == null) continue;
			
			maxScore = Math.max(maxScore, engineMove.getScoreNum());
			minScore = Math.min(minScore, engineMove.getScoreNum());
		}
		
		// Update table.
		engineView.setEngineMoves(Arrays.asList(engineMoves));
		
		// Update board arrows.
		List<BoardArrow> arrows = new ArrayList<BoardArrow>();
		if(showArrows) {
			for(EngineMove engineMove:engineMoves) {
				if(engineMove == null) continue;
				
				Square start = engineMove.getMove().getFrom();
				Square end = engineMove.getMove().getTo();
				
				double score = engineMove.getScoreNum();
				if(score > 0) {
					score = score / maxScore;
				} else {
					score = -(score / minScore);
				}
				arrows.add(new BoardArrow(start, end, (score/2) + 0.5));
			}
		}
		boardCanvas.setEngineArrows(arrows);
	}

	private String extractGroup(String line, String pattern) {
		Matcher matcher = Pattern.compile(pattern).matcher(line);
		
		if(!matcher.find()) {
			return null;
		}
		
		return matcher.group(1);
	}

	private void readLoop() {
		this.readThread = new Thread(new Runnable() {
			public void run() {
				try {
					while(true) {
						String line = reader.readLine();
						
						if(line == null) {
							System.err.println("Engine input stream closed");
							exitEngine();
							return;
						}
						
						handleEngineOutput(line);
					}
				} catch(IOException e) {
					e.printStackTrace();
					exitEngine();
				}
			}
		});
		
		readThread.setDaemon(true);
		readThread.start();
	}
	
	private void readErrLoop() {
		this.readErrThread = new Thread(new Runnable() {
			public void run() {
				try {
					while(true) {
						String line = errReader.readLine();
						
						if(line == null) {
							System.err.println("Engine error stream closed");
							exitEngine();
							return;
						}
						
						System.err.println("Engine error: " + line);
					}
				} catch(IOException e) {
					e.printStackTrace();
					exitEngine();
				}
			}
		});
		
		readErrThread.setDaemon(true);
		readErrThread.start();
	}
	
	private void updateViewLoop() {
		this.updateViewThread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							displayEngineMoves();
						}
					});
					
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		
		updateViewThread.setDaemon(true);
		updateViewThread.start();
	}
	
	public void dispose() {
		exitEngine();
	}

	public void showArrows() {
		showArrows = true;
	}
	
	public void hideArrows() {
		showArrows = false;
	}

	public boolean areArrowsShown() {
		return showArrows;
	}
}
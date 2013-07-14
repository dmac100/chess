package controller;

import org.eclipse.swt.widgets.Display;

import domain.*;

public class EnginePlayController {
	private boolean playWhite;
	private boolean playBlack;
	
	private AnalysisEngine analysisEngine;
	private MainController mainController;
	
	private Thread thread;
	
	public EnginePlayController(MainController mainController, AnalysisEngine analysisEngine) {
		this.mainController = mainController;
		this.analysisEngine = analysisEngine;
	}

	public void enginePlayWhite() {
		playWhite = true;
		playBlack = false;
		madeMove();
	}

	public void enginePlayBlack() {
		playWhite = false;
		playBlack = true;
		madeMove();
	}

	public void enginePlayBoth() {
		playWhite = true;
		playBlack = true;
		madeMove();
	}

	public void enginePlayNone() {
		playWhite = false;
		playBlack = false;
	}
	
	public boolean isEnginePlaying() {
		return playWhite || playBlack;
	}

	public synchronized void madeMove() {
		final Board startBoard = mainController.getCurrentPosition();
		
		if(startBoard.getSideToPlay() == Side.WHITE) {
			if(!playWhite) {
				return;
			}
		} else {
			if(!playBlack) {
				return;
			}
		}
		
		if(thread != null) {
			thread.interrupt();
		}
		
		this.thread = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							Board board = mainController.getCurrentPosition();
							
							if(!board.equals(startBoard)) {
								return;
							}
					
							try {
								mainController.makeMove(analysisEngine.getBestMove());
							} catch(IllegalMoveException e) {
								System.err.println("Illegal move in EnginePlayController");
							}
						}
					});
				} catch(InterruptedException e) {
					return;
				}
			}
		});
		
		thread.start();
	}
}

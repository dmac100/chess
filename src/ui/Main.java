package ui;

import org.apache.commons.cli.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import controller.MainController;
import domain.Side;

public class Main {
	private MainController mainController;
	private Shell shell;
	private MoveHistoryTree moveHistoryTree;
	private DatabaseView databaseView;
	private EngineMovesTable engineView;

	class KeyListener implements Listener {
		public void handleEvent(Event event) {
			if(event.keyCode == SWT.ARROW_LEFT) {
				mainController.prevMove();
			} else if(event.keyCode == SWT.ARROW_RIGHT) {
				mainController.nextMove();
			} else if(event.character == ' ') {
				mainController.makeEngineMove();
			} else if(event.character == 'r') {
				mainController.randomMove();
			}
		}
	}

	public Main(Shell shell) {
		this.shell = shell;
		
		shell.setLayout(new GridLayout(1, false));
		
		Composite top = new Composite(shell, SWT.BORDER);
		Composite bottom = new Composite(shell, SWT.BORDER);
		
		bottom.setLayout(new FillLayout());
		bottom.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createToolBar(top);
		
		SashForm form = new SashForm(bottom, SWT.HORIZONTAL);
		form.setLayout(new FillLayout());
		
		BoardCanvas boardCanvas = new BoardCanvas(shell, form);
		
		createSidePanels(form);
		
		form.setWeights(new int[] { 75, 25 } );
		
		System.out.println("Creating main controller...");
		
		this.mainController = new MainController(boardCanvas, moveHistoryTree, databaseView, engineView);
		
		System.out.println("Done");
		
		createMenuBar(shell);
		
		/*
		try {
			mainController.openFile("/home/david/incoming/testgame.pgn");
			//mainController.importDatabase("/home/david/incoming/games.pgn");
			//mainController.importDatabase("/home/david/output-black.pgn");
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		*/
		
		shell.addListener(SWT.KeyUp, new KeyListener());
		boardCanvas.getWidget().addListener(SWT.KeyUp, new KeyListener());
		//moveHistory.getWidget().addListener(SWT.KeyUp, new KeyListener());
		moveHistoryTree.getWidget().addListener(SWT.KeyUp, new KeyListener());
		for(Widget widget:databaseView.getWidgets()) {
			widget.addListener(SWT.KeyUp, new KeyListener());
		}
		
		boardCanvas.getWidget().setFocus();
	}
	
	private void createSidePanels(Composite parent) {
		CTabFolder tabFolder = new CTabFolder(parent, SWT.NONE);
		
		/*
		CTabItem historyItem = new CTabItem(tabFolder, SWT.NONE);
		historyItem.setText("Moves");
		this.moveHistory = new MoveHistoryTable(tabFolder);
		historyItem.setControl(moveHistory.getWidget());
		*/
		
		CTabItem moveTreeItem = new CTabItem(tabFolder, SWT.NONE);
		moveTreeItem.setText("Moves");
		this.moveHistoryTree = new MoveHistoryTree(tabFolder);
		moveTreeItem.setControl(moveHistoryTree.getWidget());
		
		CTabItem databaseItem = new CTabItem(tabFolder, SWT.NONE);
		databaseItem.setText("Database");
		this.databaseView = new DatabaseView(tabFolder);
		databaseItem.setControl(databaseView.getWidget());
		
		CTabItem engineItem = new CTabItem(tabFolder, SWT.NONE);
		engineItem.setText("Engine");
		this.engineView = new EngineMovesTable(tabFolder);
		engineItem.setControl(engineView.getWidget());
		
		tabFolder.setSelection(0);
	}

	private void createMenuBar(final Shell shell) {
		Menu menu = new Menu(shell, SWT.BAR);
		
		MenuItem fileMenuItem = new MenuItem(menu, SWT.MENU);
		fileMenuItem.setText("File");
		Menu fileMenu = new Menu(fileMenuItem);
		fileMenuItem.setMenu(fileMenu);
		
		MenuItem newItem = new MenuItem(fileMenu, SWT.NONE);
		newItem.setText("New Game");
		newItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.newGame();
			}
		});
		
		MenuItem openItem = new MenuItem(fileMenu, SWT.NONE);
		openItem.setText("Open...");
		openItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String selected = selectPgnWithDialog();
				if(selected != null) {
					try {
						mainController.openFile(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			}
		});
		
		MenuItem importDatabaseItem = new MenuItem(fileMenu, SWT.NONE);
		importDatabaseItem.setText("Import Database...");
		importDatabaseItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String selected = selectPgnWithDialog();
				if(selected != null) {
					try {
						mainController.importDatabase(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem enterFen = new MenuItem(fileMenu, SWT.NONE);
		enterFen.setText("Enter FEN...");
		enterFen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String fen = selectFenWithDialog();
				if(fen != null) {
					try {
						mainController.setFen(fen.trim());
					} catch(Exception e) {
						displayException(e);
					}
				}
			}
		});
		
		MenuItem displayFen = new MenuItem(fileMenu, SWT.NONE);
		displayFen.setText("Display FEN...");
		displayFen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String fen = mainController.getFen();
				System.out.println(fen);
				displayFenDialog(fen);
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem enterPgn = new MenuItem(fileMenu, SWT.NONE);
		enterPgn.setText("Enter PGN...");
		enterPgn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					TextAreaDialog pgnDialog = new TextAreaDialog(shell, "PGN", true);
					String pgnText = pgnDialog.open();
					if(pgnText != null) {
						mainController.setPgn(pgnText);
					}
				} catch(Exception e) {
					displayException(e);
				}
			}
		});
		
		MenuItem displayPgn = new MenuItem(fileMenu, SWT.NONE);
		displayPgn.setText("Display PGN...");
		displayPgn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					String pgn = mainController.getPgn();
					TextAreaDialog pgnDialog = new TextAreaDialog(shell, "PGN", false);
					pgnDialog.setText(pgn);
					pgnDialog.open();
				} catch(Exception e) {
					displayException(e);
				}
			}
		});
		
		MenuItem saveItem = new MenuItem(fileMenu, SWT.NONE);
		saveItem.setText("Save PGN...");
		saveItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String selected = selectSaveLocationWithDialog();
				if(selected != null) {
					try {
						mainController.savePgn(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem exitItem = new MenuItem(fileMenu, SWT.NONE);
		exitItem.setText("Exit");
		exitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
		
		MenuItem viewMenuItem = new MenuItem(menu, SWT.MENU);
		viewMenuItem.setText("View");
		Menu viewMenu = new Menu(viewMenuItem);
		viewMenuItem.setMenu(viewMenu);
		
		MenuItem flipItem = new MenuItem(viewMenu, SWT.NONE);
		flipItem.setText("Flip Board");
		flipItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.flipBoard();
			}
		});
		
		new MenuItem(viewMenu, SWT.SEPARATOR);
		
		MenuItem showMoveArrowsItem = new MenuItem(viewMenu, SWT.NONE);
		showMoveArrowsItem.setText("Show Move Arrows");
		showMoveArrowsItem.setEnabled(!mainController.areMoveArrowsShown());
		showMoveArrowsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.showMoveArrows();
				createMenuBar(shell);
			}
		});
		
		MenuItem hideMoveArrowsItem = new MenuItem(viewMenu, SWT.NONE);
		hideMoveArrowsItem.setText("Hide Move Arrows");
		hideMoveArrowsItem.setEnabled(mainController.areMoveArrowsShown());
		hideMoveArrowsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.hideMoveArrows();
				createMenuBar(shell);
			}
		});
		
		MenuItem showEngineArrowsItem = new MenuItem(viewMenu, SWT.NONE);
		showEngineArrowsItem.setText("Show Engine Arrows");
		showEngineArrowsItem.setEnabled(!mainController.areEngineArrowsShown());
		showEngineArrowsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.showEngineArrows();
				createMenuBar(shell);
			}
		});
		
		MenuItem hideEngineArrowsItem = new MenuItem(viewMenu, SWT.NONE);
		hideEngineArrowsItem.setText("Hide Engine Arrows");
		hideEngineArrowsItem.setEnabled(mainController.areEngineArrowsShown());
		hideEngineArrowsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.hideEngineArrows();
				createMenuBar(shell);
			}
		});
		
		MenuItem gameMenuItem = new MenuItem(menu, SWT.MENU);
		gameMenuItem.setText("Game");
		Menu gameMenu = new Menu(gameMenuItem);
		gameMenuItem.setMenu(gameMenu);
		
		MenuItem editCommentMenuItem = new MenuItem(gameMenu, SWT.NONE);
		editCommentMenuItem.setText("Edit Comment...");
		editCommentMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String comment = mainController.getCurrentMoveComment();
				TextAreaDialog dialog = new TextAreaDialog(shell, "Comment", true);
				dialog.setText(comment);
				
				comment = dialog.open();
				
				if(comment != null) {
					if(comment.trim().equals("")) {
						comment = null;
					}
					mainController.setCurrentMoveComment(comment);
				}
			}
		});
		
		MenuItem deleteCommentMenuItem = new MenuItem(gameMenu, SWT.NONE);
		deleteCommentMenuItem.setText("Delete Comment");
		deleteCommentMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.setCurrentMoveComment(null);
			}
		});
		
		new MenuItem(gameMenu, SWT.SEPARATOR);
		
		MenuItem promoteVariationMenuItem = new MenuItem(gameMenu, SWT.NONE);
		promoteVariationMenuItem.setText("Promote Variation");
		promoteVariationMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.promoteVariation();
			}
		});
		
		MenuItem deleteVariationMenuItem = new MenuItem(gameMenu, SWT.NONE);
		deleteVariationMenuItem.setText("Delete Variation");
		deleteVariationMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.deleteVariation();
			}
		});
		
		MenuItem trimVariationMenuItem = new MenuItem(gameMenu, SWT.NONE);
		trimVariationMenuItem.setText("Trim Variation");
		trimVariationMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.trimVariation();
			}
		});
		
		new MenuItem(gameMenu, SWT.SEPARATOR);
		
		MenuItem editPositionItem = new MenuItem(gameMenu, SWT.NONE);
		editPositionItem.setText("Edit Position");
		editPositionItem.setEnabled(!mainController.isEditingPosition());
		editPositionItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.editPosition();
				createMenuBar(shell);
			}
		});
		
		MenuItem playPositionItem = new MenuItem(gameMenu, SWT.NONE);
		playPositionItem.setText("Play Position");
		playPositionItem.setEnabled(mainController.isEditingPosition());
		playPositionItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.playPosition();
				createMenuBar(shell);
			}
		});
		
		MenuItem whiteToPlayItem = new MenuItem(gameMenu, SWT.NONE);
		whiteToPlayItem.setText("Set White to Play");
		whiteToPlayItem.setEnabled(mainController.getToPlay() == Side.BLACK);
		whiteToPlayItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.setToPlay(Side.WHITE);
				createMenuBar(shell);
			}
		});
		
		MenuItem blackToPlayItem = new MenuItem(gameMenu, SWT.NONE);
		blackToPlayItem.setText("Set Black to Play");
		blackToPlayItem.setEnabled(mainController.getToPlay() == Side.WHITE);
		blackToPlayItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.setToPlay(Side.BLACK);
				createMenuBar(shell);
			}
		});
		
		MenuItem clearItem = new MenuItem(gameMenu, SWT.NONE);
		clearItem.setText("Clear Position");
		clearItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.clearPosition();
			}
		});
		
		MenuItem engineMenuItem = new MenuItem(menu, SWT.MENU);
		engineMenuItem.setText("Engine");
		Menu engineMenu = new Menu(engineMenuItem);
		engineMenuItem.setMenu(engineMenu);

		MenuItem whiteMenuItem = new MenuItem(engineMenu, SWT.NONE);
		whiteMenuItem.setText("Engine Play White (1 second)");
		whiteMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.enginePlayWhite();
				createMenuBar(shell);
			}
		});
		
		MenuItem blackMenuItem = new MenuItem(engineMenu, SWT.NONE);
		blackMenuItem.setText("Engine Play Black (1 second)");
		blackMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.enginePlayBlack();
				createMenuBar(shell);
			}
		});
		
		MenuItem bothMenuItem = new MenuItem(engineMenu, SWT.NONE);
		bothMenuItem.setText("Engine Play Both (1 second)");
		bothMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.enginePlayBoth();
				createMenuBar(shell);
			}
		});
		
		MenuItem stopEngineMenuItem = new MenuItem(engineMenu, SWT.NONE);
		stopEngineMenuItem.setText("Stop Engine");
		stopEngineMenuItem.setEnabled(mainController.isEnginePlaying());
		stopEngineMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.enginePlayNone();
				createMenuBar(shell);
			}
		});
		
		shell.setMenuBar(menu);
	}
	
	private String selectPgnWithDialog() {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Open");
		dialog.setFilterExtensions(new String[] { "*.pgn", "*.*" });
		
		return dialog.open();
	}
	
	private String selectSaveLocationWithDialog() {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setText("Save");
		dialog.setFileName("game.pgn");
		
		return dialog.open();
	}
	
	private String selectFenWithDialog() {
		InputDialog dialog = new InputDialog(shell, "FEN");
		dialog.setText("Enter FEN");
		return dialog.open();
	}
	
	private void displayFenDialog(String fen) {
		MessageBox messageBox = new MessageBox(shell);
		messageBox.setText("FEN");
		messageBox.setMessage(fen);
		
		messageBox.open();
	}
	
	private void createToolBar(Composite parent) {
		parent.setLayout(new FillLayout());
		
		Button firstButton = new Button(parent, SWT.BORDER);
		firstButton.setText("|<");
		firstButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.firstMove();
			}
		});
		
		Button prevButton = new Button(parent, SWT.BORDER);
		prevButton.setText("<");
		prevButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.prevMove();
			}
		});
		
		Button nextButton = new Button(parent, SWT.BORDER);
		nextButton.setText(">");
		nextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.nextMove();
			}
		});
		
		Button lastButton = new Button(parent, SWT.BORDER);
		lastButton.setText(">|");
		lastButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.lastMove();
			}
		});
		
		Button promoteButton = new Button(parent, SWT.BORDER);
		promoteButton.setText("Promote Variation");
		promoteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.promoteVariation();
			}
		});
		
		Button deleteButton = new Button(parent, SWT.BORDER);
		deleteButton.setText("Delete Variation");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.deleteVariation();
			}
		});
		
		Button moveButton = new Button(parent, SWT.BORDER);
		moveButton.setText("Engine Move");
		moveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				mainController.makeEngineMove();
			}
		});
	}
	
	private void displayException(Exception e) {
		MessageBox messageBox = new MessageBox(shell);
		messageBox.setText("Error");
		messageBox.setMessage(e.getMessage() == null ? e.toString() : e.getMessage());
		e.printStackTrace();
		
		messageBox.open();
	}
	
	private void parseArgs(String[] args) {
		CommandLineParser parser = new GnuParser();
		
		Options options = new Options();
		options.addOption(new Option("f", "fen", true, "set the initial position from a FEN string"));
		options.addOption(new Option("p", "pgn", true, "loads a PGN file"));
		options.addOption(new Option("d", "database", true, "loads a database file"));
		options.addOption(new Option("h", "help", false, "show help"));
		
		try {
			CommandLine command = parser.parse(options, args);
		
			if(command.hasOption("h")) {
				new HelpFormatter().printHelp("java ui.Main", options);
				System.exit(0);
			}
			
			if(command.hasOption("p")) {
				mainController.openFile(command.getOptionValue("p"));
			} else if(command.hasOption("f")) {
				mainController.setFen(command.getOptionValue("f"));
			}
			
			if(command.hasOption("d")) {
				mainController.importDatabase(command.getOptionValue("d"));
			}
		} catch(Throwable e) {
			// Print usage and exit on any error.
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("java ui.Main", options);
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Creating display...");
		
		Display display = new Display();
		
		System.out.println("Creating shell...");
		
		Shell shell = new Shell(display);
		
		Main main = new Main(shell);
		shell.setSize(850, 650);
		shell.setText("Chess");
		shell.setVisible(true);
		
		System.out.println("Parsing args...");

		main.parseArgs(args);
		
		System.out.println("Running event loop...");
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		main.dispose();
		display.close();
	}

	private void dispose() {
		mainController.dispose();
	}
}

package ui;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import controller.MainController;
import domain.Side;

public class Main {
	private final CommandList commandList = new CommandList();
	
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
		
		this.mainController = new MainController(boardCanvas, moveHistoryTree, databaseView, engineView);
		
		System.out.println("Done");
		
		createMenuBar(shell);
		
		shell.addListener(SWT.KeyUp, new KeyListener());
		boardCanvas.getWidget().addListener(SWT.KeyUp, new KeyListener());
		moveHistoryTree.getWidget().addListener(SWT.KeyUp, new KeyListener());
		for(Widget widget:databaseView.getWidgets()) {
			widget.addListener(SWT.KeyUp, new KeyListener());
		}
		
		boardCanvas.getWidget().setFocus();
	}
	
	private void createSidePanels(Composite parent) {
		SashForm form = new SashForm(parent, SWT.VERTICAL);
		form.setLayout(new FillLayout());
		
		CTabFolder topTabFolder = new CTabFolder(form, SWT.NONE);
		topTabFolder.setTabHeight(22);
		
		CTabFolder bottomTabFolder = new CTabFolder(form, SWT.NONE);
		topTabFolder.setTabHeight(22);
		
		CTabItem moveTreeItem = new CTabItem(topTabFolder, SWT.NONE);
		moveTreeItem.setText(" Moves ");
		this.moveHistoryTree = new MoveHistoryTree(topTabFolder);
		moveTreeItem.setControl(moveHistoryTree.getWidget());

		CTabItem engineItem = new CTabItem(bottomTabFolder, SWT.NONE);
		engineItem.setText(" Engine ");
		this.engineView = new EngineMovesTable(bottomTabFolder);
		engineItem.setControl(engineView.getWidget());
		
		CTabItem databaseItem = new CTabItem(bottomTabFolder, SWT.NONE);
		databaseItem.setText(" Database ");
		this.databaseView = new DatabaseView(bottomTabFolder);
		databaseItem.setControl(databaseView.getWidget());
		
		topTabFolder.setSelection(0);
		bottomTabFolder.setSelection(0);
		form.setWeights(new int[] { 40, 60 } );
	}

	private void createMenuBar(final Shell shell) {
		MenuBuilder menuBuilder = new MenuBuilder(shell, commandList);
		
		menuBuilder.addMenu("File")
			.addItem("New Game").addSelectionListener(() -> mainController.newGame())
			.addItem("Open...").addSelectionListener(() -> {
				String selected = selectOpenLocationWithDialog("*.pgn", "*.*");
				if(selected != null) {
					try {
						mainController.openFile(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.addSeparator()
			.addItem("Import Database...").addSelectionListener(() -> {
				String selected = selectOpenLocationWithDialog("*.pgn", "*.db", "*.*");
				if(selected != null) {
					try {
						mainController.importDatabase(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.addItem("Export Database...").addSelectionListener(() -> {
				String selected = selectSaveLocationWithDialog("game", "*.db");
				if(selected != null) {
					try {
						mainController.exportDatabase(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			}).setEnabled(mainController.moveDatabaseLoaded())
			.addSeparator()
			.addItem("Enter FEN...").addSelectionListener(() -> {
				String fen = selectFenWithDialog();
				if(fen != null) {
					try {
						mainController.setFen(fen.trim());
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.addItem("Display FEN...").addSelectionListener(() -> {
				String fen = mainController.getFen();
				System.out.println(fen);
				displayFenDialog(fen);
			})
			.addSeparator()
			.addItem("Enter PGN...").addSelectionListener(() -> {
				try {
					TextAreaDialog pgnDialog = new TextAreaDialog(shell, "PGN", true);
					String pgnText = pgnDialog.open();
					if(pgnText != null) {
						mainController.setPgn(pgnText);
					}
				} catch(Exception e) {
					displayException(e);
				}
			})
			.addItem("Display PGN...").addSelectionListener(() -> {
				try {
					String pgn = mainController.getPgn();
					TextAreaDialog pgnDialog = new TextAreaDialog(shell, "PGN", false);
					pgnDialog.setText(pgn);
					pgnDialog.open();
				} catch(Exception e) {
					displayException(e);
				}
			})
			.addItem("Save PGN...").addSelectionListener(() -> {
				String selected = selectSaveLocationWithDialog("game.pgn", "*.pgn");
				if(selected != null) {
					try {
						mainController.savePgn(selected);
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.addSeparator()
			.addItem("Run Command...\tCtrl+3").addSelectionListener(() -> runCommand()).setAccelerator(SWT.CONTROL | '3')
			.addSeparator()
			.addItem("Exit").addSelectionListener(() -> shell.dispose());
		
		menuBuilder.addMenu("View")
			.addItem("Flip Board").addSelectionListener(() -> mainController.flipBoard())
			.addSeparator()
			.addItem("Show Move Arrows").addSelectionListener(() -> {
				mainController.showMoveArrows();
				createMenuBar(shell);
			}).setEnabled(!mainController.areMoveArrowsShown())
			.addItem("Hide Move Arrows").addSelectionListener(() -> {
				mainController.hideMoveArrows();
				createMenuBar(shell);
			}).setEnabled(mainController.areMoveArrowsShown())
			.addItem("Show Engine Arrows").addSelectionListener(() -> {
				mainController.showEngineArrows();
				createMenuBar(shell);
			}).setEnabled(!mainController.areEngineArrowsShown())
			.addItem("Hide Engine Arrows").addSelectionListener(() -> {
				mainController.hideEngineArrows();
				createMenuBar(shell);
			}).setEnabled(mainController.areEngineArrowsShown());
		
		menuBuilder.addMenu("Game")
			.addItem("Edit Comment...").addSelectionListener(() -> {
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
			})
			.addItem("Delete Comment").addSelectionListener(() -> {
				mainController.setCurrentMoveComment(null);
			})
			.addSeparator()
			.addItem("Promote Variation").addSelectionListener(() -> {
				mainController.promoteVariation();
			})
			.addItem("Delete Variation").addSelectionListener(() -> {
				mainController.deleteVariation();
			})
			.addItem("Trim Variation").addSelectionListener(() -> {
				mainController.trimVariation();
			})
			.addSeparator()
			.addItem("Edit Position").addSelectionListener(() -> {
				mainController.editPosition();
				createMenuBar(shell);
			}).setEnabled(!mainController.isEditingPosition())
			.addItem("Play Position").addSelectionListener(() -> {
				mainController.playPosition();
				createMenuBar(shell);
			}).setEnabled(mainController.isEditingPosition())
			.addItem("Set White to Play").addSelectionListener(() -> {
				mainController.setToPlay(Side.WHITE);
				createMenuBar(shell);
			}).setEnabled(mainController.getToPlay() == Side.BLACK)
			.addItem("Set Black to Play").addSelectionListener(() -> {
				mainController.setToPlay(Side.BLACK);
				createMenuBar(shell);
			}).setEnabled(mainController.getToPlay() == Side.WHITE)
			.addItem("Insert Null Move").addSelectionListener(() -> {
				mainController.insertNullMove();
			})
			.addItem("Clear Position").addSelectionListener(() -> {
				mainController.clearPosition();
			});
			
		menuBuilder.addMenu("Engine")		
			.addItem("Engine Play White (1 second)").addSelectionListener(() -> {
				mainController.enginePlayWhite();
				createMenuBar(shell);
			})
			.addItem("Engine Play Black (1 second)").addSelectionListener(() -> {
				mainController.enginePlayBlack();
				createMenuBar(shell);
			})
			.addItem("Engine Play Both (1 second)").addSelectionListener(() -> {
				mainController.enginePlayBoth();
				createMenuBar(shell);
			})
			.addItem("Stop Engine").addSelectionListener(() -> {
				mainController.enginePlayNone();
				createMenuBar(shell);
			}).setEnabled(mainController.isEnginePlaying());
		
		menuBuilder.build();
	}
	
	private void runCommand() {
		RunCommand runCommand = new RunCommand(shell);
		runCommand.setSearchFunction(findText -> commandList.findCommands(findText));
		String result = runCommand.open();
		if(result != null) {
			commandList.runCommand(result);
		}
	}
	
	private String selectOpenLocationWithDialog(String... extensions) {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Open");
		dialog.setFilterExtensions(extensions);
		
		return dialog.open();
	}
	
	private String selectSaveLocationWithDialog(String defaultName, String... extensions) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setText("Save");
		dialog.setFilterExtensions(extensions);
		dialog.setFileName(defaultName);
		
		return dialog.open();
	}
	
	private String selectFenWithDialog() {
		InputDialog dialog = new InputDialog(shell, "FEN", "[^/0-9a-zA-Z\\- ]");
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
		Display display = new Display();
		
		Shell shell = new Shell(display);
		
		Main main = new Main(shell);
		shell.setSize(850, 650);
		shell.setText("Chess");
		shell.setVisible(true);

		main.parseArgs(args);
		
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
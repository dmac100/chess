package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import domain.Board;
import domain.DatabaseMove;
import domain.IllegalMoveException;
import domain.Move;
import pgn.ParseException;
import pgn.PgnGame;
import pgn.PgnImporter;

public class DatabaseView {
	private SashForm sashForm;
	private Composite composite;
	private Table moveTable;
	private Table gameTable;
	private Label openingLabel;
	
	private ColorManager colorManager = new ColorManager(Display.getCurrent());
	private List<DatabaseMoveSelectedHandler> moveSelectedHandlers = new ArrayList<>();
	private List<DatabaseGameSelectedHandler> gameSelectedHandlers = new ArrayList<>();
	
	public DatabaseView(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		this.openingLabel = new Label(composite, SWT.NONE);
		openingLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		this.sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.moveTable = new Table(sashForm, SWT.NONE);
		moveTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.gameTable = new Table(sashForm, SWT.NONE);
		
		gameTable.setHeaderVisible(true);
		new TableColumn(gameTable, SWT.NONE).setText("Date");
		new TableColumn(gameTable, SWT.NONE).setText("White");
		new TableColumn(gameTable, SWT.NONE).setText("Black");
		new TableColumn(gameTable, SWT.NONE).setText("Result");
		new TableColumn(gameTable, SWT.NONE).setText("Time-Control");
		
		for(TableColumn column:gameTable.getColumns()) {
			column.pack();
		}
		
		final TableColumn column1 = new TableColumn(moveTable, SWT.NONE);
		final TableColumn column2 = new TableColumn(moveTable, SWT.NONE);
		
		column1.setWidth(70);
		column2.setWidth(100);
		
		moveTable.addListener(SWT.PaintItem, new Listener() {
			public void handleEvent(Event event) {
				GC gc = event.gc;
				
				int x = event.x;
				int y = event.y;
				int width = event.width;
				int height = event.height;
				
				TableItem item = (TableItem)event.item;
				
				DatabaseMove move = (DatabaseMove)item.getData();
				
				if(event.index == 1) {
					Color blackWin = colorManager.getHexColor("111111");
					Color draw = colorManager.getHexColor("aaaaaa");
					Color whiteWin = colorManager.getHexColor("eeeeee");
					
					int start = x + 2;
					int end = start + column2.getWidth() - 8;
					int mid1 = (int)(start + (end - start) * ((double)move.getWin() / (move.getTotal())));
					int mid2 = (int)(start + (end - start) * ((double)(move.getWin() + move.getDraw()) / (move.getTotal())));
					
					gc.setBackground(whiteWin);
					gc.fillRectangle(start, y + 2, mid1 - start, height - 4);
					
					gc.setBackground(draw);
					gc.fillRectangle(mid1, y + 2, mid2 - mid1, height - 4);
					
					gc.setBackground(blackWin);
					gc.fillRectangle(mid2, y + 2, end - mid2, height - 4);
				}
			}
		});
		
		moveTable.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				TableItem item = (TableItem)event.item;
				DatabaseMove databaseMove = (DatabaseMove)item.getData();
				Move move = databaseMove.getMove();
				
				for(DatabaseMoveSelectedHandler handler:moveSelectedHandlers) {
					handler.onDatabaseMoveSelected(move);
				}
			}
		});
		
		gameTable.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				TableItem item = (TableItem)event.item;
				String game = (String) item.getData();
				
				for(DatabaseGameSelectedHandler handler:gameSelectedHandlers) {
					handler.onDatabaseGameSelected(game);
				}
			}
		});
	}
	
	public void addMoveSelectedHandler(DatabaseMoveSelectedHandler handler) {
		moveSelectedHandlers.add(handler);
	}
	
	public void addGameSelectedHandler(DatabaseGameSelectedHandler handler) {
		gameSelectedHandlers.add(handler);
	}
	
	public void setOpening(String opening) {
		openingLabel.setText(opening);
	}
	
	public void setMoves(Board board, List<DatabaseMove> moves) {
		moveTable.removeAll();
		
		for(DatabaseMove move:moves) {
			try {
				TableItem item = new TableItem(moveTable, SWT.NONE);
				item.setText(0, board.getMoveAsPgn(move.getMove()) + " (" + move.getTotal() + ")");
				item.setData(move);
			} catch(IllegalMoveException e) {
				System.out.println("Illegal move in database: " + e.getMessage());
			}
		}
	}
	
	public void setGames(List<String> pgnGames) {
		gameTable.removeAll();
		
		if(pgnGames.size() > 100) {
			pgnGames = pgnGames.subList(0, 100);
		}
		
		for(String pgn:pgnGames) {
			TableItem item = new TableItem(gameTable, SWT.NONE);
			
			try {
				PgnGame pgnGame = new PgnImporter().importPgn(pgn);
				
				item.setText(0, pgnGame.getDate());
				item.setText(1, pgnGame.getWhite());
				item.setText(2, pgnGame.getBlack());
				item.setText(3, pgnGame.getResult().toString());
				item.setText(4, pgnGame.getTags().get("TimeControl"));
				
				item.setData(pgn);
			} catch (ParseException e) {
				throw new RuntimeException("Error paring game", e);
			}
		}
	}

	public Composite getWidget() {
		return composite;
	}
	
	public Widget[] getWidgets() {
		return new Widget[] { openingLabel, composite, moveTable };
	}
}

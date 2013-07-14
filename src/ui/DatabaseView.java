package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import domain.*;

public class DatabaseView {
	private Composite composite;
	private Table table;
	private Label openingLabel;
	
	private ColorManager colorManager = new ColorManager(Display.getCurrent());
	private List<DatabaseItemSelectedHandler> itemSelectedHandlers = new ArrayList<DatabaseItemSelectedHandler>();
	
	public DatabaseView(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		this.openingLabel = new Label(composite, SWT.NONE);
		openingLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		this.table = new Table(composite, SWT.NONE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final TableColumn column1 = new TableColumn(table, SWT.NONE);
		final TableColumn column2 = new TableColumn(table, SWT.NONE);
		
		column1.setWidth(70);
		column2.setWidth(100);
		
		table.addListener(SWT.PaintItem, new Listener() {
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
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				TableItem item = (TableItem)event.item;
				DatabaseMove databaseMove = (DatabaseMove)item.getData();
				Move move = databaseMove.getMove();
				
				for(DatabaseItemSelectedHandler handler:itemSelectedHandlers) {
					handler.onDatabaseItemSelected(move);
				}
			}
		});
	}
	
	public void addDatabaseItemSelectedHandler(DatabaseItemSelectedHandler handler) {
		itemSelectedHandlers.add(handler);
	}
	
	public void setOpening(String opening) {
		openingLabel.setText(opening);
	}
	
	public void setMoves(Board board, List<DatabaseMove> moves) {
		table.removeAll();
		
		for(DatabaseMove move:moves) {
			try {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, board.getMoveAsPgn(move.getMove()) + " (" + move.getTotal() + ")");
				item.setData(move);
			} catch(IllegalMoveException e) {
				System.out.println("Illegal move in database: " + e.getMessage());
			}
		}
	}

	public Composite getWidget() {
		return composite;
	}
	
	public Widget[] getWidgets() {
		return new Widget[] { openingLabel, composite, table };
	}
}

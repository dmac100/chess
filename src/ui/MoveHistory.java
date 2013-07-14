package ui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.*;

public class MoveHistory {
	private Table table;

	public MoveHistory(Composite parent) {
		this.table = new Table(parent, SWT.BORDER);
		
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		
		column1.setWidth(50);
		column2.setWidth(50);
		
		keepEqualWidthColumns(table);
	}
	
	public void setMoves(List<String> moves) {
		for(TableItem item:table.getItems()) {
			item.dispose();
		}
		
		TableItem item = null;
		
		boolean white = true;
		for(String move:moves) {
			if(white) {
				item = new TableItem(table, SWT.NONE);
			}
			item.setText(white ? 0 : 1, move);
			white = !white;
		}
	}
	
	public void addHistoryItemSelectedHandler(HistoryItemSelectedHandler handler) {
	}
	
	private static void keepEqualWidthColumns(final Table table) {
		table.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent event) {
				int totalWidth = table.getClientArea().width;
				int columnWidth = totalWidth / table.getColumnCount();
				
				for(TableColumn column:table.getColumns()) {
					column.setWidth(columnWidth);
				}
			}
			
			public void controlMoved(ControlEvent event) {
			}
		});
	}
}

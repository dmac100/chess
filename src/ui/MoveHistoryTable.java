package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import util.SwtUtil;

public class MoveHistoryTable {
	private Table table;
	private List<HistoryItemSelectedHandler> historyItemSelectedHandlers = new ArrayList<HistoryItemSelectedHandler>();
	
	public MoveHistoryTable(Composite parent) {
		this.table = new Table(parent, SWT.BORDER);
		
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		
		column1.setWidth(50);
		column2.setWidth(50);
		
		SwtUtil.keepEqualWidthColumns(table);
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int index = (Integer)event.item.getData();
				for(HistoryItemSelectedHandler handler:historyItemSelectedHandlers) {
					//handler.onHistoryItemSelected(index);
				}
			}
		});
	}
	
	public void setMoves(List<String> moves, boolean blackHasFirstMove) {
		table.removeAll();
		
		TableItem item = null;
		
		int index = 1;
		
		if(blackHasFirstMove) {
			item = new TableItem(table, SWT.NONE);
			item.setText(0, "1. ...");
			item.setData(2);
			index++;
		}
		
		boolean white = true;
		for(String move:moves) {
			if(white ^ blackHasFirstMove) {
				item = new TableItem(table, SWT.NONE);
				item.setData(index * 2);
				index++;
			}
			if(white ^ blackHasFirstMove) {
				item.setText(0, (index-1) + ". " + move);
			} else {
				item.setText(1, move);
			}
			white = !white;
		}
	}
	
	public void setSelected(int index) {
		table.select((index - 1) / 2);
	}
	
	public void addHistoryItemSelectedHandler(HistoryItemSelectedHandler handler) {
		historyItemSelectedHandlers.add(handler);
	}

	public Composite getWidget() {
		return table;
	}
}

package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import util.SwtUtil;
import domain.EngineMove;
import domain.Move;

public class EngineMovesTable {
	private Table table;
	private List<EngineItemSelectedHandler> engineItemSelectedHandlers = new ArrayList<EngineItemSelectedHandler>();
	
	private List<EngineMove> engineMoves = new ArrayList<EngineMove>();
	private List<Move> playerMoves = new ArrayList<Move>();
	
	public EngineMovesTable(Composite parent) {
		this.table = new Table(parent, SWT.BORDER);
		
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		
		column1.setWidth(50);
		column2.setWidth(50);
		
		SwtUtil.keepEqualWidthColumns(table);
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				EngineMove move = (EngineMove)event.item.getData();
				for(EngineItemSelectedHandler handler:engineItemSelectedHandlers) {
					handler.onEngineItemSelected(move);
				}
			}
		});
	}

	public void setEngineMoves(List<EngineMove> engineMoves) {
		this.engineMoves = engineMoves;
		refreshTable();
	}
	
	public void setPlayerMoves(List<Move> playerMoves) {
		this.playerMoves = playerMoves;
		refreshTable();
	}
	
	private void refreshTable() {
		table.getDisplay().asyncExec(new Runnable() {
			public void run() {
				table.removeAll();
				
				for(EngineMove move:engineMoves) {
					if(move == null) continue;
					
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, move.getPgnMove());
					item.setText(1, String.valueOf(move.getScore()));
					
					if(playerMoves.contains(move.getMove())) {
						item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
					}
					
					item.setData(move);
				}
			}
		});
	}

	public void addHistoryItemSelectedHandler(EngineItemSelectedHandler handler) {
		engineItemSelectedHandlers.add(handler);
	}
	
	public Composite getWidget() {
		return table;
	}
}

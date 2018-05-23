package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import domain.EngineMove;
import domain.Move;
import util.SwtUtil;

public class EngineMovesTable {
	private Composite composite;
	private EngineEvaluation engineEvaluation;
	private Button enabledButton;
	private Table table;
	private List<EngineItemSelectedHandler> engineItemSelectedHandlers = new ArrayList<>();
	private List<Consumer<Boolean>> enabledSelectedHandlers = new ArrayList<>();
	
	private List<EngineMove> engineMoves = new ArrayList<EngineMove>();
	private List<Move> playerMoves = new ArrayList<Move>();
	
	public EngineMovesTable(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		this.engineEvaluation = new EngineEvaluation(composite);
		GridData engineEvaluationGridData = new GridData(SWT.FILL, SWT.NONE, true, false);
		engineEvaluationGridData.heightHint = 12;
		engineEvaluation.setLayoutData(engineEvaluationGridData);
		
		this.table = new Table(composite, SWT.BORDER);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.enabledButton = new Button(composite, SWT.CHECK);
		enabledButton.setSelection(true);
		enabledButton.setText("Enabled");
		enabledButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		
		column1.setWidth(50);
		column2.setWidth(50);
		
		SwtUtil.keepEqualWidthColumns(table);
		
		enabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				enabledSelectedHandlers.forEach(handler -> handler.accept(enabledButton.getSelection()));
			}
		});
		
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
		EngineMovesTable.this.engineMoves = engineMoves;
		refreshTable();
	}
	
	public void setPlayerMoves(List<Move> playerMoves) {
		this.playerMoves = playerMoves;
		refreshTable();
	}
	
	private void refreshTable() {
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
		
		if(!engineMoves.isEmpty() && engineMoves.get(0) != null) {
			String bestScore = engineMoves.get(0).getScore();
			
			Matcher matcher;
			
			double score = 0;
			
			matcher = Pattern.compile("cp (-?\\d+)").matcher(bestScore);
			if(matcher.find()) {
				score = Integer.parseInt(matcher.group(1)) / 100.0;
			}
			
			matcher = Pattern.compile("mate (-?\\d+)").matcher(bestScore);
			if(matcher.find()) {
				score = Integer.parseInt(matcher.group(1)) < 0 ? -50 : 50;
			}
			
			if(bestScore.contains("Black")) {
				score = -score;
			}
			
			engineEvaluation.setScore(score);
		}
	}

	public void addHistoryItemSelectedHandler(EngineItemSelectedHandler handler) {
		engineItemSelectedHandlers.add(handler);
	}
	
	public void addEnabledSelectedHandler(Consumer<Boolean> handler) {
		enabledSelectedHandlers.add(handler);
	}
	
	public Composite getWidget() {
		return composite;
	}
}
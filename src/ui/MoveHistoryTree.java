package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import pgn.MoveHistoryFlattener;
import pgn.MoveHistoryFlattener.MoveHistoryVisitor;
import domain.MoveHistory;
import domain.MoveHistoryNode;

/**
 * Displays a move list, showing all the variations.
 */
public class MoveHistoryTree {
	private ScrolledComposite scroll;
	private Canvas canvas;
	private List<HistoryItemSelectedHandler> historyItemSelectedHandlers = new ArrayList<HistoryItemSelectedHandler>();
	
	private MoveHistory moveHistory;
	private List<MoveArea> moveAreas = new ArrayList<MoveArea>();
	private Font moveFont;
	private Font commentFont;
	private ColorManager colorManager;
	
	private static class MoveArea {
		private Rectangle rectangle;
		private MoveHistoryNode moveNode;
		
		public MoveArea(Rectangle rectangle, MoveHistoryNode moveHistoryNode) {
			this.rectangle = rectangle;
			this.moveNode = moveHistoryNode;
		}
		
		public boolean contains(int x, int y) {
			return rectangle.contains(x, y);
		}
		
		public MoveHistoryNode getMoveNode() {
			return moveNode;
		}
	}
	
	public MoveHistoryTree(Composite parent) {
		scroll = new ScrolledComposite(parent, SWT.V_SCROLL);
		canvas = new Canvas(scroll, SWT.NONE);
		
		colorManager = new ColorManager(Display.getCurrent());
		
		scroll.setContent(canvas);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paint(event.gc);
			}
		});
		
		this.moveFont = new Font(Display.getCurrent(), "Sans", 8, SWT.BOLD);
		this.commentFont = new Font(Display.getCurrent(), "Sans", 8, SWT.ITALIC);
		
		canvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				moveFont.dispose();
				commentFont.dispose();
				colorManager.dispose();
			}
		});
		
		canvas.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent event) {
				for(MoveArea moveArea:moveAreas) {
					if(moveArea.contains(event.x, event.y)) {
						for(HistoryItemSelectedHandler handler:historyItemSelectedHandlers) {
							handler.onHistoryItemSelected(moveArea.getMoveNode());
						}
					}
				}
			}
			
			public void mouseDown(MouseEvent event) {
			}
			
			public void mouseDoubleClick(MouseEvent event) {
			}
		});
	}
	
	private void paint(final GC gc) {
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(canvas.getClientArea());
		
		moveAreas.clear();
		
		if(moveHistory == null) {
			return;
		}
		
		new MoveHistoryFlattener(moveHistory).getMoveTokens(new MoveHistoryVisitor() {
			private int x = 0;
			private int y = 5;
			
			private int[] colors = { SWT.COLOR_BLACK, SWT.COLOR_BLUE, SWT.COLOR_RED, };
			private int color = 0;
			private int height = 0;
			
			@Override
			public void beginVariation() {
				color = Math.min(color + 1, colors.length - 1);
				
				drawText("(", false);
			}

			@Override
			public void endVariation() {
				drawText(")", false);
				
				color = Math.max(color - 1, 0);
			}

			@Override
			public void move(String text, MoveHistoryNode move, boolean currentMove) {
				Rectangle area = drawText(text + (move.getAnnotation() == null ? "" : move.getAnnotation()), currentMove);
				
				if(move.getComment() != null) {
					for(String word:move.getComment().split(" ")) {
						drawComment(word);
					}
				}
				
				moveAreas.add(new MoveArea(area, move));
			}
			
			private Rectangle drawText(String text, boolean highlight) {
				int width = gc.stringExtent(text).x;
				
				if(x + width + 10 > canvas.getClientArea().width) {
					x = 0;
					y += gc.getFontMetrics().getHeight() * 1.55;
				}
				
				int startX = x;
				int startY = y;
				
				if(highlight) {
					gc.setForeground(colorManager.getHexColor("000000"));
					gc.setBackground(colorManager.getHexColor("aaccff"));
					gc.fillRectangle(x-2, y-2, width+4, gc.getFontMetrics().getHeight()+4);
				} else {
					gc.setForeground(Display.getCurrent().getSystemColor(colors[color]));
				}

				gc.setFont(moveFont);
				gc.drawString(text, x, y, true);
				
				x += width;
				x += 10;
				
				height = Math.max(height, y + gc.getFontMetrics().getHeight() + 5);
				
				return new Rectangle(startX - 2, startY - 2, width + 4, gc.getFontMetrics().getHeight() + 4);
			}
			
			private void drawComment(String text) {
				int width = gc.stringExtent(text).x;
				
				if(x + width + 10 > canvas.getClientArea().width) {
					x = 0;
					y += gc.getFontMetrics().getHeight() * 1.55;
				}
				
				gc.setForeground(colorManager.getHexColor("999999"));
				gc.setBackground(colorManager.getHexColor("ffffff"));
				
				gc.setFont(commentFont);
				gc.drawString(text, x, y, true);
				
				x += width;
				x += 10;
				height = Math.max(height, y + gc.getFontMetrics().getHeight() + 5);
			}

			@Override
			public void end() {
				scroll.setMinHeight(height);
			}
		});
	}
	
	public Composite getWidget() {
		return scroll;
	}
	
	public void setMoves(MoveHistory moveHistory) {
		this.moveHistory = moveHistory;
		
		canvas.redraw();
	}
	
	public void addHistoryItemSelectedHandler(HistoryItemSelectedHandler handler) {
		historyItemSelectedHandlers.add(handler);
	}
}
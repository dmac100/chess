package ui;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import domain.*;
import domain.pieces.Piece;
import domain.pieces.PieceType;

/**
 * Display a board, and allow the making of moves by dragging the pieces.
 */
public class BoardCanvas {
	public static class BoardArrow {
		public Square start;
		public Square end;
		public double score;
		
		public BoardArrow(Square start, Square end, double score) {
			// TODO
			if(score < 0) score = 0;
			if(score > 1) score = 1;
			
			if(score < 0 || score > 1) {
				throw new IllegalArgumentException("Score must be between 0 and 1. Was: " + score);
			}
			
			this.start = start;
			this.end = end;
			this.score = score;
		}
	}
	
	private Shell shell;
	private Canvas canvas;
	private Board board;
	private java.util.List<BoardArrow> engineArrows = new ArrayList<BoardArrow>();
	private java.util.List<BoardArrow> moveArrows = new ArrayList<BoardArrow>();
	private final DragHandler dragHandler = new DragHandler();
	private ImageLoader imageLoader = new ImageLoader();
	private ImageLoader editImageLoader = new ImageLoader();
	private ColorManager colorManager;
	
	private Collection<BoardDragHandler> dragHandlers = new ArrayList<BoardDragHandler>();
	private Collection<BoardPositionChangedHandler> positionChangedHandlers = new ArrayList<BoardPositionChangedHandler>();
	
	private boolean flipped;
	private boolean editPosition;
	
	class PieceTypeAndSide {
		public PieceType pieceType;
		public Side side;
		public PieceTypeAndSide(PieceType pieceType, Side side) {
			this.pieceType = pieceType;
			this.side = side;
		}
	}
	
	/**
	 * Handles dragging of pieces by listening to mouse events.
	 */
	class DragHandler implements MouseListener, MouseMoveListener {
		private Square draggedFrom = null;
		private PieceTypeAndSide editDraggedPiece = null;
		private int draggedX;
		private int draggedY;
		private int draggedOffsetX;
		private int draggedOffsetY;

		/**
		 * Return the square of the board that the mouse is over, or null if it is not over a square.
		 */
		private Square getSquareFromEvent(MouseEvent event) {
			Rectangle area = canvas.getClientArea();
			int horizontalMargin = Math.max((area.width - area.height) / 2, 0);
			int verticalMargin = Math.max((area.height - area.width) / 2, 0);
			
			double width = canvas.getClientArea().width - (editPosition ? 32 : 0) - horizontalMargin * 2;
			double height = canvas.getClientArea().height - verticalMargin * 2;
			
			int x = (int)(((event.x - horizontalMargin) / width) * 8);
			int y = (int)(((event.y - verticalMargin) / height) * 8);
			
			if(!Square.inBounds(x, y)) {
				return null;
			}
			
			return getFlippedSquare(x, y);
		}
		
		public void mouseDoubleClick(MouseEvent event) {
		}

		public void mouseDown(MouseEvent event) {
			Rectangle area = canvas.getClientArea();
			int horizontalMargin = Math.max((area.width - area.height) / 2, 0);
			int verticalMargin = Math.max((area.height - area.width) / 2, 0);
			
			// Save starting square
			draggedFrom = getSquareFromEvent(event);
			
			double width = canvas.getClientArea().width - (editPosition ? 32 : 0) - horizontalMargin * 2;
			double height = canvas.getClientArea().height - verticalMargin * 2;
			
			// Save offset of mouse from top-left of the square.
			draggedOffsetX = floatMod(event.x - horizontalMargin, width / 8.0);
			draggedOffsetY = floatMod(event.y - verticalMargin, height / 8.0);
			
			draggedX = event.x - horizontalMargin;
			draggedY = event.y - verticalMargin;
			
			if(editPosition && event.x > width + horizontalMargin * 2) {
				int y = 0;
				
				for(Side side:Side.values()) {
					for(PieceType type:PieceType.values()) {
						y += 32;
						if(event.y < y) {
							// Save piece from the edit toolbar.
							editDraggedPiece = new PieceTypeAndSide(type, side);
							draggedFrom = null;
							draggedOffsetX = (int)(width / 16);
							draggedOffsetY = (int)(width / 16);
							redraw();
							return;
						}
					}
				}
				
			}
			
			redraw();
		}

		private int floatMod(double x, double y) {
			while(x >= y) {
				x -= y;
			}
			return (int)x;
		}

		public void mouseMove(MouseEvent event) {
			Rectangle area = canvas.getClientArea();
			int horizontalMargin = Math.max((area.width - area.height) / 2, 0);
			int verticalMargin = Math.max((area.height - area.width) / 2, 0);
			
			if(draggedFrom != null || editDraggedPiece != null) {
				// Update piece position and redraw.
				draggedX = event.x - horizontalMargin;
				draggedY = event.y - verticalMargin;
				redraw();
			}
		}
		
		public void mouseUp(MouseEvent event) {
			Square square = getSquareFromEvent(event);
			
			if(editPosition) {
				if(draggedFrom != null) {
					Piece piece = board.getPiece(draggedFrom);
					if(piece != null) {
						// Move piece dragged in edit mode.
						PieceType type = piece.getPieceType();
						Side side = piece.getSide();
						
						Board newBoard = new Board(board);
						newBoard = newBoard.clearPiece(draggedFrom);
						if(square != null) {
							newBoard = newBoard.placePiece(square, type, side);
						}
					
						for(BoardPositionChangedHandler positionChangedHandler:positionChangedHandlers) {
							positionChangedHandler.onPositionChanged(newBoard);
						}
					}
				} else if(editDraggedPiece != null) {
					// Place piece dragged from edit toolbar.
					if(square != null) {
						Board newBoard = board.placePiece(square, editDraggedPiece.pieceType, editDraggedPiece.side);
						for(BoardPositionChangedHandler positionChangedHandler:positionChangedHandlers) {
							positionChangedHandler.onPositionChanged(newBoard);
						}
					}
				}
			} else {
				if(draggedFrom != null) {
					if(square != null) {
						// Move piece dragged in game.
						PromotionChoice promote = null;
						
						Piece piece = board.getPiece(draggedFrom);
						if(piece != null) {
							if(piece.getPieceType() == PieceType.PAWN && (square.getY() == 0 || square.getY() == 7)) {
								promote = new PromotionDialog(shell).open();
							}
							
							// Allow castling if king is dragged more than one square horizontally, or onto a rook.
							boolean castling = false;
							if(piece.getPieceType() == PieceType.KING) {
								if(Math.abs(draggedFrom.getX() - square.getX()) > 1) {
									castling = true;
								}
								Piece draggedToPiece = board.getPiece(square);
								if(draggedToPiece != null && draggedToPiece.getPieceType() == PieceType.ROOK && draggedToPiece.getSide() == board.getSideToPlay()) {
									castling = true;
								}
							}
							
							// Set correct destination square for castling.
							if(castling && square.getX() != draggedFrom.getX()) {
								square = new Square(square.getX() > draggedFrom.getX() ? 6 : 2, square.getY());
							}
							
							for(BoardDragHandler dragHandler:dragHandlers) {
								dragHandler.onDrag(draggedFrom, square, castling, promote);
							}
							
							redraw();
						}
					}
				}
			}
			
			draggedFrom = null;
			editDraggedPiece = null;
		}

		public Square getDraggedFrom() {
			return draggedFrom;
		}

		public int getX() {
			return draggedX - draggedOffsetX;
		}
		
		public int getY() {
			return draggedY - draggedOffsetY;
		}
		
		public PieceTypeAndSide getDraggedPiece() {
			if(editPosition && editDraggedPiece != null) {
				return editDraggedPiece;
			}
			
			if(draggedFrom != null) {
				Piece piece = board.getPiece(draggedFrom);
				if(piece != null) {
					return new PieceTypeAndSide(piece.getPieceType(), piece.getSide());
				}
			}
			
			return null;
		}
	}
	
	public BoardCanvas(Shell shell, Composite parent) {
		this.shell = shell;
		
		colorManager = new ColorManager(Display.getCurrent());
		
		canvas = new Canvas(parent, SWT.NONE);
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				GC gc = event.gc;
				paint(gc);
			}
		});
		
		canvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				colorManager.dispose();
			}
		});
		
		canvas.addMouseListener(dragHandler);
		canvas.addMouseMoveListener(dragHandler);
	}
	
	public void setBoard(Board board) {
		this.board = board;
		redraw();
	}
	
	public void addDragHandler(BoardDragHandler dragHandler) {
		dragHandlers.add(dragHandler);
	}
	
	public void addPositionChangedHandler(BoardPositionChangedHandler positionChangedHandler) {
		positionChangedHandlers.add(positionChangedHandler);
	}
	
	public void setEngineArrows(java.util.List<BoardArrow> arrows) {
		this.engineArrows = arrows;
		redraw();
	}
	
	public void setMoveArrows(java.util.List<BoardArrow> arrows) {
		this.moveArrows = arrows;
		redraw();
	}
	
	private void redraw() {
		canvas.getDisplay().asyncExec(new Runnable() {
			public void run() {
				canvas.redraw();
			}
		});
	}
	
	private void paint(GC gc) {
		//Color black = colorManager.getHexColor("d18b47");
		//Color white = colorManager.getHexColor("ffcc9e");
		Color black = colorManager.getHexColor("749454");
		Color white = colorManager.getHexColor("f0f0d4");
		
		Rectangle area = canvas.getClientArea();
		
		int horizontalMargin = Math.max((area.width - area.height) / 2, 0);
		int verticalMargin = Math.max((area.height - area.width) / 2, 0);
		
		double w = (area.width - (editPosition ? 32 : 0) - horizontalMargin * 2) / 8.0;
		double h = (area.height - verticalMargin * 2) / 8.0;
		
		int size = (int)Math.min(w, h);
		if(size % 2 == 1) size -= 1;
		
		if(editPosition) {
			// Draw edit toolbar.
			int marginStart = area.width - 32;
			gc.setBackground(colorManager.getHexColor("777777"));
			gc.fillRectangle(marginStart, 0, area.width, area.height);
			int y = 0;
			for(Side side:Side.values()) {
				for(PieceType type:PieceType.values()) {
					Image image = editImageLoader.getPieceImage(type, side, 32, false);
					gc.drawImage(image, marginStart, y);
					y += 32;
				}
			}
		}
		
		Transform transform = new Transform(gc.getDevice());
		transform.translate(horizontalMargin, verticalMargin);
		gc.setTransform(transform);
		transform.dispose();
		
		// Draw square backgrounds.
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				gc.setBackground((x%2 == y%2) ? white : black);
				gc.fillRectangle((int)(x*w), (int)(y*h), (int)(w+1), (int)(h+1));
			}
		}
		
		// Draw row and column numbers.
		Font font = new Font(Display.getCurrent(), "Arial", 12, SWT.BOLD);
		gc.setFont(font);
		
		int fontHeight = gc.getFontMetrics().getHeight();
		
		for(int x = 0; x < 8; x++) {
			String file = String.valueOf((char)('A'+getFlippedSquare(x, 7).getX()));
			
			gc.setForeground((x%2 == 0) ? white : black);
			gc.setBackground((x%2 == 0) ? black : white);
			
			gc.drawText(file, (int)(x*w + 2), (int)(8*h - fontHeight - 2));
			
		}
		for(int y = 0; y < 8; y++) {
			String rank = String.valueOf((char)('0' + (8-getFlippedSquare(0, y).getY())));
			
			gc.setForeground((y%2 == 0) ? black : white);
			gc.setBackground((y%2 == 0) ? white : black);
			
			gc.drawText(rank, 2, (int)(y*h + 2));
		}
		font.dispose();
		
		// Draw pieces.
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				if(getFlippedSquare(x, y).equals(dragHandler.getDraggedFrom())) {
					continue;
				}

				// Draw piece within square.
				if(board != null) {
					Piece piece = board.getPiece(getFlippedSquare(x, y));
					if(piece != null) {
						Image image = imageLoader.getPieceImage(piece, size, true);
						
						int offsetX = (int)((w - image.getBounds().width) / 2.0);
						int offsetY = (int)((h - image.getBounds().height) / 2.0);
						
						gc.drawImage(image, (int)(x*w+offsetX), (int)(y*h+offsetY));
					}
				}
			}
		}
		
		// Draw piece being dragged.
		if((dragHandler.getDraggedPiece() != null)) {
			PieceTypeAndSide draggedPiece = dragHandler.getDraggedPiece();
			if(draggedPiece != null) {
				Image image = imageLoader.getPieceImage(draggedPiece.pieceType, draggedPiece.side, size, true);
				
				int offsetX = (int)((w - image.getBounds().width) / 2.0);
				int offsetY = (int)((h - image.getBounds().height) / 2.0);
				
				gc.drawImage(image, dragHandler.getX() + offsetX, dragHandler.getY() + offsetY);
			}
		}
		
		// Draw arrows.
		for(BoardArrow arrow:engineArrows) {
			Color color = colorManager.getColor((int)((1-arrow.score)*255), (int)(arrow.score*255), 0);
			drawArrow(gc, w, h, arrow.start, arrow.end, color, false);
		}
		
		for(BoardArrow arrow:moveArrows) {
			drawArrow(gc, w, h, arrow.start, arrow.end, Display.getCurrent().getSystemColor(SWT.COLOR_WHITE), true);
		}
	}
	
	private void drawArrow(GC gc, double w, double h, Square start, Square end, Color color, boolean bold) {
		start = getFlippedSquare(start);
		end = getFlippedSquare(end);
		
		double sx = start.getX()*w + w/2 - 1;
		double sy = start.getY()*h + h/2;
		double ex = end.getX()*w + w/2 - 1;
		double ey = end.getY()*h + h/2;
		
		double dx = ex - sx;
		double dy = ey - sy;
		
		double angle = Math.atan2(dx, dy) / Math.PI * 180;
		double length = Math.sqrt(dx*dx + dy*dy);
		
		Turtle t = new Turtle(sx, sy);
		t.rotate(angle);
		
		// Base
		t.rotate(90);
		t.forward(4);
		t.rotate(-90);
		// Head
		t.forward(length - 20);
		t.rotate(90);
		t.forward(6);
		t.rotate(-90 - 45);
		t.forward(Math.sqrt(10*10*2));
		// Point
		t.rotate(-90);
		// Other side
		t.forward(Math.sqrt(10*10*2));
		t.rotate(-90 - 45);
		t.forward(6);
		t.rotate(90);
		t.forward(length - 20);
	
		gc.setAlpha(140);
		
		gc.setBackground(color);
		gc.setLineWidth(bold ? 3 : 1);
		
		if(!bold) {
			gc.fillPolygon(t.getPoints());
		}
		
		if(bold) {
			gc.setAlpha(255);
		}
		
		gc.setForeground(colorManager.getColor(0, 0, 0));
		gc.drawPolygon(t.getPoints());
	}
	
	private Square getFlippedSquare(Square square) {
		return getFlippedSquare(square.getX(), square.getY());
	}

	public Square getFlippedSquare(int x, int y) {
		return flipped ? new Square(7-x, 7-y) : new Square(x, y);
	}

	public Composite getWidget() {
		return canvas;
	}

	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
		redraw();
	}

	public void setEditPosition(boolean editPosition) {
		this.editPosition = editPosition;
	}
}
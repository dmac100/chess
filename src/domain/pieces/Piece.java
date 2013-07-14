package domain.pieces;

import java.util.List;

import domain.Board;
import domain.Move;
import domain.Side;
import domain.Square;

public abstract class Piece implements Cloneable {
	protected Square square;
	protected Side side;
	
	public Piece(Square square, Side side) {
		this.square = square;
		this.side = side;
	}
	
	public Square getSquare() {
		return square;
	}
	
	public Side getSide() {
		return side;
	}
	
	public abstract PieceType getPieceType();
	
	public abstract List<Move> getPossibleMoves(Board board);
	
	protected void tryDirection(List<Move> moves, Board board, int dx, int dy) {
		int x = square.getX();
		int y = square.getY();
		
		while(true) {
			x += dx;
			y += dy;
			
			if(x < 0 || x >= 8 || y < 0 || y >= 8) return;
			
			Square to = new Square(x, y);
			
			Piece piece = board.getPiece(new Square(x, y));
			
			if(piece == null) {
				// Blank square. Carry on.
				moves.add(new Move(this.square, to, null));
			} else if(piece.getSide() != this.side) {
				// Enemy piece. Capture and return.
				moves.add(new Move(this.square, to, null));
				return;
			} else {
				// Friendly piece. Return.
				return;
			}
		}
	}
	
	protected void tryDiagonals(List<Move> moves, Board board) {
		int[] dx = {-1, 1, 1, -1};
		int[] dy = {-1, 1, -1, 1};
		for(int d = 0; d < 4; d++) {
			tryDirection(moves, board, dx[d], dy[d]);
		}
	}
	
	protected void tryRanksFiles(List<Move> moves, Board board) {
		int[] dx = {-1, 1, 0, 0};
		int[] dy = {0, 0, -1, 1};
		for(int d = 0; d < 4; d++) {
			tryDirection(moves, board, dx[d], dy[d]);
		}
	}
	
	protected boolean trySquare(List<Move> moves, Board board, int x, int y) {
		if(!square.inBounds(x, y)) return false;
		
		Square to = new Square(x, y);
		
		Piece piece = board.getPiece(to);
		
		if(piece == null || piece.getSide() != this.side) {
			moves.add(new Move(this.square, to, null));
			return true;
		} else {
			return false;
		}
	}

	public String getAlgebraicName() {
		String s = String.valueOf(getPieceType().getAlgebraic());
		return (side == side.WHITE ? s.toUpperCase() : s.toLowerCase());
	}
	
	public Piece clone() {
		try {
			return (Piece)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Piece setSquare(Square square) {
		Piece piece = this.clone();
		piece.square = square;
		return piece;
	}
}

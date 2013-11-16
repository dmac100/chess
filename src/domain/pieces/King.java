package domain.pieces;

import java.util.ArrayList;
import java.util.List;

import domain.*;

public class King extends Piece {
	public King(Square square, Side side) {
		super(square, side);
	}

	@Override
	public PieceType getPieceType() {
		return PieceType.KING;
	}

	@Override
	public List<Move> getPossibleMoves(Board board) {
		List<Move> moves = new ArrayList<Move>();
		
		for(int dx = -1; dx <= 1; dx++) {
			for(int dy = -1; dy <= 1; dy++) {
				if(dx != 0 || dy != 0) {
					int nx = square.getX() + dx;
					int ny = square.getY() + dy;
					
					trySquare(moves, board, nx, ny);
				}
			}
		}
		
		if(square.getX() == 4) {
			// Add castling moves. They will be checked when making a move.
			moves.add(new Move(square, new Square(2, square.getY()), true));
			moves.add(new Move(square, new Square(6, square.getY()), true));
		}
		
		return moves;
	}
}

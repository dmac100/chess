package domain.pieces;

import java.util.ArrayList;
import java.util.List;

import domain.Move;
import domain.Side;
import domain.Square;
import domain.Board;

public class Knight extends Piece {
	public Knight(Square square, Side side) {
		super(square, side);
	}

	@Override
	public PieceType getPieceType() {
		return PieceType.KNIGHT;
	}

	@Override
	public List<Move> getPossibleMoves(Board board) {
		List<Move> moves = new ArrayList<Move>();
		
		int[] dx = {-2, -2, -1, -1, 1, 1, 2, 2};
		int[] dy = {-1, 1, -2, 2, -2, 2, -1, 1};
		
		for(int d = 0; d < dx.length; d++) {
			int nx = square.getX() + dx[d];
			int ny = square.getY() + dy[d];
			
			trySquare(moves, board, nx, ny);
		}
		
		return moves;
	}
}

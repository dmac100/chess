package domain.pieces;

import java.util.ArrayList;
import java.util.List;

import domain.Move;
import domain.Side;
import domain.Square;
import domain.Board;

public class Bishop extends Piece {
	public Bishop(Square square, Side side) {
		super(square, side);
	}

	@Override
	public PieceType getPieceType() {
		return PieceType.BISHOP;
	}

	@Override
	public List<Move> getPossibleMoves(Board board) {
		List<Move> moves = new ArrayList<Move>();
		tryDiagonals(moves, board);
		return moves;
	}
}

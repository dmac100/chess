package domain.pieces;

import java.util.ArrayList;
import java.util.List;

import domain.Move;
import domain.Side;
import domain.Square;
import domain.Board;
import domain.Board;

public class Rook extends Piece {
	public Rook(Square square, Side side) {
		super(square, side);
	}

	@Override
	public PieceType getPieceType() {
		return PieceType.ROOK;
	}

	@Override
	public List<Move> getPossibleMoves(Board board) {
		List<Move> moves = new ArrayList<Move>();
		tryRanksFiles(moves, board);
		return moves;
	}
}

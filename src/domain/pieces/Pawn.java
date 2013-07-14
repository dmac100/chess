package domain.pieces;

import java.util.ArrayList;
import java.util.List;

import domain.*;

public class Pawn extends Piece {
	public Pawn(Square square, Side side) {
		super(square, side);
	}

	@Override
	public PieceType getPieceType() {
		return PieceType.PAWN;
	}

	@Override
	public List<Move> getPossibleMoves(Board board) {
		List<Move> moves = new ArrayList<Move>();

		int x = square.getX();
		int y = square.getY();
		
		int startRank = (this.side == Side.WHITE) ? 6 : 1;
		int direction = (this.side == Side.WHITE) ? -1 : 1;
		
		List<Move> nonCapturingMoves = new ArrayList<Move>();
		
		// Non-capturing moves.
		if(y + direction == 0 || y + direction == 7) {
			// Promotion.
			Square to = new Square(x, y + direction);
			if(board.getPiece(to) == null) {
				nonCapturingMoves.add(new Move(square, to, PromotionChoice.QUEEN));
				nonCapturingMoves.add(new Move(square, to, PromotionChoice.ROOK));
				nonCapturingMoves.add(new Move(square, to, PromotionChoice.KNIGHT));
				nonCapturingMoves.add(new Move(square, to, PromotionChoice.BISHOP));
			}
		} else {
			// One square forward.
			if(trySquare(nonCapturingMoves, board, x, y + direction)) {
				if(y == startRank) {
					// Two squares forward.
					trySquare(nonCapturingMoves, board, x, y + direction * 2);
				}
			}
		}
		
		// Add non-capturing moves after checking if a piece exists on the destination square.
		for(Move move:nonCapturingMoves) {
			if(board.getPiece(move.getTo()) == null) {
				moves.add(move);
			}
		}
		
		// En-passant.
		if(board.getEnPassant() != null) {
			Square to = board.getEnPassant();
			if(Math.abs(to.getX() - x) == 1 && y + direction == to.getY()) {
				moves.add(new Move(square, to, null));
			}
		}
		
		// Capture.
		for(int dx = -1; dx <= 1; dx+=2) {
			if(Square.inBounds(x + dx, y + direction)) {
				Square to = new Square(x + dx, y + direction);
				Piece piece = board.getPiece(to);
				if(piece != null && piece.getSide() == side.otherSide()) {
					if(y + direction == 0 || y + direction == 7) {
						// Capture and promote.
						moves.add(new Move(square, to, PromotionChoice.QUEEN));
						moves.add(new Move(square, to, PromotionChoice.BISHOP));
						moves.add(new Move(square, to, PromotionChoice.KNIGHT));
						moves.add(new Move(square, to, PromotionChoice.ROOK));
					} else {
						// Capture only.
						moves.add(new Move(square, to));
					}
				}
			}
		}
		
		return moves;
	}
}

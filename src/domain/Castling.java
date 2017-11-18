package domain;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import domain.pieces.Piece;
import domain.pieces.PieceType;

public class Castling {
	private Set<Character> allowed = new TreeSet<>();
	
	public Castling() {
		allowed.add('k');
		allowed.add('q');
		allowed.add('K');
		allowed.add('Q');
	}
	
	public Castling(String castling) {
		for(char c:castling.toCharArray()) {
			allowed.add(c);
		}
	}
	
	public Castling(Castling castling) {
		this.allowed = new TreeSet<>(castling.allowed);
	}
	
	public String toString() {
		return allowed.isEmpty() ? "-" : StringUtils.join(allowed, "");
	}
	
	public Castling nextCastling(Board board, Move move) {
		Castling next = new Castling(this);
		Square from = move.getFrom();
		Square to = move.getTo();
		Square kingSquare = getPiece(board, PieceType.KING, board.getSideToPlay());
		Piece piece = board.getPiece(move.getFrom());
		
		int backRank = (board.getSideToPlay() == Side.WHITE) ? 7 : 0;
		
		// Replace k, q, K, Q with a file name if a rook is moved to become the new outer rook on the back rank.
		if(piece.getPieceType() == PieceType.ROOK && from.getY() != backRank && to.getY() == backRank) {
			if(isOuterRook(board, to, board.getSideToPlay())) {
				if(board.getSideToPlay() == Side.WHITE) {
					if(from.getX() < kingSquare.getX() && allowed.contains('Q')) {
						next.allowed.remove('Q');
						next.allowed.add(Character.toUpperCase(getLeftmostRookFile(board, board.getSideToPlay(), backRank)));
					}
					if(from.getX() > kingSquare.getX() && allowed.contains('K')) {
						next.allowed.remove('K');
						next.allowed.add(Character.toUpperCase(getRightmostRookFile(board, board.getSideToPlay(), backRank)));
					}
				} else {
					if(from.getX() < kingSquare.getX() && allowed.contains('q')) {
						next.allowed.remove('q');
						next.allowed.add(Character.toLowerCase(getLeftmostRookFile(board, board.getSideToPlay(), backRank)));
					}
					if(from.getX() > kingSquare.getX() && allowed.contains('k')) {
						next.allowed.remove('k');
						next.allowed.add(Character.toLowerCase(getRightmostRookFile(board, board.getSideToPlay(), backRank)));
					}
				}
			}
		}
		
		// Remove castling rights on one side if a rook has moved.
		if(piece.getPieceType() == PieceType.ROOK && move.getFrom().getY() == backRank) {
			if(board.getSideToPlay() == Side.WHITE) {
				next.allowed.remove(Character.toUpperCase(piece.getSquare().getFile()));
				if(isOuterRook(board, from, board.getSideToPlay())) {
					if(from.getX() < kingSquare.getX()) next.allowed.remove('Q');
					if(from.getX() > kingSquare.getX()) next.allowed.remove('K');
				}
			} else {
				next.allowed.remove(Character.toLowerCase(piece.getSquare().getFile()));
				if(isOuterRook(board, from, board.getSideToPlay())) {
					if(from.getX() < kingSquare.getX()) next.allowed.remove('q');
					if(from.getX() > kingSquare.getX()) next.allowed.remove('k');
				}
			}
		}

		// Remove all castling rights for a side if the king has moved.
		if(from.equals(kingSquare)) {
			if(board.getSideToPlay() == Side.WHITE) {
				next.allowed.removeIf(Character::isUpperCase);
			} else {
				next.allowed.removeIf(Character::isLowerCase);
			}
		}
		
		// Replace file name with k, q, K, Q if a rook moves from the back rank leaving only one outside rook on a side.
		if(piece.getPieceType() == PieceType.ROOK && from.getY() == backRank && to.getY() != backRank) {
			String leftRooks = "";
			String rightRooks = "";
			for(int x = 0; x < 8; x++) {
				if(x != move.getFrom().getX()) {
					Piece foundPiece = board.getPiece(new Square(x, backRank));
					if(foundPiece != null && foundPiece.getPieceType() == PieceType.ROOK && foundPiece.getSide() == board.getSideToPlay()) {
						if(x < kingSquare.getX()) {
							leftRooks += new Square(x, backRank).getFile();
						} else {
							rightRooks += new Square(x, backRank).getFile();
						}
					}
				}
			}
			
			if(board.getSideToPlay() == Side.WHITE) {
				if(leftRooks.length() == 1 && next.allowed.contains(Character.toUpperCase(leftRooks.charAt(0)))) {
					next.allowed.add('Q');
					next.allowed.remove(Character.toUpperCase(leftRooks.charAt(0)));
				}
				if(rightRooks.length() == 1 && next.allowed.contains(Character.toUpperCase(rightRooks.charAt(0)))) {
					next.allowed.add('K');
					next.allowed.remove(Character.toUpperCase(rightRooks.charAt(0)));
				}
			} else {
				if(leftRooks.length() == 1 && next.allowed.contains(Character.toLowerCase(leftRooks.charAt(0)))) {
					next.allowed.add('q');
					next.allowed.remove(Character.toLowerCase(leftRooks.charAt(0)));
				}
				if(rightRooks.length() == 1 && next.allowed.contains(Character.toLowerCase(rightRooks.charAt(0)))) {
					next.allowed.add('k');
					next.allowed.remove(Character.toLowerCase(rightRooks.charAt(0)));
				}
			}
		}
		
		return next;
	}

	/**
	 * Returns the file of the leftmost rook at row y on side.
	 */
	private static char getLeftmostRookFile(Board board, Side side, int y) {
		for(int x = 0; x < 8; x++) {
			Piece piece = board.getPiece(new Square(x, y));
			if(piece != null && piece.getPieceType() == PieceType.ROOK && piece.getSide() == side) {
				return new Square(x, y).getFile();
			}
		}
		throw new RuntimeException("No rook found");
	}
	
	/**
	 * Returns the file of the rightmost rook at row y on side.
	 */
	private static char getRightmostRookFile(Board board, Side side, int y) {
		for(int x = 7; x >= 0; x--) {
			Piece piece = board.getPiece(new Square(x, y));
			if(piece != null && piece.getPieceType() == PieceType.ROOK && piece.getSide() == side) {
				return new Square(x, y).getFile();
			}
		}
		throw new RuntimeException("No rook found");
	}

	/**
	 * Returns whether square contains outer rook, with no rook between it and the king, assuming it contains a rook
	 * on the back rank, and the king is on the back rank.
	 */
	private static boolean isOuterRook(Board board, Square square, Side side) {
		boolean foundOtherLeft = false;
		boolean foundOtherRight = false;
		int y = square.getY();
		
		for(int x = square.getX() + 1; x < 8; x++) {
			Piece foundPiece = board.getPiece(new Square(x, y));
			if(foundPiece != null) {
				if(foundPiece.getSide() == side && (foundPiece.getPieceType() == PieceType.ROOK || foundPiece.getPieceType() == PieceType.KING)) {
					foundOtherRight = true;
				}
			}
		}
		
		for(int x = square.getX() - 1; x >= 0; x--) {
			Piece foundPiece = board.getPiece(new Square(x, y));
			if(foundPiece != null) {
				if(foundPiece.getSide() == side && (foundPiece.getPieceType() == PieceType.ROOK || foundPiece.getPieceType() == PieceType.KING)) {
					foundOtherLeft = true;
				}
			}
		}
		
		return !(foundOtherLeft && foundOtherRight);
	}

	private static Square getPiece(Board board, PieceType type, Side side) {
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				Square square = new Square(x, y);
				Piece piece = board.getPiece(square);
				if(piece != null && piece.getPieceType() == type && piece.getSide() == side) {
					return square;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns whether a side is allowed to make a castling move.
	 */
	public boolean allowedCastle(Board board, Side side, Move move) throws IllegalMoveException {
		int d = (move.getTo().getX() == 6) ? 1 : -1;
		
		Piece rook = null;
		for(int x = move.getFrom().getX(); x >= 0 && x <= 7; x += d) {
			Piece piece = board.getPiece(new Square(x, move.getFrom().getY()));
			if(piece != null && piece.getPieceType() == PieceType.ROOK) {
				rook = piece;
				break;
			}
		}
		
		if(rook == null) {
			return false;
		}
		
		boolean isOuterRook = isOuterRook(board, rook.getSquare(), side);
		
		if(side == Side.WHITE) {
			if(isOuterRook) {
				if(d > 0 && allowed.contains('K')) {
					return true;
				}
				if(d < 0 && allowed.contains('Q')) {
					return true;
				}
			}
			if(allowed.contains(Character.toUpperCase(rook.getSquare().getFile()))) {
				return true;
			}
		} else {
			if(isOuterRook) {
				if(d > 0 && allowed.contains('k')) {
					return true;
				}
				if(d < 0 && allowed.contains('q')) {
					return true;
				}
			}
			if(allowed.contains(Character.toUpperCase(rook.getSquare().getFile()))) {
				return true;
			}
		}
		
		return false;
	}
}

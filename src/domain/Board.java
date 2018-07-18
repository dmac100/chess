package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.GSubCallback;
import domain.pieces.*;

public class Board {
	private Piece[][] pieces = new Piece[8][8];
	private Side toPlay = Side.WHITE;
	private Castling castling = new Castling();
	private Square enPassant = null;
	private int halfMoves = 0;
	private int fullMoves = 1;
	
	/**
	 * Create a new board in the initial position.
	 */
	public Board() {
		this.pieces = parsePieces("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
	}

	/**
	 * Create a new board as a copy of another one.
	 */
	public Board(Board board) {
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				this.pieces[x][y] = board.pieces[x][y];
			}
		}
		this.toPlay = board.toPlay;
		this.castling = board.castling;
		this.enPassant = board.enPassant;
		this.halfMoves = board.halfMoves;
		this.fullMoves = board.fullMoves;
	}
	
	/**
	 * Resets counters, returning the resulting position. Counters include castling rights, enPassant, halfMoves, and fullMoves.
	 */
	public Board resetCounters() {
		Board board = new Board(this);
		board.castling = new Castling();
		board.enPassant = null;
		board.halfMoves = 0;
		board.fullMoves = 1;
		
		return board;
	}
	
	/**
	 * Sets castling based on a partial FEN string, and returns the resulting position.
	 */
	public Board setCastling(String castling) {
		Board board = new Board(this);
		board.castling = new Castling(castling);
		return board;
	}
	
	/**
	 * Makes a move on the board, returning the resulting board. Only checks for illegal moves
	 * that haven't already been checked by getPossibleMoves(), for example leaving the King in check. 
	 */
	public Board makeMove(Move move) throws IllegalMoveException {
		Board next = new Board(this);
		
		if(move.isNullMove()) {
			next.toPlay = next.toPlay.otherSide();
			if(next.toPlay == Side.WHITE) {
				next.fullMoves++;
			}
			next.enPassant = null;
			if(next.isCheck(this.toPlay)) {
				throw new IllegalMoveException("King is in check.");
			}
			return next;
		}
		
		Piece piece = getMovingPiece(move);
		Square from = move.getFrom();
		Square to = move.getTo();
		
		boolean capture = next.movePiece(from, to);
		
		// Castling.
		if(move.getCastling()) {
			performCastling(next, move);
			capture = false;
		}

		next.castling = next.castling.nextCastling(this, move);
		next.toPlay = next.toPlay.otherSide();
		next.enPassant = null;
		
		// Update half-move counter depending on pawn move or capture.
		if(capture || piece.getPieceType() == PieceType.PAWN) {
			next.halfMoves = 0;
		} else {
			next.halfMoves = halfMoves + 1;
		}
		
		// Update full move counter.
		if(next.toPlay == Side.WHITE) {
			next.fullMoves++;
		}
		
		// Update en-passant for next move.
		if(piece.getPieceType() == PieceType.PAWN) {
			if(Math.abs(from.getY() - to.getY()) == 2) {
				next.enPassant = new Square(from.getX(), (from.getY() + to.getY()) / 2);
			} else {
				next.enPassant = null;
			}
		}
		
		// Promotions.
		if(move.getPromote() != null) {
			Piece promotedPiece = null;
			if(move.getPromote() == PromotionChoice.QUEEN) promotedPiece = new Queen(move.getTo(), this.toPlay);
			if(move.getPromote() == PromotionChoice.ROOK) promotedPiece = new Rook(move.getTo(), this.toPlay);
			if(move.getPromote() == PromotionChoice.KNIGHT) promotedPiece = new Knight(move.getTo(), this.toPlay);
			if(move.getPromote() == PromotionChoice.BISHOP) promotedPiece = new Bishop(move.getTo(), this.toPlay);
			next.pieces[move.getTo().getX()][move.getTo().getY()] = promotedPiece;
		}
		
		if(next.isCheck(this.toPlay)) {
			throw new IllegalMoveException("King is in check.");
		}
		
		return next;
	}
	
	private void performCastling(Board next, Move move) throws IllegalMoveException {
		Square from = move.getFrom();
		Square to = move.getTo();
		
		int y = from.getY();
		int d = (to.getX() == 6) ? 1 : -1;
		
		// Check castling rights.
		if(!castling.allowedCastle(this, toPlay, move)) {
			throw new IllegalMoveException("Can't castle. Piece already moved.");
		}
		
		// Find rook.
		Piece rook = null;
		for(int a = from.getX(); a >= 0 && a <= 7; a += d) {
			Piece piece = getPiece(new Square(a, y));
			if(piece != null && piece.getPieceType() == PieceType.ROOK) {
				rook = piece;
				break;
			}
		}
		
		if(rook == null) {
			throw new IllegalMoveException("No rook to castle with");
		}
		
		// Check for king moving through check.
		for(int a = Math.min(to.getX(), from.getX()); a <= Math.max(to.getX(), from.getX()); a++) {
			if(this.isAttacked(new Square(a, y), toPlay.otherSide())) {
				throw new IllegalMoveException("King starts, moves or ends in check.");
			}
		}

		// Check for occupied destination square for king.
		if(!rook.getSquare().equals(to) && !from.equals(to) && getPiece(to) != null) {
			throw new IllegalMoveException("Blocked by occupied squares");
		}
		
		// Check for occupied destination square for rook.
		Square rookDestination = new Square(to.getX() - d, y);
		Piece rookDestinationPiece = getPiece(rookDestination);
		if(rookDestinationPiece != null) {
			if(!rookDestinationPiece.getSquare().equals(to) && rookDestinationPiece.getPieceType() != PieceType.KING) {
				throw new IllegalMoveException("Blocked by occupied squares");
			}
		}
		
		// Move pieces.
		Piece king = getPiece(from);
		next.pieces[king.getSquare().getX()][king.getSquare().getY()] = null;
		next.pieces[rook.getSquare().getX()][rook.getSquare().getY()] = null;
		king = king.setSquare(to);
		rook = rook.setSquare(rookDestination);
		next.pieces[king.getSquare().getX()][king.getSquare().getY()] = king;
		next.pieces[rook.getSquare().getX()][rook.getSquare().getY()] = rook;
	}

	public Board makePgnMove(String pgnMove) throws IllegalMoveException {
		return makeMove(getPgnMove(pgnMove));
	}
	
	public Move getPgnMove(String pgnMove) throws IllegalMoveException {
		if(pgnMove.matches("(?x) [oO0] - [oO0] (-[oO0])? [!?+\\#]*")) {
			// Castling moves. Move the king to the destination square.
			int fromX = -1;
			int y = (toPlay == Side.WHITE) ? 7 : 0;
			for(int a = 0; a < 8; a++) {
				Piece piece = getPiece(new Square(a, y));
				if(piece != null && piece.getPieceType() == PieceType.KING) {
					fromX = a;
				}
			}
			
			if(fromX == -1) throw new IllegalMoveException("Invaid pgn move: " + pgnMove);

			boolean kingside = !pgnMove.matches("[oO0]-[oO0]-[oO0].*");
			
			return new Move(new Square(fromX, y), new Square(kingside ? 6 : 2, y), true);
		} else {
			// Other moves.
			Pattern pattern = Pattern.compile("(?x) ^ ([RNBQK]?) ([a-h]?[1-8]?) x? ([a-h][1-8]) ((?:=[QNBRqnbr])?) ([!?+\\#]*) $");
			Matcher matcher = pattern.matcher(pgnMove);
			
			if(!matcher.find()) throw new IllegalMoveException("Invalid pgn move: " + pgnMove);
			
			String piece = matcher.group(1).toLowerCase();
			String ambiguity = matcher.group(2).toLowerCase();
			String to = matcher.group(3).toLowerCase();
			String promotion = matcher.group(4).toLowerCase();
			String annotation = matcher.group(5);
			
			if(piece.length() == 0) piece = "p";
			
			List<Move> moves = new ArrayList<Move>();
			for(Move move:getPossibleMoves()) {
				if(matchesPgnMove(move, piece, ambiguity, to, promotion, annotation)) {
					try {
						makeMove(move);
						
						moves.add(move);
					} catch(IllegalMoveException e) {
						// Don't add move.
					}
				}
			}
			
			if(moves.size() != 1) throw new IllegalMoveException("Invalid pgn move: " + pgnMove + ". " + moves.size() + " matching moves.");
			
			return moves.get(0);
		}
	}
	
	/**
	 * Returns whether a move matches the move text from a pgn file.
	 */
	private boolean matchesPgnMove(Move move, String piece, String ambiguity, String to, String promotion, String annotation) {
		Piece p = getMovingPiece(move);
		
		if(!p.getAlgebraicName().equalsIgnoreCase(piece)) return false;
		if(!move.getTo().equals(new Square(to))) return false;
		
		for(char c:ambiguity.toCharArray()) {
			if(Character.isDigit(c)) {
				int y = 8 - (c - '0');
				if(move.getFrom().getY() != y) return false;
			} else {
				int x = c - 'a';
				if(move.getFrom().getX() != x) return false;
			}
		}
		
		if(promotion.length() > 0) {
			if(move.getPromote() == null) return false;;
			if(!String.valueOf(move.getPromote().getAlgebraic()).equals(promotion.substring(1))) return false;
		} else {
			if(move.getPromote() != null) return false;
		}
		
		return true;
	}
	
	/**
	 * Return a move from this position as a pgn move (algebraic notation). 
	 */
	public String getMoveAsPgn(Move move) throws IllegalMoveException {
		if(move.isNullMove()) {
			return "-";
		}
		
		Square from = move.getFrom();
		Square to = move.getTo();
		Piece piece = getPiece(from);
		
		if(piece == null) throw new IllegalMoveException("Can't find piece.");
		
		// Get ambiguous ranks and files (check for moves with pieces of the same type, side and destination square).
		String ambiguity = "";
		if(piece.getPieceType() != PieceType.PAWN) {
			for(Move otherMove:getPossibleMoves()) {
				if(otherMove.equals(move)) continue;
				
				Square otherFrom = otherMove.getFrom();
				Square otherTo = otherMove.getTo();
				
				if(getPiece(otherFrom).getSide() == piece.getSide() && getPiece(otherFrom).getPieceType() == piece.getPieceType()) {
					if(otherTo.equals(to)) {
						if(otherFrom.getX() != from.getX()) {
							ambiguity += from.getFile();
						} else if(otherFrom.getY() != from.getY()) {
							ambiguity += from.getRank();
						}
					}
				}
			}
			
			if(ambiguity.length() > 1) {
				ambiguity = String.valueOf(from.getFile()) + String.valueOf(from.getRank());
			}
		}
		
		// Check for capture.
		String capture = "";
		if(getPiece(to) != null) {
			capture = "x";
		}
		// Check for pawn captures (including en-passant).
		if(piece.getPieceType() == PieceType.PAWN && from.getX() != to.getX()) {
			capture = "x";
		}
		
		// Get piece name, and alter if it is a pawn.
		String pieceName = piece.getAlgebraicName().toUpperCase();
		if(piece.getPieceType() == PieceType.PAWN) {
			if(capture.isEmpty()) {
				// Pawn move with no capture - don't give piece name.
				pieceName = "";
			} else {
				// Pawn move with capture - add from file name.
				pieceName = String.valueOf(from.getFile());
			}
		}
		
		// Get promotion text.
		String promotion = "";
		if(move.getPromote() != null) {
			promotion = "=" + Character.toUpperCase(move.getPromote().getAlgebraic());
		}

		// Check for check and checkmate.
		Board board = new Board(this);
		board = board.makeMove(move);
		String annotation = "";
		if(board.isCheckmate()) {
			annotation = "#";
		} else if(board.isCheck(board.toPlay)) {
			annotation = "+";
		}
		
		// Handle castling.
		if(move.getCastling()) {
			if(to.getX() == 6) {
				return "O-O" + annotation;
			} else {
				return "O-O-O" + annotation;
			}
		}
		
		// Return combined pgn text.
		return pieceName + ambiguity + capture + to + promotion + annotation;
	}
	
	/**
	 * Return the piece that a move moves.
	 */
	private Piece getMovingPiece(Move move) {
		Square from = move.getFrom();
		
		return pieces[from.getX()][from.getY()];
	}
	
	/**
	 * Moves a piece from 'from' to 'to'. Returns true if there's been a capture.
	 */
	private boolean movePiece(Square from, Square to) throws IllegalMoveException {
		Piece piece = pieces[from.getX()][from.getY()];
		if(piece == null) throw new IllegalMoveException("No piece on from square: " + from);
		piece = piece.setSquare(to);
		
		boolean capture = (pieces[to.getX()][to.getY()] != null);
		
		if(piece.getPieceType() == PieceType.PAWN) {
			// Remove piece from en-passant capture.
			if(pieces[to.getX()][to.getY()] == null) {
				pieces[to.getX()][from.getY()] = null;
			}
		}
		
		pieces[to.getX()][to.getY()] = piece;
		pieces[from.getX()][from.getY()] = null;
		
		return capture;
	}
	
	/**
	 * Creates a board from a FEN position containing: "[pieces] [toPlay] [castling] [enPassant] [halfMoves] [fullMoves]"
	 * pieces:    List of pieces as one of p,q,k,r,n,b starting from A8 with each rank separated by '/'s. Blank squares
	 *            are given by the number of blanks. White is uppercase, and black is lowercase.
	 * toPlay:    Whose turn it is to play, 'w' or 'b'.
	 * castling:  Whether castling is available and on which side. A subsequence of 'KQkr' for white/black king/queenside
	 *            castling, or '-' if neither side can castle.
	 * enPassant: The square that a pawn can move to to capture en-passant, or '-' if the last move wasn't a double pawn move.
	 * halfMoves: The number of half-moves since the last pawn move or capture, for drawing by the 50-move rule.
	 * fullMoves: The number of full moves played, starting at 1.
	 * halfMoves and fullMoves default to 0 and 1 if not specified.
	 * For example, the initial position is: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
	 */
	public Board(String fen) {
		String[] split = fen.split(" ");
		if(split.length != 4 && split.length != 6) throw new IllegalArgumentException("Invalid FEN: Expected 4 or 6 parts. Got " + split.length);
		
		this.pieces = parsePieces(split[0]);
		
		this.toPlay = split[1].equalsIgnoreCase("w") ? Side.WHITE : Side.BLACK;
		
		this.castling = new Castling(split[2]);
		
		this.enPassant = (split[3].equals("-")) ? null : new Square(split[3]);
		
		if(split.length > 4) {
			try {
				this.halfMoves = Integer.parseInt(split[4]);
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("Invalid FEN: halfMoves is not an integer: " + halfMoves);
			}
			
			try {
				this.fullMoves = Integer.parseInt(split[5]);
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("Invalid FEN: fullMoves is not an integer: " + halfMoves);
			}
		} else {
			// Set default values for last two missing parameters.
			this.halfMoves = 0;
			this.fullMoves = 1;
		}
		
		if(!validPosition()) {
			throw new IllegalArgumentException("Invalid FEN: invalid position");
		}
	}

	private Piece[][] parsePieces(String piecesFen) {
		Piece[][] pieces = new Piece[8][8];
		
		String[] ranks = piecesFen.split("/");
		if(ranks.length != 8) throw new IllegalArgumentException("Invalid FEN: Expected 8 ranks. Got " + ranks.length);
		
		for(int y = 0; y < 8; y++) {
			String rank = ranks[y];
			int x = 0;
			int i = 0;
			while(x < 8) {
				if(i >= rank.length()) throw new IllegalArgumentException("Invalid FEN: Invalid column count for rank: " + rank);
				char c = rank.charAt(i);
				if(Character.isDigit(c)) {
					x += c - '0';
				} else {
					Side side = (Character.isLowerCase(c) ? Side.BLACK : Side.WHITE);
					
					if(Character.toLowerCase(c) == 'p') pieces[x][y] = new Pawn(new Square(x, y), side);
					else if(Character.toLowerCase(c) == 'r') pieces[x][y] = new Rook(new Square(x, y), side);
					else if(Character.toLowerCase(c) == 'n') pieces[x][y] = new Knight(new Square(x, y), side);
					else if(Character.toLowerCase(c) == 'b') pieces[x][y] = new Bishop(new Square(x, y), side);
					else if(Character.toLowerCase(c) == 'q') pieces[x][y] = new Queen(new Square(x, y), side);
					else if(Character.toLowerCase(c) == 'k') pieces[x][y] = new King(new Square(x, y), side);
					else throw new IllegalArgumentException("Invalid FEN: Character is not a piece: " + c);
					
					
					x += 1;
				}
				i += 1;
			}
		}
		
		return pieces;
	}

	/**
	 * Returns the FEN string for the current position.
	 */
	public String getFen() {
		String pieces = piecesToString(this.pieces, true);
		
		String toPlay = this.toPlay.toString();
		
		String castling = this.castling.toString();
		
		String enPassant = (this.enPassant == null) ? "-" : this.enPassant.toString();
		
		String halfMoves = String.valueOf(this.halfMoves);
		
		String fullMoves = String.valueOf(this.fullMoves);
		
		return pieces + " " + toPlay + " " + castling + " " + enPassant + " " + halfMoves + " " + fullMoves;
	}
	
	public int getFullMoves() {
		return fullMoves;
	}
	
	/**
	 * Returns a string uniquely identifying a position, without the move count. This runs more quickly than
	 * getFen().
	 */
	public String getPositionDatabaseString() {
		String pieces = piecesToString(this.pieces, false);
		
		String toPlay = this.toPlay.toString();
		
		String castling = this.castling.toString();
		
		String enPassant = (this.enPassant == null) ? "-" : this.enPassant.toString();
		
		return pieces + " " + toPlay + " " + castling + " " + enPassant + " " + halfMoves + " " + fullMoves;
	}

	/**
	 * Return pieces part of an FEN string. If compressed is true, replaces consecutive blank squares with numbers,
	 * otherwise uses '1' for every blank square.
	 */
	private String piecesToString(Piece[][] pieces, boolean compressed) {
		StringBuilder rows = new StringBuilder();
		
		for(int y = 0; y < 8; y++) {
			if(y != 0) rows.append("/");
			
			StringBuilder row = new StringBuilder();
			
			for(int x = 0; x < 8; x++) {
				Piece piece = pieces[x][y];
				if(piece == null) {
					row.append("1");
				} else {
					row.append(piece.getAlgebraicName());
				}
			}
			
			if(compressed) {
				String compressedRow = GSubCallback.replaceAll(row.toString(), "\\d+", new GSubCallback.Callback() {
					public String call(Matcher m) {
						return String.valueOf(m.group().length());
					}
				});
				
				rows.append(compressedRow);
			} else {
				rows.append(row.toString());
			}
		}
		
		return rows.toString();
	}

	/**
	 * Returns whether a move is possible from this position. Doesn't check for checks or
	 * draws. These are checked by the makeMove method.
	 */
	public boolean isPossibleMove(Move move) {
		if(move.isNullMove()) return true;
		
		for(Move m:getPossibleMoves()) {
			if(m.equals(move)) return true;
		}
		return false;
	}
	
	/**
	 * Returns all possible moves from this position. Doesn't check for checks or draws.
	 * These are checked by the makeMove method.
	 */
	public List<Move> getPossibleMoves() {
		List<Move> moves = new ArrayList<Move>();
		
		// Other moves.
		for(Piece piece:getActivePieces(toPlay)) {
			moves.addAll(piece.getPossibleMoves(this));
		}
		
		return moves;
	}
	
	private List<Piece> getActivePieces(Side side) {
		List<Piece> list = new ArrayList<Piece>();
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				Piece p = pieces[x][y];
				if(p != null) {
					if(side == null || p.getSide() == side) {
						list.add(p);
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * Returns the piece on a square, or null if it is empty.
	 */
	public Piece getPiece(Square square) {
		int x = square.getX();
		int y = square.getY();
		
		return pieces[x][y];
	}
	
	/**
	 * Returns the square that can be the target square for an en-passant capture, or null.
	 */
	public Square getEnPassant() {
		return enPassant;
	}

	/**
	 * Print the board to System.out using algebraic piece characters.
	 */
	public void printBoard() {
		System.out.println("FEN: " + getFen());
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				Piece p = pieces[x][y];
				if(p == null) {
					System.out.print(".");
				} else {
					if(p.getSide() == Side.WHITE) {
						System.out.print(p.getAlgebraicName().toUpperCase());
					} else {
						System.out.print(p.getAlgebraicName());
					}
				}
			}
			System.out.println();
		}
		System.out.println();
	}
	
	@Override
	public String toString() {
		return getFen();
	}

	/**
	 * Returns whether a side is in check.
	 */
	public boolean isCheck(Side side) {
		Piece king = getKing(side);
		
		if(king == null) {
			return false;
		}
		
		return (isAttacked(king.getSquare(), side.otherSide()));
	}

	/**
	 * Return whether the side to move has been checkmated.
	 */
	public boolean isCheckmate() {
		Piece king = getKing(toPlay);
		
		if(king == null) {
			return false;
		}
		
		if(!isAttacked(king.getSquare(), toPlay.otherSide())) {
			return false;
		}
		
		for(Move move:getPossibleMoves()) {
			Board next = new Board(this);
			try {
				next = next.makeMove(move);
				king = next.getKing(toPlay);
				if(!next.isAttacked(king.getSquare(), toPlay.otherSide())) {
					return false;
				}
			} catch (IllegalMoveException e) {
				// Try another move.
			}
		}
		
		return true;
	}
	
	/**
	 * Return whether a square is being attacked by a side.
	 */
	private boolean isAttacked(Square square, Side side) {
		for(Piece piece:getActivePieces(side)) {
			for(Move move:piece.getPossibleMoves(this)) {
				// Skip pawn moves that are not captures.
				if(piece.getPieceType() == PieceType.PAWN) {
					if(move.getFrom().getFile() == (move.getTo().getFile())) {
						continue;
					}
				}
				
				// Skip castling moves.
				if(piece.getPieceType() == PieceType.KING) {
					if(Math.abs(move.getFrom().getX() - move.getTo().getX()) == 2) {
						continue;
					}
				}
				
				if(move.getTo().equals(square)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the king for a side.
	 */
	private Piece getKing(Side side) {
		for(Piece piece:getActivePieces(side)) {
			if(piece.getPieceType() == PieceType.KING) {
				return piece;
			}
		}
		return null;
	}

	/**
	 * Returns the side to play in the current position. Either Side.WHITE, or Side.BLACK.
	 */
	public Side getSideToPlay() {
		return toPlay;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || other.getClass() != Board.class) return false;
		Board otherBoard = (Board)other;
		
		return otherBoard.getFen().equals(getFen());
	}

	public Board clearPiece(Square square) {
		Board board = new Board(this);
		
		board.pieces[square.getX()][square.getY()] = null;
		
		return board;
	}

	public Board placePiece(Square square, PieceType type, Side side) {
		Board board = new Board(this);
		
		Piece piece = null;
		if(type == PieceType.PAWN) piece = new Pawn(square, side);
		if(type == PieceType.KNIGHT) piece = new Knight(square, side);
		if(type == PieceType.ROOK) piece = new Rook(square, side);
		if(type == PieceType.BISHOP) piece = new Bishop(square, side);
		if(type == PieceType.QUEEN) piece = new Queen(square, side);
		if(type == PieceType.KING) piece = new King(square, side);
		
		if(piece == null) {
			throw new IllegalArgumentException("Invalid piece type");
		}
		
		board.pieces[square.getX()][square.getY()] = piece;
		
		return board;
	}
	
	public Board setSideToPlay(Side side) {
		Board board = new Board(this);
		
		board.toPlay = side;
		
		return board;
	}
	
	/**
	 * Returns whether the current position is valid (could be reached from the standard starting position in a game).
	 */
	public boolean validPosition() {
		// Side not to move can't be in check.
		if(isCheck(toPlay.otherSide())) return false;
		
		for(Side side:Side.values()) {
			List<Piece> pieces = getActivePieces(side);
			
			// No more than 16 pieces.
			if(pieces.size() > 16) return false;
			
			for(PieceType type:PieceType.values()) {
				int count = 0;
				for(Piece piece:pieces) {
					if(piece.getPieceType() == type) {
						count++;
					}
				}
				
				// No more than 10 Rooks, Knights, Bishops, or Queens.
				if(count > 10) return false;
				
				// No more than 8 pawns.
				if(type == PieceType.PAWN && count > 8) return false;
				
				// Exactly 1 King.
				if(type == PieceType.KING && count != 1) return false;
			}
			
			for(Piece piece:pieces) {
				if(piece.getPieceType() == PieceType.PAWN) {
					// No pawns on 1st or 8th rank.
					int rank = piece.getSquare().getY();
					if(rank == 0 || rank == 7) return false;
				}
			}
		}
		
		return true;
	}
}

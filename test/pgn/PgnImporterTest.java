package pgn;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import util.FileUtil;
import domain.Move;
import domain.MoveHistoryNode;

public class PgnImporterTest {
	@Test
	public void importPgn() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "[Result \"*\"]\n\n";
		pgn += "1.e4 e6 ( 1...e5 2.d4 exd4 { A comment } )";
		
		List<Move> moves = importer.importPgn(pgn).getMainLine();
		
		assertTrue(moves.size() > 0);
	}
	
	@Test
	public void importPgn_annotation() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "1.e4!";
		
		MoveHistoryNode moves = importer.importPgn(pgn).getMoves();
		
		String annotation = moves.getNextNodes().get(0).getAnnotation();

		assertEquals("!", annotation);
	}
	
	@Test
	public void importPgn_nag() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "1.e4 $1";
		
		MoveHistoryNode moves = importer.importPgn(pgn).getMoves();
		
		String annotation = moves.getNextNodes().get(0).getAnnotation();

		assertEquals("!", annotation);
	}
	
	@Test
	public void importPgn_variation() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "[Result \"*\"]\n\n";
		pgn += "1.e4 e6 ( 1...e5 2.d4 exd4 { A comment } )";
		
		MoveHistoryNode moves = importer.importPgn(pgn).getMoves();
		MoveHistoryNode move1 = moves.getNextNodes().get(0);
		
		assertEquals(2, move1.getNextNodes().size());
		assertEquals("e6", move1.getNextNodes().get(0).getMove().getTo().toString());
		assertEquals("e5", move1.getNextNodes().get(1).getMove().getTo().toString());
	}
	
	@Test
	public void importPgn_multipleVariation() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "[Result \"*\"]\n\n";
		pgn += "1.e4 e6 ( 1...e5 2.d4 ) ( 1... c5 )";
		
		MoveHistoryNode moves = importer.importPgn(pgn).getMoves();
		MoveHistoryNode move1 = moves.getNextNodes().get(0);
		
		assertEquals(3, move1.getNextNodes().size());
		assertEquals("e6", move1.getNextNodes().get(0).getMove().getTo().toString());
		assertEquals("e5", move1.getNextNodes().get(1).getMove().getTo().toString());
		assertEquals("c5", move1.getNextNodes().get(2).getMove().getTo().toString());
	}

	@Test
	public void importPgn_nestedVariation() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "[Result \"*\"]\n\n";
		pgn += "1.e4 e6 ( 1...e5 2.d4 ( 2.d3 ) exd4 { A comment } )";
		
		MoveHistoryNode moves = importer.importPgn(pgn).getMoves();
		
		MoveHistoryNode node = moves.getNextNodes().get(0).getNextNodes().get(1).getNextNodes().get(1);
		assertEquals("d3", node.getMove().getTo().toString());
	}
	
	@Test
	public void importPgn_comment() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "[Result \"*\"]\n\n";
		pgn += "1.e4 e6 { A comment }";
		
		MoveHistoryNode moves = importer.importPgn(pgn).getMoves();
		
		MoveHistoryNode moveNode = moves.getNextNodes().get(0).getNextNodes().get(0);
		
		assertEquals("A comment", moveNode.getComment());
	}
	
	@Test
	public void importPgn_draw() throws ParseException {
		PgnImporter importer = new PgnImporter();
		
		String pgn = "[Result \"*\"]\n\n";
		pgn += "1. e4 e5 2. Nf3 Nf6 1/2-1/2";
		
		List<Move> moves = importer.importPgn(pgn).getMainLine();
		
		assertEquals(4, moves.size());
	}
	
	@Test
	public void importCollection() throws Exception {
		String game = FileUtil.readResource("/resource/games.pgn");
		
		PgnImporter importer = new PgnImporter();
		
		List<PgnGame> games = importer.importCollection(game);
		
		assertEquals(50, games.size());
	}
	
	@Test
	public void initialPosition() throws ParseException {
		String fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1";
		
		String pgn = "[FEN \"" + fen + "\"]\n";
		pgn += "[SetUp \"1\"]\n\n";
		pgn += "1. d4 exd4\n";
		
		PgnImporter importer = new PgnImporter();
		
		PgnGame game = importer.importPgn(pgn);
		
		assertEquals(fen, game.getInitialPosition().getFen());
	}
	
	@Test
	public void initialPosition_caseInsensitive() throws ParseException {
		String fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1";
		
		String pgn = "[FEN \"" + fen + "\"]\n";
		pgn += "[setup \"1\"]\n\n";
		pgn += "1. d4 exd4\n";
		
		PgnImporter importer = new PgnImporter();
		
		PgnGame game = importer.importPgn(pgn);
		
		assertEquals(fen, game.getInitialPosition().getFen());
	}

	@Test
	public void initialPosition_missingSetUp() throws ParseException {
		String fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1";
		
		String pgn = "[FEN \"" + fen + "\"]\n\n";
		pgn += "1. d4 exd4\n";
		
		PgnImporter importer = new PgnImporter();
		
		PgnGame game = importer.importPgn(pgn);
		
		assertEquals(fen, game.getInitialPosition().getFen());
	}
}
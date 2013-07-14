import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import pgn.*;
import util.FileUtil;
import domain.Board;


public class PgnFilter {
	public static void main(String[] args) throws Exception {
		String filename = "/home/david/incoming/all.pgn";
		
		List<PgnGame> games = new PgnImporter().importCollection(FileUtil.readFile(filename));
		
		PrintWriter writerWhite = null;
		PrintWriter writerBlack = null;
		
		try {
			writerWhite = new PrintWriter(new FileWriter("/home/david/output-white.pgn"));
			writerBlack = new PrintWriter(new FileWriter("/home/david/output-black.pgn"));
			
			for(PgnGame game:games) {
				String black = game.getBlack();
				String white = game.getWhite();
				String result = game.getResult().toString();
				
				PrintWriter writer = (white.equals("dmac100") ? writerWhite : writerBlack);
				
				String pgn = new PgnExporter().exportPgn(new Board(), game.getMoves());
				
				writer.println("[White \""+white+"\"]");
				writer.println("[Black \""+black+"\"]");
				writer.println("[Result \""+result+"\"]");
				writer.println();
				writer.println(pgn + " " + result);
				writer.println();
				writer.println();
			}
		} finally {
			FileUtil.close(writerWhite);
			FileUtil.close(writerBlack);
		}
	}
}

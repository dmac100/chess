package domain;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class EcoClassifierTest {
	@Test
	public void unknownOpening() {
		EcoClassifier ecoClassifier = new EcoClassifier();
		
		assertEquals("Unknown opening", ecoClassifier.classify(new ArrayList<>()));
	}
	
	@Test
	public void knownOpening() throws IllegalMoveException {
		EcoClassifier ecoClassifier = new EcoClassifier();
		
		MoveHistory history = new MoveHistory();
		for(String pgnMove:Arrays.asList("e4", "e5", "Nf3", "Nc6", "Bb5")) {
			history.makeMove(history.getCurrentPosition().getPgnMove(pgnMove));
		}
		
		assertEquals("C60 Ruy Lopez (Spanish Opening)", ecoClassifier.classify(history.getMoves()));
	}
}

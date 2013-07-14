package pgn;

import org.parboiled.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import domain.*;

class TreeNode {
	private String pgnMove;
	private String comment;
	private String annotation;
	private TreeNode mainVariation;
	private TreeNode nextSibling;
	
	public TreeNode(String pgnMove) {
		this.pgnMove = pgnMove;
	}
	
	public void printTree() {
		printTree(0);
	}
	
	public void printTree(int indent) {
		for(int x = 0; x < indent; x++) {
			System.out.print(" ");
		}
		System.out.println(pgnMove + (comment == null ? "" : " " + comment));
		if(mainVariation != null) {
			mainVariation.printTree(indent+1);
		}
		if(nextSibling != null) {
			nextSibling.printTree(indent);
		}
	}
	
	public MutableMoveHistoryNode toMoveTreeNode(Board board, MutableMoveHistoryNode parent) throws IllegalMoveException {
		Move move = board.getPgnMove(pgnMove);
		Board nextBoard = board.makeMove(move);
		
		MutableMoveHistoryNode node = new MutableMoveHistoryNode(move, parent);
		node.setComment(comment);
		node.setAnnotation(annotation);
		
		if(mainVariation != null) {
			for(TreeNode current = mainVariation; current != null; current = current.nextSibling) {
				node.addNextNode(current.toMoveTreeNode(nextBoard, node));
			}
		}
		
		return node;
	}

	public TreeNode addChild(TreeNode child) {
		if(mainVariation == null) {
			mainVariation = child;
		} else {
			mainVariation.addSibling(child);
		}
		return this;
	}
	
	public TreeNode addSibling(TreeNode sibling) {
		TreeNode node = this;
		while(node.nextSibling != null) {
			node = node.nextSibling;
		}
		node.nextSibling = sibling;
		return this;
	}

	public String getComment() {
		return comment;
	}
	
	public TreeNode setComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	public String getAnnotation() {
		return annotation;
	}
	
	public TreeNode setAnnotation(String annotation) {
		this.annotation = annotation;
		return this;
	}
	
	public TreeNode setNag(String nag) {
		if(nag.equals("$1")) annotation = "!";
		if(nag.equals("$2")) annotation = "?";
		if(nag.equals("$3")) annotation = "!!";
		if(nag.equals("$4")) annotation = "??";
		if(nag.equals("$5")) annotation = "!?";
		if(nag.equals("$6")) annotation = "?!";
		if(nag.equals("$10")) annotation = " (=)";
		if(nag.equals("$18")) annotation = " (+-)";
		if(nag.equals("$19")) annotation = " (-+)";
		return this;
	}
}

@BuildParseTree
class PgnParser extends BaseParser<TreeNode> {
	Rule MoveText() {
		return Sequence(
			WhiteSpace(),
			Line(),
			Optional(FirstOf("0-1", "1-0", "1/2-1/2", "*")),
			WhiteSpace(),
			EOI
		);
	}
	
	Rule MoveAndVariations() {
		return Sequence(
			Move(),
			Optional(Variations(), push(pop(1).addSibling(pop())))
		);
	}
	
	Rule Variations() {
		return Sequence(
			"(",
			WhiteSpace(),
			Line(),
			")",
			WhiteSpace(),
			Optional(Variations(), push(pop(1).addSibling(pop())))
		);
	}
	
	Rule Line() {
		return Sequence(
			MoveAndVariations(),
			Optional(Line(), push(pop(1).addChild(pop())))
		);
	}
	
	Rule Move() {
		return Sequence(
			Optional(Sequence(OneOrMore(AnyOf("1234567890")), OneOrMore("."))),
			WhiteSpace(),
			AlgebraicMove(),
			push(new TreeNode(match())),
			ZeroOrMore(AnyOf("+#")),
			ZeroOrMore(AnyOf("!?"), push(pop().setAnnotation(match()))),
			WhiteSpace(),
			Optional(Sequence("$", OneOrMore(AnyOf("1234567890"))), push(pop().setNag(match()))),
			WhiteSpace(),
			Optional(Comment(), push(pop(1).setComment(pop().getComment())))
		);
	}
	
	Rule AlgebraicMove() {
		return Sequence(
			TestNot(FirstOf("1-0", "0-1", "1/2-1/2")),
			FirstOf(
				OneOrMore(AnyOf("KQBNRabcdefgh12345678xabcdefgh12345678=")),
				"O-O-O",
				"O-O"
			)
		);
	}
	
	Rule Comment() {
		return Sequence(
			"{",
			OneOrMore(NoneOf("}")),
			push(new TreeNode(null).setComment(match().trim())),
			"}",
			WhiteSpace()
		);
	}

	Rule WhiteSpace() {
		return ZeroOrMore(AnyOf(" \t\f\r\n"));
	}
}

public class MoveTextParser {
	public MoveHistoryNode parseMoveText(Board initialPosition, String moveText) throws ParseException {
		PgnParser parser = Parboiled.createParser(PgnParser.class);
		
		ParseRunner<TreeNode> parseRunner = new ReportingParseRunner<TreeNode>(parser.MoveText());
		
		ParsingResult<TreeNode> result = parseRunner.run(moveText);
		
		//System.out.println(moveText);
		
		if(!result.matched) {
			for(ParseError error:result.parseErrors) {
				throw new ParseException(
					"ERROR: Parsed up to: "
					+ moveText.substring(0, error.getStartIndex())
					+ " Remaining: "
					+ moveText.substring(error.getStartIndex())
				);
			}
		}
		
		TreeNode treeNode = result.valueStack.pop();
		//treeNode.printTree();
		
		try {
			MutableMoveHistoryNode root = new MutableMoveHistoryNode(null, null);
			MutableMoveHistoryNode child = treeNode.toMoveTreeNode(initialPosition, root);
			root.addNextNode(child);
			return root;
		} catch(IllegalMoveException e) {
			throw new ParseException(e);
		}
	}
}

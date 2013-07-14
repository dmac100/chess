package pgn;

import org.parboiled.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

class TreeNode {
	private String move;
	private String comment;
	private TreeNode mainVariation;
	private TreeNode nextSibling;
	
	public TreeNode(String move) {
		this.move = move;
	}
	
	public void printTree() {
		printTree(0);
	}
	
	public void printTree(int indent) {
		for(int x = 0; x < indent; x++) {
			System.out.print(" ");
		}
		String commentText = "";
		if(comment != null) {
			commentText = " " + comment;
		}
		System.out.println(move + commentText);
		if(mainVariation != null) {
			mainVariation.printTree(indent+1);
		}
		if(nextSibling != null) {
			nextSibling.printTree(indent);
		}
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
}

@BuildParseTree
class PgnParser extends BaseParser<TreeNode> {
	Rule MoveText() {
		return Sequence(
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
			ZeroOrMore(AnyOf("1234567890.")),
			WhiteSpace(),
			OneOrMore(AnyOf("abcdefghABCDEFG12345678=KQBNRx")),
			push(new TreeNode(match())),
			ZeroOrMore(AnyOf("!?+#")),
			WhiteSpace(),
			Optional(ZeroOrMore(AnyOf("$1234567890"))),
			WhiteSpace(),
			Optional(Comment(), push(pop(2).setComment(pop().getComment())))
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

public class Test {
	public static void main(String[] args) {
		PgnParser parser = Parboiled.createParser(PgnParser.class);
		
		//ParseRunner<TreeNode> parseRunner = new RecoveringParseRunner<TreeNode>(parser.MoveText());
		ParseRunner<TreeNode> parseRunner = new ReportingParseRunner<TreeNode>(parser.MoveText());
		
		String parseText = "1. a1 a2 $13 (1... b2 b3) (2. c2 c3 { Comment (with (parens) } (d3 d4)) a3 a4! { comment } 0-1";
		ParsingResult<TreeNode> result = parseRunner.run(parseText);
		
		System.out.println("Matched: " + result.matched);
		
		if(!result.matched) {
			for(ParseError error:result.parseErrors) {
				System.err.println("ERROR: Parsed up to: " + parseText.substring(0, error.getStartIndex()));
			}
		}
		
		while(!result.valueStack.isEmpty()) {
			System.out.println("VALUE:");
			result.valueStack.pop().printTree();
		}
		
		ParseTreeUtils.printNodeTree(result);
	}
}

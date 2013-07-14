package domain;

import java.util.ArrayList;
import java.util.List;

public class MutableMoveHistoryNode implements MoveHistoryNode {
	private MutableMoveHistoryNode parent;
	private List<MutableMoveHistoryNode> nextNodes = new ArrayList<MutableMoveHistoryNode>();
	private String comment;
	private String annotation;
	private Move move;
	
	public MutableMoveHistoryNode(MoveHistoryNode node) {
		this(node, null);
	}
	
	private MutableMoveHistoryNode(MoveHistoryNode node, MutableMoveHistoryNode parent) {
		this.comment = node.getComment();
		this.annotation = node.getAnnotation();
		this.move = node.getMove();
		this.parent = parent;
		for(MoveHistoryNode next:node.getNextNodes()) {
			nextNodes.add(new MutableMoveHistoryNode(next, this));
		}
	}
	
	public MutableMoveHistoryNode(Move move, MutableMoveHistoryNode parent) {
		this.move = move;
		this.parent = parent;
	}
	
	public MutableMoveHistoryNode addNextNode(MutableMoveHistoryNode child) {
		nextNodes.add(child);
		return child;
	}
	
	public void clearNextNodes() {
		nextNodes.clear();
	}

	@Override
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public Move getMove() {
		return move;
	}
	
	public void setMove(Move move) {
		this.move = move;
	}

	@Override
	public List<MutableMoveHistoryNode> getNextNodes() {
		return nextNodes;
	}
	
	@Override
	public String getAnnotation() {
		return annotation;
	}
	
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	@Override
	public MutableMoveHistoryNode getParent() {
		return parent;
	}
	
	public void setParent(MutableMoveHistoryNode parent) {
		this.parent = parent;
	}
}
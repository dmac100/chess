package domain;

import java.util.List;

public interface MoveHistoryNode {
	Move getMove();
	String getAnnotation();
	String getComment();
	MoveHistoryNode getParent();
	List<? extends MoveHistoryNode> getNextNodes();
}
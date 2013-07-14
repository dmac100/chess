package domain;

import java.util.List;

public interface MoveTreeNode {
	Move getMove();
	List<MoveTreeNode> getNextNodes();
	String getComment();
}
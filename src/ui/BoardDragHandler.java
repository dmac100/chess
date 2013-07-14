package ui;

import domain.PromotionChoice;
import domain.Square;

public interface BoardDragHandler {
	void onDrag(Square start, Square end, PromotionChoice promote);
}

package util;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class SwtUtil {
	/**
	 * Adds resize event handlers to the table that keep all columns at an equal width
	 * spread over the width of the table.
	 */
	public static void keepEqualWidthColumns(final Table table) {
		table.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent event) {
				int totalWidth = table.getClientArea().width;
				int columnWidth = totalWidth / table.getColumnCount();
				
				for(TableColumn column:table.getColumns()) {
					column.setWidth(columnWidth);
				}
			}
			
			public void controlMoved(ControlEvent event) {
			}
		});
	}
}

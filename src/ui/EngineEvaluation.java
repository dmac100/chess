package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class EngineEvaluation extends Composite {
	private double score = 0;

	public EngineEvaluation(Composite parent) {
		super(parent, SWT.BORDER);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				int width = getBounds().width;
				int height = getBounds().height;
				GC gc = event.gc;
				
				double fraction = (score + 10) / 20.0;
				fraction = Math.max(fraction, 0);
				fraction = Math.min(fraction, 1);
				
				gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				gc.fillRectangle(0, 0, (int)(width * fraction), height);
				
				gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				gc.fillRectangle((int)(width * fraction), 0, width - (int)(width * fraction), height);
				
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
				gc.drawLine(width/2, 0, width/2, height);
			}
		});
	}
	
	public void setScore(double score) {
		this.score = score;
		redraw();
	}
}
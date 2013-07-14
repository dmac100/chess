package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Test {
	private Shell shell;
	
	public Test(final Shell shell) {
		this.shell = shell;
		
		shell.setLayout(new GridLayout(1, false));
		
		Composite top = new Composite(shell, SWT.BORDER);
		Composite bottom = new Composite(shell, SWT.BORDER);
		
		bottom.setLayout(new FillLayout());
		
		fillGridItem(bottom, true, true, 0);
		
		createToolBar(top);
	}
	
	private void createToolBar(Composite parent) {
		parent.setLayout(new FillLayout());
		
		Button button1 = new Button(parent, SWT.BORDER);
		button1.setText("Button1");
		button1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				displayMessage("Button1");
			}
		});
		
		Button button2 = new Button(parent, SWT.BORDER);
		button2.setText("Button2");
		button2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				displayMessage("Button2");
			}
		});
		
		fillGridItem(parent, true, false, 0);
	}
	
	private void displayMessage(String message) {
		MessageBox alert = new MessageBox(shell, SWT.NONE);
		alert.setText("Message");
		alert.setMessage(message);
		alert.open();
	}
	
	public static Control fillGridItem(Control widget, boolean horizontal, boolean vertical, int widthHint) {
		GridData gridData = new GridData();
		if(horizontal) {
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
		}
		if(vertical) {
			gridData.verticalAlignment = SWT.FILL;
			gridData.grabExcessVerticalSpace = true;
		}
		if(widthHint > 0) {
			gridData.widthHint = widthHint;
		}
		widget.setLayoutData(gridData);
		return widget;
	}

	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);

		Test test = new Test(shell);

		shell.setText("Hello");
		shell.setSize(400, 400);
		shell.open();

		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}
}

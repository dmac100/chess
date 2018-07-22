package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import domain.PromotionChoice;

/**
 * A dialog with a label and text field with OK/Cancel buttons.
 */
class PromotionDialog extends Dialog {
	private String result;

	public PromotionDialog(Shell parent) {
		super(parent, 0);
	}
	
	public PromotionChoice open() {
		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET);
		shell.setText("Promote");
		shell.setLayout(new GridLayout());
		
		// Form Controls

		Composite formComposite = new Composite(shell, SWT.NONE);
		formComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout formLayout = new GridLayout(2, false);
		formComposite.setLayout(formLayout);
		
		Label label = new Label(formComposite, SWT.NONE);
		label.setText("Promote to:");
		
		// Button Composite
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout buttonLayout = new GridLayout(4, true);
		buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);
		
		for(final String piece:new String[] { "Knight", "Bishop", "Rook", "Queen" }) {
			Button button = new Button(buttonComposite, SWT.NONE);
			button.setText(piece);
			GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
			gridData.heightHint = 64;
			button.setLayoutData(gridData);
			
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					result = piece;
					shell.dispose();
				}
			});
		}
		
		// Open and wait for result.
		shell.setSize(300, 140);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		if(result == null) return null;
		if(result.equals("Knight")) return PromotionChoice.KNIGHT;
		if(result.equals("Bishop")) return PromotionChoice.BISHOP;
		if(result.equals("Rook")) return PromotionChoice.ROOK;
		if(result.equals("Queen")) return PromotionChoice.QUEEN;
		
		throw new AssertionError("Invalid promotion result");
	}
}
package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A dialog with a label and text field with OK/Cancel buttons.
 */
class PgnDialog extends Dialog {
	private String result;
	private String labelText;
	private boolean edit;
	private String pgn;

	public PgnDialog(Shell parent, String labelText, boolean edit) {
		super(parent, SWT.NONE);
		this.labelText = labelText;
		this.edit = edit;
	}
	
	public void setPgn(String pgn) {
		this.pgn = pgn;
	}
	
	public String open() {
		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setText(getText());
		shell.setLayout(new GridLayout());
		
		// Form Controls

		Composite formComposite = new Composite(shell, SWT.NONE);
		formComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout formLayout = new GridLayout(2, false);
		formComposite.setLayout(formLayout);
		
		Label label = new Label(formComposite, SWT.NONE);
		label.setText(labelText + ":");
		
		final Text pgnText = new Text(formComposite, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		pgnText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if(pgn != null) {
			pgnText.setText(pgn);
		}
		pgnText.setEditable(edit);

		// Button Composite
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
		
		GridLayout buttonLayout = new GridLayout(edit ? 2 : 1, true);
		buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);
		
		Button okButton = new Button(buttonComposite, SWT.NONE);
		okButton.setText("OK");
		okButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		
		if(edit) {
			Button cancelButton = new Button(buttonComposite, SWT.NONE);
			cancelButton.setText("Cancel");
			cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
			
			cancelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					shell.dispose();
				}
			});
		}

		shell.setDefaultButton(okButton);
		
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				result = pgnText.getText();
				shell.dispose();
			}
		});
		
		// Open and wait for result.
		shell.setSize(400, 300);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
}
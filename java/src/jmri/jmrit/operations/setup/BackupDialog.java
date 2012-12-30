package jmri.jmrit.operations.setup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import java.awt.Insets;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jmri.jmrit.operations.ExceptionContext;
import jmri.jmrit.operations.ExceptionDisplayFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.UnexpectedExceptionContext;

public class BackupDialog extends JDialog {

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(BackupDialog.class.getName());

	private final JPanel contentPanel = new JPanel();
	private JLabel captionLabel;
	private JTextField setNameTextField;
	private JLabel infoLabel1;
	private JLabel infoLabel2;
	private JButton backupButton;
//	private JButton helpButton;

	private DefaultBackup backup;

	/**
	 * Create the dialog.
	 */
	public BackupDialog() {
		backup = new DefaultBackup();

		initComponents();
	}

	private void initComponents() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setModal(true);
		setTitle(Bundle.getString("BackupDialog.this.title"));
		setBounds(100, 100, 395, 199);
		getContentPane().setLayout(new BorderLayout());
		{
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			GridBagLayout gbl = new GridBagLayout();
			gbl.columnWidths = new int[] { 0, 0 };
			gbl.rowHeights = new int[] { 0, 0, 0, 0, 0 };
			gbl.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
			gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
					Double.MIN_VALUE };
			contentPanel.setLayout(gbl);
			getContentPane().add(contentPanel, BorderLayout.CENTER);
			{
				captionLabel = new JLabel(
						Bundle.getString("BackupDialog.nameLabel.text"));
				GridBagConstraints gbc_captionLabel = new GridBagConstraints();
				gbc_captionLabel.anchor = GridBagConstraints.WEST;
				gbc_captionLabel.insets = new Insets(0, 0, 5, 0);
				gbc_captionLabel.gridx = 0;
				gbc_captionLabel.gridy = 0;
				contentPanel.add(captionLabel, gbc_captionLabel);
			}
			{
				setNameTextField = new JTextField();
				setNameTextField.setText(backup.suggestBackupSetName());

				setNameTextField.getDocument().addDocumentListener(
						new DocumentListener() {

							// These should probably pass the document to
							// enableBackupButton....
							@Override
							public void removeUpdate(DocumentEvent arg0) {
								enableBackupButton();
							}

							@Override
							public void insertUpdate(DocumentEvent arg0) {
								enableBackupButton();
							}

							@Override
							public void changedUpdate(DocumentEvent arg0) {
								enableBackupButton();
							}
						});

				GridBagConstraints gbc_setNameTextField = new GridBagConstraints();
				gbc_setNameTextField.insets = new Insets(0, 0, 5, 0);
				gbc_setNameTextField.fill = GridBagConstraints.HORIZONTAL;
				gbc_setNameTextField.gridx = 0;
				gbc_setNameTextField.gridy = 1;
				contentPanel.add(setNameTextField, gbc_setNameTextField);
				setNameTextField.setColumns(10);
			}
			{
				infoLabel1 = new JLabel(Bundle.getString("BackupDialog.notesLabel1.text"));
				GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
				gbc_infoLabel1.insets = new Insets(0, 0, 5, 0);
				gbc_infoLabel1.anchor = GridBagConstraints.NORTHWEST;
				gbc_infoLabel1.gridx = 0;
				gbc_infoLabel1.gridy = 2;
				contentPanel.add(infoLabel1, gbc_infoLabel1);
			}
			{
				infoLabel2 = new JLabel(Bundle.getString("BackupDialog.notesLabel2.text"));
				GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
				gbc_infoLabel2.anchor = GridBagConstraints.WEST;
				gbc_infoLabel2.gridx = 0;
				gbc_infoLabel2.gridy = 3;
				contentPanel.add(infoLabel2, gbc_infoLabel2);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.CENTER);
			buttonPane.setLayout(fl_buttonPane);
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				backupButton = new JButton(Bundle.getString("BackupDialog.backupButton.text"));
				backupButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_backupButton_actionPerformed(e);
					}
				});
				buttonPane.add(backupButton);
				getRootPane().setDefaultButton(backupButton);
			}
			{
				JButton cancelButton = new JButton(Bundle.getString("BackupDialog.cancelButton.text"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						do_cancelButton_actionPerformed(arg0);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
// Help button isn't used yet
//			{
//				helpButton = new JButton(Bundle.getString("BackupDialog.helpButton.text"));
//				helpButton.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						do_helpButton_actionPerformed(e);
//					}
//				});
//				helpButton.setEnabled(false);
//				buttonPane.add(helpButton);
//			}
		}
	}

	protected void do_backupButton_actionPerformed(ActionEvent e) {
		// Do the backup of the files...
		String setName = null;

		try {
			log.debug("backup button activated");

			setName = setNameTextField.getText();

			// check to see if files are dirty
			if (OperationsXml.areFilesDirty()) {
				if (JOptionPane
						.showConfirmDialog(
								this,
								Bundle.getString("OperationsFilesModified"),
								Bundle.getString("SaveOperationFiles"),
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					OperationsXml.save();
				}
			}

			// check to see if directory already exists
			if (backup.checkIfBackupSetExists(setName)) {
				int result = JOptionPane.showConfirmDialog(this, MessageFormat
						.format(Bundle.getString("DirectoryAreadyExists"),
								new Object[] { setName }),
								Bundle.getString("OverwriteBackupDirectory"),
						JOptionPane.OK_CANCEL_OPTION);

				if (result != JOptionPane.OK_OPTION) {
					return;
				}
			}

			backup.backupFilesToSetName(setName);
			dispose();
		}

		catch (IOException ex) {
			ExceptionContext context = new ExceptionContext(
					ex,
					"Backing up Operation files to: " + setName,
					"Ensure that the name is a valid directory name and that you have permission to create files there, and try again.");
			new ExceptionDisplayFrame(context);

		} catch (RuntimeException ex) {
			// ex.printStackTrace();
			log.error("Doing backup...", ex);

			UnexpectedExceptionContext context = new UnexpectedExceptionContext(
					ex, "Backing up Operation files to: " + setName);

			new ExceptionDisplayFrame(context);
		} catch (Exception ex) {
			// ex.printStackTrace();
			log.error("Doing backup...", ex);

			UnexpectedExceptionContext context = new UnexpectedExceptionContext(
					ex, "Backing up Operation files to: " + setName);

			new ExceptionDisplayFrame(context);
		}
	}

	protected void do_cancelButton_actionPerformed(ActionEvent arg0) {
		dispose();
	}

	private void enableBackupButton() {
		// Enable only if we have something in the text field.
		// Still need to check for a string of blanks......
		String s = setNameTextField.getText();
		backupButton.setEnabled(s.length() > 0);
	}

	protected void do_helpButton_actionPerformed(ActionEvent e) {
		// Not implemented yet.
	}
}

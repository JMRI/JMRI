// TrainsScriptFrame.java

package jmri.jmrit.operations.trains;

import java.awt.GridBagLayout;
import java.util.List;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.FileUtil;

/**
 * Frame for user edit of startup and shutdown operation scripts.
 * 
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dan Boudreau Copyright (C) 2011
 * @version $Revision$
 */

public class TrainsScriptFrame extends OperationsFrame {

	TrainManager manager;
	TrainManagerXml managerXml;

	// script panels
	JPanel pStartUpScript = new JPanel();
	JPanel pShutDownScript = new JPanel();
	JScrollPane startUpScriptPane;
	JScrollPane shutDownScriptPane;

	// labels

	// major buttons
	JButton addStartUpScriptButton = new JButton(Bundle.getMessage("AddScript"));
	JButton addShutDownScriptButton = new JButton(Bundle.getMessage("AddScript"));
	JButton runStartUpScriptButton = new JButton(Bundle.getMessage("RunScripts"));
	JButton runShutDownScriptButton = new JButton(Bundle.getMessage("RunScripts"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	public TrainsScriptFrame() {
		super();
	}

	public void initComponents() {
		// Set up script options in a Scroll Pane..
		startUpScriptPane = new JScrollPane(pStartUpScript);
		startUpScriptPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		startUpScriptPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScriptsStartUp")));

		shutDownScriptPane = new JScrollPane(pShutDownScript);
		shutDownScriptPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		shutDownScriptPane
				.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScriptsShutDown")));

		// remember who called us

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Layout the panel by rows

		// row 1

		// row 2
		updateStartUpScriptPanel();

		// row 3
		updateShutDownScriptPanel();

		// row 4 buttons
		JPanel pB = new JPanel();
		pB.setLayout(new GridBagLayout());
		addItem(pB, saveButton, 3, 0);

		getContentPane().add(startUpScriptPane);
		getContentPane().add(shutDownScriptPane);
		getContentPane().add(pB);

		// setup buttons
		addButtonAction(addStartUpScriptButton);
		addButtonAction(addShutDownScriptButton);
		addButtonAction(runStartUpScriptButton);
		addButtonAction(runShutDownScriptButton);
		addButtonAction(saveButton);

		enableButtons(true);

		addHelpMenu("package.jmri.jmrit.operations.Operations_TrainScripts", true);
		packFrame();
	}

	private void updateStartUpScriptPanel() {
		pStartUpScript.removeAll();
		pStartUpScript.setLayout(new GridBagLayout());
		addItem(pStartUpScript, addStartUpScriptButton, 0, 0);

		// load any existing startup scripts
		List<String> scripts = manager.getStartUpScripts();
		if (scripts.size() > 0)
			addItem(pStartUpScript, runStartUpScriptButton, 1, 0);
		for (int i = 0; i < scripts.size(); i++) {
			JButton removeStartUpScripts = new JButton(Bundle.getMessage("RemoveScript"));
			removeStartUpScripts.setName(scripts.get(i));
			removeStartUpScripts.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					buttonActionRemoveStartUpScript(e);
				}
			});
			addButtonAction(removeStartUpScripts);
			JLabel pathname = new JLabel(scripts.get(i));
			addItem(pStartUpScript, removeStartUpScripts, 0, i + 1);
			addItem(pStartUpScript, pathname, 1, i + 1);
		}
	}

	private void updateShutDownScriptPanel() {
		pShutDownScript.removeAll();
		pShutDownScript.setLayout(new GridBagLayout());
		addItem(pShutDownScript, addShutDownScriptButton, 0, 0);

		// load any existing shutdown scripts
		List<String> scripts = manager.getShutDownScripts();
		if (scripts.size() > 0)
			addItem(pShutDownScript, runShutDownScriptButton, 1, 0);
		for (int i = 0; i < scripts.size(); i++) {
			JButton removeShutDownScripts = new JButton(Bundle.getMessage("RemoveScript"));
			removeShutDownScripts.setName(scripts.get(i));
			removeShutDownScripts.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					buttonActionRemoveShutDownScript(e);
				}
			});
			JLabel pathname = new JLabel(scripts.get(i));
			addItem(pShutDownScript, removeShutDownScripts, 0, i + 1);
			addItem(pShutDownScript, pathname, 1, i + 1);
		}
	}

	// Save train, add scripts buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addStartUpScriptButton) {
			log.debug("train add move script button activated");
			File f = selectFile();
			if (f != null) {
				manager.addStartUpScript(FileUtil.getPortableFilename(f));
				updateStartUpScriptPanel();
				packFrame();
			}
		}
		if (ae.getSource() == addShutDownScriptButton) {
			log.debug("train add termination script button activated");
			File f = selectFile();
			if (f != null) {
				manager.addShutDownScript(FileUtil.getPortableFilename(f));
				updateShutDownScriptPanel();
				packFrame();
			}
		}
		if (ae.getSource() == runStartUpScriptButton) {
			runScripts(manager.getStartUpScripts());
		}
		if (ae.getSource() == runShutDownScriptButton) {
			runScripts(manager.getShutDownScripts());
		}
		if (ae.getSource() == saveButton) {
			log.debug("Save button activated");
			OperationsXml.save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	public void buttonActionRemoveStartUpScript(java.awt.event.ActionEvent ae) {
		JButton rbutton = (JButton) ae.getSource();
		log.debug("remove move script button activated " + rbutton.getName());
		manager.deleteStartUpScript(rbutton.getName());
		updateStartUpScriptPanel();
		packFrame();
	}

	public void buttonActionRemoveShutDownScript(java.awt.event.ActionEvent ae) {
		JButton rbutton = (JButton) ae.getSource();
		log.debug("remove termination script button activated " + rbutton.getName());
		manager.deleteShutDownScript(rbutton.getName());
		updateShutDownScriptPanel();
		packFrame();
	}

	/**
	 * We always use the same file chooser in this class, so that the user's last-accessed directory remains available.
	 */
	JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");

	private File selectFile() {
		if (fc == null) {
			log.error("Could not find user directory");
		} else {
			fc.setDialogTitle("Find desired script file");
			// when reusing the chooser, make sure new files are included
			fc.rescanCurrentDirectory();
		}

		int retVal = fc.showOpenDialog(null);
		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			// Run the script from it's filename
			return file;
		}
		return null;
	}

	private void enableButtons(boolean enabled) {
		addStartUpScriptButton.setEnabled(enabled);
		addShutDownScriptButton.setEnabled(enabled);
		saveButton.setEnabled(enabled);
	}

	private void runScripts(List<String> scripts) {
		for (int i = 0; i < scripts.size(); i++) {
			jmri.util.PythonInterp
					.runScript(jmri.util.FileUtil.getExternalFilename(scripts.get(i)));
		}
	}

	private void packFrame() {
		setPreferredSize(null);
		pack();
		if (getWidth() < 600)
			setSize(600, getHeight());
		if (getHeight() < 300)
			setSize(getWidth(), 300);
		setVisible(true);
	}

	public void dispose() {
		super.dispose();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainsScriptFrame.class
			.getName());
}

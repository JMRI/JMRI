// TrainScriptFrame.java

package jmri.jmrit.operations.trains;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import java.util.List;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of a train's script options. Allows the user to execute
 * scripts when a train is moved or terminated.
 * 
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision: 1.6 $
 */

public class TrainScriptFrame extends OperationsFrame {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	TrainManager manager;
	TrainManagerXml managerXml;

	Train _train = null;
	TrainEditFrame _trainEditFrame;

	// script panels
	JPanel pMoveScript = new JPanel();
	JPanel pTerminationScript = new JPanel();
	JScrollPane moveScriptPane;
	JScrollPane terminationScriptPane;

	// labels
	JLabel trainName = new JLabel();
	JLabel trainDescription = new JLabel();

	// major buttons
	JButton addMoveScriptButton = new JButton(rb.getString("AddScript"));
	JButton addTerminationScriptButton = new JButton(rb.getString("AddScript"));
	JButton saveTrainButton = new JButton(rb.getString("SaveTrain"));

	public TrainScriptFrame() {
		super();
 	}

	public void initComponents(TrainEditFrame parent) {
    	// Set up script options in a Scroll Pane..
     	moveScriptPane = new JScrollPane(pMoveScript);
      	moveScriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	moveScriptPane.setBorder(BorderFactory.createTitledBorder(rb.getString("ScriptsWhenMoved")));
 
      	terminationScriptPane = new JScrollPane(pTerminationScript);
      	terminationScriptPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	terminationScriptPane.setBorder(BorderFactory.createTitledBorder(rb.getString("ScriptsWhenTerminated")));  	

		// remember who called us
		_trainEditFrame = parent;
		_trainEditFrame.setChildFrame(this);
		_train = _trainEditFrame._train;

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
				
		// Layout the panel by rows
	   	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
				
		// row 1a
       	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
    	addItem(pName, trainName, 0, 0);

		// row 1b
       	JPanel pDesc = new JPanel();
    	pDesc.setLayout(new GridBagLayout());
    	pDesc.setBorder(BorderFactory.createTitledBorder(rb.getString("Description")));
    	addItem(pDesc, trainDescription, 0, 0);
		
    	p1.add(pName);
    	p1.add(pDesc);
    
		// row 2
    	updateMoveScriptPanel();
    	
		// row 3
    	updateTerminationScriptPanel();
 
		// row 4 buttons
	   	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());		
		addItem(pB, saveTrainButton, 3, 0);
		
		getContentPane().add(p1);
		getContentPane().add(moveScriptPane);
		getContentPane().add(terminationScriptPane);
      	getContentPane().add(pB);
		
		// setup buttons
      	addButtonAction(addMoveScriptButton);
		addButtonAction(addTerminationScriptButton);
		addButtonAction(saveTrainButton);
		
		if (_train != null){
			trainName.setText(_train.getName());
			trainDescription.setText(_train.getDescription());
			enableButtons(true);
		} else {
			enableButtons(false);
		}
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
		packFrame();
	}
	
	private void updateMoveScriptPanel(){
		pMoveScript.removeAll();
	   	pMoveScript.setLayout(new GridBagLayout());
    	addItem(pMoveScript, addMoveScriptButton, 0, 0);
    	
    	// load any existing train move scripts
    	if (_train != null){
    		List<String> scripts = _train.getMoveScripts();
    		for (int i=0; i<scripts.size(); i++){
    			JButton removeMoveScripts = new JButton(rb.getString("RemoveScript"));
    			removeMoveScripts.setName(scripts.get(i));
    			removeMoveScripts.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					buttonActionRemoveMoveScript(e);
    				}
    			});
    			addButtonAction(removeMoveScripts);
    			JLabel pathname = new JLabel(scripts.get(i));
    			addItem(pMoveScript, removeMoveScripts, 0, i+1);
    			addItem(pMoveScript, pathname, 1, i+1);
    		}
    	}
	}
	
	private void updateTerminationScriptPanel(){
		pTerminationScript.removeAll();
	   	pTerminationScript.setLayout(new GridBagLayout());
    	addItem(pTerminationScript, addTerminationScriptButton, 0, 0);
    	
    	// load any existing train termination scripts
    	if (_train != null){
    		List<String> scripts = _train.getTerminationScripts();
    		for (int i=0; i<scripts.size(); i++){
    			JButton removeTerminationScripts = new JButton(rb.getString("RemoveScript"));
    			removeTerminationScripts.setName(scripts.get(i));
       			removeTerminationScripts.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					buttonActionRemoveTerminationScript(e);
    				}
    			});
    			JLabel pathname = new JLabel(scripts.get(i));
    			addItem(pTerminationScript, removeTerminationScripts, 0, i+1);
    			addItem(pTerminationScript, pathname, 1, i+1);
    		}
    	}
	}
	
	// Save train, add scripts buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train != null){
			if (ae.getSource() == addMoveScriptButton){
				log.debug("train add move script button actived");
				File f = selectFile();
				if (f != null){
					_train.addMoveScript(f.getAbsolutePath());
					updateMoveScriptPanel();
					packFrame();
				}
			}
			if (ae.getSource() == addTerminationScriptButton){
				log.debug("train add termination script button actived");
				File f = selectFile();
				if (f != null){
					_train.addTerminationScript(f.getAbsolutePath());
					updateTerminationScriptPanel();
					packFrame();
				}
			}
			if (ae.getSource() == saveTrainButton){
				log.debug("train save button actived");
				saveTrain();
			}
		}
	}
	
	public void buttonActionRemoveMoveScript(java.awt.event.ActionEvent ae){
		if (_train != null){
			JButton rb = (JButton)ae.getSource();
			log.debug("remove move script button actived "+rb.getName());
			_train.deleteMoveScript(rb.getName());
			updateMoveScriptPanel();
			packFrame();
		}
	}
	
	public void buttonActionRemoveTerminationScript(java.awt.event.ActionEvent ae){
		if (_train != null){
			JButton rb = (JButton)ae.getSource();
			log.debug("remove termination script button actived "+rb.getName());
			_train.deleteTerminationScript(rb.getName());
			updateTerminationScriptPanel();
			packFrame();
		}
	}

	/**
	 * We always use the same file chooser in this class, so that
	 * the user's last-accessed directory remains available.
	 */
	JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");

	private File selectFile() {
		if (fc==null) {
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
	
	private void saveTrain (){
		manager.save();
	}
	
	private void enableButtons(boolean enabled){
		addMoveScriptButton.setEnabled(enabled);
		addTerminationScriptButton.setEnabled(enabled);
		saveTrainButton.setEnabled(enabled);
	}
	
    private void packFrame(){
 		pack();
 		if(getWidth()<400)
 			setSize(450, getHeight()+50);
 		if (getHeight()<275)
 			setSize(getWidth(), 325);
		setVisible(true);
    }
	
	public void dispose() {
		_trainEditFrame.setChildFrame(null);
		super.dispose();
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainScriptFrame.class.getName());
}

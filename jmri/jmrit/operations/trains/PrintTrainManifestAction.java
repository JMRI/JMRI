// PrintTrainManifestAction.java

package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.*;

import javax.swing.*;


/**
 * Action to print a a train's manifest
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision: 1.2 $
 */
public class PrintTrainManifestAction  extends AbstractAction {

    public PrintTrainManifestAction(String actionName, boolean preview, Frame frame) {
        super(actionName);
        isPreview = preview;
        this.frame = frame;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    Frame frame;
    

    public void actionPerformed(ActionEvent e) {
    	TrainEditFrame f = (TrainEditFrame)frame;
    	Train train = f._train;
    	if (train == null)
    		return;
    	if (!train.isBuilt()){
    		String string = "Do you want to print the previous manifest for Train (" +train.getName()+ ")";
    		int results = JOptionPane.showConfirmDialog(null, string,
    				"Print previous manifest?",
    				JOptionPane.YES_NO_OPTION);
    		if (results != JOptionPane.YES_OPTION)
    			return;
    	}    	
    	if (!train.printManifest(isPreview)){
    		String string = "Need to build train (" +train.getName()+ ") before printing manifest";
    		JOptionPane.showMessageDialog(null, string,
    				"Can not print manifest!",
    				JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintTrainManifestAction.class.getName());
}

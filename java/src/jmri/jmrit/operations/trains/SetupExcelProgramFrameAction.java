// RunExcelProgramFrameAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;

/**
 * Swing action to create a RunExcelProgramFrame.
 * 
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class SetupExcelProgramFrameAction extends AbstractAction {

    public SetupExcelProgramFrameAction(String s) {
    	super(s);
    }

    SetupExcelProgramFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
    	if (f != null && f.isVisible()){
    		f.dispose();
    	}
    	f = new SetupExcelProgramFrame();
    	f.initComponents();
    	f.setExtendedState(Frame.NORMAL);  	
    }
}

/* @(#)TrainScriptAction.java */

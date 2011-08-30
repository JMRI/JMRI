// DecoderPro3Action.java

package apps.gui3.dp3;

import java.io.File;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import java.awt.event.ActionEvent;

/**
 * AbstractAction for the DP3 window so that further 
 * windows can be opened
 *
 * @author    Kevin Dickerson Copyright (C) 2011
 */
public class DecoderPro3Action extends jmri.util.swing.JmriAbstractAction
     {
    
    public DecoderPro3Action(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public DecoderPro3Action(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    /**
    * Method for opening a new window via the classic JMRI interface
    */
    public DecoderPro3Action(String pName, boolean allowQuit) {
        super(pName);
        this.allowQuit=allowQuit;
    }
    
    boolean allowQuit = true;
    
    public void actionPerformed(ActionEvent event) {
        mainFrame = new DecoderPro3Window();
        mainFrame.setSize(new java.awt.Dimension(1024, 600));
        mainFrame.setVisible(true);
        mainFrame.allowQuit(allowQuit);
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    DecoderPro3Window mainFrame;
    
}


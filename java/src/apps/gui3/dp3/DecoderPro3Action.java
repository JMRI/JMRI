// DecoderPro3Action.java

package apps.gui3.dp3;

import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import javax.swing.WindowConstants;

import java.awt.event.ActionEvent;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;

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
    
    @Override
    public void actionPerformed(ActionEvent event) {
        mainFrame = new DecoderPro3Window();
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if(!p.isWindowPositionSaved(mainFrame.getWindowFrameRef())) {
            mainFrame.setSize(new java.awt.Dimension(1024, 600));
            mainFrame.setPreferredSize(new java.awt.Dimension(1024, 600));
        }
        if (wi instanceof RosterGroupSelector) {
            mainFrame.setSelectedRosterGroup(((RosterGroupSelector)wi).getSelectedRosterGroup());
        }
        mainFrame.setVisible(true);
        mainFrame.allowQuit(allowQuit);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    DecoderPro3Window mainFrame;
}


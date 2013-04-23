// StatusFrame.java

package jmri.jmrit.roster.swing.speedprofile;

import javax.swing.BoxLayout;
import javax.swing.JButton;


/**
 * Frame Entry Exit Frames
 * @author	Kevin Dickerson  Copyright (C) 2011
 * @version $Revision: 1.5 $
*/
public class SpeedProfileFrame extends jmri.util.JmriJFrame {

    public SpeedProfileFrame() {
        super(false, true);
    }
    
    JButton sendButton;
    SpeedProfilePanel spPanel;
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        
        spPanel = new SpeedProfilePanel();
        
        setTitle(Bundle.getMessage("SpeedProfile"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(spPanel);
        
		addHelpMenu("package.jmri.jmrit.roster.swing.speedprofile.SpeedProfileFrame", true);

        // pack for display
        pack();
    }
}


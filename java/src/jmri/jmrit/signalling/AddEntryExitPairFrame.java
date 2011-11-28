// StatusFrame.java

package jmri.jmrit.signalling;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import jmri.jmrit.display.layoutEditor.LayoutEditor;


/**
 * Frame Entry Exit Frames
 * @author	Kevin Dickerson  Copyright (C) 2011
 * @version $Revision: 1.5 $
*/
public class AddEntryExitPairFrame extends jmri.util.JmriJFrame {

    public AddEntryExitPairFrame() {
        super(false, true);
    }
    
    JButton sendButton;
    AddEntryExitPairPanel nxPanel;
    
    public void initComponents(LayoutEditor panel) throws Exception {
        // the following code sets the frame's initial state
        
        nxPanel = new AddEntryExitPairPanel(panel);
        
        setTitle("Add Entry Exit Points");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(nxPanel);
        
		addHelpMenu("package.jmri.jmrit.signalling.EntryExitFrame", true);

        // pack for display
        pack();
    }
}


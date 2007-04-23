// LogFrame.java

package jmri.jmrit.log;

import jmri.*;
import jmri.util.*;
import java.awt.*;

import javax.swing.*;

/**
 * Frame for adding to the log file.
 * 
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision: 1.1 $
 */
public class LogFrame extends jmri.util.JmriJFrame {

    public LogFrame() {
    }

    public void initComponents() throws Exception {

        setTitle("Make Log Entry");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

 
        getContentPane().add(new LogPanel());

        pack();
    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }

        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

}

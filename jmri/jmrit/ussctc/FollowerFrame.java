// FollowerFrame.java

package jmri.jmrit.ussctc;

import jmri.*;
import jmri.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * User interface frame for creating and editing "Follower" logic
 * on USS CTC machines.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision: 1.1 $
 */
public class FollowerFrame extends jmri.util.JmriJFrame {

    public FollowerFrame() {
    }

    public void initComponents() throws Exception {
        addHelpMenu("package.jmri.jmrit.ussctc.FollowerFrame", true);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new FollowerPanel());
        setTitle(FollowerPanel.rb.getString("TitleFollower"));
        
        // pack to cause display
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FollowerFrame.class.getName());

}

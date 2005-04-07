// MangerFrame.java

package signalpro.mangler;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JMenuBar;

import jmri.util.JmriJFrame;

/**
 * Frame for manipulating (mangling) .hex files
 *
 * This is just an enclosure for the ManglerPane, which does the real work.
 *
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version             $Revision: 1.1.1.1 $
 */
public class ManglerFrame extends JmriJFrame {

    // GUI member declarations
    ManglerPane pane	= new ManglerPane();

    public ManglerFrame() {
        super(ResourceBundle.getBundle("signalpro.mangler.Mangler").getString("TitleMangler"));
        // general GUI config

        // install items in GUI
        getContentPane().add(pane);
        pack();
    }

    // handle resizing when first shown
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
        pane.dispose();
        dispose();
	// and disconnect from the SlotManager
    }
}

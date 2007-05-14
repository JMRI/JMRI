// JmriJFrame.java

package jmri.util;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import java.awt.*;

/**
 * JFrame extended for common JMRI use.
 * <P>
 * We needed a place to refactor common JFrame additions in JMRI
 * code, so this class was created.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a
 * Java 1.1.8 system, or at least try to fake it.
 * <P>
 * Features:
 * <ul>
 * <LI>Size limited to the maximum available on the screen, after
 * removing any menu bars (Mac) and taskbars (Windows)
 * </ul>
 *
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.7 $
 */

public class JmriJFrame extends JFrame implements java.awt.event.WindowListener {

    public JmriJFrame() {
	super();
        addWindowListener(this);
	// Set the image for use when minimized
	setIconImage(getToolkit().getImage("resources/jmri32x32.gif"));
    }

    public JmriJFrame(String name) {
        super(name);
        addWindowListener(this);
	// Set the image for use when minimized
	setIconImage(getToolkit().getImage("resources/jmri32x32.gif"));
    }

    /**
     * By default, Swing components should be 
     * created an installed in this method, rather than
     * in the ctor itself.
     */
    public void initComponents() throws Exception {}
    
    /**
     * @param direct true if the help menu goes directly to the help system,
     *        e.g. there are no items in the help menu
     */
    public void addHelpMenu(String ref, boolean direct) {
        // only works if no menu present?
        JMenuBar bar = getJMenuBar();
        if (bar == null) bar = new JMenuBar();
        jmri.util.HelpUtil.helpMenu(bar, this, ref, direct);
        setJMenuBar(bar);
    }
    
    public Dimension getMaximumSize() {
        // adjust maximum size to full screen minus any toolbars
        try {
            Insets insets = getToolkit().getScreenInsets(this.getGraphicsConfiguration());
            Dimension screen = getToolkit().getScreenSize();
            return new Dimension(screen.width-(insets.right+insets.left),
                screen.height-(insets.top+insets.bottom));
        } catch (NoSuchMethodError e) {
            Dimension screen = getToolkit().getScreenSize();
            return new Dimension(screen.width,
                screen.height-45);  // approximate this...
        }
    }

    public Dimension getPreferredSize() {
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
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

    
    public void windowOpened(java.awt.event.WindowEvent e) {}
    
    public void windowActivated(java.awt.event.WindowEvent e) {}
    public void windowDeactivated(java.awt.event.WindowEvent e) {}
    public void windowIconified(java.awt.event.WindowEvent e) {}
    public void windowDeiconified(java.awt.event.WindowEvent e) {}

   /**
     * Close and dispose the window when the close box is clicked.
     *<P>
     * Subclasses should this this method via super.windowClosing
     * after doing necessary cleanup.
     **/
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();	// and disconnect from the SlotManager
    }
    
    public void windowClosed(java.awt.event.WindowEvent e) {}
}
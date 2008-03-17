// JmriJFrame.java

package jmri.util;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
 
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
 * <LI>Cleanup upon closing the frame: When the
 * frame is closed (WindowClosing event), the 
 * dispose() method is invoked to do cleanup. This is
 * inherited from JFrame itself, so super.dispose() needs
 * to be invoked in the over-loading methods.
 * <LI>Maintains a list of existing JmriJFrames
 * </ul>
 *
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.16 $
 */

public class JmriJFrame extends JFrame implements java.awt.event.WindowListener {

    public JmriJFrame() {
	    super();
	    self = this;
        addWindowListener(this);
        synchronized (list) {
            list.add(this);
        }
	    // Set the image for use when minimized
	    setIconImage(getToolkit().getImage("resources/jmri32x32.gif"));
        // set the close short cut
        addWindowCloseShortCut();
    }

    public JmriJFrame(String name) {
        this();
        setTitle(name);
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
        JMenu menu = jmri.util.HelpUtil.helpMenu(bar, this, ref, direct);
        setJMenuBar(bar);
    }
    
    /**
     * Adds a "Close Window" key short cut to close window on op-W.
     */
    void addWindowCloseShortCut() {
        // modelled after code in JavaDev mailing list item by Bill Tschumy <bill@otherwise.com> 08 Dec 2004 
        AbstractAction act = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // if (log.isDebugEnabled()) log.debug("keystroke requested close window "+JmriJFrame.this.getTitle());
                JmriJFrame.this.processWindowEvent(
                    new java.awt.event.WindowEvent(JmriJFrame.this, 
                                                java.awt.event.WindowEvent.WINDOW_CLOSING));
            }
        };
        getRootPane().getActionMap().put("close", act);

        int stdMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, stdMask), "close");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }
        
    JmriJFrame self;
    
    /**
     * Provide a maximum frame size that is limited
     * to what can fit on the screen after toolbars, etc
     * are deducted.
     *<P>
     * Some of the methods used here return null pointers 
     * on some Java implementations, however, so 
     * this will return the superclasses's maximum size
     * if the algorithm used here fails.
     */
    public Dimension getMaximumSize() {
        // adjust maximum size to full screen minus any toolbars
        try {
            // Try our own alorithm.  This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fall back.
            try {
                // First, ask for the physical screen size
                Dimension screen = getToolkit().getScreenSize();

                // Next, ask for any insets on the screen.
                Insets insets = getToolkit().getScreenInsets(this.getGraphicsConfiguration());
                int widthInset = insets.right+insets.left;
                int heightInset = insets.top+insets.bottom;
                
                // If insets are zero, guess based on system type
                if (widthInset == 0 && heightInset == 0) {
                    String type = System.getProperty("os.name","");
                    if (type.equals("Linux")) {
                        // Linux generally has a bar across the top and/or bottom
                        // of the screen, but lets you have the full width.
                        heightInset = 70;
                    }
                    // Windows generally has values, but not always,
                    // so we provide observed values just in case
                    else if (type.equals("Windows XP")) {
                        heightInset = 28;  // bottom 28
                    } else if (type.equals("Windows 98")) {
                        heightInset = 28;  // bottom 28
                    } else if (type.equals("Windows 2000")) {
                        heightInset = 28;  // bottom 28
                    }
                }
                
                // Insets may also be provided as system parameters
                String sw = System.getProperty("jmri.inset.width");
                if (sw!=null) try {
                    widthInset = Integer.parseInt(sw);
                } catch (Exception e1) {log.error("Error parsing jmri.inset.width: "+e1);}
                String sh = System.getProperty("jmri.inset.height");
                if (sh!=null) try {
                    heightInset = Integer.parseInt(sh);
                } catch (Exception e1) {log.error("Error parsing jmri.inset.height: "+e1);}
                           
                // calculate size as screen size minus space needed for offsets
                return new Dimension(screen.width-widthInset, screen.height-heightInset);
                
            } catch (NoSuchMethodError e) {
                Dimension screen = getToolkit().getScreenSize();
                return new Dimension(screen.width,
                    screen.height-45);  // approximate this...
            }
        } catch (Exception e2) {
            // failed completely, fall back to standard method
            return super.getMaximumSize();
        }
    }

    /**
     * The preferred size must fit on the physical screen, so 
     * calculate the lesser of either the preferred size from the
     * layout or the screen size.
     */
    public Dimension getPreferredSize() {
        // limit preferred size to size of screen (from getMaximumSize())
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

    /**
     * Get a List of the currently-existing JmriJFrame objects.
     * The returned list is a copy made at the time of the call,
     * so it can be manipulated as needed by the caller.
     */
    public static java.util.List getFrameList() {
        java.util.List returnList;
        synchronized(list) {
            returnList = new java.util.ArrayList(list);
        }
        return returnList;
    }
    
    /**
     * Get a JmriJFrame of a particular name.
     * If more than one exists, there's no guarantee 
     * as to which is returned.
     */
    public static JmriJFrame getFrame(String name) {
        java.util.List list = getFrameList();  // needed to get synch copy
        for (int i=0; i<list.size(); i++) {
            JmriJFrame j = (JmriJFrame)list.get(i);
            if (j.getTitle().equals(name)) return j;
        }
        return null;
    }
    
    static java.util.ArrayList list = new java.util.ArrayList();
    
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

    // For marking the window as modified on MacOS X
    // See: http://developer.apple.com/qa/qa2001/qa1146.html
    final static String WINDOW_MODIFIED = "windowModified";
    public void markWindowModified(boolean yes){
        getRootPane().putClientProperty(WINDOW_MODIFIED, yes ? Boolean.TRUE : Boolean.FALSE);
    }
    
    // Window methods
    public void windowOpened(java.awt.event.WindowEvent e) {}
    
    public void windowActivated(java.awt.event.WindowEvent e) {}
    public void windowDeactivated(java.awt.event.WindowEvent e) {}
    public void windowIconified(java.awt.event.WindowEvent e) {}
    public void windowDeiconified(java.awt.event.WindowEvent e) {}

   /**
     * Close and dispose the window when the close box is clicked.
     *<P>
     * Subclasses should invoke this method via super.windowClosing
     * after doing necessary cleanup. Note that it calls dispose(),
     * so subclasses should not do that.
     **/
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        synchronized (list) {
            list.remove(this);
        }
        dispose();	// for subclasses
    }
    
    public void windowClosed(java.awt.event.WindowEvent e) {}
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriJFrame.class.getName());

}
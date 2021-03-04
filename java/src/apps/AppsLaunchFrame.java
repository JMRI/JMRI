package apps;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import jmri.util.JmriJFrame;
import jmri.util.swing.JFrameInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for main frame (window) of traditional-style JMRI applications
 * <p>
 * This is for launching after the system is initialized, so it does none of
 * that.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2010, 2014
 * @author Dennis Miller Copyright 2005
 * @author Giorgio Terdina Copyright 2008
 * @author Matthew Harris Copyright (C) 2011
 */
public class AppsLaunchFrame extends jmri.util.JmriJFrame {

    static String profileFilename;

    public AppsLaunchFrame(AppsLaunchPane containedPane, String name) {
        super(name);

        // Create a WindowInterface object based on this frame (maybe pass it in?)
        JFrameInterface wi = new JFrameInterface(this);

        // Create a menu bar
        menuBar = new JMenuBar();

        // Create menu categories and add to the menu bar, add actions to menus
        AppsMainMenu.createMenus(menuBar, wi, containedPane, containedPane.windowHelpID());

        setJMenuBar(menuBar);
        add(containedPane);

        // handle window close
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        // pack
        pack();

        // center as default
        Dimension screen = getToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);

        // then try to load location and size from preferences
        setFrameLocation();
    }

    /**
     * Set the location of the window-specific help for the preferences pane.
     * Made a separate method so if can be overridden for application specific
     * preferences help
     *
     * @param f the frame to associate with the java help reference
     * @param l Java Help reference
     */
    protected void setPrefsFrameHelp(JmriJFrame f, String l) {
        f.addHelpMenu(l, true);
    }

    /**
     * Provide access to a place where applications can expect the configuration
     * code to build run-time buttons.
     *
     * @see apps.startup.CreateButtonModelFactory
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;

    // GUI members
    private JMenuBar menuBar;

//     private final static Logger log = LoggerFactory.getLogger(AppsLaunchFrame.class);
}

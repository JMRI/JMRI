package jmri.util.swing.mdi;

import java.awt.Frame;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.util.JmriJFrame;

/**
 * Display a JmriPanel in a JInternalFrame of its own.
 *
 * Dispose() of the panel is invoked when the containing window is fully closed
 * via a listener installed here.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public class JmriJInternalFrameInterface implements jmri.util.swing.WindowInterface {

    public JmriJInternalFrameInterface(JmriJFrame mainFrame, JDesktopPane desktop) {
        this.mainFrame = mainFrame;
        this.desktop = desktop;
    }

    JDesktopPane desktop;
    JmriJFrame mainFrame;

    @Override
    public void show(final jmri.util.swing.JmriPanel child,
            jmri.util.swing.JmriAbstractAction act,
            Hint hint) {
        // create new internal frame
        JInternalFrame frame = new JInternalFrame(child.getTitle(),
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable
        frame.setLocation(50, 50);

        // add gui object, responsible for own layout
        frame.add(child);

        // add menus if requested
        JMenuBar bar = frame.getJMenuBar();
        if (bar == null) {
            bar = new JMenuBar();
        }
        List<JMenu> list = child.getMenus();
        for (JMenu menu : list) {
            bar.add(menu);
        }

        // add help menu if requested; this is similar
        // to code in JmriJFrame
        if (child.getHelpTarget() != null) {
            // add Help menu
            jmri.util.HelpUtil.helpMenu(bar, child.getHelpTarget(), true);
        }
        frame.setJMenuBar(bar);

        // set title if available
        if (child.getTitle() != null) {
            frame.setTitle(child.getTitle());
        }

        // arrange to run dispose on close
        //frame.addWindowListener( new java.awt.event.WindowAdapter(){
        //    jmri.util.swing.JmriPanel c;
        //    { c = child; }
        //    public void windowClosed(java.awt.event.WindowEvent e) {
        //        c.dispose();
        //    }
        //});
        // add to desktop
        frame.pack();
        frame.setVisible(true);
        desktop.add(frame);
        frame.moveToFront();
    }

    @Override
    public void show(final jmri.util.swing.JmriPanel child,
            jmri.util.swing.JmriAbstractAction act) {

        show(child, act, Hint.DEFAULT);
    }

    @Override
    public void dispose() {
    }

    /**
     * Create new windows on each request
     */
    @Override
    public boolean multipleInstances() {
        return true;
    }

    @Override
    public Frame getFrame() {
        return (this.mainFrame != null) ? this.mainFrame : null;
    }

}

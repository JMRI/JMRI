// JmriJInternalFrameInterface.java

package jmri.util.swing.mdi;

import javax.swing.*;

/**
 * Display a JmriPanel in a JInternalFrame of its own.
 *
 * Dispose() of the panel is invoked when the containing window is fully closed
 * via a listener installed here.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.1 $
 */

public class JmriJInternalFrameInterface implements jmri.util.swing.WindowInterface {

    public JmriJInternalFrameInterface(jmri.util.JmriJFrame mainFrame,
                                       JDesktopPane desktop) {
        this.mainFrame = mainFrame;
        this.desktop = desktop;
    }

    jmri.util.JmriJFrame      mainFrame;
    JDesktopPane    desktop;

    public void show(final jmri.util.swing.JmriPanel child, 
                        jmri.util.swing.JmriAbstractAction act,
                        Hint hint) {
            // create new internal frame
            JInternalFrame frame = new JInternalFrame(child.getTitle(), 
                                            true, //resizable
                                            true, //closable
                                            true, //maximizable
                                            true);//iconifiable
            frame.setLocation(50,50);
            
            // add gui object, responsible for own layout
            frame.add(child);
            
            // add help menu if needed
            //if (child.getHelpTarget() != null)
                //frame.addHelpMenu(child.getHelpTarget(), true);

            // set title if available
            if (child.getTitle() != null)
                frame.setTitle(child.getTitle());

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

    public void show(final jmri.util.swing.JmriPanel child, 
                        jmri.util.swing.JmriAbstractAction act) {
            
                show(child, act, Hint.DEFAULT);
            }

    public void dispose() {}
    
    /** 
     * Create new windows on each request
     */
    public boolean multipleInstances() { return true; }

}
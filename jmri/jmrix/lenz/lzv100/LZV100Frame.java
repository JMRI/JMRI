// LZV100Frame.java

package jmri.jmrix.lenz.lzv100;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Frame displaying the LZV100 configuration utility
 *
 * This is a container for holding the LZV100 configuration utility.
 * The actuall configuration utility consists of two parts:
 * {@link jmri.jmrix.lenz.lz100.LZ100InternalFrame} a command station 
 *    configuration utility for the LZ100/LZV100
 * {@link jmri.jmrix.lenz.lv102.LV102InternalFrame} a configuration 
 * utility for the LV102 power station
 *
 * @author			Paul Bender  Copyright (C) 2003,2005
 * @version			$Revision: 2.5 $
 */
public class LZV100Frame extends jmri.util.JmriJFrame {

    public LZV100Frame() {
        this("LZV100 Configuration Utility");
    }

    public LZV100Frame(String FrameName) {
        super(FrameName);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	javax.swing.JInternalFrame LV102IFrame=new jmri.jmrix.lenz.lv102.LV102InternalFrame();

	javax.swing.JPanel pane0 = new JPanel();
	pane0.add(LV102IFrame);
        getContentPane().add(pane0);

	javax.swing.JInternalFrame LZ100IFrame=new jmri.jmrix.lenz.lz100.LZ100InternalFrame();

	javax.swing.JPanel pane1 = new JPanel();
	pane1.add(LZ100IFrame);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(closeButton);
        getContentPane().add(pane2);

        // and prep for display
        pack();

        // install close button handler
        closeButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	setVisible(false);
        		dispose();
                }
            }
        );

    }

    JToggleButton closeButton = new JToggleButton("Close");

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LZV100Frame.class.getName());

}

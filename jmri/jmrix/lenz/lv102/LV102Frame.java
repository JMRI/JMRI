// LV102Frame.java

package jmri.jmrix.lenz.lv102;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgrammerException;

/**
 * Frame displaying the LV102 configuration utility
 *
 * This is a container for the LV102 configuration utility.
 * The actual utility is defined in {@link LV102InternalFrame}
 *
 * @author			Paul Bender  Copyright (C) 2004,2005
 * @version			$Revision: 1.4 $
 */
public class LV102Frame extends jmri.util.JmriJFrame {

   private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lv102.LV102Bundle");

   public LV102Frame() {
      this("LV102 Configuration Utility");
   }

   public LV102Frame(String FrameName) {

	super(FrameName);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	javax.swing.JInternalFrame LV102IFrame=new LV102InternalFrame();

        javax.swing.JPanel pane0 = new JPanel();
        pane0.add(LV102IFrame);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(closeButton);
        getContentPane().add(pane1);

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

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LV102Frame.class.getName());

}

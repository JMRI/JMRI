// LZ100Frame.java

package jmri.jmrix.lenz.swing.lz100;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import javax.swing.*;

/**
 * Frame displaying the LZ100 configuration utility
 *
 * This is a container for the LZ100 configuration utility. The actual 
 * utiliy is defined in {@link LZ100InternalFrame}
 *
 * @author			Paul Bender  Copyright (C) 2005
 * @version			$Revision$
 */
public class LZ100Frame extends jmri.util.JmriJFrame {

    //private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lz100.LZ100Bundle");

    public LZ100Frame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
	    this("LZ100 Configuration Utility",memo);
    }

    public LZ100Frame(String FrameName,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(FrameName);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

 	javax.swing.JInternalFrame LZ100IFrame=new LZ100InternalFrame(memo);

	javax.swing.JPanel pane0 = new JPanel();
	pane0.add(LZ100IFrame);
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

    }


    JToggleButton closeButton = new JToggleButton("Close");

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static Logger log = LoggerFactory.getLogger(LZ100Frame.class.getName());

}

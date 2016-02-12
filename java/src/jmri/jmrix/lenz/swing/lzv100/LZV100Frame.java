// LZV100Frame.java
package jmri.jmrix.lenz.swing.lzv100;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying the LZV100 configuration utility
 *
 * This is a container for holding the LZV100 configuration utility. The actuall
 * configuration utility consists of two parts:
 * {@link jmri.jmrix.lenz.swing.lz100.LZ100InternalFrame} a command station
 * configuration utility for the LZ100/LZV100
 * {@link jmri.jmrix.lenz.swing.lv102.LV102InternalFrame} a configuration
 * utility for the LV102 power station
 *
 * @author	Paul Bender Copyright (C) 2003,2005
 * @version	$Revision$
 */
public class LZV100Frame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -272552917307354256L;

    public LZV100Frame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LZV100 Configuration Utility", memo);
    }

    public LZV100Frame(String FrameName, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(FrameName);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        javax.swing.JInternalFrame LV102IFrame = new jmri.jmrix.lenz.swing.lv102.LV102InternalFrame();

        javax.swing.JPanel pane0 = new JPanel();
        pane0.add(LV102IFrame);
        getContentPane().add(pane0);

        javax.swing.JInternalFrame LZ100IFrame = new jmri.jmrix.lenz.swing.lz100.LZ100InternalFrame(memo);

        javax.swing.JPanel pane1 = new JPanel();
        pane1.add(LZ100IFrame);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(closeButton);
        getContentPane().add(pane2);

        // and prep for display
        pack();

        // install close button handler
        closeButton.addActionListener(new ActionListener() {
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

    private final static Logger log = LoggerFactory.getLogger(LZV100Frame.class.getName());

}

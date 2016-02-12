// LV102Frame.java
package jmri.jmrix.lenz.swing.lv102;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying the LV102 configuration utility
 *
 * This is a container for the LV102 configuration utility. The actual utility
 * is defined in {@link LV102InternalFrame}
 *
 * @author	Paul Bender Copyright (C) 2004,2005
 * @version	$Revision$
 */
public class LV102Frame extends jmri.util.JmriJFrame {

    //private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lv102.LV102Bundle");
    /**
     *
     */
    private static final long serialVersionUID = -6327549110966065451L;

    public LV102Frame() {
        this("LV102 Configuration Utility");
    }

    public LV102Frame(String FrameName) {

        super(FrameName);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        javax.swing.JInternalFrame LV102IFrame = new LV102InternalFrame();

        javax.swing.JPanel pane0 = new JPanel();
        pane0.add(LV102IFrame);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(closeButton);
        getContentPane().add(pane1);

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

    private final static Logger log = LoggerFactory.getLogger(LV102Frame.class.getName());

}

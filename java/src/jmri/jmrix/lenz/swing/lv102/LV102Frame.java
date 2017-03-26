package jmri.jmrix.lenz.swing.lv102;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Frame displaying the LV102 configuration utility
 *
 * This is a container for the LV102 configuration utility. The actual utility
 * is defined in {@link LV102InternalFrame}
 *
 * @author Paul Bender Copyright (C) 2004,2005
  */
public class LV102Frame extends jmri.util.JmriJFrame {

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
            @Override
            public void actionPerformed(ActionEvent a) {
                setVisible(false);
                dispose();
            }
        }
        );
    }

    JToggleButton closeButton = new JToggleButton("Close");

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

}

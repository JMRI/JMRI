package jmri.jmrix.lenz.swing.lzv100;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Frame displaying the LZV100 configuration utility.
 * <p>
 * This is a container for holding the LZV100 configuration utility. The actual
 * configuration utility consists of two parts:
 * <ul>
 * <li>{@link jmri.jmrix.lenz.swing.lz100.LZ100InternalFrame} a command station
 * configuration utility for the LZ100/LZV100</li>
 * <li>{@link jmri.jmrix.lenz.swing.lv102.LV102InternalFrame} a configuration
 * utility for the LV102 power station</li>
 * </ul>
 *
 * @author Paul Bender Copyright (C) 2003,2005
 */
public class LZV100Frame extends jmri.util.JmriJFrame {

    public LZV100Frame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemLZV100ConfigurationManager"), memo);
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
            @Override
            public void actionPerformed(ActionEvent a) {
                setVisible(false);
                dispose();
            }
        }
        );

    }

    JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

}

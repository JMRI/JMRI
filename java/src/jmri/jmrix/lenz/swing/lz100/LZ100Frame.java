package jmri.jmrix.lenz.swing.lz100;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Frame displaying the LZ100 configuration utility
 * <p>
 * This is a container for the LZ100 configuration utility. The actual utiliy is
 * defined in {@link LZ100InternalFrame}
 *
 * @author Paul Bender Copyright (C) 2005
 */
public class LZ100Frame extends jmri.util.JmriJFrame {

    public LZ100Frame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemLZ100ConfigurationManager"), memo);
    }

    public LZ100Frame(String FrameName, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(FrameName);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        javax.swing.JInternalFrame LZ100IFrame = new LZ100InternalFrame(memo);

        JPanel pane0 = new JPanel();
        pane0.add(LZ100IFrame);
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

    JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

}

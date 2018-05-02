package jmri.jmrix.rps.reversealign;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a RpsTrackingFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class AlignmentPanelAction extends AbstractAction {

    RpsSystemConnectionMemo memo = null;

    public AlignmentPanelAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public AlignmentPanelAction(RpsSystemConnectionMemo _memo) {
        this("RPS Alignment Tool",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");

        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        f.addHelpMenu("package.jmri.jmrix.rps.reversealign.AlignmentPanel", true);

        panel = new AlignmentPanel(memo);
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
    }

    AlignmentPanel panel;

}

// AlignmentPanelAction.java
package jmri.jmrix.rps.reversealign;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;

/**
 * Swing action to create and register a RpsTrackingFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version $Revision$
 */
public class AlignmentPanelAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8498519485972403715L;

    public AlignmentPanelAction(String s) {
        super(s);
    }

    public AlignmentPanelAction() {
        this("RPS Alignment Tool");
    }

    public void actionPerformed(ActionEvent e) {
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");

        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        f.addHelpMenu("package.jmri.jmrix.rps.reversealign.AlignmentPanel", true);

        panel = new AlignmentPanel();
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
    }

    AlignmentPanel panel;

}


/* @(#)AlignmentPanelAction.java */

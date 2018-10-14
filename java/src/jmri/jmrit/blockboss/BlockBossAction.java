package jmri.jmrit.blockboss;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Swing action to create and show a "Simple Signal Logic" GUI panel.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class BlockBossAction extends AbstractAction {

    public BlockBossAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.SignalHeadManager.class) == null) {
            setEnabled(false);
        }
    }

    public BlockBossAction() {
        this(Bundle.getMessage("Simple_Signal_Logic"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // create the frame
        JFrame f = new BlockBossFrame();
        f.setVisible(true);
    }

}

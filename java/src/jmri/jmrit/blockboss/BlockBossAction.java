package jmri.jmrit.blockboss;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Swing action to create and show a "Simple Signal Logic" GUI panel.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 */
public class BlockBossAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 1838689834093701766L;

    public BlockBossAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class) == null) {
            setEnabled(false);
        }
    }

    public BlockBossAction() {
        this(java.util.ResourceBundle.getBundle("jmri.jmrit.blockboss.BlockBossBundle").getString("Simple_Signal_Logic"));
    }

    public void actionPerformed(ActionEvent e) {

        // create the frame
        JFrame f = new BlockBossFrame();
        f.setVisible(true);
    }
}

package jmri.jmrix.rps.rpsmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a RpsMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class RpsMonAction extends AbstractAction {

    RpsSystemConnectionMemo memo = null;

    public RpsMonAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public RpsMonAction(RpsSystemConnectionMemo _memo) {
        this("RPS Monitor",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RpsMonFrame f = new RpsMonFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
        }
        f.setVisible(true);

    }

}

package jmri.jmrix.secsi.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class NodeConfigAction extends AbstractAction {

    private SecsiSystemConnectionMemo memo;

    public NodeConfigAction(String s, SecsiSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public NodeConfigAction(SecsiSystemConnectionMemo _memo) {
        this("Configure SECSI Nodes",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class);
}

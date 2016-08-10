package jmri.jmrix.cmri.serial.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class NodeConfigAction extends AbstractAction {

    CMRISystemConnectionMemo _memo = null;

    public NodeConfigAction(String s,CMRISystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public NodeConfigAction(CMRISystemConnectionMemo memo) {
        this("Configure C/MRI Nodes",memo);
    }

    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class.getName());
}

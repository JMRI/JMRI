package jmri.jmrix.cmri.serial.nodeiolist;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a Node IO ListFrame object
 *
 * @author   Dave Duchamp  Copyright (C) 2006
 * @author   Chuck Catania Copyright (C) 2014
 */
public class NodeIOListAction extends AbstractAction {
    CMRISystemConnectionMemo _memo = null;

    public NodeIOListAction(String s,CMRISystemConnectionMemo memo) {
        super(s);
    _memo = memo;}

    public NodeIOListAction(CMRISystemConnectionMemo memo) {
        this("C/MRI Node Bit Assignments",memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeIOListFrame f = new NodeIOListFrame(_memo);
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }

   private final static Logger log = LoggerFactory.getLogger(NodeIOListAction.class);
}

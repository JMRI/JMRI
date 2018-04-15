package jmri.jmrix.secsi.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Swing action to create and register a NodeConfigFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008
 */
public class NodeConfigAction extends AbstractAction {

    private SecsiSystemConnectionMemo _memo;

    public NodeConfigAction(String s, SecsiSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public NodeConfigAction(SecsiSystemConnectionMemo memo) {
        this(Bundle.getMessage("ConfigNodesTitle"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: ", ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class);

}

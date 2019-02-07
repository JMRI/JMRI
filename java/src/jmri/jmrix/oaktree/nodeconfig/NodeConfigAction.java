package jmri.jmrix.oaktree.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NodeConfigFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class NodeConfigAction extends AbstractAction {

    private OakTreeSystemConnectionMemo _memo = null;

    public NodeConfigAction(String s, OakTreeSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public NodeConfigAction(OakTreeSystemConnectionMemo memo) {
        this(Bundle.getMessage("ConfigNodesTitle"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeConfigFrame f = new NodeConfigFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}", ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class);

}

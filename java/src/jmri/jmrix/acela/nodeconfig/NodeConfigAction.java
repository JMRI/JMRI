package jmri.jmrix.acela.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class NodeConfigAction extends AbstractAction {

    private AcelaSystemConnectionMemo _memo = null;

    public NodeConfigAction(String s, AcelaSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public NodeConfigAction() {
        this(Bundle.getMessage("ConfigNodesTitle"), InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
    }

    @Override
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

    private final static Logger log = LoggerFactory.getLogger(NodeConfigAction.class);

}

package jmri.jmrix.grapevine.nodetable;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.swing.GrapevineSystemConnectionAction;

/**
 * Swing action to create and register a NodeTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008
 */
public class NodeTableAction extends GrapevineSystemConnectionAction {

    public NodeTableAction(String s, GrapevineSystemConnectionMemo memo) {
        super(s, memo);
    }

    public NodeTableAction(GrapevineSystemConnectionMemo memo) {
        this(Bundle.getMessage("WindowTitle"), memo);
    }

    public NodeTableAction() {
        this(InstanceManager.getNullableDefault(GrapevineSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GrapevineSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            NodeTableFrame f = new NodeTableFrame(memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.error("Exception: {}", ex.toString());
            }
            f.setLocation(100, 30);
            f.setVisible(true);
        } else {
            log.error("No connection to run {} with", getValue(Action.NAME));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NodeTableAction.class);

}

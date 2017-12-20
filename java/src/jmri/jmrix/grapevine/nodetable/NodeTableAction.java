package jmri.jmrix.grapevine.nodetable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;

/**
 * Swing action to create and register a NodeTableFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008
 */
public class NodeTableAction extends AbstractAction {

    private GrapevineSystemConnectionMemo memo = null;

    public NodeTableAction(String s,GrapevineSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public NodeTableAction(GrapevineSystemConnectionMemo _memo) {
        this(Bundle.getMessage("WindowTitle"),_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeTableFrame f = new NodeTableFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setLocation(100, 30);
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(NodeTableAction.class);

}

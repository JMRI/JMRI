package jmri.jmrix.maple.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class NodeConfigAction extends AbstractAction {

    private MapleSystemConnectionMemo _memo = null;

    public NodeConfigAction(String s, MapleSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public NodeConfigAction(MapleSystemConnectionMemo memo) {
        this(Bundle.getMessage("WindowTitle"), memo);

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

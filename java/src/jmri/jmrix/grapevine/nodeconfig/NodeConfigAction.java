package jmri.jmrix.grapevine.nodeconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;

/**
 * Swing action to create and register a NodeConfigFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class NodeConfigAction extends AbstractAction {

    private GrapevineSystemConnectionMemo _memo = null;

    public NodeConfigAction(String s, GrapevineSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public NodeConfigAction(GrapevineSystemConnectionMemo memo) {
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

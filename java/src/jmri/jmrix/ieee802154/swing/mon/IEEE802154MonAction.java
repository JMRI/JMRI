package jmri.jmrix.ieee802154.swing.mon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a IEEE802154MonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2013
 */
public class IEEE802154MonAction extends AbstractAction {

    private jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo _memo;

    public IEEE802154MonAction(String s, jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public IEEE802154MonAction(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo) {
        this(Bundle.getMessage("MonActionTitle"), memo);
    }

    public IEEE802154MonAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo.class).get(0);
    }

    public IEEE802154MonAction() {
        this(Bundle.getMessage("MonActionTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a IEEE802154MonFrame
        IEEE802154MonFrame f = new IEEE802154MonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("IEEE802154MonAction starting IEEE802154MonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(IEEE802154MonAction.class);

}

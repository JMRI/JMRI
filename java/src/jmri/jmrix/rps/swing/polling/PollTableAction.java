package jmri.jmrix.rps.swing.polling;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a PollTableFrame object.
 * <p>
 * We only permit one, because notification is not entirely right yet.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class PollTableAction extends AbstractAction {

    public PollTableAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public PollTableAction(RpsSystemConnectionMemo _memo) {
        this("RPS Polling Control",_memo);
    }

    PollTableFrame f = null;

    RpsSystemConnectionMemo memo = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        if (f == null) {
            f = new PollTableFrame(memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("Exception starting frame.", ex);
            }
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(PollTableAction.class);

}




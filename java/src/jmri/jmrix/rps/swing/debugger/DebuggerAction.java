package jmri.jmrix.rps.swing.debugger;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a DisplayFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class DebuggerAction extends AbstractAction {

    RpsSystemConnectionMemo memo = null;

    public DebuggerAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public DebuggerAction(RpsSystemConnectionMemo _memo) {
        this("RPS Debugger Window", _memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        DebuggerFrame f = new DebuggerFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("starting frame: Exception: ", ex);
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(DebuggerAction.class);

}

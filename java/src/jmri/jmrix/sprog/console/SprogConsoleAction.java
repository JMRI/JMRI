package jmri.jmrix.sprog.console;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogConsoleFrame object.
 *
 * @author	Andrew Crosland Copyright (C) 2008
 */
public class SprogConsoleAction extends AbstractAction {

    private SprogSystemConnectionMemo _memo;

    public SprogConsoleAction(String s, SprogSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SprogConsoleFrame f = new SprogConsoleFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SprogConsoleAction.class);

}

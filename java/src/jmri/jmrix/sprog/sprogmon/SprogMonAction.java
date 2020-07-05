package jmri.jmrix.sprog.sprogmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public class SprogMonAction extends AbstractAction {

    private SprogSystemConnectionMemo _memo = null;

    public SprogMonAction(String s, SprogSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SprogMonFrame
        SprogMonFrame f = new SprogMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SprogMonAction starting SprogMonFrame: Exception: {}", ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SprogMonAction.class);

}

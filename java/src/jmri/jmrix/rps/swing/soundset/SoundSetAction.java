package jmri.jmrix.rps.swing.soundset;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a SoundSetFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class SoundSetAction extends AbstractAction {

    RpsSystemConnectionMemo memo = null;

    public SoundSetAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public SoundSetAction(RpsSystemConnectionMemo _memo) {
        this("RPS Sound Speed Monitor",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        SoundSetFrame f = new SoundSetFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("starting frame: Exception: " + ex.toString());
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(SoundSetAction.class);

}

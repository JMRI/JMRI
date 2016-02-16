// SoundSetAction.java
package jmri.jmrix.rps.swing.soundset;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SoundSetFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class SoundSetAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 3969609830068349700L;

    public SoundSetAction(String s) {
        super(s);
    }

    public SoundSetAction() {
        this("RPS Sound Speed Monitor");
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        SoundSetFrame f = new SoundSetFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("starting frame: Exception: " + ex.toString());
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(SoundSetAction.class.getName());

}


/* @(#)SoundSetAction.java */

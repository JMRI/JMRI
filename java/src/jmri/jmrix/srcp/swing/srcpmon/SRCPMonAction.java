package jmri.jmrix.srcp.swing.srcpmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;

/**
 * Swing action to create and register a SRCPMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class SRCPMonAction extends AbstractAction {

    SRCPSystemConnectionMemo _memo = null;

    public SRCPMonAction(SRCPSystemConnectionMemo memo) {
        this("SRCP Monitor",memo);
    }

    public SRCPMonAction(String s,SRCPSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public void actionPerformed(ActionEvent e) {
        // create a SRCPMonFrame
        SRCPMonFrame f = new SRCPMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SRCPMonAction starting SRCPMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPMonAction.class.getName());

}


/* @(#)SRCPMonAction.java */

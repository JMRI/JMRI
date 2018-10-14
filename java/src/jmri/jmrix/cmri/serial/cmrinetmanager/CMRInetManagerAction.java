package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a CMRInetManagerAction object
 *
 * @author	Chuck Catania Copyright (C) 2014, 2015, 2016, 2017
 */
public class CMRInetManagerAction extends AbstractAction {

    CMRISystemConnectionMemo _memo = null;

    public CMRInetManagerAction(String s, CMRISystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public CMRInetManagerAction(CMRISystemConnectionMemo memo) {
        this("WindowTitle", memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CMRInetManagerFrame f = new CMRInetManagerFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("CMRInetManagerAction-C2: " + ex.toString());
        }
        f.setLocation(20, 40);
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(CMRInetManagerAction.class);
}

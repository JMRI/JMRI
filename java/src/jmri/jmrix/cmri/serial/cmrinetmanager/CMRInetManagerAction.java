package jmri.jmrix.cmri.serial.cmrinetmanager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a
 * CMRInetManagerAction object
 *
 * @author	Chuck Catania    Copyright (C) 2014, 2015, 2016
 */
public class CMRInetManagerAction extends AbstractAction {

    private CMRISystemConnectionMemo _memo = null;

    public CMRInetManagerAction(String s,CMRISystemConnectionMemo memo) { 
        super(s); 
        _memo = memo;
    }

    public CMRInetManagerAction(CMRISystemConnectionMemo memo) {
        this("CMRInet Network Manager",memo);
    }

    public void actionPerformed(ActionEvent e) {
        CMRInetManagerFrame f = new CMRInetManagerFrame(_memo);
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception-C2: "+ex.toString());
            }
        f.setLocation(20,40);
        f.setVisible(true);
    }
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CMRInetManagerFrame.class.getName());
}


/* @(#)CMRInetManagerAction.java */

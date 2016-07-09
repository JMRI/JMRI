package jmri.jmrix.cmri.serial.assignment;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a ListFrame object
 *
 * @author Dave Duchamp Copyright (C) 2006
 */
public class ListAction extends AbstractAction {

    private CMRISystemConnectionMemo _memo = null;

    public ListAction(String s,CMRISystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public ListAction(CMRISystemConnectionMemo memo) {
        this("List C/MRI Assignments",memo);
    }

    public void actionPerformed(ActionEvent e) {
        ListFrame f = new ListFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(ListAction.class.getName());
}

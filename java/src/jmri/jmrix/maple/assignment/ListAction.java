package jmri.jmrix.maple.assignment;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a ListFrame object
 *
 * @author Dave Duchamp Copyright (C) 2006
 */
public class ListAction extends AbstractAction {

    private MapleSystemConnectionMemo _memo = null;

    public ListAction(String s, MapleSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public ListAction(MapleSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemAssignments"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ListFrame f = new ListFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(ListAction.class);

}

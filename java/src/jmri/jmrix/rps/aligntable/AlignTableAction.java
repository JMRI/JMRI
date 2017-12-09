package jmri.jmrix.rps.aligntable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a AlignTableFrame object.
 * <p>
 * Only one frame is used (e.g. it's reused) because notification is not yet
 * completely in place.
 * <p>
 * Note that we only allow one of these right now.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class AlignTableAction extends AbstractAction {

    RpsSystemConnectionMemo memo = null;

    static final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");

    public AlignTableAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public AlignTableAction(RpsSystemConnectionMemo _memo) {
        this(rb.getString("WindowTitle"),_memo);
    }

    AlignTableFrame f;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null) {
            f = new AlignTableFrame(memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.error("Exception: " + ex.toString());
            }
            f.setLocation(100, 30);
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(AlignTableAction.class);
}

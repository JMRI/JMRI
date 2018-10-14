package jmri.jmrix.xpa.swing.xpaconfig;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a XpaConfigureFrame object.
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class XpaConfigureAction extends AbstractAction {
  
    private jmri.jmrix.xpa.XpaSystemConnectionMemo memo = null;

    public XpaConfigureAction(String s,jmri.jmrix.xpa.XpaSystemConnectionMemo m) {
        super(s);
        memo = m;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        XpaConfigureFrame f = new XpaConfigureFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(XpaConfigureAction.class);

}

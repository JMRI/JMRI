package jmri.jmrix.acela.acelamon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an AcelaMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
  *
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaMonAction extends AbstractAction {

    private jmri.jmrix.acela.AcelaSystemConnectionMemo _memo = null;

    public AcelaMonAction(String s, jmri.jmrix.acela.AcelaSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public AcelaMonAction() {
        this(Bundle.getMessage("MonitorXTitle", "Acela"), jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create an AcelaMonFrame
        AcelaMonFrame f = new AcelaMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("AcelaMonAction starting AcelaMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaMonAction.class);

}

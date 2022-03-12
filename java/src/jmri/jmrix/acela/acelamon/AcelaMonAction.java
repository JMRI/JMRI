package jmri.jmrix.acela.acelamon;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;
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
public class AcelaMonAction extends AbstractSystemConnectionAction<AcelaSystemConnectionMemo> {

    public AcelaMonAction(String s, AcelaSystemConnectionMemo memo) {
        super(s, memo);
    }

    public AcelaMonAction() {
        this(Bundle.getMessage("MonitorXTitle", "Acela"), jmri.InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create an AcelaMonFrame
        AcelaMonFrame f = new AcelaMonFrame(getSystemConnectionMemo());
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("AcelaMonAction starting AcelaMonFrame: Exception: {}", ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaMonAction.class);

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(AcelaSystemConnectionMemo.class));
    }

}

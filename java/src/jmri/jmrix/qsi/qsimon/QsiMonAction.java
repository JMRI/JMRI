package jmri.jmrix.qsi.qsimon;

import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import jmri.jmrix.qsi.swing.QsiSystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a QsiMonFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class QsiMonAction extends QsiSystemConnectionAction {

    public QsiMonAction(String s, QsiSystemConnectionMemo memo) {
        super(s, memo);
    }

    public QsiMonAction(QsiSystemConnectionMemo memo) {
        this(Bundle.getMessage("MonitorXTitle", "QSI"), memo);
    }

    public QsiMonAction() {
        this(InstanceManager.getDefault(QsiSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        QsiSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            // create a QsiMonFrame
            QsiMonFrame f = new QsiMonFrame(memo);
            try {
                f.initComponents();
            }
            catch (Exception ex) {
                log.warn("QsiMonAction starting QsiMonFrame: Exception: ", ex);
            }
            f.setVisible(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(QsiMonAction.class);

}

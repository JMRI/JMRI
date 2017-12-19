package jmri.jmrix.qsi.qsimon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a QsiMonFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class QsiMonAction extends AbstractAction {

    private QsiSystemConnectionMemo _memo = null;

    public QsiMonAction(String s, QsiSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public QsiMonAction(QsiSystemConnectionMemo memo) {
        this(java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle")
                .getString("MenuItemCommandMonitor"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a QsiMonFrame
        QsiMonFrame f = new QsiMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("QsiMonAction starting QsiMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(QsiMonAction.class);

}

// QsiMonAction.java
package jmri.jmrix.qsi.qsimon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a QsiMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version	$Revision$
 */
public class QsiMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6725018075130721947L;

    public QsiMonAction(String s) {
        super(s);
    }

    public QsiMonAction() {
        this(java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle")
                .getString("MenuItemCommandMonitor"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a QsiMonFrame
        QsiMonFrame f = new QsiMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("QsiMonAction starting QsiMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(QsiMonAction.class.getName());

}


/* @(#)QsiMonAction.java */

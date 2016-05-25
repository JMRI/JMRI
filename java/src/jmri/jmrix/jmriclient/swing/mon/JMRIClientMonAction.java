// JMRIClientMonAction.java
package jmri.jmrix.jmriclient.swing.mon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a JMRIClientMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class JMRIClientMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -3199061276329508557L;
    private jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo _memo;

    public JMRIClientMonAction(jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        this("JMRICLient Monitor", memo);
    }

    public JMRIClientMonAction(String s, jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public void actionPerformed(ActionEvent e) {
        // create a JMRIClientMonFrame
        JMRIClientMonFrame f = new JMRIClientMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("JMRIClientMonAction starting JMRIClientMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientMonAction.class.getName());

}


/* @(#)SRCPMonAction.java */

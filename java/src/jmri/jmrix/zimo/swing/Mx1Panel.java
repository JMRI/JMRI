package jmri.jmrix.zimo.swing;

import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Mrc panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 */
abstract public class Mx1Panel extends jmri.util.swing.JmriPanel implements Mx1PanelInterface {

    /**
     * make "memo" object available as convenience
     */
    protected Mx1SystemConnectionMemo memo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(Mx1SystemConnectionMemo memo) {
        this.memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof Mx1SystemConnectionMemo) {
            try {
                initComponents((Mx1SystemConnectionMemo) context);
            } catch (Exception e) {
                log.error("Unable to initialize panel", e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1Panel.class);
}

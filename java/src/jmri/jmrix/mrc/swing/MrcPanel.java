package jmri.jmrix.mrc.swing;

import jmri.jmrix.mrc.MrcSystemConnectionMemo;
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
 *
 */
abstract public class MrcPanel extends jmri.util.swing.JmriPanel implements MrcPanelInterface {

    /**
     * make "memo" object available as convenience
     */
    protected MrcSystemConnectionMemo memo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(MrcSystemConnectionMemo memo) {
        this.memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof MrcSystemConnectionMemo) {
            try {
                initComponents((MrcSystemConnectionMemo) context);
            } catch (RuntimeException e) {
                log.error("Unable to initialize components", e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MrcPanel.class);
}

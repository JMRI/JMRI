package jmri.jmrix.rfid.swing;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Rfid panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
abstract public class RfidPanel extends jmri.util.swing.JmriPanel implements RfidPanelInterface {

    /**
     * make "memo" object available as convenience
     */
    protected RfidSystemConnectionMemo memo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(RfidSystemConnectionMemo memo) {
        this.memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof RfidSystemConnectionMemo) {
            try {
                initComponents((RfidSystemConnectionMemo) context);
            } catch (Exception e) {
                log.error("PowerlinePanel initContext failed", e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RfidPanel.class);
}

package jmri.jmrix.can.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Cbus panels.
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.99.2
 */
abstract public class CanPanel extends jmri.util.swing.JmriPanel implements CanPanelInterface {

    /**
     * Make "memo" object available as convenience.
     */
    protected CanSystemConnectionMemo memo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }
    
    /**
     * Prepends Connection UserName, if present, to start of a String.
     * @param initial The Initial String
     * @return The Connection with the initial String appended
     */
    public String prependConnToString(String initial) {
        if (memo != null) {
            StringBuilder buf = new StringBuilder();
            buf.append(memo.getUserName());
            buf.append(" ");
            buf.append(initial);
            return buf.toString();
        }
        return initial;
    }

}

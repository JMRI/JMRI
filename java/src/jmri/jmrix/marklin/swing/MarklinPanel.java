package jmri.jmrix.marklin.swing;

import jmri.jmrix.marklin.MarklinSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Marklin panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.11.3
 */
abstract public class MarklinPanel extends jmri.util.swing.JmriPanel implements MarklinPanelInterface {

    /**
     * make "memo" object available as convenience
     */
    protected MarklinSystemConnectionMemo memo;

    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof MarklinSystemConnectionMemo) {
            initComponents((MarklinSystemConnectionMemo) context);
        }
    }

}

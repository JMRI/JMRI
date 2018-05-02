package jmri.jmrix.tams.swing;

import jmri.jmrix.tams.TamsSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * referetams for Tams panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
abstract public class TamsPanel extends jmri.util.swing.JmriPanel implements TamsPanelInterface {

    /**
     * make "memo" object available as convenietams
     */
    protected TamsSystemConnectionMemo memo;

    @Override
    public void initComponents(TamsSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof TamsSystemConnectionMemo) {
            initComponents((TamsSystemConnectionMemo) context);
        }
    }

}

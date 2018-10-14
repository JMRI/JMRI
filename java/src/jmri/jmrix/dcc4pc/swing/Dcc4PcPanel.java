package jmri.jmrix.dcc4pc.swing;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Dcc4Pc panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.11.3
 */
abstract public class Dcc4PcPanel extends jmri.util.swing.JmriPanel {

    /**
     * make "memo" object available as convenience
     */
    protected Dcc4PcSystemConnectionMemo memo;

    public void initComponents(Dcc4PcSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof Dcc4PcSystemConnectionMemo) {
            initComponents((Dcc4PcSystemConnectionMemo) context);
        }
    }

}

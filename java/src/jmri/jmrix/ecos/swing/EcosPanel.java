package jmri.jmrix.ecos.swing;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Ecos panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.11.3
 */
abstract public class EcosPanel extends jmri.util.swing.JmriPanel implements EcosPanelInterface {

    /**
     * Make "memo" object available as convenience.
     */
    protected EcosSystemConnectionMemo memo;

    @Override
    public void initComponents(EcosSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof EcosSystemConnectionMemo) {
            initComponents((EcosSystemConnectionMemo) context);
        }
    }

}

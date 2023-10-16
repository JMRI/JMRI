package jmri.jmrix.bidib.swing;

import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;

/**
 * Provide access to Swing components for the BiDiB subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Eckart Meyer Copyright (C) 2019
 * @since 4.2.2
 */
public class BiDiBComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public BiDiBComponentFactory(BiDiBSystemConnectionMemo memo) {
        this.memo = memo;
    }

    BiDiBSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     * 
     * @return BiDiBMenuXXX object
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new BiDiBMenu(memo);
    }
}




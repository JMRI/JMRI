// CbusPanel.java
package jmri.jmrix.can.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Cbus panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.99.2
 * @version $Revision: 17977 $
 */
abstract public class CanPanel extends jmri.util.swing.JmriPanel implements CanPanelInterface {

    /**
     *
     */
    private static final long serialVersionUID = 4619397055638527582L;
    /**
     * make "memo" object available as convenience
     */
    protected CanSystemConnectionMemo memo;

    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

}

// MarklinPanel.java
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
 * @version $Revision: 17977 $
 */
abstract public class MarklinPanel extends jmri.util.swing.JmriPanel implements MarklinPanelInterface {

    /**
     *
     */
    private static final long serialVersionUID = -997775203894632617L;
    /**
     * make "memo" object available as convenience
     */
    protected MarklinSystemConnectionMemo memo;

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

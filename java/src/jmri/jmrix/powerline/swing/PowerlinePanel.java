package jmri.jmrix.powerline.swing;

import jmri.jmrix.powerline.SerialSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for Powerline panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4 Copied from Nce.swing Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
abstract public class PowerlinePanel extends jmri.util.swing.JmriPanel implements PowerlinePanelInterface {

    /**
     * make "memo" object available as convenience
     */
    protected SerialSystemConnectionMemo memo;

    @Override
    public void initComponents(SerialSystemConnectionMemo memo) throws Exception {
        this.memo = memo;
    }

    @Override
    public void initContext(Object context) throws Exception {
        if (context instanceof SerialSystemConnectionMemo) {
            try {
                initComponents((SerialSystemConnectionMemo) context);
            } catch (Exception e) {
                //log.error("PowerlinePanel initContext failed");
                e.printStackTrace();
            }
        }
    }

}

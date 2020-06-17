package jmri.jmrix.tams.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.util.swing.JmriPanel;

/**
 * JPanel extension to handle automatic creation of window title and help
 * referetams for Tams panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
abstract public class TamsPanel extends JmriPanel implements TamsPanelInterface, SystemConnectionAction<TamsSystemConnectionMemo> {

    /**
     * make "memo" object available as convenience
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

    @Override
    public TamsSystemConnectionMemo getSystemConnectionMemo() {
        return memo;
    }

    @Override
    public void setSystemConnectionMemo(TamsSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(TamsSystemConnectionMemo.class));
    }
}

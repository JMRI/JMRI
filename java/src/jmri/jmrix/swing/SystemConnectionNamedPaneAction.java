package jmri.jmrix.swing;

import javax.swing.Icon;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.swing.JmriNamedPaneAction;
import jmri.util.swing.WindowInterface;

/**
 * {@link JmriNamedPaneAction} that implements {@link SystemConnectionAction}.
 * 
 * @author Randall Wood Copyright 2020
 * @param <M> the supported subclass of {@link jmri.SystemConnectionMemo}
 */
public abstract class SystemConnectionNamedPaneAction<M extends DefaultSystemConnectionMemo> extends JmriNamedPaneAction implements SystemConnectionAction<M> {

    protected M memo;

    public SystemConnectionNamedPaneAction(String s, String paneClass, M memo) {
        super(s, paneClass);
        this.memo = memo;
    }

    public SystemConnectionNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, M memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }
    
    public SystemConnectionNamedPaneAction(String s, WindowInterface wi, String paneClass, M memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M getSystemConnectionMemo() {
        return memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemConnectionMemo(M memo) {
        if (getSystemConnectionMemoClasses().stream().anyMatch(memo.getClass()::isAssignableFrom)) {
            this.memo = memo;
        } else {
            throw new IllegalArgumentException(memo.getClass() + " is not valid for " + this.getClass());
        }
    }
}

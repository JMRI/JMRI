package jmri.jmrix.swing;

import javax.swing.AbstractAction;
import jmri.SystemConnectionMemo;

/**
 * Base for {@link AbstractAction}s that need to be associated with a specific
 * connection.
 *
 * @author Randall Wood Copyright 2020
 * @param <M> the specific type of SystemConnectionMemo supported by this action
 */
public abstract class AbstractSystemConnectionAction<M extends SystemConnectionMemo> extends AbstractAction implements SystemConnectionAction<M> {

    private transient M memo = null;

    protected AbstractSystemConnectionAction(String name, M memo) {
        super(name);
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
     *
     * @throws IllegalArgumentException if the memo is not assignable from any
     *                                  class listed in
     *                                  {@link #getSystemConnectionMemoClasses()}
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

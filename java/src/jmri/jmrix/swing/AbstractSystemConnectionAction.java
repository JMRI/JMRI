package jmri.jmrix.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Base for {@link AbstractAction}s that need to be associated with a specific
 * connection.
 *
 * @author Randall Wood Copyright 2020
 * @param <M> the specific type of SystemConnectionMemo supported by this action
 */
public abstract class AbstractSystemConnectionAction<M extends SystemConnectionMemo> extends AbstractAction implements SystemConnectionAction {

    private M memo = null;

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
     * <p>
     * Note that this accepts any subclass of SystemConnectionMemo, not just
     * subclasses of M, to avoid compilation problems elsewhere.
     */
    @Override
    public void setSystemConnectionMemo(SystemConnectionMemo memo) throws IllegalArgumentException {
        if (getSystemConnectionMemoClasses().stream().anyMatch(memo.getClass()::isAssignableFrom)) {
            this.memo = (M) memo;
        } else {
            throw new IllegalArgumentException(memo.getClass() + " is not valid for " + this.getClass());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return a set containing one entry allowing any SystemConnectionMemo to
     *         be used
     */
    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(SystemConnectionMemo.class));
    }

}

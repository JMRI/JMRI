package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.implementation.AbstractMemory;

/**
 * A null memory.
 */
public class NullMemory extends AbstractMemory {

    /**
     * Create a new NullMemory instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullMemory(@Nonnull String sys) {
        super(sys);
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported.");
    }

}

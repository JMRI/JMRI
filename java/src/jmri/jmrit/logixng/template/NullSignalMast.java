package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.implementation.AbstractSignalMast;

/**
 * A null signal mast.
 */
public class NullSignalMast extends AbstractSignalMast {

    /**
     * Create a new NullSignalMast instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullSignalMast(@Nonnull String sys) {
        super(sys);
    }

}

package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.implementation.AbstractTurnout;

/**
 * A null turnout.
 */
public class NullTurnout extends AbstractTurnout {

    /**
     * Create a new NullTurnout instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullTurnout(@Nonnull String sys) {
        super(sys);
    }

    @Override
    protected void forwardCommandChangeToLayout(int s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
        throw new UnsupportedOperationException("Not supported.");
    }

}

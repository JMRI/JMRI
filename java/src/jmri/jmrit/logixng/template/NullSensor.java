package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.implementation.AbstractSensor;

/**
 * A null turnout.
 */
public class NullSensor extends AbstractSensor {

    /**
     * Create a new NullTurnout instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullSensor(@Nonnull String sys) {
        super(sys);
    }

    @Override
    public void requestUpdateFromLayout() {
        throw new UnsupportedOperationException("Not supported.");
    }

}

package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.implementation.AbstractLight;

/**
 * A null light.
 */
public class NullLight extends AbstractLight {

    /**
     * Create a new NullLight instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullLight(@Nonnull String sys) {
        super(sys);
    }

}

package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.implementation.AbstractAudio;

/**
 * A null turnout.
 */
public class NullAudio extends AbstractAudio {

    /**
     * Create a new NullTurnout instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullAudio(@Nonnull String sys) {
        super(sys);
    }

    @Override
    public void cleanup() {     // This method is public to allow the test class to access it
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public char getSubType() {
        return 'B';     // B = Buffer
    }

    @Override
    public void stateChanged(int oldState) {
        throw new UnsupportedOperationException("Not supported.");
    }

}

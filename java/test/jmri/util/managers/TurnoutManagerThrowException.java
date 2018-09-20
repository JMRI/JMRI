package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.Turnout;
import jmri.jmrix.internal.InternalTurnoutManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 */
public class TurnoutManagerThrowException extends InternalTurnoutManager {

    public TurnoutManagerThrowException() {
        super("I");
    }
    
    /** {@inheritDoc} */
    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Turnout provideTurnout(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Turnout getTurnout(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Turnout getBySystemName(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Turnout getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Turnout newTurnout(@Nonnull String systemName, @Nullable String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}

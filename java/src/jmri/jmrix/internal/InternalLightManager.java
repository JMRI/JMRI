package jmri.jmrix.internal;

import javax.annotation.Nonnull;
import jmri.Light;
import jmri.NamedBean;
import jmri.implementation.AbstractVariableLight;
import jmri.util.PreferNumericComparator;

/**
 * Implement a LightManager for "Internal" (virtual) lights.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class InternalLightManager extends jmri.managers.AbstractLightManager {

    public InternalLightManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Create and return an internal (no layout connection) Light
     */
    @Override
    protected Light createNewLight(@Nonnull String systemName, String userName) {
        return new AbstractVariableLight(systemName, userName) {

            @Override
            protected void sendIntensity(double intensity) {
                // nothing to do
            }

            @Override
            protected void sendOnOffCommand(int newState) {
                // nothing to do
            }

            @Override
            protected int getNumberOfSteps() {
                return 100;
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public InternalSystemConnectionMemo getMemo() {
        return (InternalSystemConnectionMemo) memo;
    }

    @Override
    public boolean supportsVariableLights(@Nonnull String systemName) {
        return true;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

}

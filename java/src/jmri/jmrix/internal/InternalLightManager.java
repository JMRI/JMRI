package jmri.jmrix.internal;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.Light;
import jmri.NamedBean;
import jmri.implementation.AbstractVariableLight;

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
    protected Light createNewLight(String systemName, String userName) {
        return new AbstractVariableLight(systemName, userName) {

            //protected void forwardCommandChangeToLayout(int s) {}
            @Override
            protected void sendIntensity(double intensity) {
            }

            @Override
            protected void sendOnOffCommand(int newState) {
            }

            @Override
            protected int getNumberOfSteps() {
                return 100;
            }

            @CheckReturnValue
            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
                return suffix1.compareTo(suffix2);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InternalSystemConnectionMemo getMemo() {
        return (InternalSystemConnectionMemo) memo;
    }

    @Override
    public boolean validSystemNameConfig(String systemName) {
        return true;
    }

    @Override
    public boolean supportsVariableLights(String systemName) {
        return true;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
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

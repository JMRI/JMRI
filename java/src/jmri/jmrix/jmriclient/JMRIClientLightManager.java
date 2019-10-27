package jmri.jmrix.jmriclient;

import java.util.Locale;
import jmri.Light;

import javax.annotation.Nonnull;

/**
 * Implement LightManager for JMRIClient systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the light number without padding.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientLightManager extends jmri.managers.AbstractLightManager {

    public JMRIClientLightManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    @Override
    public Light createNewLight(String systemName, String userName) {
        Light t;
        int addr = Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
        t = new JMRIClientLight(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (systemName.startsWith(getSystemNamePrefix())
                && Integer.parseInt(systemName.substring(getSystemNamePrefix().length())) > 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return super.validateIntegerSystemNameFormat(name, 0, Integer.MAX_VALUE, locale);
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false' for now, this method always returns 'true'; it is needed for the
     * Abstract Light class
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

}

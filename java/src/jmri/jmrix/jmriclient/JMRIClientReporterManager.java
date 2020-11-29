package jmri.jmrix.jmriclient;

import javax.annotation.Nonnull;
import jmri.Reporter;

/**
 * Implement reporter manager for JMRIClient systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the reporter number without padding.
 *
 * @author Paul Bender Copyright (C) 2011
 */
public class JMRIClientReporterManager extends jmri.managers.AbstractReporterManager {

    public JMRIClientReporterManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    @Override
    public Reporter createNewReporter(@Nonnull String systemName, String userName) {
        Reporter r;
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        r = new JMRIClientReporter(addr, getMemo());
        r.setUserName(userName);
        return r;
    }
    
    /** 
     * Validates to only numeric system names.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateSystemNameFormatOnlyNumeric(name,locale);
    }

}

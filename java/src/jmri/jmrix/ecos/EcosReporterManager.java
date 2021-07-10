package jmri.jmrix.ecos;

import javax.annotation.Nonnull;
import jmri.Reporter;

/**
 * EcosReporterManager implements the ReporterManager for ECoS
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class EcosReporterManager extends jmri.managers.AbstractReporterManager {

    // ctor has to register for ECoS events
    public EcosReporterManager(EcosSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public EcosSystemConnectionMemo getMemo() {
        return (EcosSystemConnectionMemo) memo;
    }

    @Nonnull
    @Override
    protected Reporter createNewReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Reporter r = new EcosReporter(systemName, userName);
        register(r);
        return r;
    }
    
    /**
     * Validates to contain at least 1 number . . .
     * <p>
     * TODO: Custom validation for EcosReporterManager could be improved.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateTrimmedMin1NumberSystemNameFormat(name,locale);
    }

}

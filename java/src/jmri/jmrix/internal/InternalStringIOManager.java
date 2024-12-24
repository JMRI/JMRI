package jmri.jmrix.internal;

import javax.annotation.Nonnull;

import jmri.StringIO;

import jmri.implementation.DefaultStringIO;

/**
 * Implement a StringIOManager for "Internal" (virtual) StringIOs.
 *
 * @author Bob Jacobsen      Copyright (C) 2009
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class InternalStringIOManager extends jmri.managers.AbstractStringIOManager {

    public InternalStringIOManager(InternalSystemConnectionMemo memo) {
        super(memo);
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
    @Nonnull
    public StringIO provideStringIO(@Nonnull String sName) throws IllegalArgumentException {
        return new DefaultStringIO(sName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public StringIO provide(@Nonnull String name) throws IllegalArgumentException { 
        return provideStringIO(name); 
    }

    @Override
    @Nonnull
    public StringIO createNewStringIO(String sName, String uName) {
        return new DefaultStringIO(sName, uName);
    }
}

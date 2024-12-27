package jmri.jmrix.openlcb;

import javax.annotation.Nonnull;

import jmri.StringIO;
import jmri.implementation.DefaultStringIO;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Implement a StringIOManager for "Internal" (virtual) StringIOs.
 *
 * @author Bob Jacobsen      Copyright (C) 2024
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class OlcbStringIOManager extends jmri.managers.AbstractStringIOManager {

    public OlcbStringIOManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    @Override
    @Nonnull
    public StringIO provideStringIO(@Nonnull String sName) throws IllegalArgumentException {
        return new OlcbStringIO(getMemo(), sName);
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

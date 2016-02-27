/* ProgrammingMode.java */
package jmri;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Denote a single programming mode.
 * <P>
 * User code gets a list of available modes from the {@link Programmer} or
 * {@link AddressedProgrammer} in use. It then selects a mode to use and
 * indicates it via a setMode() call.
 * <p>
 * A ProgrammingMode has a user-readable name to help humans select it.
 * <P>
 * We define a number of modes as constants in
 * {@link jmri.managers.DefaultProgrammerManager} because they are common across
 * so many DCC-specific implementations, they've become defacto standards.
 * <p>
 * More specific definitions (and Bundle entries) should retreat to specific
 * Programmer implementations. The whole idea is to have code deal with the
 * modes that a specific {@link Programmer} provides, not make assumptions about
 * global values.
 *
 * @see jmri.Programmer
 * @see jmri.ProgrammerManager
 * @see jmri.managers.DefaultProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2014
 */
@net.jcip.annotations.Immutable
public final class ProgrammingMode {

    public ProgrammingMode(@NonNull String standardName) {
        this.standardName = standardName;
        this.localName = Bundle.getMessage(standardName);  // note in jmri package
    }

    /*
     * Create an instance where you directly
     * provide the localized name.  
     * <p>
     * This is useful when you want to create a ProgrammingMode deep
     * within some system-specific code, and don't want to add
     * translations to the top-level jmri.Bundle.
     */
    public ProgrammingMode(@NonNull String standardName, @NonNull String localName) {
        this.standardName = standardName;
        this.localName = localName;
    }

    /**
     * Display the localized (human readable) name
     */
    @Override
    public @NonNull String toString() {
        return localName;
    }

    /**
     * Return the standard (not localized, human readable) name
     */
    public @NonNull String getStandardName() {
        return standardName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgrammingMode)) {
            return false;
        }
        ProgrammingMode that = (ProgrammingMode) o;
        return this.standardName.equals(that.standardName);
    }

    @Override
    public int hashCode() {
        return standardName.hashCode();
    }

    private @NonNull final String standardName;
    private @NonNull final String localName;

}

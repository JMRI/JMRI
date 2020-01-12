/* ProgrammingMode.java */
package jmri;

import javax.annotation.Nonnull;

/**
 * Denote a single programming mode.
 * <p>
 * User code gets a list of available modes from the {@link Programmer} or
 * {@link AddressedProgrammer} in use. It then selects a mode to use and
 * indicates it via a setMode() call.
 * <p>
 * A ProgrammingMode has a user-readable name to help humans select it.
 * <p>
 * We define a number of modes as constants because they are common across so
 * many DCC-specific implementations, they've become defacto standards.
 * <p>
 * More specific definitions (and Bundle entries) should retreat to specific
 * Programmer implementations. The whole idea is to have code deal with the
 * modes that a specific {@link Programmer} provides, not make assumptions about
 * global values.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2014
 */
@javax.annotation.concurrent.Immutable
public final class ProgrammingMode {

    /**
     * NMRA "Paged" mode
     */
    public static final ProgrammingMode PAGEMODE = new ProgrammingMode("PAGEMODE");

    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the
     * bit-wise operations
     */
    public static final ProgrammingMode OPSBITMODE = new ProgrammingMode("OPSBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the byte-wise operations and "extended" addressing.
     */
    public static final ProgrammingMode OPSACCEXTBYTEMODE = new ProgrammingMode("OPSACCEXTBYTEMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the bit-wise operations. Note that this is defined as using the "normal",
     * not "extended" addressing.
     */
    public static final ProgrammingMode OPSACCBITMODE = new ProgrammingMode("OPSACCBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the bit-wise operations and "extended" addressing.
     */
    public static final ProgrammingMode OPSACCEXTBITMODE = new ProgrammingMode("OPSACCEXTBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the byte-wise operations. Note that this is defined as using the
     * "normal", not "extended" addressing.
     */
    public static final ProgrammingMode OPSACCBYTEMODE = new ProgrammingMode("OPSACCBYTEMODE");

    /**
     * NMRA "Address-only" mode. Often implemented as a proper subset of
     * "Register" mode, as the underlying operation is the same.
     */
    public static final ProgrammingMode ADDRESSMODE = new ProgrammingMode("ADDRESSMODE");

    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the
     * byte-wise operations.
     */
    public static final ProgrammingMode OPSBYTEMODE = new ProgrammingMode("OPSBYTEMODE");

    /**
     * NMRA "Direct" mode, using only the byte-wise operations.
     */
    public static final ProgrammingMode DIRECTBYTEMODE = new ProgrammingMode("DIRECTBYTEMODE");

    /**
     * NMRA "Register" mode.
     */
    public static final ProgrammingMode REGISTERMODE = new ProgrammingMode("REGISTERMODE");

    /**
     * NMRA "Direct" mode, using only the bit-wise operations.
     */
    public static final ProgrammingMode DIRECTBITMODE = new ProgrammingMode("DIRECTBITMODE");

    /**
     * NMRA "Direct" mode, using both the bit-wise and byte-wise operations.
     */
    public static final ProgrammingMode DIRECTMODE = new ProgrammingMode("DIRECTMODE");

    /**
     * Create a ProgrammingMode instance.
     *
     * @param standardName a name, not to be translated, for internal use
     */
    public ProgrammingMode(@Nonnull String standardName) {
        this.standardName = standardName;
        this.localName = Bundle.getMessage(standardName);  // note in jmri package
    }

    /**
     * Create an instance with a provided localized name.
     * <p>
     * This is useful when creating a ProgrammingMode deep within some
     * system-specific code, and translations cannot be added to the top-level
     * jmri.Bundle.
     *
     * @param standardName a name, not to be translated, for internal use
     * @param localName    a localized, human-readable name for the mode
     */
    public ProgrammingMode(@Nonnull String standardName, @Nonnull String localName) {
        this.standardName = standardName;
        this.localName = localName;
    }

    /**
     * Display the localized (human readable) name.
     *
     * @return the localized name
     */
    @Override
    @Nonnull
    public String toString() {
        return localName;
    }

    /**
     * Return the standard (not localized, human readable) name.
     *
     * @return the standard name
     */
    @Nonnull
    public String getStandardName() {
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

    @Nonnull
    private final String standardName;
    @Nonnull
    private final String localName;

}

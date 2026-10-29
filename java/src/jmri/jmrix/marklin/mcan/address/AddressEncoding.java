package jmri.jmrix.marklin.mcan.address;

import java.util.Objects;

/**
 * Immutable result of a CdB accessory address decode.
 * <p>
 * {@link #format()} is one of {@code "DCC"}, {@code "DCC ext."}, {@code "MM"} or {@code "CAN"}.
 * {@link #address()} is the 1-based user-visible address (except for CAN format, where it is the raw
 * wire value).
 */
public final class AddressEncoding {

    private final String format;
    private final int    address;

    public AddressEncoding(String format, int address) {
        this.format  = Objects.requireNonNull(format, "format");
        this.address = address;
    }

    public String format()  { return format; }
    public int    address() { return address; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddressEncoding)) return false;
        AddressEncoding that = (AddressEncoding) o;
        return address == that.address && format.equals(that.format);
    }

    @Override public int hashCode() { return Objects.hash(format, address); }

    @Override public String toString() { return format + ":" + address; }
}

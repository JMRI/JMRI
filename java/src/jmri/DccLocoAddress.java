package jmri;

/**
 * Encapsulate information for a DCC Locomotive Decoder Address.
 *
 * In particular, this handles the "short" (standard) vs "extended" (long)
 * address selection.
 *
 * An address must be one of these, hence short vs long is encoded as a boolean.
 *
 * Once created, the number and long/short status cannot be changed.
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
@javax.annotation.concurrent.Immutable
public class DccLocoAddress implements LocoAddress {

    public DccLocoAddress(int number, boolean isLong) {
        this.number = number;
        if (isLong) {
            protocol = LocoAddress.Protocol.DCC_LONG;
        } else {
            protocol = LocoAddress.Protocol.DCC_SHORT;
        }
    }

    public DccLocoAddress(int number, LocoAddress.Protocol protocol) {
        this.number = number;
        this.protocol = protocol;
    }

    public DccLocoAddress(DccLocoAddress l) {
        this.number = l.number;
        this.protocol = l.protocol;
    }

    @Override
    public boolean equals(Object a) {
        if (a != null && a.getClass().equals(this.getClass())) {
            try {
                DccLocoAddress other = (DccLocoAddress) a;
                if (this.number != other.number) {
                    return false;
                }
                if (this.protocol != other.protocol) {
                    return false;
                }
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        switch (protocol) {
            case DCC_LONG:
                return (int) (20000 + number & 0xFFFFFFFF);
            case SELECTRIX:
                return (int) (30000 + number & 0xFFFFFFFF);
            case MOTOROLA:
                return (int) (40000 + number & 0xFFFFFFFF);
            case MFX:
                return (int) (50000 + number & 0xFFFFFFFF);
            case M4:
                return (int) (60000 + number & 0xFFFFFFFF);
            case OPENLCB:
                return (int) (70000 + number & 0xFFFFFFFF);
            case LGB:
                return (int) (80000 + number & 0xFFFFFFFF);
            case DCC_SHORT:
            default:
                return (int) (number & 0xFFFFFFFF);
        }
    }

    @Override
    public String toString() {
        switch (protocol) {
            case DCC_SHORT:
                return "" + number + "(S)";
            case DCC_LONG:
                return "" + number + "(L)";
            case SELECTRIX:
                return "" + number + "(SX)";
            case MOTOROLA:
                return "" + number + "(MM)";
            case M4:
                return "" + number + "(M4)";
            case MFX:
                return "" + number + "(MFX)";
            case OPENLCB:
                return "" + number + "(OpenLCB)";
            case LGB:
                return "" + number + "(LGB)";
            default:
                return "" + number + "(D)";
        }
    }

    public boolean isLongAddress() {
        if (protocol == LocoAddress.Protocol.DCC_SHORT) {
            return false;
        }
        return true;
    }

    @Override
    public LocoAddress.Protocol getProtocol() {
        return protocol;
    }

    @Override
    public int getNumber() {
        return (int) number;
    }
    final protected long number;
    final protected LocoAddress.Protocol protocol;
}

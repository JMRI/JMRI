package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * CBUS event frames worked out from the CBUS specification (Developer's Guide
 * 6b, "Accessory events"), for checking the frames simulated detectors send.
 * It is written from the specification, not from JMRI's CBUS code, and must
 * not use {@code CbusAddress}, {@code CbusSensor}, {@code CbusMessage} or
 * {@code CbusOpCodes} (TestArchitectureTest enforces this), so a fault in how
 * JMRI encodes or matches events shows as a difference.
 * <p>
 * A long event is ACON (0x90) or ACOF (0x91) followed by the node number and
 * the event number, each two bytes, high byte first. A short event is ASON
 * (0x98) or ASOF (0x99) followed by the sending node's number and the device
 * number; only the device number identifies it, so the node number bytes
 * are not given here.
 * <p>
 * A sensor address in a JMRI system name is one event, or two separated by
 * ";": "+N1E2" (on event, node 1 event 2), "-N1E2" (its off event), "+7" and
 * "-7" (short events). With one event, the sensor is active on that event
 * and inactive on its opposite; with two, active on the first and inactive
 * on the second. An inverted sensor swaps them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class CbusEventSpec {

    static final int ACON = 0x90;
    static final int ACOF = 0x91;
    static final int ASON = 0x98;
    static final int ASOF = 0x99;

    /** An event: its opcode, node number (long events only) and event number. */
    static final class Event {
        final int opcode;
        final int node;
        final int event;

        Event(int opcode, int node, int event) {
            this.opcode = opcode;
            this.node = node;
            this.event = event;
        }

        boolean isShort() {
            return opcode == ASON || opcode == ASOF;
        }

        Event opposite() {
            switch (opcode) {
                case ACON: return new Event(ACOF, node, event);
                case ACOF: return new Event(ACON, node, event);
                case ASON: return new Event(ASOF, node, event);
                default: return new Event(ASON, node, event);
            }
        }

        /**
         * Does a frame carry this event?
         *
         * @param data the frame's data bytes
         * @return true if it does
         */
        boolean matches(int[] data) {
            if (data.length != 5 || data[0] != opcode) {
                return false;
            }
            if (((data[3] << 8) | data[4]) != event) {
                return false;
            }
            return isShort() || ((data[1] << 8) | data[2]) == node;
        }

        @Override
        public String toString() {
            return String.format("%02X %s E%d", opcode, isShort() ? "short" : "N" + node, event);
        }
    }

    private static final Pattern LONG = Pattern.compile("([+-])N(\\d+)E(\\d+)");
    private static final Pattern SHORT = Pattern.compile("([+-])(\\d+)");

    private CbusEventSpec() {
    }

    private static Event parse(String one) {
        Matcher m = LONG.matcher(one);
        if (m.matches()) {
            return new Event(m.group(1).equals("+") ? ACON : ACOF, Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)));
        }
        m = SHORT.matcher(one);
        if (m.matches()) {
            return new Event(m.group(1).equals("+") ? ASON : ASOF, 0, Integer.parseInt(m.group(2)));
        }
        throw new IllegalArgumentException("not a form these tests use: " + one);
    }

    /**
     * The events that set a sensor active and inactive.
     *
     * @param address  the sensor's address, its system name without the
     *                 connection prefix and type letter
     * @param inverted true if the sensor is inverted
     * @return the active event, then the inactive event
     */
    @Nonnull
    static List<Event> sensorEvents(@Nonnull String address, boolean inverted) {
        String[] parts = address.split(";");
        Event active = parse(parts[0]);
        Event inactive = parts.length > 1 ? parse(parts[1]) : active.opposite();
        List<Event> list = new ArrayList<>();
        list.add(inverted ? inactive : active);
        list.add(inverted ? active : inactive);
        return list;
    }
}

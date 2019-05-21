package jmri.jmrix.openlcb;

import org.openlcb.implementations.BitProducerConsumer;

import jmri.NamedBean;

/**
 * Helper functions used by multiple implementations but specific to JMRI to not go to the
 * OpenLCB.jar.
 *
 * @author Balazs Racz Copyright (C) 2018
 */

public final class OlcbUtils {
    public static final String PROPERTY_IS_AUTHORITATIVE = "IsAuthoritative";
    public static final String PROPERTY_IS_PRODUCER = "IsProducer";
    public static final String PROPERTY_IS_CONSUMER = "IsConsumer";
    public static final String PROPERTY_QUERY_AT_STARTUP = "QueryAtStartup";
    public static final String PROPERTY_LISTEN = "ListenStateMessages";
    public static final String PROPERTY_LISTEN_INVALID = "ListenInvalidStateMessages";

    /**
     * Updates existing flags based on a boolean property.
     *
     * @param flags       previous value of flags
     * @param parent      the parent JMRI object (OlcbTurnout or OlcbSensor)
     * @param propertyKey string identifying the property
     * @param flagValue   the bit to set/clear in the flag. If negative, then will get inverted
     *                    from the found property value.
     * @return new set of flags.
     */
    private static int updateBooleanProperty(int flags, NamedBean parent, String propertyKey, int
            flagValue) {
        Object propValue = parent.getProperty(propertyKey);
        if (propValue == null) return flags;
        if (!(propValue instanceof Boolean)) {
            boolean v = Boolean.valueOf((String)propValue);
            parent.setProperty(propertyKey, v);
            propValue = v;
        }
        boolean prop = (Boolean)propValue;
        if (flagValue < 0) {
            prop = !prop;
            flagValue = -flagValue;
        }
        if (prop) {
            return flags | flagValue;
        } else {
            return flags & (~flagValue);
        }
    }

    /**
     * Checks the NamedBean properties and updates the BitProducerConsumer flags based on them.
     *
     * @param parent       NamedBean (OlcbSensor or OlcbTurnout) whose properties we'll check
     * @param defaultFlags previous value of the flags.
     * @return new value of flags. If not property is set on the object, then == defaultFlags.
     */
    static int overridePCFlagsFromProperties(NamedBean parent, int defaultFlags) {
        int ret = defaultFlags;
        ret = updateBooleanProperty(ret, parent, PROPERTY_IS_AUTHORITATIVE, -BitProducerConsumer
                .SEND_UNKNOWN_EVENT_IDENTIFIED);
        ret = updateBooleanProperty(ret, parent, PROPERTY_IS_PRODUCER, BitProducerConsumer
                .IS_PRODUCER);
        ret = updateBooleanProperty(ret, parent, PROPERTY_IS_CONSUMER, BitProducerConsumer
                .IS_CONSUMER);
        ret = updateBooleanProperty(ret, parent, PROPERTY_QUERY_AT_STARTUP, BitProducerConsumer
                .QUERY_AT_STARTUP);
        ret = updateBooleanProperty(ret, parent, PROPERTY_LISTEN, BitProducerConsumer
                .LISTEN_EVENT_IDENTIFIED);
        ret = updateBooleanProperty(ret, parent, PROPERTY_LISTEN_INVALID, BitProducerConsumer
                .LISTEN_INVALID_STATE);
        return ret;
    }

    /**
     * Tests whether a given NamedBead is an OpenLCB implementation.
     * @param b named bean object (e.g. Turnout object or Sensor object).
     * @return true if it is an Olcb implementation.
     */
    static boolean isOlcbBean(NamedBean b) {
        return b.getClass().getName().contains("Olcb");
    }
}

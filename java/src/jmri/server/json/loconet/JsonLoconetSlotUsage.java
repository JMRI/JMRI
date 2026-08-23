package jmri.server.json.loconet;

/**
 * Constants for the JSON LocoNet slot usage service.
 *
 * FIELD REPORT (Andrew Deak): added to give ESP32/WiFi throttle clients an
 * authoritative answer to "is the command station's loco slot table
 * actually full?" instead of inferring it from link-confirmation retries
 * failing, which can also fail for unrelated reasons (address mismatches,
 * timing) and produce false "slot full" diagnoses.
 */
public class JsonLoconetSlotUsage {

    /**
     * {@value #LOCONET_SLOT_USAGE}
     */
    public static final String LOCONET_SLOT_USAGE = "loconetSlotUsage"; // NOI18N

    private JsonLoconetSlotUsage() {
        // static class, do not instantiate
    }
}

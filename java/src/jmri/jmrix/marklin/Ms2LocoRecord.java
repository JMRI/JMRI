package jmri.jmrix.marklin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import jmri.jmrix.marklin.mcan.address.AddressSpace;

/** MS2 loco list entry and optional full lokinfo.
 *
 * This follows different rules than JMRI's native roster
 * JMRI holds the original text structure, any interpretation is exposed by accessors/getters
 *
 *
 */
public final class Ms2LocoRecord {
    private final String name;
    private final String listText;
    private final String infoText;
    private final Map<String, String> fields;

    public Ms2LocoRecord(String name, String listText, String infoText) {
        this.name = name;
        this.listText = listText;
        this.infoText = infoText;
        Map<String, String> values = new LinkedHashMap<>();
        appendFields(values, "list:", listText);
        appendFields(values, "info:", infoText);
        fields = Collections.unmodifiableMap(values);
    }

    private static void appendFields(Map<String, String> values, String prefix, String text) {
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        for (String line : text.split("\\r?\\n")) {
            String token = line.strip();
            if (token.isEmpty()) {
                continue;
            }
            int equal = token.indexOf('=');
            String key = equal < 0 ? token : token.substring(0, equal);
            String value = equal < 0 ? "" : token.substring(equal + 1);
            int occurrence = occurrences.merge(key, 1, Integer::sum);
            values.put(prefix + key + "[" + occurrence + "]", value);
        }
    }

    public String getName() { return name; }
    public String getListText() { return listText; }
    public String getInfoText() { return infoText; }
    public Map<String, String> getFields() { return fields; }
    public boolean hasInfo() { return !infoText.isEmpty(); }

    public String getField(String token) {
        return fields.getOrDefault("info:" + token + "[1]",
                fields.getOrDefault("list:" + token + "[1]", ""));
    }

    private Optional<Integer> canId() {
        String value = getField(".uid");
        if (value.matches("0[xX][0-9a-fA-F]{1,4}")) {
            return Optional.of(Integer.parseInt(value.substring(2), 16));
        }
        if (value.matches("[0-9]{1,5}")) {
            int number = Integer.parseInt(value);
            if (number <= 0xFFFF) {
                return Optional.of(number);
            }
        }
        return Optional.empty();
    }

    private Optional<AddressSpace> locoSpace() {
        return canId().flatMap(id -> AddressSpace.forCanOffset(id,
                java.util.List.of(AddressSpace.MM_LOCO, AddressSpace.MM_LOCO_20KHZ,
                        AddressSpace.MFX_LOCO, AddressSpace.DCC_LOCO)));
    }

    /** Protocol inferred from the raw locomotive Local-ID, not the decoder subtype. */
    public String getProtocol() {
        return locoSpace().map(space -> {
            switch (space) {
                case MM_LOCO:
                case MM_LOCO_20KHZ: return "MM2";
                case MFX_LOCO: return "MFX";
                case DCC_LOCO: return "DCC";
                default: return "";
            }
        }).orElse("");
    }

    /** Decimal ID within the protocol range; locomotive IDs do not add one. */
    public String getProtocolId() {
        return locoSpace().map(space -> Integer.toString(canId().orElseThrow() - space.base())).orElse("");
    }
}

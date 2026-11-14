package jmri.jmrix.marklin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import jmri.LocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Imports a locomotive read from an MS2 into the JMRI roster.
 * <p>
 * Motorola and DCC locomotives are matched to existing roster entries by protocol and address.
 * A matching entry keeps its ID and other data, including decoder CVs. Only the origin and MS2
 * name attributes are refreshed. Otherwise a new entry named after the MS2 locomotive is created.
 * mfx locomotives are not imported because their address is a changeable binding, not an
 * identity. Mfx support must be managed by a service which goes beyond this importer.
 */
public final class Ms2RosterImporter {

    /** Custom Roster attribute for the system prefix of the importing Märklin connection. */
    public static final String ATTRIBUTE_CONNECTION = "MarklinConnection"; // NOI18N
    /** Roster attribute holding the UID of the MS2 the locomotive was read from. */
    public static final String ATTRIBUTE_MS2_UID = "MarklinMs2Uid"; // NOI18N
    /** Custom Roster attribute mirroring the locomotive name shown on the MS2. Refreshed on every import. */
    public static final String ATTRIBUTE_MS2_NAME = "MarklinMs2Name"; // NOI18N

    /** Outcome of a successful import. */
    public enum Outcome { CREATED, UPDATED }

    /** Result of a successful import. */
    public static final class Result {
        private final Outcome outcome;
        private final RosterEntry entry;

        Result(Outcome outcome, RosterEntry entry) {
            this.outcome = outcome;
            this.entry = entry;
        }

        public Outcome getOutcome() { return outcome; }
        public RosterEntry getEntry() { return entry; }
    }

    /** A locomotive that cannot be imported. Provide a localized reason. */
    public static final class ImportException extends Exception {
        ImportException(String message) {
            super(message);
        }
    }

    private final Roster roster;

    public Ms2RosterImporter(@Nonnull Roster roster) {
        this.roster = roster;
    }

    /**
     * Whether the record has a protocol and address this importer supports.
     *
     * @param record MS2 locomotive record
     * @return true for Motorola and DCC locomotives with a non-zero address
     */
    public static boolean isImportable(@Nonnull Ms2LocoRecord record) {
        return protocolFamily(record) != null && address(record) > 0;
    }

    /**
     * Create or update the roster entry for an MS2 locomotive.
     *
     * @param record       locomotive read from the MS2
     * @param ms2Uid       UID of the MS2 the record was read from, e.g. {@code 0x4D533272}
     * @param systemPrefix system prefix of the Märklin connection
     * @return the created or updated entry
     * @throws ImportException if the record is not importable or the match is ambiguous
     */
    public Result importLoco(@Nonnull Ms2LocoRecord record, @Nonnull String ms2Uid,
            @Nonnull String systemPrefix) throws ImportException {
        Set<LocoAddress.Protocol> family = protocolFamily(record);
        int address = address(record);
        if (family == null || address <= 0) {
            throw new ImportException(Bundle.getMessage("Ms2ImportUnsupported",
                    record.getName(), record.getProtocol()));
        }
        String number = Integer.toString(address);
        List<RosterEntry> matches = roster.getAllEntries().stream()
                .filter(e -> number.equals(e.getDccAddress()) && family.contains(e.getProtocol()))
                .collect(Collectors.toList());
        if (matches.size() > 1) {
            // Prefer an entry imported earlier from the same connection and MS2.
            List<RosterEntry> tagged = matches.stream()
                    .filter(e -> systemPrefix.equals(e.getAttribute(ATTRIBUTE_CONNECTION))
                            && ms2Uid.equals(e.getAttribute(ATTRIBUTE_MS2_UID)))
                    .collect(Collectors.toList());
            if (tagged.size() != 1) {
                throw new ImportException(Bundle.getMessage("Ms2ImportAmbiguous", record.getName(),
                        matches.stream().map(RosterEntry::getId).collect(Collectors.joining(", "))));
            }
            matches = tagged;
        }
        if (matches.size() == 1) {
            RosterEntry entry = matches.get(0);
            tag(entry, record, ms2Uid, systemPrefix);
            entry.changeDateUpdated();
            entry.updateFile();
            roster.writeRoster();
            return new Result(Outcome.UPDATED, entry);
        }
        RosterEntry entry = new RosterEntry();
        entry.setId(uniqueId(record.getName().isBlank() ? record.getProtocol() + " " + number
                : record.getName().strip()));
        entry.setDccAddress(number);
        entry.setProtocol(protocol(family, address));
        tag(entry, record, ms2Uid, systemPrefix);
        entry.ensureFilenameExists();
        roster.addEntry(entry);
        entry.writeFile(null, null);
        roster.writeRoster();
        return new Result(Outcome.CREATED, entry);
    }

    private static void tag(RosterEntry entry, Ms2LocoRecord record, String ms2Uid, String systemPrefix) {
        entry.putAttribute(ATTRIBUTE_CONNECTION, systemPrefix);
        entry.putAttribute(ATTRIBUTE_MS2_UID, ms2Uid);
        entry.putAttribute(ATTRIBUTE_MS2_NAME, record.getName());
    }

    private String uniqueId(String base) {
        String id = base;
        for (int i = 2; roster.getEntryForId(id) != null; i++) {
            id = base + " (" + i + ")";
        }
        return id;
    }

    private static Set<LocoAddress.Protocol> protocolFamily(Ms2LocoRecord record) {
        switch (record.getProtocol()) {
            case "MM2":
                return Set.of(LocoAddress.Protocol.MOTOROLA);
            case "DCC":
                return Set.of(LocoAddress.Protocol.DCC, LocoAddress.Protocol.DCC_SHORT,
                        LocoAddress.Protocol.DCC_LONG);
            default:
                return null;
        }
    }

    private static LocoAddress.Protocol protocol(Set<LocoAddress.Protocol> family, int address) {
        if (family.contains(LocoAddress.Protocol.MOTOROLA)) {
            return LocoAddress.Protocol.MOTOROLA;
        }
        return MarklinThrottleManager.isLongAddress(address)
                ? LocoAddress.Protocol.DCC_LONG : LocoAddress.Protocol.DCC_SHORT;
    }

    private static int address(Ms2LocoRecord record) {
        String id = record.getProtocolId();
        return id.isEmpty() ? -1 : Integer.parseInt(id);
    }
}

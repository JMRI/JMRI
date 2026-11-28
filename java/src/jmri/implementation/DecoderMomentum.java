package jmri.implementation;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jdom2.Element;

import jmri.BasicRosterEntry;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * How long a loco's decoder takes to slow from full speed to a stop, from the
 * deceleration rate (CV4) stored in its roster entry's file, as DecoderPro
 * saves it once the CV has been read or written.
 * <p>
 * By NMRA S-9.2.2, CV4 times 0.896 seconds, divided by the number of speed
 * steps in use, is the time per step, so from full speed to a stop takes
 * CV4 times 0.896 seconds whatever the speed step mode.
 * <p>
 * Each roster entry's file is read once, the first time it is asked about;
 * a CV changed later is used once JMRI is restarted.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class DecoderMomentum {

    /** Seconds per unit of CV4, from full speed to a stop. */
    static final double SECONDS_PER_UNIT = 0.896;

    /** What is known of each roster entry: its time, or NaN if none is stored. */
    private static final Map<BasicRosterEntry, Double> KNOWN = Collections.synchronizedMap(new WeakHashMap<>());

    private DecoderMomentum() {
    }

    /**
     * @param entry a loco's roster entry
     * @return the seconds its decoder takes from full speed to a stop, or null
     *         if no deceleration rate is stored for it
     */
    @CheckForNull
    static Double stoppingSeconds(@Nonnull BasicRosterEntry entry) {
        double seconds = KNOWN.computeIfAbsent(entry, e -> {
            Integer cv4 = e instanceof RosterEntry ? readCv4((RosterEntry) e) : null;
            return cv4 == null ? Double.NaN : cv4 * SECONDS_PER_UNIT;
        });
        return Double.isNaN(seconds) ? null : seconds;
    }

    /**
     * Use a time for a roster entry instead of its file's: for tests.
     *
     * @param entry   the roster entry
     * @param seconds the time from full speed to a stop, or null for none
     *                stored
     */
    static void setForTesting(@Nonnull BasicRosterEntry entry, @CheckForNull Double seconds) {
        KNOWN.put(entry, seconds != null ? seconds : Double.valueOf(Double.NaN));
    }

    @CheckForNull
    private static Integer readCv4(@Nonnull RosterEntry entry) {
        String name = entry.getFileName();
        if (name == null || name.isEmpty()) {
            return null;
        }
        File file = new File(Roster.getDefault().getRosterFilesLocation() + name);
        if (!file.exists()) {
            return null;
        }
        try {
            Element root = new XmlFile() {
            }.rootFromFile(file);
            Element loco = root.getChild("locomotive");
            Element values = loco == null ? null : loco.getChild("values");
            if (values == null) {
                return null;
            }
            for (Element cv : values.getChildren("CVvalue")) {
                if ("4".equals(cv.getAttributeValue("name"))) {
                    String value = cv.getAttributeValue("value");
                    return value == null ? null : Integer.valueOf(value.trim());
                }
            }
        } catch (java.io.IOException | org.jdom2.JDOMException | NumberFormatException e) {
            log.warn("Could not read the deceleration rate (CV4) of roster entry {}", entry.getId(), e);
        }
        return null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecoderMomentum.class);
}

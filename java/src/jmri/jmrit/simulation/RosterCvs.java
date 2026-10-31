package jmri.jmrit.simulation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import org.jdom2.Element;

/**
 * Reads CV values stored in a roster entry's file, as DecoderPro saves them.
 * <p>
 * The roster file is read directly with {@link XmlFile}: JMRI's own way of
 * loading CVs from a roster entry ({@code RosterEntry.loadCvModel}) fills a
 * {@code CvTableModel}, a Swing table model tied to a programmer and to error
 * dialogs, which a simulation running without a GUI can't use.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class RosterCvs {

    private RosterCvs() {
    }

    /**
     * @param entry the roster entry
     * @return the stored CV values by CV number; empty if none can be read
     */
    @Nonnull
    static Map<String, Integer> read(@Nonnull RosterEntry entry) {
        Map<String, Integer> result = new HashMap<>();
        String name = entry.getFileName();
        if (name == null || name.isEmpty()) {
            return result;
        }
        File file = new File(Roster.getDefault().getRosterFilesLocation() + name);
        if (!file.exists()) {
            return result;
        }
        try {
            Element root = new XmlFile() {
            }.rootFromFile(file);
            Element loco = root.getChild("locomotive");
            Element values = (loco == null) ? null : loco.getChild("values");
            if (values == null) {
                return result;
            }
            for (Element cv : values.getChildren("CVvalue")) {
                String number = cv.getAttributeValue("name");
                String value = cv.getAttributeValue("value");
                if (number != null && value != null) {
                    try {
                        result.put(number, Integer.valueOf(value.trim()));
                    } catch (NumberFormatException e) {
                        log.debug("CV {} of {} is not a number: {}", number, entry.getId(), value);
                    }
                }
            }
        } catch (java.io.IOException | org.jdom2.JDOMException e) {
            log.warn("Could not read the CVs of roster entry {}", entry.getId(), e);
        }
        return result;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RosterCvs.class);
}

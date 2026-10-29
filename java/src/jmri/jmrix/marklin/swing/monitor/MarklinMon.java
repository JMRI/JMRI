package jmri.jmrix.marklin.swing.monitor;

import jmri.jmrix.marklin.MarklinMessageFormatter;
import jmri.jmrix.marklin.MarklinReply;

/**
 * Class to convert Marklin Can bus messages to a human readable form.
 * Uses {@link MarklinMessageFormatter} for message formatting with i18n support.
 */
public class MarklinMon {

    // class only supplies static methods
    private MarklinMon(){}

    /**
     * Display a MarklinReply message in human-readable format.
     *
     * @param r the MarklinReply to display
     * @return formatted message string with i18n support
     */
    public static String displayReply(MarklinReply r) {
        // Convert MarklinReply to raw data array
        int[] rawData = new int[13];
        for (int i = 0; i < 13; i++) {
            rawData[i] = r.getElement(i);
        }

        // Use MarklinMessageFormatter with Bundle i18n provider
        return MarklinMessageFormatter.formatRaw(rawData, new MarklinMessageFormatter.I18nProvider() {
            @Override
            public String getMessage(String key, Object... args) {
                try {
                    if (args.length == 0) {
                        return Bundle.getMessage(key);
                    } else {
                        return Bundle.getMessage(key, args);
                    }
                } catch (Exception e) {
                    // If Bundle doesn't have the key, return null to use English fallback
                    return null;
                }
            }
        });
    }
}

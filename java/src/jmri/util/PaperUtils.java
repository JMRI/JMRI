package jmri.util;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;

import java.awt.Dimension;
import java.util.Locale;
import java.util.Set;

enum PaperSize {
    A4,
    Letter,
    Legal
}

public class PaperUtils {
    // Countries primarily using US Letter
    private static final Set<String> LETTER_COUNTRIES = Set.of("US", "CA", "MX", "PH", "PR");

    private static PaperSize cachedPaperSize = null;

    private static final int pointsPerInch = 72;

    /**
     * Returns the default paper size as a Dimension object in points
     *
     * @return The Dimension object representing the default paper size in
     *         points
     */
    public static Dimension getPaperSizeDimension() {
        PaperSize paperSize = getPaperSize();
        if (paperSize.equals(PaperSize.Letter)) {
            return new Dimension((int) (8.5 * pointsPerInch), (int) (11.0 * pointsPerInch));
        }
        if (paperSize.equals(PaperSize.Legal)) {
            return new Dimension((int) (8.5 * pointsPerInch), (int) (14.0 * pointsPerInch));
        }
        return new Dimension((int) (8.27 * pointsPerInch), (int) (11.69 * pointsPerInch));
    }

    /**
     * Returns the default paper size as a PaperSize enum
     * 
     * @return A PaperSize of either A4, Letter, or Legal.
     */
    public static PaperSize getPaperSize() {
        if (cachedPaperSize != null) {
            return cachedPaperSize;
        }
        cachedPaperSize = getAutomaticPaperSize();
        return cachedPaperSize;
    }

    /**
     * Returns the default paper size as a PaperSize enum. This interrogates the default
     * printer and may be slow if the printer is not available. You should
     * probably use {@link #getPaperSize()} instead as it caches the result.
     * 
     * @return A string of either A4, Letter, or Legal.
     */
    public static PaperSize getAutomaticPaperSize() {
        // Try Printer Discovery
        try {
            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultService != null) {
                Object defaultMedia = defaultService.getDefaultAttributeValue(Media.class);

                if (defaultMedia instanceof MediaSizeName) {
                    MediaSizeName msn = (MediaSizeName) defaultMedia;
                    String name = msn.toString().toLowerCase();

                    if (name.contains("letter")) {
                        return PaperSize.Letter;
                    } else if (name.contains("a4")) {
                        return PaperSize.A4;
                    } else if (name.contains("legal")) {
                        return PaperSize.Legal;
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail to fallback if print services are unavailable/malfunctioning
        }

        // Fallback to System Locale
        String country = Locale.getDefault().getCountry().toUpperCase();
        if (LETTER_COUNTRIES.contains(country)) {
            return PaperSize.Letter;
        }

        // Final Default (Global Standard)
        return PaperSize.A4;
    }
}
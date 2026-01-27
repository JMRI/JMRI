package jmri.util;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.PageAttributes;
import java.awt.PageAttributes.MediaType;
import java.util.Locale;
import java.util.Set;

public class PaperUtils {
    // Countries primarily using US Letter
    private static final Set<String> LETTER_COUNTRIES = Set.of("US", "CA", "MX", "PH", "PR");

    private static Dimension cachedPaperSize = null;

    private static final int pointsPerInch = 72;

    /**
     * Returns the default paper size as a Dimension object in points
     *
     * @return The Dimension object representing the default paper size in
     *         points
     */
    public static Dimension getPaperSizeDimension() {
        if (cachedPaperSize == null) {
            cachedPaperSize = getAutomaticPaperSize();
        }
        return cachedPaperSize;
    }

    /**
     * Retrieves the default printer's paper size in points (1/72"), adjusted
     * for the current default orientation. If this is not possible, returns
     * null.
     * 
     * @return The Dimension object representing the default paper size in
     *         points, or null if it is not possible to retrieve the paper size.
     */
    public static Dimension getOrientedPaperSizeInPointsIfPossible() {
        try {
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();
            if (service == null) {
                log.warn("No default printer found");
                return null;
            }

            // Get Physical Paper Dimensions
            Media mediaName = (MediaSizeName) service.getDefaultAttributeValue(Media.class);

            // Handle missing default media name
            if (mediaName == null) {
                Object supported = service.getSupportedAttributeValues(Media.class, null, null);
                if (supported instanceof Media[] && ((Media[]) supported).length > 0) {
                    mediaName = ((Media[]) supported)[0];
                }
            }

            if (!(mediaName instanceof MediaSizeName)) {
                return null;
            }

            MediaSize size = MediaSize.getMediaSizeForName((MediaSizeName) mediaName);

            if (size == null) {
                return null;
            }

            int finalWidth = (int) (size.getX(MediaSize.INCH) * 72);
            int finalHeight = (int) (size.getY(MediaSize.INCH) * 72);

            // Get Orientation and Swap if necessary
            OrientationRequested orient =
                    (OrientationRequested) service.getDefaultAttributeValue(OrientationRequested.class);

            // LANDSCAPE and REVERSE_LANDSCAPE mean we swap width and height
            if (orient == OrientationRequested.LANDSCAPE || orient == OrientationRequested.REVERSE_LANDSCAPE) {
                int temp = finalWidth;
                finalWidth = finalHeight;
                finalHeight = temp;
            }

            return new Dimension(finalWidth, finalHeight);
        } catch (Exception e) {
            // Handle silently
        }

        return null;
    }

    /**
     * Returns the default paper size as a PaperSize enum. This interrogates the
     * default printer and may be slow if the printer is not available. You
     * should probably use {@link #getPaperSizeDimension()} instead as it caches
     * the result.
     * 
     * @return A Dimension object representing the default paper size in points.
     */
    public static Dimension getAutomaticPaperSize() {
        // Try Printer Discovery
        Dimension size = getOrientedPaperSizeInPointsIfPossible();

        if (size != null) {
            return size;
        }

        // Fallback to System Locale
        String country = Locale.getDefault().getCountry().toUpperCase();
        if (LETTER_COUNTRIES.contains(country)) {
            return new Dimension((int) (8.5 * pointsPerInch), (int) (11.0 * pointsPerInch));
        }

        // Final Default (Global Standard)
        return new Dimension((int) (8.27 * pointsPerInch), (int) (11.69 * pointsPerInch));
    }

    /**
     * Syncs the PageAttributes object to the default printer's settings.
     * 
     * @param pageAttr The PageAttributes object to sync.
     */
    public static void syncPageAttributesToPrinter(PageAttributes pageAttr) {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service != null) {
            Media media = (Media) service.getDefaultAttributeValue(Media.class);

            if (media != null && media instanceof MediaSizeName) {
                // Map the javax.print MediaSizeName to AWT PageAttributes.MediaType
                if (media.equals(MediaSizeName.NA_LETTER)) {
                    pageAttr.setMedia(MediaType.NA_LETTER);
                } else if (media.equals(MediaSizeName.NA_LEGAL)) {
                    pageAttr.setMedia(MediaType.NA_LEGAL);
                } else if (media.equals(MediaSizeName.ISO_A4)) {
                    pageAttr.setMedia(MediaType.ISO_A4);
                } else {
                    log.warn("Unsupported media: {}", media);
                }
            }
        } else {
            log.warn("No default printer found");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PaperUtils.class);

}
package jmri.jmrit.operations.trains;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JComboBox;

/**
 * Train print utilities.
 *
 * @author Daniel Boudreau (C) 2025
 */
public class TrainPrintUtilities {

    public static JComboBox<String> getPrinterJComboBox() {
        JComboBox<String> box = new JComboBox<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : services) {
            box.addItem(printService.getName());
        }

        // Set to default printer
        box.setSelectedItem(getDefaultPrinterName());

        return box;
    }

    public static String getDefaultPrinterName() {
        if (PrintServiceLookup.lookupDefaultPrintService() != null) {
            return PrintServiceLookup.lookupDefaultPrintService().getName();
        }
        return ""; // no default printer specified
    }
}

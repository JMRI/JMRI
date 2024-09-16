package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Action to print a summary of car loads ordered by car type.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2011
 */
public class PrintCarLoadsAction extends AbstractAction {

    public PrintCarLoadsAction(boolean isPreview) {
        super(isPreview ? Bundle.getMessage("MenuItemCarLoadsPreview") : Bundle.getMessage("MenuItemCarLoadsPrint"));
        _isPreview = isPreview;
    }

    /**
     * Frame hosting the printing
     */
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean _isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        new CarLoadPrintOption();
    }

    public class CarLoadPrintOption {

        static final String TAB = "\t"; // NOI18N
        static final String NEW_LINE = "\n"; // NOI18N

        // no frame needed for now
        public CarLoadPrintOption() {
            super();
            printCars();
        }

        private void printCars() {

            // obtain a HardcopyWriter to do this
            try ( HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleCarLoads"), Control.reportFontSize, .5,
                            .5, .5, .5, _isPreview); ){

                // Loop through the Roster, printing as needed
                String[] carTypes = InstanceManager.getDefault(CarTypes.class).getNames();
                Hashtable<String, List<CarLoad>> list = InstanceManager.getDefault(CarLoads.class).getList();

                String header = Bundle.getMessage("Type") +
                        TAB +
                        tabString(Bundle.getMessage("Load"),
                                InstanceManager.getDefault(CarLoads.class).getMaxNameLength() + 1) +
                        Bundle.getMessage("Type") +
                        "  " +
                        Bundle.getMessage("Priority") +
                        "  " +
                        tabString(Bundle.getMessage("Hazardous"), 4) +
                        Bundle.getMessage("LoadPickupMessage") +
                        "   " +
                        Bundle.getMessage("LoadDropMessage") +
                        NEW_LINE;
                writer.write(header);
                for (String carType : carTypes) {
                    List<CarLoad> carLoads = list.get(carType);
                    if (carLoads == null) {
                        continue;
                    }
                    boolean printType = true;
                    for (CarLoad carLoad : carLoads) {
                        // don't print out default load or empty
                        if ((carLoad.getName()
                                .equals(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName()) ||
                                carLoad.getName()
                                        .equals(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName())) &&
                                carLoad.getPickupComment().equals(CarLoad.NONE) &&
                                carLoad.getDropComment().equals(CarLoad.NONE) &&
                                carLoad.getPriority().equals(CarLoad.PRIORITY_LOW) &&
                                !carLoad.isHazardous()) {
                            continue;
                        }
                        // print the car type once
                        if (printType) {
                            writer.write(carType + NEW_LINE);
                            printType = false;
                        }
                        StringBuffer buf = new StringBuffer(TAB);
                        buf.append(tabString(carLoad.getName(),
                                InstanceManager.getDefault(CarLoads.class).getMaxNameLength() + 1));
                        buf.append(tabString(carLoad.getLoadType(), 6)); // load
                                                                         // or
                                                                         // empty
                        buf.append(tabString(carLoad.getPriority(), 5)); // low
                                                                         // med
                                                                         // high
                        buf.append(tabString(carLoad.isHazardous()? Bundle.getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"), 4));
                        buf.append(tabString(carLoad.getPickupComment(), 27));
                        buf.append(tabString(carLoad.getDropComment(), 27));
                        writer.write(buf.toString() + NEW_LINE);
                    }
                }
                // and force completion of the printing
//                writer.close(); not needed when using try / catch
            } catch (HardcopyWriter.PrintCanceledException ex) {
                log.debug("Print cancelled");
            } catch (IOException ex) {
                log.error("Error printing car roster: {}", ex.getLocalizedMessage());
            }
        }
    }

    private static String tabString(String s, int fieldSize) {
        if (s.length() > fieldSize) {
            s = s.substring(0, fieldSize - 1);
        }
        StringBuffer buf = new StringBuffer(s + " ");
        while (buf.length() < fieldSize) {
            buf.append(" ");
        }
        return buf.toString();
    }

    private final static Logger log = LoggerFactory.getLogger(PrintCarLoadsAction.class);
}

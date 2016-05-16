// PrintCarLoadsAction.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of car loads ordered by car type.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision$
 */
public class PrintCarLoadsAction extends AbstractAction {

    CarManager manager = CarManager.instance();

    public PrintCarLoadsAction(String actionName, boolean preview, Component pWho) {
        super(actionName);
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

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
            HardcopyWriter writer = null;
            Frame mFrame = new Frame();
            try {
                writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleCarLoads"), Control.reportFontSize, .5, .5, .5, .5, isPreview);
            } catch (HardcopyWriter.PrintCanceledException ex) {
                log.debug("Print cancelled");
                return;
            }

            // Loop through the Roster, printing as needed
            String[] carTypes = CarTypes.instance().getNames();
            Hashtable<String, List<CarLoad>> list = CarLoads.instance().getList();
            try {
                String s = Bundle.getMessage("Type") + TAB
                        + tabString(Bundle.getMessage("Load"), CarLoads.instance().getMaxNameLength() + 1)
                        + Bundle.getMessage("Type") + "  " + Bundle.getMessage("Priority") + "  "
                        + Bundle.getMessage("LoadPickupMessage") + "   " + Bundle.getMessage("LoadDropMessage")
                        + NEW_LINE;
                writer.write(s);
                for (String carType : carTypes) {
                    List<CarLoad> carLoads = list.get(carType);
                    if (carLoads == null) {
                        continue;
                    }
                    boolean printType = true;
                    for (CarLoad carLoad : carLoads) {
                        // don't print out default load or empty
                        if ((carLoad.getName().equals(CarLoads.instance().getDefaultEmptyName()) || carLoad.getName()
                                .equals(CarLoads.instance().getDefaultLoadName()))
                                && carLoad.getPickupComment().equals(CarLoad.NONE)
                                && carLoad.getDropComment().equals(CarLoad.NONE)
                                && carLoad.getPriority().equals(CarLoad.PRIORITY_LOW)) {
                            continue;
                        }
                        // print the car type once
                        if (printType) {
                            writer.write(carType + NEW_LINE);
                            printType = false;
                        }
                        StringBuffer buf = new StringBuffer(TAB);
                        buf.append(tabString(carLoad.getName(), CarLoads.instance().getMaxNameLength() + 1));
                        buf.append(tabString(carLoad.getLoadType(), 6)); // load or empty
                        buf.append(tabString(carLoad.getPriority(), 5)); // low or high
                        buf.append(tabString(carLoad.getPickupComment(), 27));
                        buf.append(tabString(carLoad.getDropComment(), 27));
                        writer.write(buf.toString() + NEW_LINE);
                    }
                }
            } catch (IOException we) {
                log.error("Error printing car roster");
            }
            // and force completion of the printing
            writer.close();
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

    private final static Logger log = LoggerFactory.getLogger(PrintCarLoadsAction.class.getName());
}

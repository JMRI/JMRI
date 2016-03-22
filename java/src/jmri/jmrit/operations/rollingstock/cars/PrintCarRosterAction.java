// PrintCarRosterAction.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * MacIntosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013, 2014
 * @version $Revision$
 */
public class PrintCarRosterAction extends AbstractAction {

    public PrintCarRosterAction(String actionName, Frame frame, boolean preview, CarsTableFrame pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    CarsTableFrame panel;
    CarPrintOptionFrame cpof = null;

    public void actionPerformed(ActionEvent e) {
        if (cpof == null) {
            cpof = new CarPrintOptionFrame(this);
        } else {
            cpof.setVisible(true);
        }
        cpof.initComponents();
    }

    int numberCharPerLine;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    private void printCars() {

        boolean landscape = false;
        if (manifestOrientationComboBox.getSelectedItem() != null
                && manifestOrientationComboBox.getSelectedItem() == Setup.LANDSCAPE) {
            landscape = true;
        }
        
        int fontSize = (int) fontSizeComboBox.getSelectedItem();

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleCarRoster"), fontSize, .5, .5, .5, .5, isPreview, "",
                    landscape, true, null);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        numberCharPerLine = writer.getCharactersPerLine();

        // Loop through the Roster, printing as needed
        String location = "";
        String number;
        String road;
        String type;
        String length = "";
        String weight = "";
        String color = "";
        String owner = "";
        String built = "";
        String load = "";
        String kernel = "";
        String train = "";
        String destination = "";
        String finalDestination = "";
        String returnWhenEmpty = "";
        String value = "";
        String rfid = "";
        String last = "";
        String wait = "";
        String schedule = "";
        String comment = "";

        try {
            printTitleLine(writer);
            String previousLocation = null;
            List<RollingStock> cars = panel.carsTableModel.getCarList(sortByComboBox.getSelectedIndex());
            for (RollingStock rs : cars) {
                Car car = (Car) rs;
                if (printCarsWithLocation.isSelected() && car.getLocation() == null) {
                    continue; // car doesn't have a location skip
                }
                location = "";
                destination = "";
                finalDestination = "";
                returnWhenEmpty = "";

                if (printCarLocation.isSelected()) {
                    if (car.getLocation() != null) {
                        location = car.getLocationName().trim() + " - " + car.getTrackName().trim();
                    }
                    location = padAttribute(location, LocationManager.instance().getMaxLocationAndTrackNameLength() + 3);
                }
                // Page break between locations?
                if (previousLocation != null && !car.getLocationName().trim().equals(previousLocation)
                        && printPage.isSelected()) {
                    writer.pageBreak();
                    printTitleLine(writer);
                } // Add a line between locations?
                else if (previousLocation != null && !car.getLocationName().trim().equals(previousLocation)
                        && printSpace.isSelected()) {
                    writer.write(NEW_LINE);
                }
                previousLocation = car.getLocationName().trim();

                // car number
                number = padAttribute(car.getNumber().trim(), Control.max_len_string_print_road_number);
                // car road
                road = padAttribute(car.getRoadName().trim(), CarRoads.instance().getMaxNameLength());
                // car type
                type = padAttribute(car.getTypeName().trim(), CarTypes.instance().getMaxFullNameLength());

                if (printCarLength.isSelected()) {
                    length = padAttribute(car.getLength().trim(), Control.max_len_string_length_name);
                }
                if (printCarWeight.isSelected()) {
                    weight = padAttribute(car.getWeight().trim(), Control.max_len_string_weight_name);
                }
                if (printCarColor.isSelected()) {
                    color = padAttribute(car.getColor().trim(), CarColors.instance().getMaxNameLength());
                }
                if (printCarLoad.isSelected()) {
                    load = padAttribute(car.getLoadName().trim(), CarLoads.instance().getMaxNameLength());
                }
                if (printCarKernel.isSelected()) {
                    kernel = padAttribute(car.getKernelName().trim(), Control.max_len_string_attibute);
                }
                if (printCarOwner.isSelected()) {
                    owner = padAttribute(car.getOwner().trim(), CarOwners.instance().getMaxNameLength());
                }
                if (printCarBuilt.isSelected()) {
                    built = padAttribute(car.getBuilt().trim(), Control.max_len_string_built_name);
                }
                if (printCarLast.isSelected()) {
                    last = padAttribute(car.getLastDate().split(" ")[0], 10);
                }
                if (printCarWait.isSelected()) {
                    wait = padAttribute(Integer.toString(car.getWait()), 4);
                }
                if (printCarPickup.isSelected()) {
                    schedule = padAttribute(car.getPickupScheduleName(), 10);
                }
                if (printCarValue.isSelected()) {
                    value = padAttribute(car.getValue().trim(), Control.max_len_string_attibute);
                }
                if (printCarRfid.isSelected()) {
                    rfid = padAttribute(car.getRfid().trim(), Control.max_len_string_attibute);
                }
                if (printCarTrain.isSelected()) // pad out train to half of its maximum
                {
                    train = padAttribute(car.getTrainName().trim(), Control.max_len_string_train_name / 2);
                }
                if (printCarDestination.isSelected()) {
                    if (car.getDestination() != null) {
                        destination = car.getDestinationName().trim() + " - " + car.getDestinationTrackName();
                    }
                    destination = padAttribute(destination, LocationManager.instance()
                            .getMaxLocationAndTrackNameLength() + 3);
                }
                if (printCarFinalDestination.isSelected()) {
                    if (car.getFinalDestination() != null) {
                        finalDestination = car.getFinalDestinationName().trim() + " - "
                                + car.getFinalDestinationTrackName().trim();
                    }
                    finalDestination = padAttribute(finalDestination, LocationManager.instance()
                            .getMaxLocationAndTrackNameLength() + 3);
                }
                if (printCarRWE.isSelected()) {
                    if (car.getReturnWhenEmptyDestination() != null) {
                        returnWhenEmpty = car.getReturnWhenEmptyDestinationName().trim() + " - "
                                + car.getReturnWhenEmptyDestTrackName().trim();
                    }
                    returnWhenEmpty = padAttribute(returnWhenEmpty, LocationManager.instance()
                            .getMaxLocationAndTrackNameLength() + 3);
                }
                if (printCarComment.isSelected()) {
                    comment = car.getComment().trim();
                }

                String s = number + road + type + length + weight + color + load + kernel + owner + built + last + wait
                        + schedule + value + rfid + location + train + destination + finalDestination + returnWhenEmpty
                        + comment;

                if (s.length() > numberCharPerLine) {
                    s = s.substring(0, numberCharPerLine);
                }
                writer.write(s + NEW_LINE);
            }

            // and force completion of the printing
            writer.close();
        } catch (IOException we) {
            log.error("Error printing car roster");
        }
    }

    private void printTitleLine(HardcopyWriter writer) throws IOException {
        String s = padAttribute(Bundle.getMessage("Number"), Control.max_len_string_print_road_number)
                + padAttribute(Bundle.getMessage("Road"), CarRoads.instance().getMaxNameLength())
                + padAttribute(Bundle.getMessage("Type"), CarTypes.instance().getMaxFullNameLength())
                + (printCarLength.isSelected() ? Bundle.getMessage("Len") + "  " : "")
                + (printCarWeight.isSelected() ? "     " : "")
                + (printCarColor.isSelected() ? padAttribute(Bundle.getMessage("Color"), CarColors.instance()
                                .getMaxNameLength()) : "")
                + (printCarLoad.isSelected() ? padAttribute(Bundle.getMessage("Load"), CarLoads.instance()
                                .getMaxNameLength()) : "")
                + (printCarKernel.isSelected() ? padAttribute(("Kernel"), Control.max_len_string_attibute) : "")
                + (printCarOwner.isSelected() ? padAttribute(Bundle.getMessage("Owner"), CarOwners.instance().getMaxNameLength()) : "")
                + (printCarBuilt.isSelected() ? Bundle.getMessage("Built") + " " : "")
                + (printCarLast.isSelected() ? Bundle.getMessage("LastMoved") + " " : "")
                + (printCarWait.isSelected() ? Bundle.getMessage("Wait") + " " : "")
                + (printCarPickup.isSelected() ? padAttribute(Bundle.getMessage("Pickup"), 10) : "")
                + (printCarValue.isSelected() ? Setup.getValueLabel() + "        " : "")
                + (printCarRfid.isSelected() ? Setup.getRfidLabel() + "        " : "")
                + (printCarLocation.isSelected() ? padAttribute(Bundle.getMessage("Location"), LocationManager
                                .instance().getMaxLocationAndTrackNameLength() + 3) : "")
                + (printCarTrain.isSelected() ? padAttribute(Bundle.getMessage("Train"),
                                Control.max_len_string_train_name / 2) : "")
                + (printCarDestination.isSelected() ? padAttribute(Bundle.getMessage("Destination"), LocationManager
                                .instance().getMaxLocationAndTrackNameLength() + 3) : "")
                + (printCarFinalDestination.isSelected() ? padAttribute(Bundle.getMessage("FinalDestination"),
                                LocationManager.instance().getMaxLocationAndTrackNameLength() + 3) : "")
                + (printCarRWE.isSelected() ? padAttribute(Bundle.getMessage("ReturnWhenEmpty"), LocationManager
                                .instance().getMaxLocationAndTrackNameLength() + 3) : "")
                + (printCarComment.isSelected() ? Bundle.getMessage("Comment") : "");
        if (s.length() > numberCharPerLine) {
            s = s.substring(0, numberCharPerLine);
        }
        writer.write(s + NEW_LINE);
    }

    private String padAttribute(String attribute, int length) {
        if (attribute.length() > length) {
            attribute = attribute.substring(0, length);
        }
        StringBuffer buf = new StringBuffer(attribute);
        for (int i = attribute.length(); i < length + 1; i++) {
            buf.append(" ");
        }
        return buf.toString();
    }

    JComboBox<String> sortByComboBox = new JComboBox<>();
    JComboBox<String> manifestOrientationComboBox = new JComboBox<>();
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();

    JCheckBox printCarsWithLocation = new JCheckBox(Bundle.getMessage("PrintCarsWithLocation"));
    JCheckBox printCarLength = new JCheckBox(Bundle.getMessage("PrintCarLength"));
    JCheckBox printCarWeight = new JCheckBox(Bundle.getMessage("PrintCarWeight"));
    JCheckBox printCarColor = new JCheckBox(Bundle.getMessage("PrintCarColor"));
    JCheckBox printCarOwner = new JCheckBox(Bundle.getMessage("PrintCarOwner"));
    JCheckBox printCarBuilt = new JCheckBox(Bundle.getMessage("PrintCarBuilt"));
    JCheckBox printCarLoad = new JCheckBox(Bundle.getMessage("PrintCarLoad"));
    JCheckBox printCarKernel = new JCheckBox(Bundle.getMessage("PrintKernel"));
    JCheckBox printCarValue = new JCheckBox(MessageFormat.format(Bundle.getMessage("PrintCar"), new Object[]{Setup
        .getValueLabel()}));
    JCheckBox printCarRfid = new JCheckBox(MessageFormat.format(Bundle.getMessage("PrintCar"), new Object[]{Setup
        .getRfidLabel()}));
    JCheckBox printCarLast = new JCheckBox(Bundle.getMessage("PrintCarLastMoved"));
    JCheckBox printCarWait = new JCheckBox(Bundle.getMessage("PrintCarWait"));
    JCheckBox printCarPickup = new JCheckBox(Bundle.getMessage("PrintCarPickup"));
    JCheckBox printCarLocation = new JCheckBox(Bundle.getMessage("PrintCarLocation"));
    JCheckBox printCarTrain = new JCheckBox(Bundle.getMessage("PrintCarTrain"));
    JCheckBox printCarDestination = new JCheckBox(Bundle.getMessage("PrintCarDestination"));
    JCheckBox printCarFinalDestination = new JCheckBox(Bundle.getMessage("PrintCarFinalDestination"));
    JCheckBox printCarRWE = new JCheckBox(Bundle.getMessage("PrintCarReturnWhenEmpty"));
    JCheckBox printCarComment = new JCheckBox(Bundle.getMessage("PrintCarComment"));
    JCheckBox printSpace = new JCheckBox(Bundle.getMessage("PrintSpace"));
    JCheckBox printPage = new JCheckBox(Bundle.getMessage("PrintPage"));

    JButton okayButton = new JButton(Bundle.getMessage("ButtonOkay"));

    static final String NEW_LINE = "\n"; // NOI18N

    public class CarPrintOptionFrame extends OperationsFrame {

        /**
         *
         */
        private static final long serialVersionUID = -7320807344781148331L;
        PrintCarRosterAction pcr;

        public CarPrintOptionFrame(PrintCarRosterAction pcr) {
            super();
            this.pcr = pcr;
            // create panel
            JPanel pSortBy = new JPanel();
            pSortBy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));
            pSortBy.add(sortByComboBox);
            addComboBoxAction(sortByComboBox);

            JPanel pOrientation = new JPanel();
            pOrientation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOrientation")));
            pOrientation.add(manifestOrientationComboBox);
            
            manifestOrientationComboBox.addItem(Setup.PORTRAIT);
            manifestOrientationComboBox.addItem(Setup.LANDSCAPE);
            
            JPanel pFontSize = new JPanel();
            pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
            pFontSize.add(fontSizeComboBox);
            
            // load font sizes 5 through 14
            for (int i = 5; i < 15; i++) {
                fontSizeComboBox.addItem(i);
            }
            
            fontSizeComboBox.setSelectedItem(Control.reportFontSize);

            JPanel pPanel = new JPanel();
            pPanel.setLayout(new GridBagLayout());
            JScrollPane panePanel = new JScrollPane(pPanel);
            panePanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
            addItemLeft(pPanel, printCarsWithLocation, 0, 0);
            addItemLeft(pPanel, printCarLength, 0, 1);
            addItemLeft(pPanel, printCarWeight, 0, 2);
            addItemLeft(pPanel, printCarColor, 0, 3);
            addItemLeft(pPanel, printCarLoad, 0, 4);
            addItemLeft(pPanel, printCarKernel, 0, 5);
            addItemLeft(pPanel, printCarOwner, 0, 6);
            addItemLeft(pPanel, printCarBuilt, 0, 7);
            addItemLeft(pPanel, printCarLast, 0, 8);
            addItemLeft(pPanel, printCarWait, 0, 9);
            addItemLeft(pPanel, printCarPickup, 0, 10);
            if (Setup.isValueEnabled()) {
                addItemLeft(pPanel, printCarValue, 0, 11);
            }
            if (Setup.isRfidEnabled()) {
                addItemLeft(pPanel, printCarRfid, 0, 12);
            }
            addItemLeft(pPanel, printCarLocation, 0, 13);
            addItemLeft(pPanel, printCarTrain, 0, 14);
            addItemLeft(pPanel, printCarDestination, 0, 15);
            addItemLeft(pPanel, printCarFinalDestination, 0, 16);
            addItemLeft(pPanel, printCarRWE, 0, 17);
            addItemLeft(pPanel, printCarComment, 0, 18);
            addItemLeft(pPanel, printSpace, 0, 19);
            addItemLeft(pPanel, printPage, 0, 20);

            // set defaults
            printCarsWithLocation.setSelected(false);
            printCarLength.setSelected(true);
            printCarWeight.setSelected(false);
            printCarColor.setSelected(true);
            printCarLoad.setSelected(false);
            printCarKernel.setSelected(false);
            printCarOwner.setSelected(false);
            printCarBuilt.setSelected(false);
            printCarLast.setSelected(false);
            printCarWait.setSelected(false);
            printCarPickup.setSelected(false);
            printCarValue.setSelected(false);
            printCarRfid.setSelected(false);
            printCarLocation.setSelected(true);
            printCarTrain.setSelected(false);
            printCarDestination.setSelected(false);
            printCarFinalDestination.setSelected(false);
            printCarRWE.setSelected(false);
            printCarComment.setSelected(false);
            printSpace.setSelected(false);
            printPage.setSelected(false);

            // add tool tips
            printSpace.setToolTipText(Bundle.getMessage("TipSelectSortByLoc"));
            printPage.setToolTipText(Bundle.getMessage("TipSelectSortByLoc"));

            JPanel pButtons = new JPanel();
            pButtons.setLayout(new GridBagLayout());
            pButtons.add(okayButton);
            pButtons.setBorder(BorderFactory.createTitledBorder(""));
            addButtonAction(okayButton);

            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            getContentPane().add(pSortBy);
            getContentPane().add(pOrientation);
            getContentPane().add(pFontSize);
            getContentPane().add(panePanel);
            getContentPane().add(pButtons);

            initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight500));
        }

        public void initComponents() {
            if (isPreview) {
                cpof.setTitle(Bundle.getMessage("MenuItemPreview"));
            } else {
                cpof.setTitle(Bundle.getMessage("MenuItemPrint"));
            }
            loadSortByComboBox(sortByComboBox);
            printSpace.setEnabled(panel.sortByLocation.isSelected());
            printPage.setEnabled(panel.sortByLocation.isSelected());
            if (!panel.sortByLocation.isSelected()) {
                printSpace.setSelected(false);
                printPage.setSelected(false);
            }
        }

        private void loadSortByComboBox(JComboBox<String> box) {
            for (int i = panel.carsTableModel.SORTBY_NUMBER; i <= panel.carsTableModel.SORTBY_LAST; i++) {
                box.addItem(panel.carsTableModel.getSortByName(i));
            }
            box.setSelectedItem(panel.carsTableModel.getSortByName());
        }

        public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
            setVisible(false);
            pcr.printCars();
        }
        
        public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
            if (sortByComboBox.getSelectedItem() != null && sortByComboBox.getSelectedItem().equals(panel.carsTableModel.getSortByName(panel.carsTableModel.SORTBY_LOCATION))) {
                printSpace.setEnabled(true);
                printPage.setEnabled(true);
            } else {
                printSpace.setEnabled(false);
                printPage.setEnabled(false);
                printSpace.setSelected(false);
                printPage.setSelected(false);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintCarRosterAction.class.getName());
}

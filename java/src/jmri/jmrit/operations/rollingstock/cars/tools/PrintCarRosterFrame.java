package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Prints a summary of the car roster
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * MacIntosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2023
 */

public class PrintCarRosterFrame extends OperationsFrame {

    boolean _isPreview;
    CarsTableFrame _ctf;

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
    JCheckBox printCarValue = new JCheckBox(
            Bundle.getMessage("PrintCar", Setup.getValueLabel()));
    JCheckBox printCarRfid = new JCheckBox(
            Bundle.getMessage("PrintCar", Setup.getRfidLabel()));
    JCheckBox printCarLast = new JCheckBox(Bundle.getMessage("PrintCarLastMoved"));
    JCheckBox printCarWait = new JCheckBox(Bundle.getMessage("PrintCarWait"));
    JCheckBox printCarPickup = new JCheckBox(Bundle.getMessage("PrintCarPickup"));
    JCheckBox printCarLocation = new JCheckBox(Bundle.getMessage("PrintCarLocation"));
    JCheckBox printCarTrain = new JCheckBox(Bundle.getMessage("PrintCarTrain"));
    JCheckBox printCarDestination = new JCheckBox(Bundle.getMessage("PrintCarDestination"));
    JCheckBox printCarFinalDestination = new JCheckBox(Bundle.getMessage("PrintCarFinalDestination"));
    JCheckBox printCarRoutePath = new JCheckBox(Bundle.getMessage("PrintCarRoutePath"));
    JCheckBox printCarRWE = new JCheckBox(Bundle.getMessage("PrintCarReturnWhenEmpty"));
    JCheckBox printCarRWL = new JCheckBox(Bundle.getMessage("PrintCarReturnWhenLoaded"));
    JCheckBox printDivision = new JCheckBox(Bundle.getMessage("PrintCarDivision"));
    JCheckBox printCarStatus = new JCheckBox(Bundle.getMessage("PrintCarStatus"));
    JCheckBox printCarComment = new JCheckBox(Bundle.getMessage("PrintCarComment"));
    JCheckBox printSpace = new JCheckBox(Bundle.getMessage("PrintSpace"));
    JCheckBox printPage = new JCheckBox(Bundle.getMessage("PrintPage"));

    JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));

    public PrintCarRosterFrame(boolean isPreview, CarsTableFrame carsTableFrame) {
        super();
        _isPreview = isPreview;
        _ctf = carsTableFrame;

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

        OperationsPanel.loadFontSizeComboBox(fontSizeComboBox);
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
        addItemLeft(pPanel, printCarRWL, 0, 18);
        addItemLeft(pPanel, printDivision, 0, 19);
        addItemLeft(pPanel, printCarStatus, 0, 20);
        addItemLeft(pPanel, printCarRoutePath, 0, 21);
        addItemLeft(pPanel, printCarComment, 0, 22);
        addItemLeft(pPanel, printSpace, 0, 23);
        addItemLeft(pPanel, printPage, 0, 24);

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
        printCarRoutePath.setSelected(false);
        printCarRWE.setSelected(false);
        printCarRWL.setSelected(false);
        printDivision.setSelected(false);
        printCarStatus.setSelected(false);
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

        if (_isPreview) {
            setTitle(Bundle.getMessage("MenuItemPreview"));
        } else {
            setTitle(Bundle.getMessage("MenuItemPrint"));
        }
        loadSortByComboBox(sortByComboBox);
        updateLocationCheckboxes();
    }

    private void loadSortByComboBox(JComboBox<String> box) {
        box.removeAllItems();
        for (int i = _ctf.carsTableModel.SORTBY_NUMBER; i <= _ctf.carsTableModel.SORTBY_COMMENT; i++) {
            box.addItem(_ctf.carsTableModel.getSortByName(i));
        }
        box.setSelectedItem(_ctf.carsTableModel.getSortByName());
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        setVisible(false);
        printCars();
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        updateLocationCheckboxes();
    }

    private void updateLocationCheckboxes() {
        if (sortByComboBox.getSelectedItem() != null &&
                sortByComboBox.getSelectedItem()
                        .equals(_ctf.carsTableModel.getSortByName(_ctf.carsTableModel.SORTBY_LOCATION))) {
            printSpace.setEnabled(true);
            printPage.setEnabled(true);
        } else {
            printSpace.setEnabled(false);
            printPage.setEnabled(false);
            printSpace.setSelected(false);
            printPage.setSelected(false);
        }
    }

    int numberCharPerLine;

    private void printCars() {
        boolean landscape = false;
        if (manifestOrientationComboBox.getSelectedItem() != null &&
                manifestOrientationComboBox.getSelectedItem().equals(Setup.LANDSCAPE)) {
            landscape = true;
        }

        int fontSize = (int) fontSizeComboBox.getSelectedItem();

        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleCarRoster"), fontSize, .5,
                .5, .5, .5, _isPreview, "", landscape, true, null)) {

            numberCharPerLine = writer.getCharactersPerLine();

            printHeader(writer);

            printRoster(writer);

        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        } catch (IOException we) {
            log.error("Error printing car roster");
        }
    }

    private void printHeader(HardcopyWriter writer) throws IOException {
        String s = padAttribute(Bundle.getMessage("Number"), Control.max_len_string_print_road_number) +
                padAttribute(Bundle.getMessage("Road"),
                        InstanceManager.getDefault(CarRoads.class).getMaxNameLength()) +
                padAttribute(Bundle.getMessage("Type"),
                        InstanceManager.getDefault(CarTypes.class).getMaxFullNameLength()) +
                (printCarLength.isSelected() ? Bundle.getMessage("Len") + "  " : "") +
                (printCarWeight.isSelected() ? "     " : "") +
                (printCarColor.isSelected()
                        ? padAttribute(Bundle.getMessage("Color"),
                                InstanceManager.getDefault(CarColors.class).getMaxNameLength())
                        : "") +
                (printCarLoad.isSelected()
                        ? padAttribute(Bundle.getMessage("Load"),
                                InstanceManager.getDefault(CarLoads.class).getMaxNameLength())
                        : "") +
                (printCarKernel.isSelected() ? padAttribute(("Kernel"), Control.max_len_string_attibute) : "") +
                (printCarOwner.isSelected()
                        ? padAttribute(Bundle.getMessage("Owner"),
                                InstanceManager.getDefault(CarOwners.class).getMaxNameLength())
                        : "") +
                (printCarBuilt.isSelected() ? Bundle.getMessage("Built") + " " : "") +
                (printCarLast.isSelected() ? Bundle.getMessage("LastMoved") + " " : "") +
                (printCarWait.isSelected() ? Bundle.getMessage("Wait") + " " : "") +
                (printCarPickup.isSelected() ? padAttribute(Bundle.getMessage("Pickup"), 10) : "") +
                (printCarValue.isSelected() ? padAttribute(Setup.getValueLabel(), Control.max_len_string_attibute)
                        : "") +
                (printCarRfid.isSelected() ? padAttribute(Setup.getRfidLabel(), Control.max_len_string_attibute)
                        : "") +
                (printCarLocation.isSelected()
                        ? padAttribute(Bundle.getMessage("Location"),
                                InstanceManager.getDefault(LocationManager.class)
                                        .getMaxLocationAndTrackNameLength() +
                                        3)
                        : "") +
                (printCarTrain.isSelected()
                        ? padAttribute(Bundle.getMessage("Train"), Control.max_len_string_train_name / 2)
                        : "") +
                (printCarDestination.isSelected()
                        ? padAttribute(Bundle.getMessage("Destination"),
                                InstanceManager.getDefault(LocationManager.class)
                                        .getMaxLocationAndTrackNameLength() +
                                        3)
                        : "") +
                (printCarFinalDestination.isSelected()
                        ? padAttribute(Bundle.getMessage("FinalDestination"),
                                InstanceManager.getDefault(LocationManager.class)
                                        .getMaxLocationAndTrackNameLength() +
                                        3)
                        : "") +
                (printCarRWE.isSelected()
                        ? padAttribute(Bundle.getMessage("ReturnWhenEmpty"),
                                InstanceManager.getDefault(LocationManager.class)
                                        .getMaxLocationAndTrackNameLength() +
                                        3)
                        : "") +
                (printCarRWL.isSelected()
                        ? padAttribute(Bundle.getMessage("ReturnWhenLoaded"),
                                InstanceManager.getDefault(LocationManager.class)
                                        .getMaxLocationAndTrackNameLength() +
                                        3)
                        : "") +
                (printDivision.isSelected() ? Bundle.getMessage("HomeDivision") + " " : "") +
                (printCarStatus.isSelected() ? Bundle.getMessage("Status") + " " : "") +
                (printCarRoutePath.isSelected()
                        ? padAttribute(Bundle.getMessage("Route"),
                                InstanceManager.getDefault(LocationManager.class)
                                        .getMaxLocationAndTrackNameLength() +
                                        3)
                        : "") +
                (printCarComment.isSelected() ? Bundle.getMessage("Comment") : "");
        if (s.length() > numberCharPerLine) {
            s = s.substring(0, numberCharPerLine);
            writer.write(Bundle.getMessage("WarningTextPage") + NEW_LINE);
        }
        writer.write(s + NEW_LINE);
    }

    private void printRoster(HardcopyWriter writer) throws IOException {
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
        String returnWhenLoaded = "";
        String division = "";
        String value = "";
        String rfid = "";
        String last = "";
        String wait = "";
        String schedule = "";
        String status = "";
        String routePath = "";
        String comment = "";
        String previousLocation = null;
        List<Car> cars = _ctf.carsTableModel.getCarList(sortByComboBox.getSelectedIndex());
        for (Car car : cars) {
            if (printCarsWithLocation.isSelected() && car.getLocation() == null) {
                continue; // car doesn't have a location skip
            }
            location = "";
            destination = "";
            finalDestination = "";
            returnWhenEmpty = "";
            returnWhenLoaded = "";

            if (printCarLocation.isSelected()) {
                if (car.getLocation() != null) {
                    location = car.getLocationName().trim() + " - " + car.getTrackName().trim();
                }
                location = padAttribute(location,
                        InstanceManager.getDefault(LocationManager.class).getMaxLocationAndTrackNameLength() +
                                3);
            }
            // Page break between locations?
            if (previousLocation != null &&
                    !car.getLocationName().trim().equals(previousLocation) &&
                    printPage.isSelected()) {
                writer.pageBreak();
                printHeader(writer);
            } // Add a line between locations?
            else if (previousLocation != null &&
                    !car.getLocationName().trim().equals(previousLocation) &&
                    printSpace.isSelected()) {
                writer.write(NEW_LINE);
            }
            previousLocation = car.getLocationName().trim();

            // car number
            number = padAttribute(car.getNumber().trim(), Control.max_len_string_print_road_number);
            // car road
            road = padAttribute(car.getRoadName().trim(),
                    InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
            // car type
            type = padAttribute(car.getTypeName().trim(),
                    InstanceManager.getDefault(CarTypes.class).getMaxFullNameLength());

            if (printCarLength.isSelected()) {
                length = padAttribute(car.getLength().trim(), Control.max_len_string_length_name);
            }
            if (printCarWeight.isSelected()) {
                weight = padAttribute(car.getWeight().trim(), Control.max_len_string_weight_name);
            }
            if (printCarColor.isSelected()) {
                color = padAttribute(car.getColor().trim(),
                        InstanceManager.getDefault(CarColors.class).getMaxNameLength());
            }
            if (printCarLoad.isSelected()) {
                load = padAttribute(car.getLoadName().trim(),
                        InstanceManager.getDefault(CarLoads.class).getMaxNameLength());
            }
            if (printCarKernel.isSelected()) {
                kernel = padAttribute(car.getKernelName().trim(), Control.max_len_string_attibute);
            }
            if (printCarOwner.isSelected()) {
                owner = padAttribute(car.getOwnerName().trim(),
                        InstanceManager.getDefault(CarOwners.class).getMaxNameLength());
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
            // pad out train to half of its maximum length
            if (printCarTrain.isSelected()) {
                train = padAttribute(car.getTrainName().trim(), Control.max_len_string_train_name / 2);
            }
            if (printCarDestination.isSelected()) {
                if (car.getDestination() != null) {
                    destination =
                            car.getDestinationName().trim() + " - " + car.getDestinationTrackName().trim();
                }
                destination = padAttribute(destination,
                        InstanceManager.getDefault(LocationManager.class).getMaxLocationAndTrackNameLength() +
                                3);
            }
            if (printCarFinalDestination.isSelected()) {
                if (car.getFinalDestination() != null) {
                    finalDestination = car.getFinalDestinationName().trim();
                    if (car.getFinalDestinationTrack() != null) {
                        finalDestination = finalDestination +
                                " - " +
                                car.getFinalDestinationTrackName().trim();
                    }
                }
                finalDestination = padAttribute(finalDestination,
                        InstanceManager.getDefault(LocationManager.class).getMaxLocationAndTrackNameLength() +
                                3);
            }
            // long route paths will wrap
            if (printCarRoutePath.isSelected()) {
                routePath = car.getRoutePath() + " ";
            }
            if (printCarRWE.isSelected()) {
                if (car.getReturnWhenEmptyDestination() != null) {
                    returnWhenEmpty = car.getReturnWhenEmptyDestinationName().trim() +
                            " - " +
                            car.getReturnWhenEmptyDestTrackName().trim();
                }
                returnWhenEmpty = padAttribute(returnWhenEmpty,
                        InstanceManager.getDefault(LocationManager.class).getMaxLocationAndTrackNameLength() +
                                3);
            }
            if (printCarRWL.isSelected()) {
                if (car.getReturnWhenLoadedDestination() != null) {
                    returnWhenLoaded = car.getReturnWhenLoadedDestinationName().trim() +
                            " - " +
                            car.getReturnWhenLoadedDestTrackName().trim();
                }
                returnWhenLoaded = padAttribute(returnWhenLoaded,
                        InstanceManager.getDefault(LocationManager.class).getMaxLocationAndTrackNameLength() +
                                3);
            }
            if (printDivision.isSelected()) {
                division = padAttribute(car.getDivisionName(), Bundle.getMessage("HomeDivision").length());
            }
            if (printCarStatus.isSelected()) {
                status = padAttribute(car.getStatus(), Bundle.getMessage("Status").length());
            }
            // comment gets trimmed by line length
            if (printCarComment.isSelected()) {
                comment = car.getComment().trim();
            }

            String s = number +
                    road +
                    type +
                    length +
                    weight +
                    color +
                    load +
                    kernel +
                    owner +
                    built +
                    last +
                    wait +
                    schedule +
                    value +
                    rfid +
                    location +
                    train +
                    destination +
                    finalDestination +
                    returnWhenEmpty +
                    returnWhenLoaded +
                    division +
                    status +
                    routePath +
                    comment;

            s = s.trim();
            if (s.length() > numberCharPerLine) {
                writer.write(s.substring(0, numberCharPerLine) + NEW_LINE);
                s = s.substring(numberCharPerLine, s.length());
                String tab = getTab();
                int subStringLength = numberCharPerLine - tab.length();
                while (tab.length() + s.length() > numberCharPerLine) {
                    writer.write(tab + s.substring(0, subStringLength) + NEW_LINE);
                    s = s.substring(subStringLength, s.length());
                }
                s = tab + s;
            }
            writer.write(s + NEW_LINE);
        }
    }

    private String padAttribute(String attribute, int length) {
        return TrainCommon.padAndTruncate(attribute, length) + TrainCommon.SPACE;
    }

    private String getTab() {
        return padAttribute("",
                Control.max_len_string_print_road_number +
                        InstanceManager.getDefault(CarRoads.class).getMaxNameLength() +
                        1);
    }

    private final static Logger log = LoggerFactory.getLogger(PrintCarRosterFrame.class);
}

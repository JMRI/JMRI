package jmri.jmrit.operations.rollingstock.engines.tools;

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
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Prints engine roster.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2023
 */

public class PrintEngineRosterFrame extends OperationsFrame {

    boolean _isPreview;
    EnginesTableFrame _etf;

    private int numberCharPerLine = 90;
    private int lastLength = 19;

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    JCheckBox printLocosWithLocation = new JCheckBox(Bundle.getMessage("PrintLocosWithLocation"));

    JComboBox<String> sortByComboBox = new JComboBox<>();
    JComboBox<String> manifestOrientationComboBox = new JComboBox<>();
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();

    JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));

    public PrintEngineRosterFrame(boolean isPreview, EnginesTableFrame etf) {
        super();
        _isPreview = isPreview;
        _etf = etf;

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
        manifestOrientationComboBox.setSelectedItem(Setup.LANDSCAPE);

        JPanel pFontSize = new JPanel();
        pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
        pFontSize.add(fontSizeComboBox);

        OperationsPanel.loadFontSizeComboBox(fontSizeComboBox);
        fontSizeComboBox.setSelectedItem(Control.reportFontSize);

        JPanel pPanel = new JPanel();
        pPanel.setLayout(new GridBagLayout());
        JScrollPane panePanel = new JScrollPane(pPanel);
        panePanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
        addItemLeft(pPanel, printLocosWithLocation, 0, 0);

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

        if (_isPreview) {
            setTitle(Bundle.getMessage("MenuItemPreview"));
        } else {
            setTitle(Bundle.getMessage("MenuItemPrint"));
        }
        loadSortByComboBox(sortByComboBox);

        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight300));
    }

    @Override
    public void initComponents() {
        sortByComboBox.setSelectedItem(_etf.enginesModel.getSortByName());
    }

    private void loadSortByComboBox(JComboBox<String> box) {
        box.removeAllItems();
        for (int i =
                _etf.enginesModel.SORTBY_NUMBER; i <= _etf.enginesModel.SORTBY_COMMENT; i++) {
            box.addItem(_etf.enginesModel.getSortByName(i));
        }
        box.setSelectedItem(_etf.enginesModel.getSortByName());
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        setVisible(false);
        printEngines();
    }

    private void printEngines() {
        boolean landscape = false;
        if (manifestOrientationComboBox.getSelectedItem() != null &&
                manifestOrientationComboBox.getSelectedItem().equals(Setup.LANDSCAPE)) {
            landscape = true;
        }

        int fontSize = (int) fontSizeComboBox.getSelectedItem();

        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleEngineRoster"),
                fontSize, .5, .5, .5, .5, _isPreview, "", landscape, true, null);) {

            numberCharPerLine = writer.getCharactersPerLine();

            // create header
            write(writer, createHeader());

            printRoster(writer);

            // and force completion of the printing
            writer.close();
        } catch (IOException we) {
            log.error("Error printing ConsistRosterEntry: {}", we.getLocalizedMessage());
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        }
    }

    private String createHeader() {
        StringBuffer header = new StringBuffer();

        header.append(padAttribute(Bundle.getMessage("Number"), Control.max_len_string_print_road_number) +
                padAttribute(Bundle.getMessage("Road"),
                        InstanceManager.getDefault(CarRoads.class).getMaxNameLength()) +
                padAttribute(Bundle.getMessage("Model"),
                        InstanceManager.getDefault(EngineModels.class).getMaxNameLength()) +
                padAttribute(Bundle.getMessage("Type"),
                        InstanceManager.getDefault(EngineTypes.class).getMaxNameLength()) +
                padAttribute(Bundle.getMessage("Len"), Control.max_len_string_length_name));

        if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_TRAIN ||
                sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_DESTINATION) {
            header.append(padAttribute(Bundle.getMessage("Train"), Control.max_len_string_train_name / 2));
        } else {
            header.append(padAttribute(Bundle.getMessage("Consist"),
                    InstanceManager.getDefault(ConsistManager.class).getMaxNameLength()));
        }
        header.append(padAttribute(Bundle.getMessage("Location"),
                locationManager.getMaxLocationAndTrackNameLength() + 3));
        // one of eight user selections
        if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_OWNER) {
            header.append(padAttribute(Bundle.getMessage("Owner"), Control.max_len_string_attibute));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_MOVES) {
            header.append(padAttribute(Bundle.getMessage("Moves"), 5));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_VALUE) {
            header.append(padAttribute(Setup.getValueLabel(), Control.max_len_string_attibute));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_LAST) {
            header.append(padAttribute(Bundle.getMessage("LastMoved"), lastLength));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_RFID) {
            header.append(padAttribute(Setup.getRfidLabel(), Control.max_len_string_attibute));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_DCC_ADDRESS) {
            header.append(padAttribute(Bundle.getMessage("DccAddress"), 5));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_BUILT) {
            header.append(padAttribute(Bundle.getMessage("Built"), Control.max_len_string_built_name));
        } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_DESTINATION) {
            header.append(Bundle.getMessage("Destination"));
        } else {
            header.append(padAttribute(Bundle.getMessage("Comment"), engineManager.getMaxCommentLength()));
        }
        return header.toString() + NEW_LINE;
    }

    private void printRoster(HardcopyWriter writer) throws IOException {
        // Loop through the Roster, printing as needed
        String number;
        String road;
        String model;
        String type;
        String length;
        String train = "";
        String consist = "";
        String location = "";
        String moves = "";
        String owner = "";
        String built = "";
        String dccAddress = "";
        String value = "";
        String rfid = "";
        String last = "";
        String comment = "";

        List<Engine> engines = _etf.enginesModel.getEngineList(sortByComboBox.getSelectedIndex());
        for (Engine engine : engines) {
            if (printLocosWithLocation.isSelected() && engine.getLocation() == null) {
                continue;
            }
            String destination = "";
            // engine number, road, model, type, and length are always printed
            number = padAttribute(engine.getNumber(), Control.max_len_string_print_road_number);
            road = padAttribute(engine.getRoadName(),
                    InstanceManager.getDefault(CarRoads.class).getMaxNameLength());
            model = padAttribute(engine.getModel(),
                    InstanceManager.getDefault(EngineModels.class).getMaxNameLength());
            type = padAttribute(engine.getTypeName(),
                    InstanceManager.getDefault(EngineTypes.class).getMaxNameLength());
            length = padAttribute(engine.getLength(), Control.max_len_string_length_name);

            // show train or consist name
            if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_TRAIN ||
                    sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_DESTINATION) {
                train = padAttribute(engine.getTrainName().trim(), Control.max_len_string_train_name / 2);
            } else {
                consist = padAttribute(engine.getConsistName(),
                        InstanceManager.getDefault(ConsistManager.class).getMaxNameLength());
            }

            // show one of 8 options, comment is default
            if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_OWNER) {
                owner = padAttribute(engine.getOwnerName(), Control.max_len_string_attibute);
            } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_MOVES) {
                moves = padAttribute(Integer.toString(engine.getMoves()), 5);
            } else if (sortByComboBox
                    .getSelectedIndex() == _etf.enginesModel.SORTBY_DCC_ADDRESS) {
                dccAddress = padAttribute(engine.getDccAddress(), 5);
            } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_LAST) {
                last = padAttribute(engine.getSortDate(), lastLength);
            } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_VALUE) {
                value = padAttribute(engine.getValue(), Control.max_len_string_attibute);
            } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_RFID) {
                rfid = padAttribute(engine.getRfid(), Control.max_len_string_attibute);
            } else if (sortByComboBox.getSelectedIndex() == _etf.enginesModel.SORTBY_BUILT) {
                built = padAttribute(engine.getBuilt(), Control.max_len_string_built_name);
            } else if (sortByComboBox
                    .getSelectedIndex() == _etf.enginesModel.SORTBY_DESTINATION) {
                if (engine.getDestination() != null) {
                    destination = padAttribute(
                            engine.getDestinationName() + " - " + engine.getDestinationTrackName(),
                            locationManager.getMaxLocationAndTrackNameLength() +
                                    3);
                }
            } else {
                comment = padAttribute(engine.getComment(), engineManager.getMaxCommentLength());
            }

            if (!engine.getLocationName().equals(Engine.NONE)) {
                location = padAttribute(engine.getLocationName() + " - " + engine.getTrackName(),
                        locationManager.getMaxLocationAndTrackNameLength() + 3);
            } else {
                location = padAttribute("",
                        locationManager.getMaxLocationAndTrackNameLength() + 3);
            }

            String s = number +
                    road +
                    model +
                    type +
                    length +
                    consist +
                    train +
                    location +
                    moves +
                    owner +
                    value +
                    rfid +
                    dccAddress +
                    built +
                    last +
                    comment +
                    destination;
            write(writer, s);
        }
    }

    private void write(HardcopyWriter writer, String s) throws IOException {
        if (s.length() > numberCharPerLine) {
            s = s.substring(0, numberCharPerLine);
        }
        writer.write(s + NEW_LINE);
    }

    private String padAttribute(String attribute, int length) {
        return TrainCommon.padAndTruncate(attribute, length) + TrainCommon.SPACE;
    }

    private final static Logger log = LoggerFactory.getLogger(PrintEngineRosterFrame.class);
}

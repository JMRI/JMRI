package jmri.jmrit.operations.trains.tools;

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
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Prints a summary of a train or trains. The trains list is controlled by the
 * "Show All" checkbox and the "Build" checkboxes in the TrainsTableFrame.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009, 2023
 */
public class PrintTrainsFrame extends OperationsFrame {

    static final String TAB = "\t"; // NOI18N
    static final char FORM_FEED = '\f'; // NOI18N

    public static final int MAX_NAME_LENGTH = Control.max_len_string_train_name - 10;

    boolean _isPreview;
    Train _train;
    TrainsTableFrame _trainsTableFrame;

    JComboBox<String> sortByComboBox = new JComboBox<>();
    JComboBox<String> manifestOrientationComboBox = new JComboBox<>();
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();

    JCheckBox printSummary = new JCheckBox(Bundle.getMessage("PrintSummary"));
    JCheckBox printDetails = new JCheckBox(Bundle.getMessage("PrintDetails"));
    JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));

    public PrintTrainsFrame(boolean isPreview, Train train) {
        _isPreview = isPreview;
        _train = train;
        printTrain();
    }

    public PrintTrainsFrame(boolean isPreview, TrainsTableFrame trainsTableFrame) {
        _isPreview = isPreview;
        _trainsTableFrame = trainsTableFrame;

        // create panel
        JPanel pSortBy = new JPanel();
        pSortBy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));
        pSortBy.add(sortByComboBox);

        loadSortByComboBox(sortByComboBox);
        addComboBoxAction(sortByComboBox);

        JPanel pOrientation = new JPanel();
        pOrientation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOrientation")));
        pOrientation.add(manifestOrientationComboBox);

        manifestOrientationComboBox.addItem(Setup.PORTRAIT);
        manifestOrientationComboBox.addItem(Setup.LANDSCAPE);
        //        manifestOrientationComboBox.setSelectedItem(Setup.LANDSCAPE);

        JPanel pFontSize = new JPanel();
        pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
        pFontSize.add(fontSizeComboBox);

        OperationsPanel.loadFontSizeComboBox(fontSizeComboBox);
        fontSizeComboBox.setSelectedItem(Control.reportFontSize);

        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new GridBagLayout());
        pPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
        addItemLeft(pPanel, printSummary, 0, 0);
        addItemLeft(pPanel, printDetails, 0, 1);

        printSummary.setSelected(true);
        printDetails.setSelected(true);

        // add tool tips
        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        pButtons.add(okayButton);
        addButtonAction(okayButton);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pSortBy);
        getContentPane().add(pOrientation);
        getContentPane().add(pFontSize);
        getContentPane().add(pPanel);
        getContentPane().add(pButtons);
        setPreferredSize(null);
        if (_isPreview) {
            setTitle(Bundle.getMessage("MenuItemPreview"));
        } else {
            setTitle(Bundle.getMessage("MenuItemPrint"));
        }
        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight250));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        setVisible(false);
        printTrains();
    }

    private void printTrains() {
        if (!printSummary.isSelected() && !printDetails.isSelected()) {
            return;
        }

        // obtain a HardcopyWriter to do this
        boolean landscape = false;
        if (manifestOrientationComboBox.getSelectedItem() != null &&
                manifestOrientationComboBox.getSelectedItem().equals(Setup.LANDSCAPE)) {
            landscape = true;
        }

        int fontSize = (int) fontSizeComboBox.getSelectedItem();
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleTrainsTable"),
                fontSize, .5, .5, .5, .5, _isPreview, "", landscape, true, null);) {

            List<Train> trains = _trainsTableFrame.getSortByList((String) sortByComboBox.getSelectedItem());

            if (printSummary.isSelected()) {
                printSummaryTrains(writer, trains, _trainsTableFrame);
                if (printDetails.isSelected()) {
                    writer.write(FORM_FEED); // new page
                }
            }

            if (printDetails.isSelected()) {
                // now do the details for each train
                for (Train train : trains) {
                    if ((train.isBuildEnabled() || _trainsTableFrame.showAllBox.isSelected()) &&
                            train.getRoute() != null) {
                        List<RouteLocation> route = train.getRoute().getLocationsBySequenceList();
                        // determine if another detailed summary can fit on the same page
                        if (writer.getLinesPerPage() - writer.getCurrentLineNumber() < route.size() +
                                getNumberOfLines(writer, Bundle.getMessage("Comment") + ": " + train.getComment()) +
                                NUMBER_OF_HEADER_LINES) {
                            writer.write(FORM_FEED);
                        } else if (writer.getCurrentLineNumber() > 0) {
                            writer.write(NEW_LINE);
                        }
                        printTrain(writer, train);
                    }
                }
            }
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        } catch (IOException e1) {
            log.error("Exception in print train details: {}", e1.getLocalizedMessage());
        }
    }

    private void printSummaryTrains(HardcopyWriter writer, List<Train> trains, TrainsTableFrame trainsTableFrame)
            throws IOException {
        int maxLineLength = writer.getCharactersPerLine() - 1;
        int maxTrainNameLength = InstanceManager.getDefault(TrainManager.class).getMaxTrainNameLength();
        int maxLocationNameLength = InstanceManager.getDefault(LocationManager.class).getMaxLocationNameLength();
        String s = Bundle.getMessage("Time") +
                "  " +
                truncate(Bundle.getMessage("Name"), maxTrainNameLength) +
                truncate(Bundle.getMessage("Description")) +
                truncate(Bundle.getMessage("Route")) +
                truncate(Bundle.getMessage("Departs"), maxLocationNameLength) +
                truncate(Bundle.getMessage("Terminates"), maxLocationNameLength);
        writer.write(truncate(s, maxLineLength) + NEW_LINE);
        for (Train train : trains) {
            if (train.isBuildEnabled() || trainsTableFrame.showAllBox.isSelected()) {
                String name = truncate(train.getName(), maxTrainNameLength);
                String desc = truncate(train.getDescription());
                String route = truncate(train.getTrainRouteName());
                String departs = truncate(train.getTrainDepartsName(), maxLocationNameLength);
                String terminates = truncate(train.getTrainTerminatesName(), maxLocationNameLength);

                s = train.getDepartureTime() +
                        " " +
                        name +
                        desc +
                        route +
                        departs +
                        terminates;
                writer.write(truncate(s, maxLineLength) + NEW_LINE);
            }
        }
    }

    private String truncate(String string) {
        return truncate(string, MAX_NAME_LENGTH);
    }

    private String truncate(String string, int length) {
        return TrainCommon.padAndTruncate(string, length) + TrainCommon.SPACE;
    }

    private void printTrain() {
        if (_train == null) {
            return;
        }
        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleTrain", _train.getName()),
                Control.reportFontSize, .5, .5, .5, .5, _isPreview)) {

            printTrain(writer, _train);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        } catch (IOException ex) {
            log.error("Exception in print train: {}", ex.getLocalizedMessage());
        }
    }

    // 7 lines of header plus NEW_LINE at start
    private static final int NUMBER_OF_HEADER_LINES = 8;

    private void printTrain(HardcopyWriter writer, Train train) throws IOException {
        String s = Bundle.getMessage("Name") + ": " + train.getName() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("Description") + ": " + train.getDescription() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("Departs") + ": " + train.getTrainDepartsName() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("DepartTime") + ": " + train.getDepartureTime() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("Terminates") + ": " + train.getTrainTerminatesName() + NEW_LINE;
        writer.write(s);

        writer.write(NEW_LINE);

        s = Bundle.getMessage("Route") + ": " + train.getTrainRouteName() + NEW_LINE;
        writer.write(s);
        Route route = train.getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                s = TAB + rl.getName() + NEW_LINE;
                writer.write(s);
            }
        }
        if (!train.getComment().equals(Train.NONE)) {
            s = Bundle.getMessage("Comment") + ": " + train.getComment() + NEW_LINE;
            writer.write(s);
        }
    }

    private void loadSortByComboBox(JComboBox<String> box) {
        box.addItem(TrainsTableModel.IDCOLUMNNAME);
        box.addItem(TrainsTableModel.TIMECOLUMNNAME);
        box.addItem(TrainsTableModel.NAMECOLUMNNAME);
        box.addItem(TrainsTableModel.DESCRIPTIONCOLUMNNAME);
        box.addItem(TrainsTableModel.ROUTECOLUMNNAME);
        box.addItem(TrainsTableModel.DEPARTSCOLUMNNAME);
        box.addItem(TrainsTableModel.TERMINATESCOLUMNNAME);
        box.addItem(TrainsTableModel.STATUSCOLUMNNAME);

        box.setSelectedItem(_trainsTableFrame.getSortBy());
    }

    private int getNumberOfLines(HardcopyWriter writer, String string) {
        String[] lines = string.split(NEW_LINE);
        int count = lines.length;
        // any long lines that exceed the page width?
        for (String line : lines) {
            int add = line.length() / writer.getCharactersPerLine();
            count = count + add;
        }
        return count;
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainsFrame.class);
}

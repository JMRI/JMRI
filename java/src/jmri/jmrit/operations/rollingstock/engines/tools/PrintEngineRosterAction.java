package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EnginesTableFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of the Roster contents
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2014, 2019
 */
public class PrintEngineRosterAction extends AbstractAction {

    private int numberCharPerLine = 90;
    final int ownerMaxLen = 5; // Only show the first 5 characters of the owner's name

    EngineManager manager = InstanceManager.getDefault(EngineManager.class);

    public PrintEngineRosterAction(String actionName, boolean preview, EnginesTableFrame pWho) {
        super(actionName);
        isPreview = preview;
        panel = pWho;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    EnginesTableFrame panel;
    EnginePrintOptionFrame epof = null;

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N

    @Override
    public void actionPerformed(ActionEvent e) {
        if (epof == null) {
            epof = new EnginePrintOptionFrame(this);
        } else {
            epof.setVisible(true);
        }
        epof.initComponents();
    }

    JComboBox<String> sortByComboBox = new JComboBox<>();
    JComboBox<String> manifestOrientationComboBox = new JComboBox<>();
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();

    public void printEngines() {

        boolean landscape = false;
        if (manifestOrientationComboBox.getSelectedItem() != null &&
                manifestOrientationComboBox.getSelectedItem() == Setup.LANDSCAPE) {
            landscape = true;
        }

        int fontSize = (int) fontSizeComboBox.getSelectedItem();

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleEngineRoster"), fontSize, .5, .5, .5, .5,
                    isPreview, "", landscape, true, null);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        numberCharPerLine = writer.getCharactersPerLine();

        try {
            // create header
            writer.write(createHeader());
            
            // Loop through the Roster, printing as needed
            String number;
            String road;
            String model;
            String type;
            String length;   
            String train = "";
            String consist = "";
            String moves = "";
            String owner = "";
            String built = "";
            String dccAddress = "";
            String value = "";
            String rfid = "";
            String last = "";
            String location ="";

            List<Engine> engines = panel.enginesModel.getEngineList(sortByComboBox.getSelectedIndex());
            for (Engine engine : engines) {

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
                if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_TRAIN) {
                    train = padAttribute(engine.getTrainName().trim(), Control.max_len_string_train_name / 2);
                } else {
                    consist = padAttribute(engine.getConsistName(), Control.max_len_string_attibute);
                }

                // show one of 7 options, built is default
                if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_OWNER) {
                    owner = padAttribute(engine.getOwner(), ownerMaxLen);
                } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_MOVES) {
                    moves = padAttribute(Integer.toString(engine.getMoves()), 5);
                } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_DCC_ADDRESS) {
                    dccAddress = padAttribute(engine.getDccAddress(), 5);
                } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_LAST) {
                    last = padAttribute(engine.getLastDate().split(" ")[0], 10);
                } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_VALUE) {
                    value = padAttribute(engine.getValue(), Control.max_len_string_attibute);
                } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_RFID) {
                    rfid = padAttribute(engine.getRfid(), Control.max_len_string_attibute);
                } else {
                    built = padAttribute(engine.getBuilt(), Control.max_len_string_built_name);
                }

                if (!engine.getLocationName().equals(Engine.NONE)) {
                    location = engine.getLocationName() + " - " + engine.getTrackName();
                }

                String s = number + road + model + type + length + consist + train + moves + owner + value + rfid + dccAddress + built + last + location;
                if (s.length() > numberCharPerLine) {
                    s = s.substring(0, numberCharPerLine);
                }
                writer.write(s + NEW_LINE);
            }
        } catch (IOException we) {
            log.error("Error printing ConsistRosterEntry: " + we);
        }
        // and force completion of the printing
        writer.close();
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
        
        if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_TRAIN) {
            header.append(padAttribute(Bundle.getMessage("Train"), Control.max_len_string_train_name / 2));
        } else {
            header.append(padAttribute(Bundle.getMessage("Consist"), Control.max_len_string_attibute));
        }

        if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_OWNER) {
            header.append(padAttribute(Bundle.getMessage("Owner"), ownerMaxLen));
        } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_MOVES) {
            header.append(padAttribute(Bundle.getMessage("Moves"), 5));
        } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_VALUE) {
            header.append(padAttribute(Setup.getValueLabel(), Control.max_len_string_attibute));
        } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_LAST) {
            header.append(padAttribute(Bundle.getMessage("LastMoved"), 10));
        } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_RFID) {
            header.append(padAttribute(Setup.getRfidLabel(), Control.max_len_string_attibute));
        } else if (sortByComboBox.getSelectedIndex() == panel.enginesModel.SORTBY_DCC_ADDRESS) {
            header.append(padAttribute(Bundle.getMessage("DccAddress"), 5));
        } else {
            header.append(padAttribute(Bundle.getMessage("Built"), Control.max_len_string_built_name));
        }
        header.append(Bundle.getMessage("Location") + NEW_LINE);
        return header.toString();
    }


    private String padAttribute(String attribute, int length) {
        attribute = attribute.trim();
        if (attribute.length() > length) {
            attribute = attribute.substring(0, length);
        }
        StringBuffer buf = new StringBuffer(attribute);
        for (int i = attribute.length(); i < length + 1; i++) {
            buf.append(" ");
        }
        return buf.toString();
    }

    public class EnginePrintOptionFrame extends OperationsFrame {

        PrintEngineRosterAction pcr;
        JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));

        public EnginePrintOptionFrame(PrintEngineRosterAction pcr) {
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

            JPanel pButtons = new JPanel();
            pButtons.setLayout(new GridBagLayout());
            pButtons.add(okayButton);
            pButtons.setBorder(BorderFactory.createTitledBorder(""));
            addButtonAction(okayButton);

            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            getContentPane().add(pSortBy);
            getContentPane().add(pOrientation);
            getContentPane().add(pFontSize);
            getContentPane().add(pButtons);

            initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight250));
        }
        
        @Override
        public void initComponents() {
            if (isPreview) {
                setTitle(Bundle.getMessage("MenuItemPreview"));
            } else {
                setTitle(Bundle.getMessage("MenuItemPrint"));
            }
            loadSortByComboBox(sortByComboBox);
        }

        private void loadSortByComboBox(JComboBox<String> box) {
            box.removeAllItems();
            for (int i = panel.enginesModel.SORTBY_NUMBER; i <= panel.enginesModel.SORTBY_DCC_ADDRESS; i++) {
                box.addItem(panel.enginesModel.getSortByName(i));
            }
            box.setSelectedItem(panel.enginesModel.getSortByName());
        }

        @Override
        public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
            setVisible(false);
            pcr.printEngines();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintEngineRosterAction.class);
}

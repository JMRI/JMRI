// PrintEngineRosterAction.java
package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2014
 * @version $Revision$
 */
public class PrintEngineRosterAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5500987959098367364L;
    private int numberCharPerLine = 90;
    final int ownerMaxLen = 5; // Only show the first 5 characters of the owner's name

    EngineManager manager = EngineManager.instance();

    public PrintEngineRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (EnginesTableFrame) pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    EnginesTableFrame panel;

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleEngineRoster"), Control.reportFontSize, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        numberCharPerLine = writer.getCharactersPerLine();

        // Loop through the Roster, printing as needed
        String number;
        String road;
        String model;
        String type;
        String length;
        String owner = "";
        String consist = "";
        String built = "";
        String value = "";
        String rfid = "";
        String location;

        List<RollingStock> engines = panel.getSortByList();
        try {
            // header
            String header = padAttribute(Bundle.getMessage("Number"), Control.max_len_string_print_road_number)
                    + padAttribute(Bundle.getMessage("Road"), CarRoads.instance().getMaxNameLength())
                    + padAttribute(Bundle.getMessage("Model"), EngineModels.instance().getMaxNameLength())
                    + padAttribute(Bundle.getMessage("Type"), EngineTypes.instance().getMaxNameLength())
                    + padAttribute(Bundle.getMessage("Len"), Control.max_len_string_length_name)
                    + (panel.sortByConsist.isSelected() ? padAttribute(Bundle.getMessage("Consist"),
                                    Control.max_len_string_attibute) : padAttribute(Bundle.getMessage("Owner"), ownerMaxLen))
                    + (panel.sortByValue.isSelected() ? padAttribute(Setup.getValueLabel(),
                                    Control.max_len_string_attibute) : "")
                    + (panel.sortByRfid.isSelected() ? padAttribute(Setup.getRfidLabel(),
                                    Control.max_len_string_attibute) : "")
                    + ((!panel.sortByValue.isSelected() && !panel.sortByRfid.isSelected()) ? padAttribute(Bundle
                                    .getMessage("Built"), Control.max_len_string_built_name) : "")
                    + Bundle.getMessage("Location") + NEW_LINE;
            writer.write(header);
            for (RollingStock rs : engines) {
                Engine engine = (Engine) rs;

                // loco number
                number = padAttribute(engine.getNumber(), Control.max_len_string_print_road_number);
                road = padAttribute(engine.getRoadName(), CarRoads.instance().getMaxNameLength());
                model = padAttribute(engine.getModel(), EngineModels.instance().getMaxNameLength());
                type = padAttribute(engine.getTypeName(), EngineTypes.instance().getMaxNameLength());
                length = padAttribute(engine.getLength(), Control.max_len_string_length_name);

                if (panel.sortByConsist.isSelected()) {
                    consist = padAttribute(engine.getConsistName(), Control.max_len_string_attibute);
                } else {
                    owner = padAttribute(engine.getOwner(), ownerMaxLen);
                }

                if (panel.sortByValue.isSelected()) {
                    value = padAttribute(engine.getValue(), Control.max_len_string_attibute);
                } else if (panel.sortByRfid.isSelected()) {
                    rfid = padAttribute(engine.getRfid(), Control.max_len_string_attibute);
                } else {
                    built = padAttribute(engine.getBuilt(), Control.max_len_string_built_name);
                }

                location = "";
                if (!engine.getLocationName().equals(Engine.NONE)) {
                    location = engine.getLocationName() + " - " + engine.getTrackName();
                }

                String s = number + road + model + type + length + owner + consist + value + rfid + built + location;
                if (s.length() > numberCharPerLine) {
                    s = s.substring(0, numberCharPerLine);
                }
                writer.write(s + NEW_LINE);
            }

            // and force completion of the printing
            writer.close();
        } catch (IOException we) {
            log.error("Error printing ConsistRosterEntry: " + e);
        }
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

    private final static Logger log = LoggerFactory.getLogger(PrintEngineRosterAction.class.getName());
}

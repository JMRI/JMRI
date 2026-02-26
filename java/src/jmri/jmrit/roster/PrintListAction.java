package jmri.jmrit.roster;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.beans.BeanUtil;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.roster.swing.RosterFrame;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a very compact summary listing of the Roster contents.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Egbert Broerse Copyright (C) 2018
 */
public class PrintListAction extends jmri.util.swing.JmriAbstractAction {

    public PrintListAction(String s, jmri.util.swing.WindowInterface wi) {
        super(s, wi);
        isPreview = true;
    }

    public PrintListAction(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
        super(s, i, wi);
        isPreview = true;
    }

    public PrintListAction(String actionName, Frame frame, boolean preview) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }

    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    /**
     * Frame hosting the printing.
     */
    Frame mFrame = new Frame();

    /**
     * Variable to set whether this is to be printed or previewed.
     */
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        // obtain a HardcopyWriter to do this
        Roster r = Roster.getDefault();
        String title = Bundle.getMessage("TitleDecoderProRoster");
        String rosterGroup = r.getDefaultRosterGroup();
        // rosterGroup may legitimately be null
        // but getProperty returns null if the property cannot be found, so
        // we test that the property exists before attempting to get its value
        if (BeanUtil.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            rosterGroup = (String) BeanUtil.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        if (rosterGroup == null) {
            title = title + " " + Bundle.getMessage("ALLENTRIES");
        } else {
            title = title +
                    " " +
                    Bundle.getMessage("TitleGroup") +
                    " " +
                    Bundle.getMessage("TitleEntries", rosterGroup);
        }
        try (HardcopyWriter writer = new HardcopyWriter(mFrame, title, "SansSerif", null, 9,
                .5 * 72, .5 * 72, .5 * 72, .5 * 72, isPreview, null, null, null, null, null);) {

            // add the icon
            writer.writeDecoderProIcon();

            // Loop through the Roster, printing a 1 line list entry as needed
            List<RosterEntry> l;

            if (BeanUtil.hasProperty(wi, "allRosterEntries")) {
                l = Arrays.asList(((RosterFrame) wi).getAllRosterEntries());
            } else {
                l = r.matchingList(null, null, null, null, null, null, null); // take all
            }
            log.debug("Roster list size: {}", l.size());

            // print table column headers, match column order + widths with RosterEntry#PrintEntryLine
            // fields copied from RosterTableModel#getColumnName(int)
            List<HardcopyWriter.Column> columns = new ArrayList<>();
            columns.add(new HardcopyWriter.Column(0, 15, HardcopyWriter.Align.LEFT));  // ID
            columns.add(new HardcopyWriter.Column(0, 5, HardcopyWriter.Align.RIGHT));  // DCC Address
            columns.add(new HardcopyWriter.Column(0, 7, HardcopyWriter.Align.LEFT));   // Road name
            columns.add(new HardcopyWriter.Column(0, 6, HardcopyWriter.Align.LEFT));   // Road number
            columns.add(new HardcopyWriter.Column(0, 6, HardcopyWriter.Align.LEFT));   // Manufacturer
            columns.add(new HardcopyWriter.Column(0, 10, HardcopyWriter.Align.LEFT));  // Model
            columns.add(new HardcopyWriter.Column(0, 10, HardcopyWriter.Align.LEFT));  // Decoder model
            columns.add(new HardcopyWriter.Column(0, 12, HardcopyWriter.Align.LEFT));  // Protocol
            columns.add(new HardcopyWriter.Column(0, 6, HardcopyWriter.Align.LEFT));   // Owner
            columns.add(new HardcopyWriter.Column(0, 10, HardcopyWriter.Align.LEFT));  // Date updated

            columns = HardcopyWriter.Column.stretchColumns(columns, 
                                                           (int) writer.getPrintablePagesizePoints().getWidth(), 
                                                           writer.getFontSize() / 2);

            // If the paper is very wide, we may need to reduce the width of some columns
            // so to make the other columns a bit larger
            List<String> dccAddress = new ArrayList<>();
            List<String> protocols = new ArrayList<>();
            for (RosterEntry re : l) {
                dccAddress.add(re.getDccAddress());
                protocols.add(re.getProtocol().toString());
            }

            Rectangle2D dccAddressBounds = writer.measure(dccAddress);
            Rectangle2D protocolsBounds = writer.measure(protocols);

            boolean changed = false;

            if (Math.ceil(dccAddressBounds.getWidth()) < columns.get(1).getWidth()) {
                columns.get(1).setWidth((int) Math.ceil(dccAddressBounds.getWidth()));
                changed = true;
            }
            if (Math.ceil(protocolsBounds.getWidth()) < columns.get(7).getWidth()) {
                columns.get(7).setWidth((int) Math.ceil(protocolsBounds.getWidth()));
                changed = true;
            }

            if (changed) {
                columns = HardcopyWriter.Column.stretchColumns(columns, 
                                                               (int) writer.getPrintablePagesizePoints().getWidth(), 
                                                               writer.getFontSize() / 2);
            }

            writer.setColumns(columns);                                            
            String headerText = "";
            // IDCOL (= Filename)
            headerText += Bundle.getMessage("FieldID") + "\t";
            // ADDRESSCOL:
            headerText += Bundle.getMessage("FieldDCCAddress") + "\t";
            // ROADNAMECOL:
            headerText += Bundle.getMessage("FieldRoadName") + "\t";
            // ROADNUMBERCOL:
            headerText += Bundle.getMessage("FieldRoadNumber") + "\t";
            // MFGCOL:
            headerText += Bundle.getMessage("FieldManufacturer") + "\t";
            // MODELCOL:
            headerText += Bundle.getMessage("FieldModel") + "\t";
            // DECODERCOL:
            headerText += Bundle.getMessage("FieldDecoderModel") + "\t";
            // PROTOCOL:
            headerText += Bundle.getMessage("FieldProtocol") + "\t";
            // OWNERCOL:
            headerText += Bundle.getMessage("FieldOwner") + "\t";
            // DATEUPDATECOL:
            headerText += Bundle.getMessage("FieldDateUpdated") + "\n";

            int currentPageNumber = -1;

            for (RosterEntry re : l) {
                if (currentPageNumber != writer.getPageNum()) {
                    currentPageNumber = writer.getPageNum();
                    try {
                        // start a new line
                        writer.write("\n", 0, 1);
                        writer.setFont(null, Font.BOLD, null);
                        writer.write(headerText);
                        writer.setFont(null, Font.PLAIN, null);
                        writer.leaveVerticalSpace(writer.getLineHeight()/2);
                    } catch (IOException ex) {
                        log.warn("error during printing", ex);
                    }
                }
                if (rosterGroup != null) {
                    if (re.getAttribute(Roster.getRosterGroupProperty(rosterGroup)) != null &&
                            re.getAttribute(Roster.getRosterGroupProperty(rosterGroup)).equals("yes")) {
                        re.printEntryLine(writer);
                    }
                } else {
                    re.printEntryLine(writer);
                }
            }

            // and force completion of the printing
            // writer.close(); not needed when using try / catch
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    @Override
    public void setParameter(String parameter, String value) {
        parameter = parameter.toLowerCase();
        value = value.toLowerCase();
        if (parameter.equals("ispreview")) {
            isPreview = value.equals("true");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintListAction.class);

}

package jmri.jmrit.symbolicprog;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.util.FileUtil;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print the information in the CV table.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003; D Miller Copyright 2003, 2005
 */
public class PrintCvAction extends AbstractAction {

    final int TABLE_COLS = 3;

    public PrintCvAction(String actionName, CvTableModel pModel, PaneProgFrame pParent, boolean preview, RosterEntry pRoster) {
        super(actionName);
        mModel = pModel;
        mFrame = pParent;
        isPreview = preview;
        mRoster = pRoster;
    }

    /**
     * Frame hosting the printing
     */
    PaneProgFrame mFrame;
    CvTableModel mModel;
    RosterEntry mRoster;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    public void printInfoSection(HardcopyWriter w) {
        ImageIcon icon = new ImageIcon(FileUtil.findURL("resources/decoderpro.gif", FileUtil.Location.INSTALLED));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        w.write(icon.getImage(), new JLabel(icon));
        w.setFontStyle(Font.BOLD);
        //Add a number of blank lines
        int height = icon.getImage().getHeight(null);
        int blanks = (height - w.getLineAscent()) / w.getLineHeight();

        try {
            for (int i = 0; i < blanks; i++) {
                String s = "\n";
                w.write(s, 0, s.length());
            }
        } catch (IOException e) {
            log.warn("error during printing: " + e);
        }
        mRoster.printEntry(w);
        w.setFontStyle(Font.PLAIN);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, mFrame.getRosterEntry().getId(), 10, .8, .5, .5, .5, isPreview);

            // print the decoder info section, etc
            printInfoSection(writer);
            String s = "\n\n";
            writer.write(s, 0, s.length());

            //Initialize some variables to define the CV table size
            int cvCount = mModel.getRowCount();
            int tableLeft = 1, tableRight = TABLE_COLS * 24 + 1, tableTopRow = 0, tableBottomRow = 0, tableHeight = cvCount / TABLE_COLS;
            if (cvCount % TABLE_COLS > 0) {
                tableHeight++;
            }

            /*Start drawing the table of CVs. Set up the table with 4 columns of CV/Value
             pairs and Draw the table borders and lines.  Each column width is
             16 characters, including the starting vertical line, but not the
             ending one.  Therefore the total table width is 64+1 characters
             The colummn headings take 2 lines
             4 columns of 20 gives 80 CVs possible. NMRA specs only define about 70 CVs
             including all the optional ones plus some Manufacturer ones.  80 should be
             enough, although more can be added by increasing the tableHeight value
             */
            //Set the top row and draw top line to start the table of CVs
            tableTopRow = writer.getCurrentLineNumber();
            writer.write(tableTopRow, tableLeft, tableTopRow, tableRight);

            //set the bottom of the table
            tableBottomRow = tableTopRow + tableHeight + 2;

            //Draw vertical lines for columns
            for (int i = 1; i < 76; i = i + 24) {
                writer.write(tableTopRow, i, tableBottomRow, i);
            }

            //Draw remaining horozontal lines
            writer.write(tableTopRow + 2, tableLeft, tableTopRow + 2, tableRight);
            writer.write(tableBottomRow, tableLeft, tableBottomRow, tableRight);

            writer.setFontStyle(1);  //set font to Bold
            // print a simple heading with I18N
            s = String.format("%1$21s%1$24s%1$24s", Bundle.getMessage("Value")); // pad with spaces to column width, 3 x insert Value as var %1
            writer.write(s, 0, s.length());
            s = "\n";
            writer.write(s, 0, s.length());
            // NOI18N
            s = "            CV  Dec Hex             CV  Dec Hex             CV  Dec Hex\n";
            writer.write(s, 0, s.length());
            writer.setFontStyle(0); //set font back to Normal

            /* Create array to hold CV/Value strings to allow reformatting and sorting.
             * Same size as the table drawn above (4 columns*tableHeight; heading rows
             * not included
             */
            String[] cvStrings = new String[TABLE_COLS * tableHeight];

            //blank the array
            for (int i = 0; i < cvStrings.length; i++) {
                cvStrings[i] = "";
            }

            // get each CV and value
            for (int i = 0; i < mModel.getRowCount(); i++) {
                CvValue cv = mModel.getCvByRow(i);
                int value = cv.getValue();

                //convert and pad numbers as needed
                String numString = String.format("%12s", cv.number());
                String valueString = Integer.toString(value);
                String valueStringHex = Integer.toHexString(value).toUpperCase();
                if (value < 16) {
                    valueStringHex = "0" + valueStringHex;
                }
                for (int j = 1; j < 3; j++) {
                    if (valueString.length() < 3) {
                        valueString = " " + valueString;
                    }
                }
                //Create composite string of CV and its decimal and hex values
                s = "  " + numString + "  " + valueString + "  " + valueStringHex + " ";

                //populate printing array - still treated as a single column
                cvStrings[i] = s;
            }

            //sort the array in CV order (just the members with values)
            String temp;
            boolean swap = false;
            do {
                swap = false;
                for (int i = 0; i < mModel.getRowCount() - 1; i++) {
                    if (cvSortOrderVal(cvStrings[i + 1].substring(0, 15).trim()) < cvSortOrderVal(cvStrings[i].substring(0, 15).trim())) {
                        temp = cvStrings[i + 1];
                        cvStrings[i + 1] = cvStrings[i];
                        cvStrings[i] = temp;
                        swap = true;
                    }
                }
            } while (swap == true);

            //Print the array in three columns
            for (int i = 0; i < tableHeight; i++) {
                s = cvStrings[i] + cvStrings[i + tableHeight] + cvStrings[i + tableHeight * 2] + "\n";
                writer.write(s, 0, s.length());
            }
            //write an extra character to work around the
            //last character truncation bug with HardcopyWriter
            s = " \n";
            writer.write(s, 0, s.length());
        } catch (java.io.IOException ex1) {
            log.error("IO exception while printing");
            return;
        } catch (HardcopyWriter.PrintCanceledException ex2) {
            log.debug("Print cancelled");
            return;
        }

        writer.close();
    }

    /**
     * Returns a representation of a CV name as a long integer sort order value.
     * The value itself is not meaningful, but is used in comparisons when
     * sorting.
     */
    public static long cvSortOrderVal(String cvName) {
        final int MAX_CVMNUM_SPACE = 1200;

        String[] cvNumStrings = cvName.split("\\.");
        long sortVal = 0;
        for (int i = 0; i < (cvNumStrings.length); i++) {
            sortVal = (sortVal * MAX_CVMNUM_SPACE) + Integer.parseInt(cvNumStrings[i]);
        }
        return sortVal;
    }

    private final static Logger log = LoggerFactory.getLogger(PrintCvAction.class);
}

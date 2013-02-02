// PrintCvAction.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import jmri.util.davidflanagan.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import java.awt.event.*;
import jmri.jmrit.roster.RosterEntry;
import java.awt.Font;
import java.io.IOException;

import javax.swing.*;

/**
 * Action to print the information in the CV table.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author		Bob Jacobsen   Copyright (C) 2003; D Miller Copyright 2003, 2005
 * @version             $Revision$
 */
public class PrintCvAction  extends AbstractAction {

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
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        w.write(icon.getImage(), new JLabel(icon));
        w.setFontStyle(Font.BOLD);
        //Add a number of blank lines
        int height = icon.getImage().getHeight(null);
        int blanks = (height-w.getLineAscent())/w.getLineHeight();
        
        try{
            for(int i = 0; i<blanks; i++){
                String s = "\n";
                w.write(s,0,s.length());
            }
        } catch (IOException e) { log.warn("error during printing: "+e);
        }
        mRoster.printEntry(w);
        w.setFontStyle(Font.PLAIN);
    }

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
            int tableLeft = 1, tableRight = 65, tableTopRow = 0, tableBottomRow = 0, tableHeight = cvCount/4;
            if (cvCount%4 > 0) tableHeight++;

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
            writer.write(tableTopRow,tableLeft,tableTopRow,tableRight);

            //set the bottom of the table
            tableBottomRow = tableTopRow + tableHeight + 2;

            //Draw vertical lines for columns
            for (int i=1; i<66; i=i+16){
              writer.write(tableTopRow,i,tableBottomRow,i);
            }

            //Draw remaining horozontal lines
            writer.write(tableTopRow+2,tableLeft,tableTopRow+2,tableRight);
            writer.write(tableBottomRow,tableLeft,tableBottomRow,tableRight);

            writer.setFontStyle(1);  //set font to Bold
            // print a simple heading
            s = "         Value           Value           Value           Value\n";
            writer.write(s, 0, s.length());
            s = "   CV   Dec Hex    CV   Dec Hex    CV   Dec Hex    CV   Dec Hex\n";
            writer.write(s, 0, s.length());
            writer.setFontStyle(0); //set font back to Normal

            //create an array to hold CV/Value strings to allow reformatting and sorting
            //Same size as the table drawn above (4 columns*tableHeight; heading rows
            //not included

            String[] cvStrings= new String[4*tableHeight];

            //blank the array
            for (int i=0; i < cvStrings.length; i++) cvStrings[i] = "";

            // get each CV and value
            for (int i=0; i<mModel.getRowCount(); i++) {
                CvValue cv = mModel.getCvByRow(i);
                int num = cv.number();
                int value = cv.getValue();

                //convert and pad numbers as needed
                String numString = Integer.toString(num);
                String valueString = Integer.toString(value);
                String valueStringHex = Integer.toHexString(value).toUpperCase();
                if (value<16) valueStringHex = "0"+ valueStringHex;
                for (int j=1; j<3; j++){
                  if (numString.length() < 3) numString = " "+numString;
                }
                for (int j=1; j<3; j++) {
                  if (valueString.length() < 3) valueString = " "+ valueString;
                }
                //Create composite string of CV and its decimal and hex values
                s = "  " + numString + "   " + valueString + "  " + valueStringHex + " ";

                //populate printing array - still treated as a single column
                cvStrings[i] =  s;
            }
            //sort the array in CV order (just the members with values)
            String temp;
            boolean swap=false;
            do {
            swap=false;
            for (int i=0; i < mModel.getRowCount()-1; i++){
              if (Integer.parseInt(cvStrings[i+1].substring(2,5).trim())<Integer.parseInt(cvStrings[i].substring(2,5).trim())) {
                temp=cvStrings[i+1];
                cvStrings[i+1]=cvStrings[i];
                cvStrings[i]=temp;
                swap=true;
              }
            }
            }
            while (swap==true);

            //Print the array in four columns
            for (int i=0; i < tableHeight; i++){
              s=cvStrings[i]+cvStrings[i+tableHeight]+cvStrings[i+tableHeight*2]+cvStrings[i+tableHeight*3]+"\n";
              writer.write(s, 0, s.length());
            }
            //write an extra character to work around the
            //last character truncation bug with HardcopyWriter
            s=" \n";
            writer.write(s, 0, s.length());
        }
        catch (java.io.IOException ex1) {
            log.error("IO exception while printing");
            return;
        }
        catch (HardcopyWriter.PrintCanceledException ex2) {
            log.debug("Print cancelled");
            return;
        }

        writer.close();
    }

    static Logger log = Logger.getLogger(PrintCvAction.class.getName());
}

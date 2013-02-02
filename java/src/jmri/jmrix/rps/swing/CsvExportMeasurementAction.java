// CsvExportMeasurementAction.java

package jmri.jmrix.rps.swing;

import org.apache.log4j.Logger;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;

/**
 * Action to export the incoming raw data to a CSV-format file
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version     $Revision$
 * @since 2.3.1
 */
public class CsvExportMeasurementAction extends AbstractAction implements MeasurementListener {

    public CsvExportMeasurementAction(String actionName) {
        super(actionName);
    }

    public CsvExportMeasurementAction() {
        this("Start CSV Export Measurement...");
    }

    JFrame mParent ;

    boolean logging = false;
    PrintStream str;
    JFileChooser fileChooser;
    
    public void actionPerformed(ActionEvent e) {
        if (logging) stopLogging(e);
        else startLogging(e);
    }
    
    void stopLogging(ActionEvent e) {
        Distributor.instance().removeMeasurementListener(this);
        
        // reset menu item
        ((JMenuItem)(e.getSource())).setText("Start CSV Export Measurement...");
        
        logging = false;
        
        str.flush();
        str.close();
    }
    
    void startLogging(ActionEvent e) {
        
        System.out.println(""+e);
        ((JMenuItem)(e.getSource())).setText("Stop CSV Export Measurement...");

        // initialize chooser
        if ( fileChooser == null ){
            fileChooser = new JFileChooser() ;
        } else {
            fileChooser.rescanCurrentDirectory();
        }

        // get file
        int retVal = fileChooser.showSaveDialog( mParent ) ;

        if(retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled())  log.debug("start to log to file "+file);

            try {

                str = new PrintStream(new FileOutputStream(file));

                Distributor.instance().addMeasurementListener(this);
                
                logging = true;
            }
            catch (IOException ex) {
                log.error("Error opening file: "+ex);
            }
        }
    }

    public void notify(Measurement m) {
        if (!logging || str == null) return;
        // first measurement info
        str.print(""+m.getID()+","+m.getX()+","+m.getY()+","+m.getZ()+","+m.getCode()+",");
        // then reading info
        Reading r = m.getReading();
        for (int i = 0; i<r.getNValues()-1; i++) {
            str.print(r.getValue(i)+",");
        }
        str.println(r.getValue(r.getNValues()-1));
    }
    
    static Logger log = Logger.getLogger(CsvExportMeasurementAction.class.getName());
}

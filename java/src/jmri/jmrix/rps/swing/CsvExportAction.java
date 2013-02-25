// CsvExportAction.java

package jmri.jmrix.rps.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.ReadingListener;
import jmri.jmrix.rps.Reading;

/**
 * Action to export the incoming raw data to a CSV-format file
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version     $Revision$
 * @since 2.3.1
 */
public class CsvExportAction extends AbstractAction implements ReadingListener {

    public CsvExportAction(String actionName) {
        super(actionName);
    }

    public CsvExportAction() {
        this("Start CSV Export Reading...");
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
        Distributor.instance().removeReadingListener(this);
        
        // reset menu item
        ((JMenuItem)(e.getSource())).setText("Start CSV Export Reading...");
        
        logging = false;
        
        str.flush();
        str.close();
    }
    
    void startLogging(ActionEvent e) {
        
        System.out.println(""+e);
        ((JMenuItem)(e.getSource())).setText("Stop CSV Export Reading...");

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

                Distributor.instance().addReadingListener(this);
                
                logging = true;
            }
            catch (IOException ex) {
                log.error("Error opening file: "+ex);
            }
        }
    }

    public void notify(Reading r) {
        if (!logging || str == null) return;
        str.print(r.getID()+",");
        for (int i = 0; i<r.getNValues()-1; i++) {
            str.print(r.getValue(i)+",");
        }
        str.println(r.getValue(r.getNValues()-1));
    }
    
    static Logger log = LoggerFactory.getLogger(CsvExportAction.class.getName());
}

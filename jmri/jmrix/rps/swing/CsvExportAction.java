// CsvExportAction.java

package jmri.jmrix.rps.swing;

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
 * @version     $Revision: 1.1 $
 * @since 2.3.1
 */
public class CsvExportAction extends AbstractAction implements ReadingListener {

    public CsvExportAction(String actionName) {
        super(actionName);
    }

    public CsvExportAction() {
        this("Start CSV Export...");
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
        ((JMenuItem)(e.getSource())).setText("Start CSV Export...");
        
        logging = false;
        
        str.flush();
        str.close();
    }
    
    void startLogging(ActionEvent e) {
        
        System.out.println(""+e);
        ((JMenuItem)(e.getSource())).setText("Stop CSV Export...");

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
        if (r.getRawData() == null)
            str.println("<no valid line>");
        else
            str.println(r.getRawData());
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CsvExportAction.class.getName());
}

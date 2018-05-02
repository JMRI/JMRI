package jmri.jmrix.rps.swing;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the incoming raw data to a CSV-format file
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class CsvExportMeasurementAction extends AbstractAction implements MeasurementListener {

    RpsSystemConnectionMemo memo = null;

    public CsvExportMeasurementAction(String actionName,RpsSystemConnectionMemo _memo) {
        super(actionName);
        memo = _memo;
    }

    public CsvExportMeasurementAction(RpsSystemConnectionMemo _memo) {
        this("Start CSV Export Measurement...",_memo);
    }

    JFrame mParent;

    boolean logging = false;
    PrintStream str;
    JFileChooser fileChooser;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (logging) {
            stopLogging(e);
        } else {
            startLogging(e);
        }
    }

    void stopLogging(ActionEvent e) {
        Distributor.instance().removeMeasurementListener(this);

        // reset menu item
        ((JMenuItem) (e.getSource())).setText("Start CSV Export Measurement...");

        logging = false;

        str.flush();
        str.close();
    }

    void startLogging(ActionEvent e) {

        System.out.println("" + e);
        ((JMenuItem) (e.getSource())).setText("Stop CSV Export Measurement...");

        // initialize chooser
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        } else {
            fileChooser.rescanCurrentDirectory();
        }

        // get file
        int retVal = fileChooser.showSaveDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to log to file " + file);
            }

            try {

                str = new PrintStream(new FileOutputStream(file));

                Distributor.instance().addMeasurementListener(this);

                logging = true;
            } catch (IOException ex) {
                log.error("Error opening file: " + ex);
            }
        }
    }

    @Override
    public void notify(Measurement m) {
        if (!logging || str == null) {
            return;
        }
        // first measurement info
        str.print("" + m.getId() + "," + m.getX() + "," + m.getY() + "," + m.getZ() + "," + m.getCode() + ",");
        // then reading info
        Reading r = m.getReading();
        for (int i = 0; i < r.getNValues() - 1; i++) {
            str.print(r.getValue(i) + ",");
        }
        str.println(r.getValue(r.getNValues() - 1));
    }

    private final static Logger log = LoggerFactory.getLogger(CsvExportMeasurementAction.class);
}

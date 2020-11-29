package jmri.jmrix.rps.swing;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the incoming raw data to a CSV-format file.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class CsvExportMeasurementAction extends AbstractAction implements MeasurementListener {

    RpsSystemConnectionMemo memo = null;

    public CsvExportMeasurementAction(String actionName, RpsSystemConnectionMemo _memo) {
        super(actionName);
        memo = _memo;
    }

    public CsvExportMeasurementAction(RpsSystemConnectionMemo _memo) {
        this("Start CSV Export Measurement...", _memo);
    }

    JFrame mParent;

    boolean logging = false;
    CSVPrinter str;
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

        try {
            str.flush();
            str.close();
        } catch (IOException ex) {
            log.error("Error closing file: {}", ex);
        }
    }

    void startLogging(ActionEvent e) {

        log.debug("{}", e);
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
            log.debug("start to log to file {}", file);

            try {

                str = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT);

                Distributor.instance().addMeasurementListener(this);

                logging = true;
            } catch (IOException ex) {
                log.error("Error opening file: {}", ex);
            }
        }
    }

    @Override
    public void notify(Measurement m) {
        if (!logging || str == null) {
            return;
        }
        ArrayList<Object> values = new ArrayList<>();
        // first measurement info
        values.addAll(Arrays.asList(new Object[]{m.getId(), m.getX(), m.getY(), m.getZ(), m.getCode()}));
        // then reading info
        Reading r = m.getReading();
        values.addAll(Arrays.asList(r.getValues()));
        try {
            str.printRecord(values);
        } catch (IOException ex) {
            log.error("Error writing file {}", ex);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CsvExportMeasurementAction.class);

}

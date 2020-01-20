package jmri.jmrix.bachrus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import jmri.util.FileUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a dimensionless speed profile of a DCC decoder.
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 */
public class DccSpeedProfile {

    protected int _length;
    protected float[] _dataPoints;
    protected float _max;
    // index of last valid data point, -1 means no data
    protected int _lastPoint;
    protected List<CSVRecord> dccProfileData;

    public DccSpeedProfile(int len) {
        _length = len;
        _dataPoints = new float[_length];

        for (int i = 0; i < _length; i++) {
            _dataPoints[i] = 0.0F;
        }
        _max = 40;
        _lastPoint = -1;
    }

    public boolean setPoint(int idx, float val) {
        boolean ret = false;
        if (idx < _length) {
            _dataPoints[idx] = val;
            _lastPoint++;
            log.debug("Index: " + idx + " val: " + val);
            if (val > _max) {
                log.debug("     Old max: " + _max);
                // Adjust maximum value
                _max = (float) (Math.floor(val / 20) + 1) * 20;
                log.debug("     New max: " + _max);
            }
            ret = true;
        }
        return ret;
    }

    public void clear() {
        for (int i = 0; i < _length; i++) {
            _dataPoints[i] = 0.0F;
        }
        _max = 40;
        _lastPoint = -1;
    }

    public float getPoint(int idx) {
        if ((idx < _length) && (idx <= _lastPoint)) {
            return _dataPoints[idx];
        } else {
            return -1;
        }
    }

    public int getLength() {
        return _length;
    }

    public void setMax(float m) {
        _max = m;
    }

    public float getMax() {
        return _max;
    }

    public int getLast() {
        return _lastPoint;
    }

    public static void printHeading(CSVPrinter p, int address) throws IOException {
        if (p != null) {
            Date today;
            String result;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
            today = new Date();
            result = formatter.format(today);
            // title 
            String annotate = "Bachrus MTS-DCC " + Bundle.getMessage("ProfileFor") + " "
                    + address + " " + Bundle.getMessage("CreatedOn")
                    + " " + result;
            p.print(annotate);
            p.println();
        }
    }

    // Save data as CSV
    public static void export(DccSpeedProfile sp, int address, String dirString, int units) {
        try {
            openExportFile();

            String unitsString;
            if (units == Speed.MPH) {
                unitsString = "MPH";
            } else {
                unitsString = "KPH";
            }
            // Save rows
            if (p != null) {
                printHeading(p, address);
                p.print("Step");
                p.print("Speed(" + dirString + " " + unitsString + ")");
                p.println();
                // for each data point
                for (int i = 0; i < sp.getLength(); i++) {
                    p.print(i);
                    if (units == Speed.MPH) {
                        p.print(Speed.kphToMph(sp.getPoint(i)));
                    } else {
                        p.print(sp.getPoint(i));
                    }
                    p.println();
                }
            }
            closeExportFile();
        } catch (IOException ex) {
            log.error("Error exporting speed profile", ex);
        }
    }

    public static void export(DccSpeedProfile[] sp, int address, int units) {
        try {
            openExportFile();

            String unitsString;
            if (units == Speed.MPH) {
                unitsString = "MPH";
            } else {
                unitsString = "KPH";
            }
            // Save rows
            if (p != null) {
                printHeading(p, address);
                p.print("Step");
                p.print("Speed(fwd " + unitsString + ")");
                p.print("Speed(rev " + unitsString + ")");
                p.println();
                // for each data point
                for (int i = 0; i < sp[0].getLength(); i++) {
                    p.print(i);
                    // for each profile
                    for (DccSpeedProfile item : sp) {
                        if (units == Speed.MPH) {
                            p.print(Speed.kphToMph(item.getPoint(i)));
                        } else {
                            p.print(item.getPoint(i));
                        }
                    }
                    p.println();
                }
            }
            closeExportFile();
        } catch (IOException ex) {
            log.error("Error exporting speed profile", ex);
        }
    }

    private static CSVPrinter p = null;

    private static void openExportFile() {
        JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());

        // get filename
        // start at current file, show dialog
        int retVal = fileChooser.showSaveDialog(null);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // Create a print writer based on the file, so we can print to it.
                p = new CSVPrinter(new FileWriter(file), CSVFormat.DEFAULT);
            } catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Problem creating output stream " + ex);
                }
            }
            if (p == null) {
                log.error("Null Print Writer");
            }
        }
    }

    private static void closeExportFile() throws IOException {
        if (p != null) {
            p.flush();
            p.close();
        }
    }

    public int importDccProfile(int units) {
        openImportFile();
        if (dccProfileData.size() < 31) {
            log.error("Not enough lines in reference speed profile file");
            clear();
            return -1;
        }

        String secondLine = dccProfileData.get(1).toString();
        if (!(secondLine.contains("MPH") || secondLine.contains("KPH"))) {
            log.error("Bad 'units' format on line 2 of reference speed profile file");
            clear();
            return -1;
        }
        for (int i = 2; i < dccProfileData.size(); i++) {
            try {
                String value = dccProfileData.get(i).get(1);
                float speed = Float.valueOf(value);
                // speed values from the speedometer are calc'd and stored in 
                // the DccSpeedProfile object as KPH so need to convert
                // if the file was in MPH
                if (secondLine.contains("MPH")) {
                    speed = Speed.mphToKph(speed);
                }

                setPoint(i - 2, speed);
            } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                log.error("Bad data or format in reference speed profile file: " + ex);
                clear();
                return -1;
            }
        }
        return 0;
    }

    private void openImportFile() {
        JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());

        // get filename
        // start at current file, show dialog
        int retVal = fileChooser.showOpenDialog(null);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                dccProfileData = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT).getRecords();
            } catch (IOException ex) {
                log.error("Failed to read reference profile file " + ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DccSpeedProfile.class);

}

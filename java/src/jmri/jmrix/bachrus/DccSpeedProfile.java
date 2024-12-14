package jmri.jmrix.bachrus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
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
            log.debug("Index: {} val: {}", idx, val);
            if (val > _max) {
                log.debug("     Old max: {}", _max);
                // Adjust maximum value
                _max = (float) (Math.floor(val / 20) + 1) * 20;
                log.debug("     New max: {}", _max);
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

    public static void printHeading(@Nonnull CSVPrinter p, int address) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        String today = formatter.format(new Date());
        // title
        String annotate = Bundle.getMessage("ProfileFor") + " "
                + address + " " + Bundle.getMessage("CreatedOn")
                + " " + today;
        // should this be printComment instead?
        p.printRecord(annotate);
    }

    // Save data as CSV
    public static void export(DccSpeedProfile sp, int address, String dirString, Speed.Unit unit) {
        File file = openExportFile();
        try (CSVPrinter p = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)), CSVFormat.DEFAULT)) {
            String unitsString;
            if (unit == Speed.Unit.MPH) {
                unitsString = "MPH";
            } else {
                unitsString = "KPH";
            }
            // Save rows
            printHeading(p, address);
            p.printRecord("Step", "Speed(" + dirString + " " + unitsString + ")");
            // for each data point
            for (int i = 0; i < sp.getLength(); i++) {
                p.printRecord(i, unit == Speed.Unit.MPH ? Speed.kphToMph(sp.getPoint(i)) : sp.getPoint(i));
            }
            p.flush();
            p.close();
        } catch (IOException ex) {
            log.error("Error exporting speed profile", ex);
        }
    }

    public static void export(DccSpeedProfile[] sp, int address, Speed.Unit unit) {
        File file = openExportFile();
        try (CSVPrinter p = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)), CSVFormat.DEFAULT)) {

            String unitsString;
            if (unit == Speed.Unit.MPH) {
                unitsString = "MPH";
            } else {
                unitsString = "KPH";
            }
            // Save rows
            printHeading(p, address);
            p.printRecord("Step", "Speed(fwd " + unitsString + ")", "Speed(rev " + unitsString + ")");
            // for each data point
            for (int i = 0; i < sp[0].getLength(); i++) {
                ArrayList<Object> list = new ArrayList<>();
                list.add(i);
                // for each profile
                for (DccSpeedProfile item : sp) {
                    list.add(unit == Speed.Unit.MPH ? Speed.kphToMph(item.getPoint(i)) : item.getPoint(i));
                }
                p.printRecord(list);
            }
            p.flush();
            p.close();
        } catch (IOException ex) {
            log.error("Error exporting speed profile", ex);
        }
    }

    private static File openExportFile() {
        JFileChooser fileChooser = new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public int importDccProfile(Speed.Unit unit) {
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
                float speed = Float.parseFloat(value);
                // speed values from the speedometer are calc'd and stored in
                // the DccSpeedProfile object as KPH so need to convert
                // if the file was in MPH
                if (secondLine.contains("MPH")) {
                    speed = Speed.mphToKph(speed);
                }

                setPoint(i - 2, speed);
            } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                log.error("Bad data or format in reference speed profile file", ex);
                clear();
                return -1;
            }
        }
        return 0;
    }

    private void openImportFile() {
        JFileChooser fileChooser = new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                dccProfileData = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT).getRecords();
            } catch (IOException ex) {
                log.error("Failed to read reference profile file", ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DccSpeedProfile.class);

}

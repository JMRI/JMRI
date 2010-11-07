// DccSpeedProfile.java

package jmri.jmrix.bachrus;

import java.io.*;
import javax.swing.JFileChooser;

/**
 * Class to represent a dimensionless speed profile of a DCC decoder.
 * 
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision: 1.1 $
 */
public class DccSpeedProfile {

    protected int _length;
    protected float[] _dataPoints;
    protected float _max;
    // index of last valid data point, -1 means no data
    protected int _lastPoint;

    public DccSpeedProfile(int len) {
        _length = len;
        _dataPoints = new float[_length];

        for (int i=0; i<_length; i++) {
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
            if (val > _max) {
                // Adjust maximum value
                _max = (float)(Math.floor(val/20)+1)*20;
            }
            ret = true;
        }
        return ret;
    }

    public void clear() {
        for (int i=0; i<_length; i++) {
            _dataPoints[i] = 0.0F;
        }
        _lastPoint = -1;
    }
    
    public float getPoint(int idx) {
        if ((idx < _length) && (idx <= _lastPoint)) {
            return _dataPoints[idx];
        } else {
            return -1;
        }
    }

    public int getLength() { return _length; }
    public void setMax(float m) { _max = m; }
    public float getMax() { return _max; }
    public int getLast() { return _lastPoint; }


    // Save data as CSV
    public static void export(DccSpeedProfile sp) {
        openExportFile();

        // Save rows
        if ((out != null) && (p != null)) {
            // for each data point
            for (int i = 0; i < sp.getLength(); i++) {
                p.print(i);
                p.print(",");
                p.println(sp.getPoint(i));
            }
        }
        closeExportFile();
    }

    public static void export(DccSpeedProfile [] sp) {
        openExportFile();
        // Save rows
        if ((out != null) && (p != null)) {
            // for each data point
            for (int i = 0; i < sp[0].getLength(); i++) {
                p.print(i);
                // for each profile
                for (int j = 0; j < sp.length; j++) {
                    p.print(",");
                    p.println(sp[j].getPoint(i));
                }
            }
        }
        closeExportFile();
    }

    private static FileOutputStream out = null;
    private static PrintWriter p = null;

    private static void openExportFile() {
        JFileChooser fileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
        String fileName = null;
        
        // get filename
        // start at current file, show dialog
        int retVal = fileChooser.showSaveDialog(null);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
          fileName = fileChooser.getSelectedFile().getPath();
        }
        try {
            // Create a print writer based on the file, so we can print to it.
            out = new FileOutputStream(fileName);
            p = new PrintWriter(out, true);
        } catch (IOException ex) {
            if (log.isDebugEnabled()) log.debug("Problem creating output stream " + ex);
        }
        if (out == null) { log.error("Null File Output Stream"); }
        if (p == null) { log.error("Null Print Writer"); }
    }

    private static void closeExportFile() {
        try {
            if (p != null) {
                p.flush();
                p.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException ex) {
            log.error("Exception writing CSV " + ex);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DccSpeedProfile.class.getName());
}

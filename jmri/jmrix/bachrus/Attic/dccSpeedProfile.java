// GraphPane.java

package jmri.jmrix.bachrus;

import java.io.*;
import javax.swing.JFileChooser;

/**
 * Class to represent a dimensionless speed profile of a DCC decoder.
 * 
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision: 1.6 $
 */
public class dccSpeedProfile {

    protected int _length;
    protected float[] _dataPoints;
    protected float _max;
    // index of last valid data point, -1 means no data
    protected int _lastPoint;

    public dccSpeedProfile(int len) {
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

    final JFileChooser fileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());

    // Save data as CSV
    @SuppressWarnings("null")
    public void export() {
        String fileName = null;
        //File saveFile;
        
        log.debug("Export()");
        
        // get filename
        // start at current file, show dialog
        int retVal = fileChooser.showSaveDialog(null);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
          fileName = fileChooser.getSelectedFile().getPath();
          //saveFile = new File(fileName);
        }
        FileOutputStream out = null;
        PrintWriter p = null;
        try {
            // Create a print writer based on the file, so we can print to it.
            out = new FileOutputStream(fileName);
            p = new PrintWriter(out, true);
        } catch (IOException ex) {
            if (log.isDebugEnabled()) log.debug("Problem creating output stream " + ex);
        }

        if (out == null) log.error("Null File Output Stream");
        if (p == null) log.error("Null Print Writer");

        // Save rows
        if ((out != null) && (p != null)) {
            for (int i = 0; i < _length; i++) {
                p.print(i);
                p.print(",");
                p.println(_dataPoints[i]);
            }
        }
        
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(dccSpeedProfile.class.getName());
}

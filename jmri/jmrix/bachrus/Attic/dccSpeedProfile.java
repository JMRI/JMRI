// GraphPane.java

package jmri.jmrix.bachrus;

/**
 * Class to represent the speed profile of a DCC decoder
 *
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision: 1.1 $
 */
public class dccSpeedProfile {

    protected int _length;
    protected float[] _dataPoints;
    protected float _max;

    public dccSpeedProfile(int len) {
        _length = len;
        _dataPoints = new float[_length];

        for (int i=0; i<_length; i++) {
            _dataPoints[i] = 0.0F;
        }
        _max = 25;
    }
    
    public boolean setPoint(int idx, float val) {
        boolean ret = false;
        if (idx < _length) {
            _dataPoints[idx] = val;
            if (val > _max) {
                // Adjust maximum value
                _max = (float)(Math.floor(val/20)+1)*20;
            }
            ret = true;
        }
        return ret;
    }

    public float getPoint(int idx) {
        if (idx < _length) {
            return _dataPoints[idx];
        } else {
            return 0;
        }
    }

    public int getLength() { return _length; }
    public float getMax() { return _max; }

    // Save data as CSV
    public void export() {

    }
}

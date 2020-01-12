package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some helpful implementations and values for Calculators.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public abstract class AbstractCalculator implements Calculator {

    // Sensor position objects, fully packed array indexed from zero
    Point3d sensors[];

    // Values for usual calculators
    // These are fully packed (start at index 0, no skipped entries)
    double[] Tr;  // received time
    double[] Xr;  // receiver X
    double[] Yr;  // receiver Y
    double[] Zr;  // receiver Z
    int nr;

    public void prep(Reading r) {

        if (log.isDebugEnabled()) {
            log.debug("Reading: {}", r.toString());
            log.debug("Sensors: {}", sensors.length);
            if (sensors.length >= 1 && sensors[0] != null) {
                log.debug("Sensor[0]: {},{},{}", sensors[0].x, sensors[0].y, sensors[0].z);
            }
            if (sensors.length >= 2 && sensors[1] != null) {
                log.debug("Sensor[1]: {},{},{}", sensors[1].x, sensors[1].y, sensors[1].z);
            }
        }

        nr = r.getNValues();

        // zero doesn't count in receivers
        if (nr != sensors.length - 1) {
            log.error("Mismatch: {} readings, {} receivers", nr, (sensors.length - 1));
        }
        nr = Math.min(nr, sensors.length - 1); // accept the shortest

        Tr = new double[nr];
        Xr = new double[nr];
        Yr = new double[nr];
        Zr = new double[nr];

        // generate vector
        int j = 0;
        for (int i = 0; i <= nr; i++) {
            if (sensors[i] != null) {
                Tr[j] = r.getValue(i);
                Xr[j] = sensors[i].x;
                Yr[j] = sensors[i].y;
                Zr[j] = sensors[i].z;
                j++;
            }
        }
        nr = j;

        summarize();
    }

    void summarize() {
        log.debug("nr is {}", nr);
        for (int j = 0; j < nr; j++) {
            log.debug(" t: {} to {},{},{}", Tr[j], Xr[j], Yr[j], Zr[j]);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractCalculator.class);

}

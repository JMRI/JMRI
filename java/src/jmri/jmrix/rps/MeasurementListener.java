package jmri.jmrix.rps;

/**
 * Connect to a source of Measurements.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public interface MeasurementListener {

    public void notify(Measurement r);

}

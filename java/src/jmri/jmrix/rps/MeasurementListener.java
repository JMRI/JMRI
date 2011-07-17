// MeasurementListener.java

package jmri.jmrix.rps;

/**
 * Connect to a source of Measurements
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public interface MeasurementListener {

    public void notify(Measurement r);

}

/* @(#)MeasurementListener.java */

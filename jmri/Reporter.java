// Reporter.java

package jmri;

/**
 * Represent a device that can report identification information.
 * <P>
 * Repproting devices might include:
 * <ul>
 *  <li>A DCC device that reports a locomotive number
 *      when it's in a particular location
 *  <LI>A device that reports something about the layout
 *      environment, e.g. the current drawn or light intensity
 *  <LI>A device that reacts to some happening on the layout
 *      with a complicated report
 *</UL>
 * <P>In contrast to Sensors, a Reporter provides more detailed
 * information. A Sensor provides a status of ACTIVE or INACTIVE, while
 * a Reporter returns an Object.  The real type of that object can
 * be whatever a particular Reporter finds useful to report.  Typical
 * values might be a String or Int, both of which can be displayed, 
 * printed, equated, etc.
 * <P>
 * A Reporter might also not be able to report all the time.  The
 * previous value remains available, but it's also possible to 
 * distinquish this case by using the getCurrentReport member function.
 * 
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.1 $
 * @see         jmri.Sensor
 * @see         jmri.AbstractReporter
 * @see         jmri.ReporterManager
 * @see         jmri.InstanceManager
 */
public interface Reporter extends NamedBean {

    /**
     * Query the last report.  This will return a 
     * value even if there's no current report available.
     */
    public Object getLastReport();

    /**
     * Query the current report.  If there is no current report
     * available (e.g. the reporting hardware says no information is
     * is currently available, this will return a null object.
     */
    public Object getCurrentReport();
    
    /**
     * Provide an int form of the last report.
     *
     */
    public int getState();

}

/* @(#)Reporter.java */

package jmri;

/**
 * Represent a device that can report identification information.
 * <p>
 * Reporting devices might include:
 * <ul>
 * <li>A DCC device that reports a locomotive number when it's in a particular
 * location
 * <li>A device that reports something about the layout environment, e.g. the
 * current drawn or light intensity
 * <li>A device that reacts to some happening on the layout with a complicated
 * report
 * </ul>
 * <p>
 * In contrast to Sensors, a Reporter provides more detailed information. A
 * Sensor provides a status of ACTIVE or INACTIVE, while a Reporter returns an
 * Object. The real type of that object can be whatever a particular Reporter
 * finds useful to report. Typical values might be a String or Int, both of
 * which can be displayed, printed, equated, etc.
 * <p>
 * A Reporter might also not be able to report all the time. The previous value
 * remains available, but it's also possible to distinguish this case by using
 * the getCurrentReport member function.
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Matthew Harris Copyright (C) 2011
 * @see jmri.Sensor
 * @see jmri.ReporterManager
 * @see jmri.InstanceManager
 */
public interface Reporter extends NamedBean {

    /**
     * Query the last report. This will return a value even if there's no
     * current report available. If there is a current report, both this and the
     * current report will be equal. If nothing has ever been reported, this
     * will return a null object.
     *
     * @return the last report or null
     */
    public Object getLastReport();

    /**
     * Query the current report. If there is no current report available (e.g.
     * the reporting hardware says no information is currently available) this
     * will return a null object.
     *
     * @return the current report or null
     */
    public Object getCurrentReport();

    /**
     * Set the report to an arbitrary object.
     * <p>
     * A Reporter object will usually just "report"; its contents usually come
     * from the layout, and hence are only set by lower-level implementation
     * classes. But there are occasionally reasons to set it from inside the
     * program, e.g. debugging via entering values in the Reporter Table. Hence
     * provision of this method.
     *
     * @param r the report
     */
    public void setReport(Object r);

    /**
     * Provide an integer form of the last report.
     *
     */
    @Override
    public int getState();

}

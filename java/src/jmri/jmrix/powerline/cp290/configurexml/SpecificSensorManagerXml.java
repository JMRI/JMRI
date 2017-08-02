package jmri.jmrix.powerline.cp290.configurexml;

/**
 * Simple class to allow configurexml to locate a persistance class for
 * {@link jmri.jmrix.powerline.SerialSensorManager} and its subclasses.
 * <p>
 * This extends {@link jmri.jmrix.powerline.configurexml.SerialSensorManagerXml}
 * without changing any of its behavior. This lets the configuration system
 * locate a mirror class for
 * {@link jmri.jmrix.powerline.cm11.SpecificSensorManager}, but the actual
 * loading and storing will be done by
 * {@link jmri.jmrix.powerline.configurexml.SerialSensorManagerXml} in terms of
 * which-ever manager object is currently installed.
 *
 * @author Bob Jacobsen 2008
 * @author Ken Cameron, (C) 2009, add sensors from poll replies
 */
public class SpecificSensorManagerXml extends jmri.jmrix.powerline.configurexml.SerialSensorManagerXml {

}

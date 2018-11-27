package jmri.jmrix.configurexml;

import org.junit.*;
import org.jdom2.Element;
import jmri.jmrix.ConnectionConfig;
import javax.swing.JPanel;

/**
 * Base tests for Simulator ConnectionConfigXml objects.
 * The AbstractSimulatorConnectionConfig is currently based on serial objects. 
 * most of the simulators save objects using classes derived directly from 
 * AbstractConnectionConfigXml.  This eventually needs to change.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractSimulatorConnectionConfigXmlTestBase extends AbstractConnectionConfigXmlTestBase {
}

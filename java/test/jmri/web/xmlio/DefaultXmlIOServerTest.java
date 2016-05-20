// DefaultXmlIOServerTest.java

package jmri.web.xmlio;

import junit.framework.*;

import jmri.*;
import jmri.util.*;

import org.jdom.*;

/**
 * Invokes complete set of tests of the jmri.web.xmlio.DefaultXmlIOServerTest class
 *
 * @author	    Bob Jacobsen  Copyright 2008, 2009, 2010
 * @version         $Revision$
 */
public class DefaultXmlIOServerTest extends TestCase {

    // check infrastructure for test
    public void testTestSetup() {
        Assert.assertTrue("turnout not null", t1!=null);
        Assert.assertTrue("sensor not null", s1!=null);
    }
    
    Element getReadOneCommand() {
        Element e1 = new Element("xmlio");
        
        Element e2 = new Element("item");
        e2.addContent(new Element("type").addContent("turnout"));
        e2.addContent(new Element("name").addContent("IT2"));
        e1.addContent(e2);

        return e1;
    }
    
    Element getWriteOneCommand() {
        Element e1 = new Element("xmlio");
        
        Element e2 = new Element("item");
        e2.addContent(new Element("type").addContent("turnout"));
        e2.addContent(new Element("name").addContent("IT2"));
        e2.addContent(new Element("set").addContent(""+Turnout.THROWN));
        e1.addContent(e2);

        return e1;
    }
    
    Element getWriteThrottleCommand() {
        Element e1 = new Element("xmlio");
        
        Element e2 = new Element("throttle");
        e2.addContent(new Element("address").addContent("3"));
        e1.addContent(e2);

        return e1;
    }
    
    Element getWritePowerCommand() {
        Element e1 = new Element("xmlio");
        
        Element e2 = new Element("item");
        e2.addContent(new Element("type").addContent("power"));
        e2.addContent(new Element("name").addContent("power"));
        e2.addContent(new Element("set").addContent("2"));
        e1.addContent(e2);

        return e1;
    }
    
    public void testSynchReadComplete() throws JmriException {
        Element e = server.immediateRequest(getReadOneCommand());
        
        Assert.assertNotNull(e.getChildren("item"));
        Assert.assertEquals(1, e.getChildren("item").size());

        Assert.assertNotNull(e.getChildren("item").get(0));
        Element item = (Element) e.getChildren("item").get(0);
        Assert.assertNotNull(item.getChild("value"));
        Assert.assertNotNull(item.getChild("type"));
        Assert.assertNotNull(item.getChild("name"));

        Assert.assertEquals("IT2", item.getChild("name").getText());
        Assert.assertEquals("turnout", item.getChild("type").getText());
    }
    
    public void testSynchReadOneClosed() throws JmriException {
        t2.setCommandedState(Turnout.CLOSED);

        Element e = server.immediateRequest(getReadOneCommand());

        Element item = (Element) e.getChildren("item").get(0);
        Assert.assertEquals(""+Turnout.CLOSED, item.getChild("value").getText());
    }

    public void testSynchReadOneThrown() throws JmriException {
        t2.setCommandedState(Turnout.THROWN);

        Element e = server.immediateRequest(getReadOneCommand());

        Element item = (Element) e.getChildren("item").get(0);
        Assert.assertEquals(""+Turnout.THROWN, item.getChild("value").getText());
    }
    
    public void testSynchReadOneClosedWValueElement() throws JmriException {
        t2.setCommandedState(Turnout.CLOSED);

        Element e = server.immediateRequest(getWriteOneCommand());

        Element item = (Element) e.getChildren("item").get(0);
        Assert.assertEquals(""+Turnout.THROWN, item.getChild("value").getText());
        Assert.assertEquals(Turnout.THROWN, t2.getCommandedState());
    }
    
    public void testThrottleElement() throws JmriException {
    
        // first request registers
        Element e = server.immediateRequest(getWriteThrottleCommand());
        // need second oen for data
        e = server.immediateRequest(getWriteThrottleCommand());

        e = e.getChild("throttle");
        
        Element item;
        
        item = e.getChild("address");
        Assert.assertTrue("address exists", item != null);
        if (item!=null) Assert.assertEquals("address correct", "3", item.getText());

        item = e.getChild("speed");
        Assert.assertTrue("speed exists", item != null);
        if (item!=null) Assert.assertEquals("speed correct", "0.0", item.getText());

        item = e.getChild("forward");
        Assert.assertTrue("forward exists", item != null);
        if (item!=null) Assert.assertEquals("forward correct", "true", item.getText());

        item = e.getChild(Throttle.F0);
        Assert.assertTrue("F0 exists", item != null);
        if (item!=null) Assert.assertEquals("F0 correct", "false", item.getText());

    }

    public void testPowerElement() throws JmriException {
    
        Element e = server.immediateRequest(getWritePowerCommand());

        e = e.getChild("item");
        
        Element item;
        
        item = e.getChild("type");
        Assert.assertTrue("type exists", item != null);
        if (item!=null) Assert.assertEquals("type correct", "power", item.getText());

        item = e.getChild("name");
        Assert.assertTrue("name exists", item != null);
        if (item!=null) Assert.assertEquals("name correct", "power", item.getText());

        item = e.getChild("value");
        Assert.assertTrue("value exists", item != null);
        if (item!=null) Assert.assertEquals("value correct", "2", item.getText());

    }

    // common objects
    DefaultXmlIOServer server;
    Sensor s1;
    Turnout t1;
    Turnout t2;
    
    // from here down is testing infrastructure
    public DefaultXmlIOServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DefaultXmlIOServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultXmlIOServerTest.class);
        return suite;
    }

    // Setup Log4J and managers
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugPowerManager();
        
        server = new DefaultXmlIOServer();
        s1 = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        t1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        t2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        
    }
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
        super.tearDown();
    }
}

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
 * @version         $Revision: 1.3 $
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

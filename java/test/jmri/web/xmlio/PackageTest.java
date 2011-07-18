// PackageTest.java

package jmri.web.xmlio;

import junit.framework.*;
import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * Invokes complete set of tests in the jmri.web.xmlio tree
 *
 * @author	    Bob Jacobsen  Copyright 2008, 2009, 2010
 * @version         $Revision$
 */
public class PackageTest extends TestCase {

    // basic something
    public void testCtor() {
        new XmlIOFactory();
    }
    
    // check that JDOM serialization works as expected
    // by sending an element through a stream.
    // 
    // Note that you can't do this twice; SAX input
    // expects end-of-stream after the end of the element.
    //
    public void testJdomSerialization() throws Exception {
        Element send = new Element("foo");
        send.addContent(new Element("bar").addContent("biff"));
        ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
        
        XMLOutputter outputter = new XMLOutputter();

        outputter.output(send, os); 
        
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        SAXBuilder builder = new SAXBuilder();

        Element received = builder.build(is).getRootElement();
        
        Assert.assertEquals("biff", received.getChild("bar").getText());
    }
    
    
    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(XmlIOFactoryTest.suite());
        suite.addTest(DefaultXmlIOServerTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}

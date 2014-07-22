// AbstractNamedBeanManagerConfigXMLTest.java

package jmri.managers.configurexml;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import org.jdom.Element;

import jmri.NamedBean;
import jmri.implementation.AbstractNamedBean;

/**
 * Checks of basic NamedBean storage
 * 
 * @author Bob Jacobsen Copyright 2009
 * @version $Revision$
 */
public class AbstractNamedBeanManagerConfigXMLTest extends TestCase {

    public void testStoreBean() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new AbstractNamedBeanManagerConfigXML(){
            public boolean load(Element e){return false;}
            public void load(Element e, Object o){}
            public Element store(Object o){return null;}
        };
        
        // create a NamedBean with two properties to store
	    NamedBean from = new AbstractNamedBean("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
            public String getBeanType(){ return ""; }
	    };

	    from.setProperty("foo", "bar");
	    from.setProperty("biff", Boolean.valueOf(true));
	    
	    // create element for properties
	    Element p = new Element("test");
	    
	    x.storeProperties(from, p);
	    
	    // create NamedBean to load
	    NamedBean to = new AbstractNamedBean("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
            public String getBeanType(){ return ""; }
	    };
          
        x.loadProperties(to, p);

        // and test
	    Assert.assertEquals("bar", to.getProperty("foo"));
	    Assert.assertEquals(Boolean.valueOf(true), to.getProperty("biff"));

    }
    
    public void testStoreNoProperties() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new AbstractNamedBeanManagerConfigXML(){
            public boolean load(Element e){return false;}
            public void load(Element e, Object o){}
            public Element store(Object o){return null;}
        };
        
        // create a NamedBean with two properties to store
	    NamedBean from = new AbstractNamedBean("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
            public String getBeanType(){ return ""; }
	    };
	    
	    // create element for properties
	    Element p = new Element("test");
	    
	    x.storeProperties(from, p);
	    
	    // create NamedBean to load
	    NamedBean to = new AbstractNamedBean("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
            public String getBeanType(){ return ""; }
	    };
          
        x.loadProperties(to, p);

        // and test
	    Assert.assertEquals(null, to.getProperty("foo"));
	    Assert.assertEquals(null, to.getPropertyKeys());

    }
    
    
    public void testStoreNullProperty() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new AbstractNamedBeanManagerConfigXML(){
            public boolean load(Element e){return false;}
            public void load(Element e, Object o){}
            public Element store(Object o){return null;}
        };
        
        // create a NamedBean with two properties to store
	    NamedBean from = new AbstractNamedBean("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
            public String getBeanType(){ return ""; }
	    };

	    from.setProperty("foo", null);
	    from.setProperty("biff", Boolean.valueOf(true));
	    
	    // create element for properties
	    Element p = new Element("test");
	    
	    x.storeProperties(from, p);
	    
	    // create NamedBean to load
	    NamedBean to = new AbstractNamedBean("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
            public String getBeanType(){ return ""; }
	    };
          
        x.loadProperties(to, p);

        // and test
	    Assert.assertEquals(null, to.getProperty("foo"));
	    Assert.assertEquals(Boolean.valueOf(true), to.getProperty("biff"));

    }
    
    // from here down is testing infrastructure

    public AbstractNamedBeanManagerConfigXMLTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", AbstractNamedBeanManagerConfigXMLTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractNamedBeanManagerConfigXMLTest.class);
        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(AbstractNamedBeanManagerConfigXMLTest.class.getName());
}

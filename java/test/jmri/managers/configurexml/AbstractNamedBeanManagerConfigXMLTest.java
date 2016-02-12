// AbstractNamedBeanManagerConfigXMLTest.java
package jmri.managers.configurexml;

import jmri.NamedBean;
import jmri.implementation.AbstractNamedBean;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks of basic NamedBean storage
 *
 * @author Bob Jacobsen Copyright 2009
 * @version $Revision$
 */
public class AbstractNamedBeanManagerConfigXMLTest extends TestCase {

    public void testStoreBean() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        // create a NamedBean with two properties to store
        NamedBean from = new AbstractNamedBean("sys", "usr") {
            /**
             *
             */
            private static final long serialVersionUID = 8103890609137268240L;

            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
        };

        from.setProperty("foo", "bar");
        from.setProperty("biff", Boolean.valueOf(true));

        // create element for properties
        Element p = new Element("test");

        x.storeProperties(from, p);

        // create NamedBean to load
        NamedBean to = new AbstractNamedBean("sys", "usr") {
            /**
             *
             */
            private static final long serialVersionUID = -5772255040585062885L;

            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
        };

        x.loadProperties(to, p);

        // and test
        Assert.assertEquals("bar", to.getProperty("foo"));
        Assert.assertEquals(Boolean.valueOf(true), to.getProperty("biff"));

    }

    public void testStoreNoProperties() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        // create a NamedBean with two properties to store
        NamedBean from = new AbstractNamedBean("sys", "usr") {
            /**
             *
             */
            private static final long serialVersionUID = -6948669321294943243L;

            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
        };

        // create element for properties
        Element p = new Element("test");

        x.storeProperties(from, p);

        // create NamedBean to load
        NamedBean to = new AbstractNamedBean("sys", "usr") {
            /**
             *
             */
            private static final long serialVersionUID = 8693438303061601658L;

            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
        };

        x.loadProperties(to, p);

        // and test
        Assert.assertEquals(null, to.getProperty("foo"));
        Assert.assertEquals(null, to.getPropertyKeys());

    }

    public void testStoreNullProperty() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        // create a NamedBean with two properties to store
        NamedBean from = new AbstractNamedBean("sys", "usr") {
            /**
             *
             */
            private static final long serialVersionUID = 2830149244800009676L;

            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
        };

        from.setProperty("foo", null);
        from.setProperty("biff", Boolean.valueOf(true));

        // create element for properties
        Element p = new Element("test");

        x.storeProperties(from, p);

        // create NamedBean to load
        NamedBean to = new AbstractNamedBean("sys", "usr") {
            /**
             *
             */
            private static final long serialVersionUID = 7324369333872804221L;

            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractNamedBeanManagerConfigXMLTest.class.getName());
    
    private class NamedBeanManagerConfigXMLTest extends AbstractNamedBeanManagerConfigXML {

        @Override
        public boolean load(Element shared, Element perNode) {
            return false;
        }

        @Override
        public void load(Element e, Object o) {
        }

        @Override
        public Element store(Object o) {
            return null;
        }
    }
}

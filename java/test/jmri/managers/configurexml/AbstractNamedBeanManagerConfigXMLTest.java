package jmri.managers.configurexml;

import jmri.NamedBean;
import jmri.implementation.AbstractNamedBean;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;
import org.junit.Assert;

/**
 * Checks of basic NamedBean storage
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class AbstractNamedBeanManagerConfigXMLTest extends TestCase {

    public void testStoreBean() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        // create a NamedBean with two properties to store
        NamedBean from = new AbstractNamedBean("sys", "usr") {

            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
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
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
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
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
            public String getBeanType() {
                return "";
            }
        };

        // create element for properties
        Element p = new Element("test");

        x.storeProperties(from, p);

        // create NamedBean to load
        NamedBean to = new AbstractNamedBean("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
            public String getBeanType() {
                return "";
            }
        };

        x.loadProperties(to, p);

        // and test
        Assert.assertEquals(null, to.getProperty("foo"));
        Assert.assertTrue(to.getPropertyKeys()!=null);
        Assert.assertEquals(0, to.getPropertyKeys().size());

    }

    public void testStoreNullProperty() {
        // Create the manager to test
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        // create a NamedBean with two properties to store
        NamedBean from = new AbstractNamedBean("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
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
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
            public String getBeanType() {
                return "";
            }
        };

        x.loadProperties(to, p);

        // and test
        Assert.assertEquals(null, to.getProperty("foo"));
        Assert.assertEquals(Boolean.valueOf(true), to.getProperty("biff"));

    }

    public void testGetUserName() {
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        Element e = new Element("test");
        Assert.assertEquals(null, x.getUserName(e));
        
        e = new Element("test");
        Element e2 = new Element("userName");
        e2.addContent("foo");
        e.addContent(e2);
        Assert.assertEquals("foo", x.getUserName(e));
        
        e = new Element("test");
        e.setAttribute("userName", "bar");
        Assert.assertEquals("bar", x.getUserName(e));

        e = new Element("test");
        e2 = new Element("userName");
        e2.addContent("foo");
        e.addContent(e2);
        e.setAttribute("userName", "bar");
        Assert.assertEquals("foo", x.getUserName(e));

    }

    public void testGetSystemName() {
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        Element e = new Element("test");
        Assert.assertEquals(null, x.getSystemName(e));
        
        e = new Element("test");
        Element e2 = new Element("systemName");
        e2.addContent("foo");
        e.addContent(e2);
        Assert.assertEquals("foo", x.getSystemName(e));
        
        e = new Element("test");
        e.setAttribute("systemName", "bar");
        Assert.assertEquals("bar", x.getSystemName(e));

        e = new Element("test");
        e2 = new Element("systemName");
        e2.addContent("foo");
        e.addContent(e2);
        e.setAttribute("systemName", "bar");
        Assert.assertEquals("foo", x.getSystemName(e));

    }
    
    public void testCheckedNamedBeanName() {
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        jmri.Turnout t = null;
        jmri.TurnoutManager tm = new jmri.managers.InternalTurnoutManager();
        tm.provideTurnout("IT01").setUserName("foo");

        Assert.assertEquals(null, x.checkedNamedBeanName(null, t, tm));
        Assert.assertEquals(null, x.checkedNamedBeanName("", t, tm));
        Assert.assertEquals(null, x.checkedNamedBeanName("bar", t, tm));
        Assert.assertEquals("IT01", x.checkedNamedBeanName("IT01", t, tm));
        Assert.assertEquals("foo", x.checkedNamedBeanName("foo", t, tm));        
    }

    public void testCheckedNamedBeanReference() {
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();

        jmri.Turnout t = null;
        jmri.TurnoutManager tm = new jmri.managers.InternalTurnoutManager();
        NamedBean nb = tm.provideTurnout("IT01");
        nb.setUserName("foo");

        Assert.assertEquals(null, x.checkedNamedBeanReference(null, t, tm));
        Assert.assertEquals(null, x.checkedNamedBeanReference("", t, tm));
        Assert.assertEquals(null, x.checkedNamedBeanReference("bar", t, tm));
        Assert.assertEquals(nb, x.checkedNamedBeanReference("IT01", t, tm));
        Assert.assertEquals(nb, x.checkedNamedBeanReference("foo", t, tm));        
    }

    public void testCheckedNamedBeanHandle() {
        AbstractNamedBeanManagerConfigXML x = new NamedBeanManagerConfigXMLTest();
        jmri.util.JUnitUtil.resetInstanceManager();
        
        jmri.Turnout t = null;
        jmri.TurnoutManager tm = new jmri.managers.InternalTurnoutManager();
        jmri.Turnout nb = tm.provideTurnout("IT01");
        nb.setUserName("foo");
        

        Assert.assertEquals(null, x.checkedNamedBeanHandle(null, t, tm));
        Assert.assertEquals(null, x.checkedNamedBeanHandle("", t, tm));
        Assert.assertEquals(null, x.checkedNamedBeanHandle("bar", t, tm));
        Assert.assertEquals(new jmri.NamedBeanHandle<jmri.Turnout>("IT01", nb), x.checkedNamedBeanHandle("IT01", t, tm));
        Assert.assertEquals(new jmri.NamedBeanHandle<jmri.Turnout>("foo", nb), x.checkedNamedBeanHandle("foo", t, tm));        
    }

    // from here down is testing infrastructure
    public AbstractNamedBeanManagerConfigXMLTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AbstractNamedBeanManagerConfigXMLTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractNamedBeanManagerConfigXMLTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

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

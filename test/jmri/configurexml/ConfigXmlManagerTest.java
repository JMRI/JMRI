
package jmri.configurexml;

import junit.framework.*;



public class ConfigXmlManagerTest extends TestCase {

    public ConfigXmlManagerTest(String s) {
        super(s);
    }

    boolean innerFlag;

    public void testRegister() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
            void locateFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                innerFlag=true;
            }
        };
        Object o1=  "";
        innerFlag=false;
        configxmlmanager.register(o1);
        Assert.assertTrue("register didn't find class", innerFlag);
    }

    public void testAdapterName() {
        ConfigXmlManager c = new ConfigXmlManager();
        Assert.assertEquals("String class adapter", "java.lang.configurexml.StringXml",
                            c.adapterName(""));
    }

  	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(ConfigXmlManagerTest.class);
		return suite;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConfigXmlManagerTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
}

// ConfigXmlManagerTest.java

package jmri.configurexml;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for ConfigXmlManager.
 * <P>
 * Uses the local preferences for test files.
 * @author Bob Jacobsen Copyright 2003
 * @version $Revision: 1.8 $
 */
public class ConfigXmlManagerTest extends TestCase {

    public ConfigXmlManagerTest(String s) {
        super(s);
    }

    boolean innerFlag;

    public void testRegisterFail() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                void locateClassFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            };
        Object o1=  "";
        innerFlag=false;
        configxmlmanager.registerConfig(o1);
        Assert.assertTrue("register didn't find class", innerFlag);
    }

    public void testRegisterOK() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                void locateFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            };
        Object o1=  new jmri.TripleTurnoutSignalHead("","", null, null, null);
        innerFlag=false;
        configxmlmanager.registerConfig(o1);
        Assert.assertTrue("register found class", !innerFlag);
        Assert.assertTrue("stored in clist", configxmlmanager.clist.size() == 1);
        configxmlmanager.deregister(o1);
        Assert.assertTrue("removed from clist", configxmlmanager.clist.size() == 0);
    }
    public void testFind() throws ClassNotFoundException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                void locateFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            };
        Object o1=  new jmri.TripleTurnoutSignalHead("","", null, null, null);
        Object o2=  new jmri.TripleTurnoutSignalHead("","", null, null, null);
        Object o3=  new jmri.TripleTurnoutSignalHead("","", null, null, null);
        innerFlag=false;
        configxmlmanager.registerConfig(o1);
        Assert.assertTrue("find found it", configxmlmanager.findInstance(o1.getClass(),1)==o1);
        Assert.assertTrue("find only one so far", configxmlmanager.findInstance(o1.getClass(),2)==null);
        configxmlmanager.deregister(o1);
        Assert.assertTrue("find none", configxmlmanager.findInstance(o1.getClass(),1)==null);
        configxmlmanager.registerConfig(o1);
        configxmlmanager.registerConfig(o2);
        configxmlmanager.registerConfig(o3);
        Assert.assertTrue("find found 2nd", configxmlmanager.findInstance(o1.getClass(),2)==o2);
        Assert.assertTrue("find found subclass", configxmlmanager.findInstance(Class.forName("jmri.SignalHead"),2)==o2);

    }

    public void testDeregister() {
    }

    public void testAdapterName() {
        ConfigXmlManager c = new ConfigXmlManager();
        Assert.assertEquals("String class adapter", "java.lang.configurexml.StringXml",
                            c.adapterName(""));
    }

    public void testFindFile() throws FileNotFoundException, IOException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
            void locateClassFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            void locateFileFailed(String f) {
                    // suppress warning during testing
                }
            };
        File result;
        result = configxmlmanager.find("foo.biff");
        Assert.assertTrue("dont find foo.biff", result==null);
        result = configxmlmanager.find("roster.xml");
        Assert.assertTrue("should find roster.xml", result!=null);

        // make sure no test file exists in "layout"
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"layout");
        File f = new File(XmlFile.prefsDir()+"layout"+File.separator+"testConfigXmlManagerTest.xml");
        f.delete();  // remove it if its there

        // if file is at top level, remove that too
        result = new File("testConfigXmlManagerTest.xml");
        if (result.exists()) result.delete();

        // check for not found if doesn't exist
        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        Assert.assertTrue("should not find testConfigXmlManagerTest.xml", result==null);

        // put file back and find
        PrintStream p = new PrintStream (new FileOutputStream(f));
        p.println("stuff"); // load a new one
        p.close();

        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        Assert.assertTrue("should find testConfigXmlManagerTest.xml", result!=null);
        f.delete();  // make sure it's gone again

        // check file in the current app dir
        f = new File("testConfigXmlManagerTest.xml");
        f.delete();  // remove it if its there
        p = new PrintStream (new FileOutputStream(f));
        p.println("stuff"); // load a new one
        p.close();

        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        Assert.assertTrue("should find testConfigXmlManagerTest.xml in app dir", result!=null);
        f.delete();  // make sure it's gone again
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

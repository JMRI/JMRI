// ConfigXmlManagerTest.java

package jmri.configurexml;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import jmri.util.FileUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for ConfigXmlManager.
 * <P>
 * Uses the local preferences for test files.
 * @author Bob Jacobsen Copyright 2003
 * @version $Revision$
 */
public class ConfigXmlManagerTest extends TestCase {

    public ConfigXmlManagerTest(String s) {
        super(s);
    }

    boolean innerFlag;

    public void testRegisterOK() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                @SuppressWarnings("unused")
				void locateFailed(Throwable ex, String adapterName, Object o) {
                }
            };
        Object o1=  new jmri.implementation.TripleTurnoutSignalHead("","", null, null, null);
        configxmlmanager.registerConfig(o1);
        Assert.assertTrue("stored in clist", configxmlmanager.clist.size() == 1);
        configxmlmanager.deregister(o1);
        Assert.assertTrue("removed from clist", configxmlmanager.clist.size() == 0);
    }
    public void testFind() throws ClassNotFoundException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                @SuppressWarnings("unused")
				void locateFailed(Throwable ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            };
        Object o1=  new jmri.implementation.TripleTurnoutSignalHead("","", null, null, null);
        Object o2=  new jmri.implementation.TripleTurnoutSignalHead("","", null, null, null);
        Object o3=  new jmri.implementation.TripleTurnoutSignalHead("","", null, null, null);
        innerFlag=false;
        configxmlmanager.registerConfig(o1, jmri.Manager.SIGNALHEADS);
        Assert.assertTrue("find found it", configxmlmanager.findInstance(o1.getClass(),0)==o1);
        Assert.assertTrue("find only one so far", configxmlmanager.findInstance(o1.getClass(),1)==null);
        configxmlmanager.deregister(o1);
        Assert.assertTrue("find none", configxmlmanager.findInstance(o1.getClass(),0)==null);
        configxmlmanager.registerConfig(o1, jmri.Manager.SIGNALHEADS);
        configxmlmanager.registerConfig(o2, jmri.Manager.SIGNALHEADS);
        configxmlmanager.registerConfig(o3, jmri.Manager.SIGNALHEADS);
        Assert.assertTrue("find found 2nd", configxmlmanager.findInstance(o1.getClass(),1)==o2);
        Assert.assertTrue("find found subclass", configxmlmanager.findInstance(Class.forName("jmri.SignalHead"),1)==o2);

    }

    public void testDeregister() {
    }

    public void testAdapterName() {
        //ConfigXmlManager c = new ConfigXmlManager();
        Assert.assertEquals("String class adapter", "java.lang.configurexml.StringXml",
        		ConfigXmlManager.adapterName(""));
    }

    public void testFindFile() throws FileNotFoundException, IOException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
            void locateClassFailed(Throwable ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            void locateFileFailed(String f) {
                    // suppress warning during testing
                }
            };
        URL result;
        result = configxmlmanager.find("foo.biff");
        Assert.assertTrue("dont find foo.biff", result==null);

        // make sure no test file exists in "layout"
        FileUtil.createDirectory(FileUtil.getUserFilesPath()+"layout");
        File f = new File(FileUtil.getUserFilesPath()+"layout"+File.separator+"testConfigXmlManagerTest.xml");
        f.delete();  // remove it if its there

        // if file is at top level, remove that too
        f = new File("testConfigXmlManagerTest.xml");
        if (f.exists()) f.delete();

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

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", ConfigXmlManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigXmlManagerTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(ConfigXmlManagerTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}

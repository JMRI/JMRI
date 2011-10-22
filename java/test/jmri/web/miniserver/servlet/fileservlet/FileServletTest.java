// FileServletTest.java

package jmri.web.miniserver.servlet.fileservlet;

import java.io.File;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests for the FileServlet class.
 *
 * @author	    Bob Jacobsen  Copyright 2008
 * @version         $Revision$
 */
public class FileServletTest extends TestCase {

    public void testFindPath() {
        FileServlet fs = new FileServlet();
        FileServlet.paths = new TestPaths();
        
        String result;
        
        result = fs.getFilename("foo.gif");
        Assert.assertEquals("simple file", "foo.gif", result);

        result = fs.getFilename("file/foo.gif");
        Assert.assertEquals("file/ ref", "resources/foo.gif", result);

        result = fs.getFilename("jmri/foo.gif");
        Assert.assertEquals("jmri/ ref", "foo.gif", result);
    }

    public void testFindType() {
        FileServlet fs = new FileServlet();
        FileServlet.paths = new TestPaths();
        FileServlet.types = new TestMIME();
        
        String result;
        
        result = fs.getMimeType("foo.gif");
        Assert.assertEquals("simple file", "image/gif", result);

        result = fs.getMimeType("file/foo.gif");
        Assert.assertEquals("pathname", "image/gif", result);

        result = fs.getMimeType("foo.html.gif");
        Assert.assertEquals("two dots", "image/gif", result);

        result = fs.getMimeType("foo");
        Assert.assertEquals("no dots", "text", result);

        result = fs.getMimeType("foo.gif?123,456");
        Assert.assertEquals("with ? char", "image/gif", result);
    }

    public void testSecurityCheckOK() throws java.io.IOException {
        FileServlet fs = new FileServlet();
        Assert.assertTrue("file in resources", fs.isSecurityLimitOK("resources/logo.gif"));
        Assert.assertTrue("file in prefs", fs.isSecurityLimitOK(jmri.jmrit.XmlFile.prefsDir()+"/roster"));
    }
    
    public void testSecurityCheckFail() throws java.io.IOException {
        FileServlet fs = new FileServlet();
        Assert.assertTrue("file above resources", !fs.isSecurityLimitOK(".."));
        Assert.assertTrue("file above prefs", !fs.isSecurityLimitOK(jmri.jmrit.XmlFile.prefsDir()+"/.."));
    }

    public void testRelativeURL() {
        Assert.assertEquals("file in program", "/dist/resources/logo.gif", FileServlet.getRelativeURL("program:resources/logo.gif"));
        Assert.assertEquals("file in prefs", "/prefs/index.html", FileServlet.getRelativeURL("preference:index.html"));
        Assert.assertEquals("file in resources", "/dist/resources/logo.gif", FileServlet.getRelativeURL("resource:resources/logo.gif"));
        Assert.assertNotNull("file in file", FileServlet.getRelativeURL("file:."));
        Assert.assertNull("file in home", FileServlet.getRelativeURL("home:/."));
        Assert.assertNull("absolute path to home", FileServlet.getRelativeURL(System.getProperty("user.home") + File.separator + "."));
    }

    // provide a set of path properties for the test
     public class TestPaths extends java.util.ListResourceBundle {
         public Object[][] getContents() {
             return contents;
         }
         Object[][] contents = {
            {"jmri",""},
            {"file","resources"}
         };
     }

    // provide a set of MIME properties for the test
     public class TestMIME extends java.util.ListResourceBundle {
         public Object[][] getContents() {
             return contents;
         }
         Object[][] contents = {
            {".html","text/html"},
            {".gif","image/gif"}
         };
     }

    // from here down is testing infrastructure
    
    public FileServletTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FileServletTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FileServletTest.class);
        return suite;
    }

    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileServletTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}

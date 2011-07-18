// AbstractServletTest.java

package jmri.web.miniserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Tests for the AbstractService class.
 *
 * @author	    Bob Jacobsen  Copyright 2008
 * @version         $Revision$
 */
public class AbstractServletTest extends TestCase {

    public void testGetInput() throws java.io.IOException {
        AbstractServlet as = new AbstractServlet(){
            public void service(ServletRequest req, ServletResponse res) {}
         };
 
        java.io.BufferedReader in = 
            new java.io.BufferedReader(
                new java.io.StringReader(
                    "line 1\nline 2\n")
            );
        
        String[] result = as.getInputLines(in);
        Assert.assertEquals("line 1", "line 1", result[0]);        
        Assert.assertEquals("line 2", "line 2", result[1]);        
                        
        String[] resultAgain = as.getInputLines(in);
        Assert.assertEquals("same result", result, resultAgain);        
    }

    public void testGetRequest() throws java.io.IOException {
        AbstractServlet as = new AbstractServlet(){
            public void service(ServletRequest req, ServletResponse res) {}
         };
 
        java.io.BufferedReader in = 
            new java.io.BufferedReader(
                new java.io.StringReader(
                    "GET /jmri/help/en/html/hardware/images/jmriMainPageOk.jpg HTTP/1.1\n"
                    +"User-Agent: Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_4; en-us) AppleWebKit/525.18 (KHTML, like Gecko) Version/3.1.2 Safari/525.20.1\n"
                    +"Accept-Language: en-us")
            );
        
        as.getInputLines(in);
        String result = as.getRequest();
        
        Assert.assertEquals("result", "/jmri/help/en/html/hardware/images/jmriMainPageOk.jpg", result);        
    }

    public void testGetContext() throws java.io.IOException {
        AbstractServlet as1 = new AbstractServlet(){
            public void service(ServletRequest req, ServletResponse res) {}
         };
        Assert.assertNotNull(as1.getServletContext());
        
        javax.servlet.ServletContext c1 = as1.getServletContext();
        javax.servlet.ServletContext c2 = as1.getServletContext();
        Assert.assertEquals(c1, c2);
        
        AbstractServlet as2 = new AbstractServlet(){
            public void service(ServletRequest req, ServletResponse res) {}
         };
        Assert.assertNotNull(as2.getServletContext());
        Assert.assertEquals(c1, as2.getServletContext());
    }
    
    // from here down is testing infrastructure
    
    public AbstractServletTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractServletTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractServletTest.class);
        return suite;
    }

    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractServletTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}

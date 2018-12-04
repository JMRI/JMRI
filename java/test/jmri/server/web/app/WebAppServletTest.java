package jmri.server.web.app;

import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JAVASCRIPT;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import jmri.InstanceManager;
import jmri.Version;
import jmri.profile.ProfileManager;
import org.springframework.mock.web.MockHttpSession;
import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class WebAppServletTest {

    @Test
    public void testCTor() {
        WebAppServlet t = new WebAppServlet();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws InitializationException {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.resetProfileManager();
        WebAppManager wam = new WebAppManager();
        wam.initialize(ProfileManager.getDefault().getActiveProfile());
        InstanceManager.setDefault(WebAppManager.class, wam);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testProcessRequestAbout() throws ServletException, IOException {
        HttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app/about");
        WebAppServlet instance = new WebAppServlet();
        instance.processRequest(request, response);
        Assert.assertEquals("Response connection header", "Keep-Alive", response.getHeader("Connection"));
        Assert.assertEquals("Response type", UTF8_APPLICATION_JSON, response.getContentType());
        JsonNode json = (new ObjectMapper()).readTree(response.getContentAsString());
        Assert.assertTrue("productInfo->JMRI object is array", json.get("productInfo").isArray());
    }

    @Test
    public void testProcessRequestLocale() throws ServletException, IOException {
        HttpSession session = new MockHttpSession();
        WebAppServlet instance = new WebAppServlet();
        // test English locale
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app");
        request.setPathInfo("/locale-en.json");
        instance.processRequest(request, response);
        Assert.assertEquals("Response connection header", "Keep-Alive", response.getHeader("Connection"));
        Assert.assertEquals("Response type", UTF8_APPLICATION_JSON, response.getContentType());
        JsonNode json = (new ObjectMapper()).readTree(response.getContentAsString());
        Assert.assertTrue("Is JSON object", json.isObject());
        Assert.assertTrue("Contains POWER object", json.hasNonNull("POWER"));
        Assert.assertTrue("POWER object has POWER object", json.get("POWER").hasNonNull("POWER"));
        Assert.assertEquals("Inner POWER object is \"Power\"", "Power", json.get("POWER").get("POWER").asText());
        Assert.assertTrue("Contains HELP object", json.hasNonNull("HELP"));
        Assert.assertTrue("HELP object has ABOUT object", json.get("HELP").hasNonNull("ABOUT"));
        Assert.assertEquals("Inner ABOUT object is \"About\"", "About", json.get("HELP").get("ABOUT").asText());
        // test German locale - can't reuse request and response objects 
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app");
        request.setPathInfo("/locale-de.json");
        instance.processRequest(request, response);
        Assert.assertEquals("Response connection header", "Keep-Alive", response.getHeader("Connection"));
        Assert.assertEquals("Response type", UTF8_APPLICATION_JSON, response.getContentType());
        json = (new ObjectMapper()).readTree(response.getContentAsString());
        Assert.assertTrue("Is JSON object", json.isObject());
        Assert.assertTrue("Contains POWER object", json.hasNonNull("POWER"));
        Assert.assertTrue("POWER object has POWER object", json.get("POWER").hasNonNull("POWER"));
        Assert.assertEquals("Inner POWER object is \"Bahnspannung\"", "Bahnspannung", json.get("POWER").get("POWER").asText());
    }

    @Test
    public void testProcessRequestScript() throws ServletException, IOException {
        HttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app/script");
        WebAppServlet instance = new WebAppServlet();
        instance.processRequest(request, response);
        Assert.assertEquals("Response connection header", "Keep-Alive", response.getHeader("Connection"));
        Assert.assertEquals("Response type", UTF8_APPLICATION_JAVASCRIPT, response.getContentType());
        String body = response.getContentAsString();
        Assert.assertTrue("Starts with comment", body.startsWith("/*\n"));
    }
    // private final static Logger log = LoggerFactory.getLogger(WebAppServletTest.class);

}

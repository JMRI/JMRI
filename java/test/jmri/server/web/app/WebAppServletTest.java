package jmri.server.web.app;

import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JAVASCRIPT;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import jmri.InstanceManager;
import jmri.Version;
import jmri.profile.ProfileManager;

import org.springframework.mock.web.MockHttpSession;

import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import jmri.web.servlet.ServletUtil;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WebAppServletTest {

    @Test
    public void testCTor() {
        WebAppServlet t = new WebAppServlet();
        assertNotNull( t, "exists");
    }

    public void validateAbout(MockHttpServletResponse response) throws UnsupportedEncodingException, IOException {
        assertEquals( "Keep-Alive", response.getHeader("Connection"), "Response connection header");
        assertEquals( UTF8_APPLICATION_JSON, response.getContentType(), "Response type");
        JsonNode json = (new ObjectMapper()).readTree(response.getContentAsString());
        assertEquals( Version.getCopyright(), json.get("copyright").asText(), "copyright is correct");
        assertTrue( json.get("productInfo").isArray(), "productInfo->JMRI object is array");
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
        validateAbout(response);
    }

    @Test
    public void testDoGetAbout() throws ServletException, IOException {
        HttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app/about");
        WebAppServlet instance = new WebAppServlet();
        instance.doGet(request, response);
        validateAbout(response);
    }

    @Test
    public void testDoPostAbout() throws ServletException, IOException {
        HttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app/about");
        WebAppServlet instance = new WebAppServlet();
        instance.doPost(request, response);
        validateAbout(response);
    }

    @Test
    public void testProcessRequestApp() throws ServletException, IOException {
        HttpSession session = new MockHttpSession();
        WebAppServlet instance = new WebAppServlet();
        // test English locale
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app");
        request.setPathInfo("");
        instance.processRequest(request, response);
        assertEquals( "Keep-Alive", response.getHeader("Connection"), "Response connection header");
        assertEquals( UTF8_TEXT_HTML, response.getContentType(), "Response type");
        String body = response.getContentAsString();
        // test for some built, and some not built artifacts in response
        assertTrue( body.startsWith("<!DOCTYPE html>\n"), "Is HTML");
        assertTrue( body.contains(InstanceManager.getDefault(ServletUtil.class).getRailroadName(false)),
            "Contains RR Name");
        assertTrue( body.contains("<script src=\"/app/script\"></script>"), "Contains local script");
        assertTrue( body.contains("<body ng-app=\"jmri.app\">"), "Is Angular app");
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
        assertEquals( "Keep-Alive", response.getHeader("Connection"), "Response connection header");
        assertEquals( UTF8_APPLICATION_JSON, response.getContentType(), "Response type");
        JsonNode json = (new ObjectMapper()).readTree(response.getContentAsString());
        assertTrue( json.isObject(), "Is JSON object");
        assertTrue( json.hasNonNull("POWER"), "Contains POWER object");
        assertTrue( json.get("POWER").hasNonNull("POWER"), "POWER object has POWER object");
        assertEquals( "Power", json.get("POWER").get("POWER").asText(), "Inner POWER object is \"Power\"");
        assertTrue( json.hasNonNull("HELP"), "Contains HELP object");
        assertTrue( json.get("HELP").hasNonNull("ABOUT"), "HELP object has ABOUT object");
        assertEquals( "About", json.get("HELP").get("ABOUT").asText(), "Inner ABOUT object is \"About\"");
        // test German locale - can't reuse request and response objects 
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setSession(session);
        request.setContextPath("/app");
        request.setPathInfo("/locale-de.json");
        instance.processRequest(request, response);
        assertEquals( "Keep-Alive", response.getHeader("Connection"), "Response connection header");
        assertEquals( UTF8_APPLICATION_JSON, response.getContentType(), "Response type");
        json = (new ObjectMapper()).readTree(response.getContentAsString());
        assertTrue( json.isObject(), "Is JSON object");
        assertTrue( json.hasNonNull("POWER"), "Contains POWER object");
        assertTrue( json.get("POWER").hasNonNull("POWER"), "POWER object has POWER object");
        assertEquals( "Bahnspannung", json.get("POWER").get("POWER").asText(),
            "Inner POWER object is \"Bahnspannung\"");
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
        assertEquals( "Keep-Alive", response.getHeader("Connection"), "Response connection header");
        assertEquals( UTF8_APPLICATION_JAVASCRIPT, response.getContentType(), "Response type");
        String body = response.getContentAsString();
        assertTrue( body.startsWith("/*\n"), "Starts with comment");
    }
    
    @Test
    public void testGetServletInfo() {
        assertEquals("JMRI Web App support", new WebAppServlet().getServletInfo());
    }

    @BeforeEach
    public void setUp() throws InitializationException {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.resetProfileManager();
        WebAppManager wam = new WebAppManager();
        wam.initialize(ProfileManager.getDefault().getActiveProfile());
        InstanceManager.setDefault(WebAppManager.class, wam);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WebAppServletTest.class);

}

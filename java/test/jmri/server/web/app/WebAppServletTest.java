package jmri.server.web.app;

import static jmri.util.web.MockServletExchange.GET;
import static jmri.util.web.MockServletExchange.POST;

import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JAVASCRIPT;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import javax.servlet.ServletException;

import jmri.InstanceManager;
import jmri.Version;
import jmri.profile.ProfileManager;

import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import jmri.util.web.MockServletExchange;
import jmri.web.servlet.ServletUtil;

import org.junit.jupiter.api.*;

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

    void validateAbout(MockServletExchange ctx) throws IOException {
        assertEquals("Keep-Alive", ctx.getResponse().getHeader("Connection"), "Response connection header");
        assertEquals(UTF8_APPLICATION_JSON, ctx.getResponseContentType(), "Response type");

        JsonNode json = (new ObjectMapper()).readTree(ctx.getResponseContentAsString());
        assertEquals( Version.getCopyright(), json.get("copyright").asText(), "copyright is correct");
        assertTrue( json.get("productInfo").isArray(), "productInfo->JMRI object is array");
    }

    @Test
    public void testProcessRequestAbout() throws ServletException, IOException {
        MockServletExchange ctx = new MockServletExchange(GET, "/app/about")
            .withContextPath("/app/about");
        new WebAppServlet().processRequest(ctx.getRequest(), ctx.getResponse());
        validateAbout(ctx);
    }

    @Test
    public void testDoGetAbout() throws ServletException, IOException {
        MockServletExchange ctx = new MockServletExchange(GET, "/app/about")
            .withContextPath("/app/about");
        WebAppServlet instance = new WebAppServlet();
        instance.doGet(ctx.getRequest(), ctx.getResponse());
        validateAbout(ctx);
    }

    @Test
    public void testDoPostAbout() throws ServletException, IOException {
        var ctx = new MockServletExchange(POST, "/app/about")
            .withContextPath("/app/about");
        WebAppServlet instance = new WebAppServlet();
        instance.doPost(ctx.getRequest(), ctx.getResponse());
        validateAbout(ctx);
    }

    @Test
    public void testProcessRequestApp() throws ServletException, IOException {
        WebAppServlet instance = new WebAppServlet();

        MockServletExchange ctx = new MockServletExchange(GET, "/app")
                .withContextPath("/app")
                .withPathInfo("")
                .withRequestContentType(UTF8_TEXT_HTML);

        instance.processRequest(ctx.getRequest(), ctx.getResponse());

        assertEquals("Keep-Alive", ctx.getResponse().getHeader("Connection"), "Response connection header");
        assertEquals(UTF8_TEXT_HTML, ctx.getResponseContentType(), "Response type");

        String body = ctx.getResponseContentAsString();
        // test for some built, and some not built artifacts in response
        assertTrue( body.startsWith("<!DOCTYPE html>\n"), "Is HTML");
        assertTrue( body.contains(InstanceManager.getDefault(ServletUtil.class).getRailroadName(false)),
            "Contains RR Name");
        assertTrue( body.contains("<script src=\"/app/script\"></script>"), "Contains local script");
        assertTrue( body.contains("<body ng-app=\"jmri.app\">"), "Is Angular app");
    }

    @Test
    public void testProcessRequestLocale() throws ServletException, IOException {
        WebAppServlet instance = new WebAppServlet();
        ObjectMapper mapper = new ObjectMapper();

        // test English locale
        MockServletExchange enCtx = new MockServletExchange(GET, "/app/locale-en.json")
                .withContextPath("/app")
                .withPathInfo("/locale-en.json");

        instance.processRequest(enCtx.getRequest(), enCtx.getResponse());

        assertEquals("Keep-Alive", enCtx.getResponse().getHeader("Connection"), "Response connection header");
        assertEquals(UTF8_APPLICATION_JSON, enCtx.getResponseContentType(), "Response type");

        JsonNode json = mapper.readTree(enCtx.getResponseContentAsString());
        assertTrue( json.isObject(), "Is JSON object");
        assertTrue( json.hasNonNull("POWER"), "Contains POWER object");
        assertTrue( json.get("POWER").hasNonNull("POWER"), "POWER object has POWER object");
        assertEquals( "Power", json.get("POWER").get("POWER").asText(), "Inner POWER object is \"Power\"");
        assertTrue( json.hasNonNull("HELP"), "Contains HELP object");
        assertTrue( json.get("HELP").hasNonNull("ABOUT"), "HELP object has ABOUT object");
        assertEquals( "About", json.get("HELP").get("ABOUT").asText(), "Inner ABOUT object is \"About\"");

        // test German locale - can't reuse request and response objects 
        MockServletExchange deCtx = new MockServletExchange(GET, "/app/locale-de.json")
                .withContextPath("/app")
                .withPathInfo("/locale-de.json");

        instance.processRequest(deCtx.getRequest(), deCtx.getResponse());

        assertEquals("Keep-Alive", deCtx.getResponse().getHeader("Connection"), "Response connection header");
        assertEquals(UTF8_APPLICATION_JSON, deCtx.getResponseContentType(), "Response type");

        json = mapper.readTree(deCtx.getResponseContentAsString());
        assertTrue( json.isObject(), "Is JSON object");
        assertTrue( json.hasNonNull("POWER"), "Contains POWER object");
        assertTrue( json.get("POWER").hasNonNull("POWER"), "POWER object has POWER object");
        assertEquals( "Bahnspannung", json.get("POWER").get("POWER").asText(),
            "Inner POWER object is \"Bahnspannung\"");
    }

    @Test
    public void testProcessRequestScript() throws ServletException, IOException {

        MockServletExchange ctx = new MockServletExchange(GET, "/app/script")
                .withContextPath("/app/script");

        WebAppServlet instance = new WebAppServlet();

        instance.processRequest(ctx.getRequest(), ctx.getResponse());

        assertEquals("Keep-Alive", ctx.getResponse().getHeader("Connection"), "Response connection header");
        assertEquals(UTF8_APPLICATION_JAVASCRIPT, ctx.getResponseContentType(), "Response type");

        String body = ctx.getResponseContentAsString();
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

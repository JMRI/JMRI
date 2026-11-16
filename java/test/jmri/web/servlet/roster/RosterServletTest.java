package jmri.web.servlet.roster;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import jmri.jmrit.roster.Roster;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.web.MockServletExchange;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the jmri.web.servlet.roster.RosterServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RosterServletTest {

    private static final String UPLOAD_NAME = "RosterServletTest-upload.png";

    @Test
    public void testCtor() {
        RosterServlet a = new RosterServlet();
        Assertions.assertNotNull(a);
    }

    // java.io.tmpdir and the test profile are normally on one filesystem, so these don't hit the tmpfs case (checked on a Pi)
    @Test
    public void testUploadNewImage() throws IOException, ServletException {
        File target = new File(new File(Roster.getDefault().getRosterLocation(), "roster"), UPLOAD_NAME);
        Assertions.assertFalse(target.exists());

        MockServletExchange exchange = new MockServletExchange(MockServletExchange.POST, "/roster/")
                .withRequestContentType("multipart/form-data; boundary=test")
                .withParameter("fileReplace", "false");
        Part part = mock(Part.class);
        when(part.getContentType()).thenReturn("image/png");
        when(part.getHeader("content-disposition")).thenReturn("form-data; name=\"files[]\"; filename=\"" + UPLOAD_NAME + "\"");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream("new image".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getRequest().getParts()).thenReturn(List.of(part));
        // init() only creates the JSON mapper when running under /roster
        when(exchange.getConfig().getServletContext().getContextPath()).thenReturn("/roster");

        RosterServlet servlet = new RosterServlet();
        servlet.init(exchange.getConfig());
        servlet.doPost(exchange.getRequest(), exchange.getResponse());

        Assertions.assertTrue(exchange.getResponseContentAsString().contains("file added"), exchange.getResponseContentAsString());
        Assertions.assertTrue(target.exists());
        Assertions.assertEquals("new image", Files.readString(target.toPath()));
    }

    @Test
    public void testUploadReplacesImage() throws IOException, ServletException {
        File rosterFolder = new File(Roster.getDefault().getRosterLocation(), "roster");
        Files.createDirectories(rosterFolder.toPath());
        File target = new File(rosterFolder, UPLOAD_NAME);
        Files.writeString(target.toPath(), "old image");

        MockServletExchange exchange = new MockServletExchange(MockServletExchange.POST, "/roster/")
                .withRequestContentType("multipart/form-data; boundary=test")
                .withParameter("fileReplace", "true");
        Part part = mock(Part.class);
        when(part.getContentType()).thenReturn("image/png");
        when(part.getHeader("content-disposition")).thenReturn("form-data; name=\"files[]\"; filename=\"" + UPLOAD_NAME + "\"");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream("new image".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getRequest().getParts()).thenReturn(List.of(part));
        when(exchange.getConfig().getServletContext().getContextPath()).thenReturn("/roster");

        RosterServlet servlet = new RosterServlet();
        servlet.init(exchange.getConfig());
        servlet.doPost(exchange.getRequest(), exchange.getResponse());

        Assertions.assertTrue(exchange.getResponseContentAsString().contains("file replaced"), exchange.getResponseContentAsString());
        Assertions.assertEquals("new image", Files.readString(target.toPath()));
    }

    @Test
    public void testUploadMoveFails() throws IOException, ServletException {
        // a non-empty directory at the target can't be deleted or replaced, so the move fails
        File rosterFolder = new File(Roster.getDefault().getRosterLocation(), "roster");
        File target = new File(rosterFolder, UPLOAD_NAME);
        Files.createDirectories(target.toPath());
        Files.writeString(new File(target, "blocker").toPath(), "blocker");

        MockServletExchange exchange = new MockServletExchange(MockServletExchange.POST, "/roster/")
                .withRequestContentType("multipart/form-data; boundary=test")
                .withParameter("fileReplace", "true");
        Part part = mock(Part.class);
        when(part.getContentType()).thenReturn("image/png");
        when(part.getHeader("content-disposition")).thenReturn("form-data; name=\"files[]\"; filename=\"" + UPLOAD_NAME + "\"");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream("new image".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getRequest().getParts()).thenReturn(List.of(part));
        when(exchange.getConfig().getServletContext().getContextPath()).thenReturn("/roster");

        RosterServlet servlet = new RosterServlet();
        servlet.init(exchange.getConfig());
        servlet.doPost(exchange.getRequest(), exchange.getResponse());

        Assertions.assertTrue(exchange.getResponseContentAsString().contains("upload FAILED"), exchange.getResponseContentAsString());
        Assertions.assertTrue(target.isDirectory());
        Assertions.assertFalse(new File(System.getProperty("java.io.tmpdir"), UPLOAD_NAME).exists());
        JUnitAppender.assertWarnMessageStartingWith("Unable to move");
        JUnitAppender.assertErrorMessage("upload FAILED for '" + UPLOAD_NAME + "', could not rename existing file. : Rename Failed");
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

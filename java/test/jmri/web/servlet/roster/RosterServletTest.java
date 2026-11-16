package jmri.web.servlet.roster;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Path target;

    @Test
    public void testCtor() {
        RosterServlet a = new RosterServlet();
        Assertions.assertNotNull(a);
    }

    // java.io.tmpdir and the test profile are normally on one filesystem, so these don't hit the tmpfs case
    @Test
    public void testUploadNewImage() throws IOException, ServletException {
        String response = upload(false);
        Assertions.assertTrue(response.contains("file added"), response);
        Assertions.assertEquals("new image", Files.readString(target));
    }

    @Test
    public void testUploadReplacesImage() throws IOException, ServletException {
        Files.writeString(target, "old image");
        String response = upload(true);
        Assertions.assertTrue(response.contains("file replaced"), response);
        Assertions.assertEquals("new image", Files.readString(target));
    }

    @Test
    public void testUploadMoveFails() throws IOException, ServletException {
        // a non-empty directory at the target can't be deleted or replaced, so the move fails
        Files.createDirectories(target.resolve("blocker"));
        String response = upload(true);
        Assertions.assertTrue(response.contains("upload FAILED"), response);
        Assertions.assertTrue(Files.isDirectory(target));
        Assertions.assertFalse(new File(System.getProperty("java.io.tmpdir"), UPLOAD_NAME).exists());
        JUnitAppender.assertWarnMessageStartingWith("Unable to move");
        JUnitAppender.assertErrorMessage("upload FAILED for '" + UPLOAD_NAME + "', could not rename existing file. : Rename Failed");
    }

    // posts one image the way the roster page does and returns the JSON reply
    private String upload(boolean replace) throws IOException, ServletException {
        MockServletExchange exchange = new MockServletExchange(MockServletExchange.POST, "/roster/")
                .withParameter("fileReplace", Boolean.toString(replace));
        Part part = mock(Part.class);
        when(part.getContentType()).thenReturn("image/png");
        when(part.getHeader("content-disposition")).thenReturn("form-data; filename=\"" + UPLOAD_NAME + "\"");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream("new image".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getRequest().getParts()).thenReturn(List.of(part));
        // init() only creates the JSON mapper when running under /roster
        when(exchange.getConfig().getServletContext().getContextPath()).thenReturn("/roster");

        RosterServlet servlet = new RosterServlet();
        servlet.init(exchange.getConfig());
        servlet.doPost(exchange.getRequest(), exchange.getResponse());
        return exchange.getResponseContentAsString();
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initRosterConfigManager();
        target = Path.of(Roster.getDefault().getRosterLocation(), "roster", UPLOAD_NAME);
        Files.createDirectories(target.getParent());
    }

    @AfterEach
    public void tearDown() {
        target = null;
        JUnitUtil.tearDown();
    }
}

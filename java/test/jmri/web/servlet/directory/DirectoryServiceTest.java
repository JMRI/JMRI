package jmri.web.servlet.directory;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.web.MockServletExchange;

import org.eclipse.jetty.util.resource.EmptyResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Randall Wood Copyright 2017, 2020
 */
public class DirectoryServiceTest {

    private DirectoryService instance;

    @Test
    public void testSendDirectoryNotAllowed() throws IOException {
        instance.setDirAllowed(false);
        var ctx = new MockServletExchange("GET", "");
        instance.sendDirectory(ctx.getRequest(), ctx.getResponse(), EmptyResource.INSTANCE, "");
        verify(ctx.getResponse()).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testSendDirectoryDist() throws IOException {
        var ctx = new MockServletExchange("GET","/servlet/dist")
            .withPathInfo("");

        Resource resource = new DirectoryResource(ctx.getRequest().getLocale(), new PathResource(FileUtil.getFile(FileUtil.getProgramPath())));
        instance.sendDirectory(ctx.getRequest(), ctx.getResponse(), resource, "");
        verify(ctx.getResponse()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testSendDirectoryDistHelp() throws IOException {
        var ctx = new MockServletExchange("GET","/dist/help")
            .withPathInfo("");

        Resource resource = new DirectoryResource(ctx.getRequest().getLocale(), new PathResource(FileUtil.getFile(FileUtil.getProgramPath())));
        instance.sendDirectory(ctx.getRequest(), ctx.getResponse(), resource, "");
        verify(ctx.getResponse()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testNotFound() throws IOException {
        var ctx = new MockServletExchange("GET", "/dist/not-there");

        // testing that status is not changed
        instance.notFound(ctx.getRequest(), ctx.getResponse());
        verify(ctx.getResponse(), never()).setStatus(anyInt());
        verify(ctx.getResponse(), never()).sendError(anyInt());
        verify(ctx.getResponse(), never()).sendError(anyInt(), anyString());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        instance = new DirectoryService();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        JUnitUtil.tearDown();
    }

}

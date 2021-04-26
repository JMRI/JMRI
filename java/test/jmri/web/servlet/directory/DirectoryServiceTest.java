package jmri.web.servlet.directory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.resource.EmptyResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Randall Wood Copyright 2017, 2020
 */
public class DirectoryServiceTest {

    private DirectoryService instance;

    @Test
    public void testSendDirectoryNotAllowed() throws IOException {
        instance.setDirAllowed(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        instance.sendDirectory(request, response, EmptyResource.INSTANCE, "");
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testSendDirectoryDist() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/dist");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Resource resource = new DirectoryResource(request.getLocale(), new PathResource(FileUtil.getFile(FileUtil.getProgramPath())));
        instance.sendDirectory(request, response, resource, "");
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void testSendDirectoryDistHelp() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/dist/help");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Resource resource = new DirectoryResource(request.getLocale(), new PathResource(FileUtil.getFile(FileUtil.getProgramPath())));
        instance.sendDirectory(request, response, resource, "");
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void testNotFound() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/dist/not-there");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(0); // not a valid HTTP status
        // testing that status is not changed
        instance.notFound(request, response);
        assertThat(response.getStatus()).isEqualTo(0);
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

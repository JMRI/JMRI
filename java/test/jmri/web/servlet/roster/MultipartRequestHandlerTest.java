package jmri.web.servlet.roster;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultipartRequestHandlerTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testHandlerUpload() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("fileReplace")).thenReturn("false");
        Assertions.assertNotNull(MultipartRequestHandler.uploadByJavaServletAPI(request));

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MultipartRequestHandlerTest.class);

}

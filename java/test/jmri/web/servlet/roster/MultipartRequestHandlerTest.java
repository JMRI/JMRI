package jmri.web.servlet.roster;

import java.io.IOException;
import javax.servlet.ServletException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultipartRequestHandlerTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testHandlerUpload() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("fileReplace", "false");
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

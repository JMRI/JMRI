package jmri.util.xml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XMLUtilTest {

    @Test
    public void createXMLReader() throws org.xml.sax.SAXException {
        Assertions.assertNotNull( XMLUtil.createXMLReader(), "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XMLUtilTest.class);

}

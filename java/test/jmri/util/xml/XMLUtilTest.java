package jmri.util.xml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XMLUtilTest {

    @Test
    public void createXMLReader() throws org.xml.sax.SAXException {
        Assert.assertNotNull("exists",XMLUtil.createXMLReader());
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

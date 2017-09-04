package jmri.util.xml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class XMLUtilTest {

    @Test
    public void createXMLReader() throws org.xml.sax.SAXException {
        Assert.assertNotNull("exists",XMLUtil.createXMLReader());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XMLUtilTest.class);

}

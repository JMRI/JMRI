package jmri.jmrix.can.cbus.node;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeXmlTest {
    
    @Test
    public void testCTor() {
        
        CbusNode node = new CbusNode(null,256);
        
        CbusNodeXml t = new CbusNodeXml(node);
        Assert.assertNotNull("exists",t);
        
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // The minimal setup for log4J
    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeXmlTest.class);

}

package apps;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FileLocationPaneTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        
        FileLocationPane t = new FileLocationPane();
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FileLocationPaneTest.class);

}

package jmri.profile;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProfileManagerDialogTest {

    @Test
    @Disabled("works locally, causes crash on Travis")
    @DisabledIfHeadless
    public void testCTor() {
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame();
        ProfileManagerDialog t = new ProfileManagerDialog(jf,false);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProfileManagerDialogTest.class);

}

package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen
 */
public class CvTableModelTest {

    @Test
    public void testStart() {
        Assertions.assertNotNull(new CvTableModel(new JLabel(), null));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CvTableModelTest.class);

}

package jmri.jmrix.openlcb;

import jmri.jmrix.xpa.XpaSystemConnectionMemo;
import jmri.jmrix.xpa.XpaTurnout;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OlcbUtilsTest {

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    @Test
    public void isOlcbBean() {
        // we can't use an anonymous bean for the false case, so we create a bean from another package.
        XpaSystemConnectionMemo xpaMemo = Mockito.mock(XpaSystemConnectionMemo.class);
        Mockito.when(xpaMemo.getSystemPrefix()).thenReturn("X");
        Assertions.assertFalse( OlcbUtils.isOlcbBean(new XpaTurnout(1,xpaMemo)));
        Assertions.assertTrue( OlcbUtils.isOlcbBean(new OlcbLight("ML1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9")));
    }

}

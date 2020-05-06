package jmri.jmrix.openlcb;

import jmri.jmrix.xpa.XpaSystemConnectionMemo;
import jmri.jmrix.xpa.XpaTurnout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

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
    void isOlcbBean() {
        // we can't use an anonymous bean for the false case, so we create a bean from another package.
        XpaSystemConnectionMemo xpaMemo = Mockito.mock(XpaSystemConnectionMemo.class);
        Mockito.when(xpaMemo.getSystemPrefix()).thenReturn("X");
        assertThat(OlcbUtils.isOlcbBean(new XpaTurnout(1,xpaMemo))).isFalse();
        assertThat(OlcbUtils.isOlcbBean(new OlcbLight("ML1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9"))).isTrue();
    }

}

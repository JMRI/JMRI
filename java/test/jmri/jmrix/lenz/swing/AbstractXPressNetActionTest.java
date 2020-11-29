package jmri.jmrix.lenz.swing;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractXPressNetActionTest {

    private XNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        AbstractXPressNetAction t = new AbstractXPressNetAction("test",memo){
           @Override
           public void actionPerformed(java.awt.event.ActionEvent ae){
           }
        };
        assertThat(t).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        XNetTrafficController tc = Mockito.mock(XNetTrafficController.class);
        LenzCommandStation cs = Mockito.mock(LenzCommandStation.class);
        Mockito.when(tc.getCommandStation()).thenReturn(cs);
        memo = Mockito.mock(XNetSystemConnectionMemo.class);
        Mockito.when(memo.getXNetTrafficController()).thenReturn(tc);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractXPressNetActionTest.class);

}

package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractXNetInitializationManagerTest {

    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        AbstractXNetInitializationManager t = new AbstractXNetInitializationManager(memo){
            @Override
            public void init(){
            }
        };
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = Mockito.mock(XNetTrafficController.class);
        memo = Mockito.mock(XNetSystemConnectionMemo.class);
        Mockito.when(memo.getXNetTrafficController()).thenReturn(tc);
    }

    @AfterEach
    public void tearDown() {
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractXNetInitializationManagerTest.class);

}

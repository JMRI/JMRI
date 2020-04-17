package jmri.jmrix.can.cbus.eventtable;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventTableShutdownTaskTest {

    @Test
    public void testCTor() {
        
        CbusEventTableShutdownTask t = new CbusEventTableShutdownTask("CBUS Test Shutdown Task",null);
        assertThat(t).isNotNull();
        
    }
    
    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableShutdownTaskTest.class);

}

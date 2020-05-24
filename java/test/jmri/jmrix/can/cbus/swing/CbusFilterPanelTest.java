package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.jmrix.can.cbus.CbusFilterType;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusFilterPanelTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCTorNode() {
        CbusFilterPanel t = new CbusFilterPanel(null,1);
        assertThat(t).isNotNull();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCTorFilter() {
        CbusFilterPanel t = new CbusFilterPanel(null,CbusFilterType.CFIN);
        assertThat(t).isNotNull();
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventFilterTest.class);

}

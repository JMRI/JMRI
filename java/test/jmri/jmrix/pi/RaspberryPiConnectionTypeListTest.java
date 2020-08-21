package jmri.jmrix.pi;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RaspberryPiConnectionTypeListTest {

    @Test
    public void testCTor() {
        RaspberryPiConnectionTypeList t = new RaspberryPiConnectionTypeList();
        assertThat(t).isNotNull();
        assertThat(t.getManufacturers()).contains("Raspberry Pi Foundation");
        assertThat(t.getAvailableProtocolClasses()).contains("jmri.jmrix.pi.RaspberryPiConnectionConfig");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiConnectionTypeListTest.class);

}

package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.openlcb.NodeID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmrix.openlcb.OpenLcbLocoAddress class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OpenLcbLocoAddressTest {

    @Test
    public void testEqualsAndHashCode() {
        OpenLcbLocoAddress a1 = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        OpenLcbLocoAddress a2 = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        OpenLcbLocoAddress a3 = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 0, 0}));
        assertThat(a1).isEqualTo(a2).isNotEqualTo(a3).isNotEqualTo(null).isNotEqualTo("foo");
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

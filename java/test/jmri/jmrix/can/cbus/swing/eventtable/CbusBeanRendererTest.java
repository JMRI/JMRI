package jmri.jmrix.can.cbus.swing.eventtable;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusBeanRenderer
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusBeanRendererTest  {

    @Test
    public void testBeanRendererCtor() {
        // for now, just makes sure there isn't an exception.
        Assertions.assertNotNull(new CbusBeanRenderer(26));
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

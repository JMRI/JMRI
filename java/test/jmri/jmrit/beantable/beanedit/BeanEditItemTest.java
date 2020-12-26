package jmri.jmrit.beantable.beanedit;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BeanEditItemTest {

    @Test
    public void testCTor() {
        BeanItemPanel p = new BeanItemPanel();
        BeanEditItem t = new BeanEditItem(p,"IS1","");
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BeanEditItemTest.class);

}

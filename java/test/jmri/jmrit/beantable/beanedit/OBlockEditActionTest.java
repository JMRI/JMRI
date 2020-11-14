package jmri.jmrit.beantable.beanedit;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OBlockEditActionTest {

    @Test
    public void testCTor() {
        OBlockEditAction t = new OBlockEditAction(new ActionEvent(this,1,null));
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockEditActionTest.class);

}

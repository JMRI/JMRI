package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BeanTableFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new BeanTableFrame();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(BeanTableFrameTest.class);

}

package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MemoryInputIconTest extends PositionableJPanelTest {

    @Test
    @Override
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", p);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            MemoryInputIcon t = new MemoryInputIcon(5, editor);
            t.setMemory("IM1");
            p = t;
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(MemoryInputIconTest.class);

}

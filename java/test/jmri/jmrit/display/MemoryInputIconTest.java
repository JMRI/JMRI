package jmri.jmrit.display;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class MemoryInputIconTest extends PositionableJPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);

        editor = new EditorScaffold();
        MemoryInputIcon t = new MemoryInputIcon(5, editor);
        t.setMemory("IM1");
        p = t;

    }

    // private final static Logger log = LoggerFactory.getLogger(MemoryInputIconTest.class);

}

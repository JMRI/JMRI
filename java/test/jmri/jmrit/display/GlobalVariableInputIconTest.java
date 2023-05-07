package jmri.jmrit.display;

import jmri.jmrit.logixng.GlobalVariableManager;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class GlobalVariableInputIconTest extends PositionableJPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("MyVar");
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);

        editor = new EditorScaffold();
        GlobalVariableInputIcon t = new GlobalVariableInputIcon(5, editor);
        t.setGlobalVariable("MyVar");
        p = t;

    }

    // private final static Logger log = LoggerFactory.getLogger(MemoryInputIconTest.class);

}

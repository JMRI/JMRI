package jmri.jmrit.display;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of MemoryComboIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class MemoryComboIconTest extends PositionableJPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);

        editor = new EditorScaffold();
        String args[] = {"foo", "bar"};
        MemoryComboIcon bci = new MemoryComboIcon(editor, args);
        bci.setMemory("IM1");
        p = bci;

    }

}

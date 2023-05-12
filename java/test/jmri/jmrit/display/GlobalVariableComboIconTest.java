package jmri.jmrit.display;

import jmri.jmrit.logixng.GlobalVariableManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of GlobalVariableComboIcon
 *
 * @author Paul Bender      Copyright (C) 2016
 * @author Daniel Bergqvist Copyright (C) 2022
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class GlobalVariableComboIconTest extends PositionableJPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("MyVar");
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);

        editor = new EditorScaffold();
        String args[] = {"foo", "bar"};
        GlobalVariableComboIcon bci = new GlobalVariableComboIcon(editor, args);
        bci.setGlobalVariable("MyVar");
        p = bci;

    }

}

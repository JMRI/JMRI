package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.gui.GuiLafPreferencesManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OBlockEditActionTest {

    @Test
    public void testCTor() {
        OBlockEditAction obea = new OBlockEditAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        Assertions.assertNotNull(obea, "exists");
    }

    // test OBlockEditor via OBlockTableActionTest

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // use _tabbed interface
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(true);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockEditActionTest.class);

}

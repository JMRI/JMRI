package jmri.jmrit.display.switchboardEditor.configurexml;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SwitchboardEditorClassMigrationTest {

    @Test
    public void testCTor() {
        SwitchboardEditorClassMigration t = new SwitchboardEditorClassMigration();
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

}

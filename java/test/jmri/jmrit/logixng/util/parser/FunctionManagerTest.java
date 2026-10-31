package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test FunctionManager
 *
 * @author Daniel Bergqvist 2019
 */
public class FunctionManagerTest {

    @Test
    public void testFunctions() {
        FunctionManager fm = InstanceManager.getDefault(FunctionManager.class);

        for (Map.Entry<String, Function> entry : fm.getFunctions().entrySet()) {
            assertEquals(entry.getKey(), entry.getValue().getName());
            assertNotNull(entry.getValue().getName());
            assertNotEquals("", entry.getValue().getName());
            assertNotNull(entry.getValue().getModule());
            assertNotEquals("", entry.getValue().getModule());
            assertNotNull(entry.getValue().getDescription());
            assertNotEquals("", entry.getValue().getDescription());
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initTimeProviderManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}

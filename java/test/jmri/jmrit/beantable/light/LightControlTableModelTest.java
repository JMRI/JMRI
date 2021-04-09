package jmri.jmrit.beantable.light;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class LightControlTableModelTest {
    
    private LightControlTableModel t;
    
    @Test
    public void testCTor() {
        t = new LightControlTableModel(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testGetColumnCount() {
        t = new LightControlTableModel(null);
        assertEquals(4, t.getColumnCount());
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}

package jmri;

import java.util.Arrays;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Steve
 */
public class SelectionPropertyDescriptorTest {
    
    @Test
    public void testCtor() {
        SelectionPropertyDescriptorImpl t = new SelectionPropertyDescriptorImpl();
        assertNotNull( t, "exists");
    }

    @Test
    public void testGetOptions() {
        SelectionPropertyDescriptorImpl t = new SelectionPropertyDescriptorImpl();
        assertArrayEquals(new String[]{"A","B","C"}, t.getOptions());
    }

    @Test
    public void testGetOptionToolTips() {
        SelectionPropertyDescriptorImpl t = new SelectionPropertyDescriptorImpl();
        assertEquals(Arrays.asList(new String[]{"A Tip","B Tip","C Tip"}), t.getOptionToolTips());
    }
    
    @Test
    public void testPassThroughSuper() {
        SelectionPropertyDescriptorImpl t = new SelectionPropertyDescriptorImpl();
        assertEquals( "B",t.defaultValue, "Default property option");
        assertEquals( "TEST_PROPERTY_KEY",t.propertyKey, "property key set");
        assertTrue( t.isEditable(null), "editable set true");
        assertEquals( "Column Header Text",t.getColumnHeaderText(), "column header set");
    }

    private static class SelectionPropertyDescriptorImpl extends SelectionPropertyDescriptor {

        private SelectionPropertyDescriptorImpl() {
            super("TEST_PROPERTY_KEY", new String[]{"A","B","C"}, new String[]{"A Tip","B Tip","C Tip"}, "B");
        }
        
        @Override
        public boolean isEditable(NamedBean bean){
            return true;
        }
        
        @Override
        public String getColumnHeaderText(){
            return "Column Header Text";
        }
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}

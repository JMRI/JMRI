package jmri;

import java.util.Arrays;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author Steve
 */
public class SelectionPropertyDescriptorTest {
    
    @Test
    public void testCTor() {
        SelectionPropertyDescriptorImpl t = new SelectionPropertyDescriptorImpl();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetValueClass() {
        SelectionPropertyDescriptorImpl t = new SelectionPropertyDescriptorImpl();
        assertEquals(javax.swing.JComboBox.class, t.getValueClass());
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
        Assert.assertEquals("Default property option","B",t.defaultValue);
        Assert.assertEquals("property key set","TEST_PROPERTY_KEY",t.propertyKey);
        Assert.assertEquals("editable set true",true,t.isEditable(null));
        Assert.assertEquals("column header set","Column Header Text",t.getColumnHeaderText());
    }

    private class SelectionPropertyDescriptorImpl extends SelectionPropertyDescriptor {

        public SelectionPropertyDescriptorImpl() {
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

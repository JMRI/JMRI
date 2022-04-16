package jmri.jmrix.can.cbus.swing.modules;

import jmri.jmrix.can.cbus.node.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author andrew
 */
public class CbusConfigPaneProviderTest {
    
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCtor() {
        CbusConfigPaneProviderImpl t = new CbusConfigPaneProviderImpl();
        Assert.assertNotNull("exists",t);
    }
    
    // Abstract class cannot be instantiated directly
    public class CbusConfigPaneProviderImpl extends CbusConfigPaneProvider {

        public CbusConfigPaneProviderImpl() {
            super();
        }

        @Override
        public String getModuleType() {
            return null;
        }
        
        @Override
        public String getNVNameByIndex(int index) {
            return null;
        }

        @Override
        public AbstractEditNVPane getEditNVFrameInstance() {
            return null;
        }
        
        @Override
        public AbstractEditNVPane getEditNVFrame(CbusNodeNVTableDataModel editFrame, CbusNode node) {
            return null;
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

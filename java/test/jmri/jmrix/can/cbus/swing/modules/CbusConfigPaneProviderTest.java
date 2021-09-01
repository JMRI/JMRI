/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.can.cbus.swing.modules;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.*;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author andrew
 */
public class CbusConfigPaneProviderTest {
    
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNode nd = new CbusNode(null,12345);
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
        public JPanel getEditNVFrame(CbusNodeNVTableDataModel editFrame, CbusNode node) {
            return null;
        }

        @Override
        public JPanel getNewEditNVFrame(CbusNodeNVTableDataModel editFrame, CbusNode node) {
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

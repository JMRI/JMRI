package jmri.jmrix.bachrus.kpfserialdriver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Steve Young (c) 2022
 */
public class SerialDriverAdapterTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {
    
    @Override
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        apc = new SerialDriverAdapter();
    }
    
    @Override
    @AfterEach
    public void tearDown(){
        super.tearDown();
        JUnitUtil.tearDown();
    }
    
}

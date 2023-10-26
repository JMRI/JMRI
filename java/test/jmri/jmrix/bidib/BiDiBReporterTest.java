package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBReporter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBReporterTest  extends jmri.implementation.AbstractReporterTestBase {
    
    BiDiBSystemConnectionMemo memo;
        
    @Override
    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        r = new BiDiBReporter("BR42", new BiDiBReporterManager(memo));
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        r = null;
        JUnitUtil.tearDown();
    }

}

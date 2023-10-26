package jmri.jmrix.bidib.swing.mon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBMonPane class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {
    
    BiDiBSystemConnectionMemo memo;
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));

        // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new BiDiBMonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = "BiDiB Traffic Monitor";
    }
    
    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

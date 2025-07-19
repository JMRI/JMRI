package jmri.jmrix.bidib.swing;

import jmri.jmrit.beantable.signalmast.AbstractSignalMastAddPaneTestBase;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBSignalMastAddPane class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSignalMastAddPaneTest  extends AbstractSignalMastAddPaneTestBase {

    BiDiBSystemConnectionMemo memo;

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() {
        return new BiDiBSignalMastAddPane();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

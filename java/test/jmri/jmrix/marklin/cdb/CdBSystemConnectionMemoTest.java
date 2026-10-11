package jmri.jmrix.marklin.cdb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the CdBSystemConnectionMemo class.
 * @author Steve Young Copyright (C) 2024
 */
public class CdBSystemConnectionMemoTest extends
    jmri.jmrix.SystemConnectionMemoTestBase<CdBSystemConnectionMemo> {

    @Test
    @Disabled("Tested class requires further development")
    @Override
    public void testGetActionFactory(){}

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new CdBSystemConnectionMemo();
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}

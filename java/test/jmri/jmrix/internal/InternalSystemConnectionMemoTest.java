package jmri.jmrix.internal;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InternalSystemConnectionMemoTest extends SystemConnectionMemoTestBase<InternalSystemConnectionMemo> {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new InternalSystemConnectionMemo();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(InternalSystemConnectionMemoTest.class);
}

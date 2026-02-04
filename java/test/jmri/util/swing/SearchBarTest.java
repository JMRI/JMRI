package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class SearchBarTest {

    @Test
    public void testCTor() {
        SearchBar t = new SearchBar(null, null, null);
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SearchBarTest.class);

}

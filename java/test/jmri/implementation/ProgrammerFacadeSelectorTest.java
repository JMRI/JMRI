package jmri.implementation;

import jmri.*;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProgrammerFacadeSelectorTest {

    // no Ctor test, tested class only supplies static methods

    @Test
    public void testLoadFacadeElements() {

        Element element = new Element("IncorrectElement");
        ProgrammerScaffold progScaff = new ProgrammerScaffold(ProgrammingMode.DIRECTMODE);

        Programmer t = ProgrammerFacadeSelector.loadFacadeElements(element, progScaff, false, null);
        Assertions.assertEquals( progScaff, t);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProgrammerFacadeSelectorTest.class);

}

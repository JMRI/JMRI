package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for AudioException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class AudioExceptionTest {

    @Test
    public void testConstructor(){
        assertNotNull( new AudioException(), "AudioException constructor");
    }

    @Test
    public void testStringConstructor(){
        assertNotNull( new AudioException("test exception"), "AudioException string constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}

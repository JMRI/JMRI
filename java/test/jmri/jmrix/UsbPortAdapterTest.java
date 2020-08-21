package jmri.jmrix;

import jmri.SystemConnectionMemo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbPortAdapterTest {

    @Test
    public void testCTor() {
        SystemConnectionMemo memo = Mockito.mock(SystemConnectionMemo.class);
        Mockito.when(memo.getUserName()).thenReturn("test");
        Mockito.when(memo.getSystemPrefix()).thenReturn("I");
        UsbPortAdapter t = new UsbPortAdapter(memo);
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}

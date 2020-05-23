package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.implementation.DefaultRoute;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for jmri.jmrit.beantable.routtable.RouteExportToLogix
 *
 * @author Paul Bender Copyright (C) 2020
 */
class RouteExportToLogixTest {

    private LogixManager lm;
    private ConditionalManager cm;
    private RouteManager rm;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        lm = Mockito.mock(LogixManager.class);
        Mockito.when(lm.getSystemNamePrefix()).thenReturn("IL");
        cm = Mockito.mock(ConditionalManager.class);
        rm = Mockito.mock(RouteManager.class);
    }

    @AfterEach
    void tearDown() {
        lm = null;
        rm = null;
        cm = null;
        JUnitUtil.tearDown();
    }

    @Test
    void export() {
        Route r = new DefaultRoute("IO12345","Hello World");
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(r);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(r);
        Logix l = Mockito.mock(Logix.class);
        //Mockito.when(lm.getBySystemName("IL:RTX:IO12345")).thenReturn(l);
        Mockito.when(lm.createNewLogix("IL:RTX:IO12345")).thenReturn(l);
        Mockito.when(lm.createNewLogix("IL:RTX:IO12345","Hello World")).thenReturn(l);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        Mockito.verify(rm).deleteRoute(r);
    }
}

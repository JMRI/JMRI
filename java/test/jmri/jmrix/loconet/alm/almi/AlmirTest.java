package jmri.jmrix.loconet.alm.almi;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author B. Milhaupt  Copyright (C) 2022
 */
class AlmirTest {

    @Test
    public void testAlmRouteCapabilitiesQuery() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 15});
        Assert.assertEquals("Routes capabilities (command station, DS7x, etc.) query.\n", Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x00, 0, 0x00, 0x0f, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 15});
        Assert.assertEquals("Routes capabilities (command station, DS7x, etc.) query.\n", Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteDataQuery1() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x00, 0, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x01, 0x00, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 1 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 5-8.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 2, 0x00, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 9-12.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x03, 0x00, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 2 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 13-16.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x20, 0x00, 0x0, 0xF, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7});
        Assert.assertEquals("Query command station Route 17 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 9 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7c, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 127 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7f, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 13-16.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7D, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 127 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 5-8.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7E, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 128 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 9-12.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 13-16.\n",
                Almir.interpretAlmRoutes(l));
    }

    @Test
    public void testAlmRouteDataReport1() {
        LocoNetMessage l;
//E6 10 01 02 ALM-style command station Route Data Report

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Report command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, report command station Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x02, 0x00, 0x0F, 0x0C, 0x20, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x2B});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x7F ,0x7F ,0x75});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, 128t, 129c, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x7F ,0x7F ,0x75});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, 128t, 129c, Unused.\n",
                Almir.interpretAlmRoutes(l));
    }

    @Test
    public void testAlmRouteCSDataWrite1() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x03, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Write command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, write command station Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteCSDataReport1() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, 128t, 129c, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x10 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128t, 129c, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128c, 129c, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128c, 129t, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x14 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128c, 129t, 513t.\n",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteDeviceDeselect1() {
        LocoNetMessage l;
         //EE 10 02 00 ALM-style DS7x-series device de-selection for routes accesses

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03});
        Assert.assertEquals(" ALM task 2 test 1 - deselect",
                "De-select the device currently selected for ALM Routes configuration, if any.\n",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteLengthTest1() {
        LocoNetMessage l;

         l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03,0});
        Assert.assertEquals(" ALM task check length test 1",
                "",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" ALM task check length test 2",
                "",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteDevSel1() {
        LocoNetMessage l;
        //EE 10 02 0e ALM-style DS7x-series device selection for routes accesses

         l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x02, 0x0e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7c, 0x60, 0x00, 0x00, 0x00, 0x00, 0x03});
        Assert.assertEquals(" ALM task 2 test 5 - selecting",
                "Selecting device of type DS78V, s/n 0x0, using addresses 1 thru 8 for Routes programming.\n",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteCSCapabilitiesReport1() {
        //E6 10 01 00 ALM-style command station Routes Capability Report
        LocoNetMessage l;

         l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x00, 0x40, 0x00, 0x03, 0x02, 0x08, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03});
        Assert.assertEquals(" ALM task 2 test 6 - reporting routes capabilities",
                "Command Station Routes Capabilities reply: 32 routes, 8 entries each route.\n",
                Almir.interpretAlmRoutes(l));
    }

    @Test
    public void testAlmRouteDeviceCapabilitiesReport1() {
        //E6 10 02 00 ALM-style DS7x-series Route capabilities Report
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x02, 0x00, 0x10, 0x00, 0x00, 0x02, 0x08, 0x74, 0x02, 0x02, 0x06, 0x03, 0x04, 0x03});
        Assert.assertEquals(" ALM task 2 test 7 - reporting routes capabilities 2",
                "Device DS74 (s/n 0x302) in Slow motion mode (routes currently enabled), using turnout addresses 516 thru 519, with 8 routes of 8 entries per route, may be configured using ALM messaging.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x02, 0x00, 0x20, 0x00, 0x00, 0x02, 0x08, 0x7c, 0x04, 0x02, 0x06, 0x03, 0x04, 0x03});
        Assert.assertEquals(" ALM task 2 test 7 - reporting routes capabilities 2",
                "Device DS78V (s/n 0x302) in Servo (2-position) mode (routes currently enabled), using turnout addresses 516 thru 523, with 16 routes of 8 entries per route, may be configured using ALM messaging.\n",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteSomeUndefineds1() {
        LocoNetMessage l;

         l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0});
        Assert.assertEquals("OPC_ALM_RD task 0 test 1 - Undefined operation",
                "",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x02, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0});
        Assert.assertEquals("OPC_ALM_RD task 0 test 2 - Undefined operation",
                "",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x03, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0x00, 0, 0});
        Assert.assertEquals("OPC_ALM_RD task 0 test 3 - Undefined operation",
                "",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testRoutesALMGeneral() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03});
        Assert.assertEquals(" ALM task 2 test 1",
                "De-select the device currently selected for ALM Routes configuration, if any.\n",
                Almir.interpretAlmRoutes(l));
        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x00, 0, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x01, 0x00, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 1 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 5-8.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 2, 0x00, 0x0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 9-12.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x03, 0x00, 0, 0x00,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query command station Route 2 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 1 entries 13-16.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 32, 0x00, 0, 0xF, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7, 0xF, 0x2D});

        Assert.assertEquals("Query command station Route 17 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 9 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x78, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 125 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 63 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7C, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 127 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 1-4.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7D, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 127 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 5-8.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7E, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 128 entries 1-4 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 9-12.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, query command station Route 64 entries 13-16.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x03, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Write command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, write command station Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Report command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, report command station Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Report command station Route 128 entries 5-8 if 8 entries per route.  Or if 16 entries per route, report command station Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x02, 0x00, 0x0F, 0x0C, 0x20, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x2B});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x7F ,0x7F ,0x75});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, 128t, 129c, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x7F ,0x7F ,0x75});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, 128t, 129c, Unused.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13c, 128t, 129c, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x10 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128t, 129c, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128c, 129c, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128c, 129t, 513c.\n",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x14 ,0x48});
        Assert.assertEquals("Report command station Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report command station Route 1 entries 9-12 with 13t, 128c, 129t, 513t.\n",
                Almir.interpretAlmRoutes(l));

     }

    @Test
    public void testAlmRouteDevDataReport() {
        //E6 10 02 02 ALM-style DS7x-series Route Data Report
        LocoNetMessage l = new LocoNetMessage(new int[] {
        0xE6, 0x10, 0x02, 0x02, 0x00, 0x00, 0x00, 0x1F, 0x10, 0x44, 0x37, 0x39, 0x12, 0x75, 0x1B, 0x30});
        Assert.assertEquals("check 1a","Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 32t, 965c, 314t, 1526t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(8,0);
        l.setElement(9,0);
        l.setElement(10,0);
        l.setElement(11,0);
        l.setElement(12,0);
        l.setElement(13,0);
        l.setElement(14,0);
        int i;
        for (i = 1; i < 0x80; ) {
            l.setElement(7, i);
            Assert.assertEquals("check 1 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with "+(i+1)+"t, 1t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(7,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(8, i);
            Assert.assertEquals("check 2 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with "+((i<<7)+1)+"t, 1t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(8,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(9, i);
            Assert.assertEquals("check 3 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, "+(i+1)+"t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(9,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(10, i);
            Assert.assertEquals("check 4 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, "+((i<<7)+1)+"t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(10,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(11, i);
            Assert.assertEquals("check 5 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1t, "+(i+1)+"t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(11,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(12, i);
            Assert.assertEquals("check 6 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1t, "+((i<<7)+1)+"t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(12,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(13, i);
            Assert.assertEquals("check 7 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1t, 1t, "+(i+1)+"t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(13,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(14, i);
            Assert.assertEquals("check 8 loop "+i,"Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1t, 1t, "+((i<<7)+1)+"t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(14,0);
        l.setElement(8,0x20);
        Assert.assertEquals("check closed turnout 1","Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1c, 1t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(8,0);
        l.setElement(10,0x20);
        Assert.assertEquals("check closed turnout 2","Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1c, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(10,0);
        l.setElement(12,0x20);
        Assert.assertEquals("check closed turnout 3","Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1t, 1c, 1t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(12,0);
        l.setElement(14,0x20);
        Assert.assertEquals("check closed turnout 4","Report device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, report device Route 1 entries 1-4 with 1t, 1t, 1t, 1c.\n", Almir.interpretAlmRoutes(l));
    }

    @Test
    public void testAlmRouteCsDataWr() {
        //EE 10 01 03 ALM-style Command station Route Data Write
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x03, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("",
                Almir.interpretAlmRoutes(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x03, 0x7D, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Write command station Route 127 entries 5-8 if 8 entries per route.  Or if 16 entries per route, write command station Route 64 entries 5-8 with Unused, Unused, Unused, Unused.\n",
                Almir.interpretAlmRoutes(l));

    }

    @Test
    public void testAlmRouteDevRouteDataWrite() {
        //EE 10 02 03 ALM-style DS7x-series Route Data Write
        LocoNetMessage l = new LocoNetMessage(new int[] {
            0xEE, 0x10, 0x02, 0x03, 0x02, 0x00, 0x00, 0x7F, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x30});
        Assert.assertEquals("check 1a","Write device Route 2 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 9-12 with Unused, Unused, Unused, Unused.\n", Almir.interpretAlmRoutes(l));

        l.setElement(4,1);
        Assert.assertEquals("check 1b","Write device Route 1 entries 5-8 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 5-8 with Unused, Unused, Unused, Unused.\n", Almir.interpretAlmRoutes(l));

        l.setElement(4,3);
        Assert.assertEquals("check 1c","Write device Route 2 entries 5-8 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 13-16 with Unused, Unused, Unused, Unused.\n", Almir.interpretAlmRoutes(l));

        l.setElement(4,0x7F);
        Assert.assertEquals("check 1d","Write device Route 64 entries 5-8 if 8 entries per route.  Or if 16 entries per route, write device Route 32 entries 13-16 with Unused, Unused, Unused, Unused.\n", Almir.interpretAlmRoutes(l));

        l.setElement(4,0x00);
        l.setElement(5, 0x01);
        Assert.assertEquals("check 13","Write device Route 65 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 33 entries 1-4 with Unused, Unused, Unused, Unused.\n", Almir.interpretAlmRoutes(l));

        l.setElement(4,0);
        l.setElement(5,0);
        l.setElement(8,0);
        l.setElement(9,0);
        l.setElement(10,0);
        l.setElement(11,0);
        l.setElement(12,0);
        l.setElement(13,0);
        l.setElement(14,0);
        int i;
        for (i = 1; i < 0x80; ) {
            l.setElement(7, i);
            Assert.assertEquals("check 1 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with "+(i+1)+"t, 1t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(7,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(8, i);
            Assert.assertEquals("check 2 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with "+((i<<7)+1)+"t, 1t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(8,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(9, i);
            Assert.assertEquals("check 3 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, "+(i+1)+"t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(9,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(10, i);
            Assert.assertEquals("check 4 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, "+((i<<7)+1)+"t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(10,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(11, i);
            Assert.assertEquals("check 5 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1t, "+(i+1)+"t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(11,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(12, i);
            Assert.assertEquals("check 6 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1t, "+((i<<7)+1)+"t, 1t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(12,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(13, i);
            Assert.assertEquals("check 7 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1t, 1t, "+(i+1)+"t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(13,0);
        for (i = 1; i < 0x10; ) {
            l.setElement(14, i);
            Assert.assertEquals("check 8 loop "+i,"Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1t, 1t, "+((i<<7)+1)+"t.\n", Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(14,0);
        l.setElement(8,0x20);
        Assert.assertEquals("check closed turnout 1","Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1c, 1t, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(8,0);
        l.setElement(10,0x20);
        Assert.assertEquals("check closed turnout 2","Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1c, 1t, 1t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(10,0);
        l.setElement(12,0x20);
        Assert.assertEquals("check closed turnout 3","Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1t, 1c, 1t.\n", Almir.interpretAlmRoutes(l));
        l.setElement(12,0);
        l.setElement(14,0x20);
        Assert.assertEquals("check closed turnout 4","Write device Route 1 entries 1-4 if 8 entries per route.  Or if 16 entries per route, write device Route 1 entries 1-4 with 1t, 1t, 1t, 1c.\n", Almir.interpretAlmRoutes(l));
    }

    @Test
    public void testAlmRouteDevRouteDataQuery() {
        //EE 10 02 02 ALM-style DS7x-series Route Data Query
        LocoNetMessage l = new LocoNetMessage(new int[] {
            0xEE, 0x10, 0x02, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x74, 0x00, 0x00, 0x00, 0x00, 0x30});
        Assert.assertEquals("check 1a","Query the selected device, Route 1, entries 1 thru 4.\n", Almir.interpretAlmRoutes(l));
        int i;
        for (i = 1; i < 0x80; ) {
            l.setElement(4, i);
            Assert.assertEquals("check 1 loop "+i,"Query the selected device, Route "
                    + (1+(i/2)) + ", entries " + ((i&1)==1?"5 thru 8":"1 thru 4")
                    + ".\n",
                    Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(4,0);
        for (i = 1; i < 0x80; ) {
            l.setElement(5, i);
            if (i < 2) {
                Assert.assertEquals("check 2 loop "+i,"Query the selected device, Route "
                        +(1+(i<<6))+", entries 1 thru 4.\n",
                        Almir.interpretAlmRoutes(l));
            } else {
                Assert.assertEquals("check 2 loop "+i,"",Almir.interpretAlmRoutes(l));
            }
            i <<= 1;
        }
    }

    @Test
    public void testAlmRouteDevChgAddr() {
        //EE 10 02 0f ALM-style DS7x-series device change base address
        //Device {0}, s/n 0x{1}, using addresses {2} thru {3} has been selected for Routes configuration.\n
        LocoNetMessage l = new LocoNetMessage(new int[] {
            0xEE, 0x10, 0x02, 0x0f, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x74, 0x00, 0x01, 0x02, 0x03, 0x04, 0x00});
        Assert.assertEquals("basic check 1",
                "Change DS74, s/n 0x101 to use new Base Address 516.\n",
                Almir.interpretAlmRoutes(l));

        l.setElement(9,0x7c);
        Assert.assertEquals("basic check 1",
                "Change DS78V, s/n 0x101 to use new Base Address 516.\n",
                Almir.interpretAlmRoutes(l));

        l.setElement(9,0x74);
        int i;
        l.setElement(12, 0);
        for (i = 1; i < 0x80;) {
            l.setElement(11, i);
            Assert.assertEquals("basic check 3 loop "+i,
                    "Change DS74, s/n 0x"
                    +Integer.toHexString(i)+" to use new Base Address 516.\n",
                    Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(9, 0x7C);
        l.setElement(11, 0);
        for (i = 1; i< 0x80;) {
            l.setElement(12, i);
            Assert.assertEquals("basic check 4 loop "+i,
                    "Change DS78V, s/n 0x"
                    +Integer.toHexString(i<<7)+" to use new Base Address 516.\n",
                    Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(12, 0);
        l.setElement(14, 0);
        for (i = 1; i< 0x80;) {
            l.setElement(13, i);
            Assert.assertEquals("basic check 5 loop "+i,
                    "Change DS78V, s/n 0x0 to use new Base Address "+(i+1)+".\n",
                    Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
        l.setElement(9, 0x74);
        l.setElement(13, 0);
        for (i = 1; i< 0x80;) {
            l.setElement(14, i);
            Assert.assertEquals("basic check 6 loop "+i,
                    "Change DS74, s/n 0x0 to use new Base Address "+Integer.toString(((i<<7)+1))+".\n",
                    Almir.interpretAlmRoutes(l));
            i <<= 1;
        }
    }
    
    @Test
    public void testAlmRouteDevSelectReply() {
        //E6 10 02 0E ALM-style Device selection Reply
        //Device {0}, s/n 0x{1}, using addresses {2} thru {3} has been selected for Routes configuration.\n
        LocoNetMessage l = new LocoNetMessage(new int[] {
            0xE6, 0x10, 0x02, 0x0E, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        });
        Assert.assertEquals("alm route dev select reply case 1",
                "Device (Unknown), s/n 0x0, using addresses 1 thru 1 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        
        int i;
        for (i = 0; i < 0x80;) {
            l.setElement(13, i);
            Assert.assertEquals("alm route dev select reply case 2 loop "+i,
                    "Device (Unknown), s/n 0x0, using addresses "+(i+1)+" thru "+(i+1)+" has been selected for Routes configuration.\n",
                    Almir.interpretAlmRoutes(l));
            if (i == 0) {
                i = 1;
            } else {
                i <<= 1;
            }
        }
        l.setElement(13, 0);
        for (i = 0; i < 0x80;) {
            l.setElement(14, i);
            Assert.assertEquals("alm route dev select reply case 3 loop "+i,
                    "Device (Unknown), s/n 0x0, using addresses "+((i<<7)+1)+" thru "+((i<<7)+1)+" has been selected for Routes configuration.\n",
                    Almir.interpretAlmRoutes(l));
            if (i == 0) {
                i = 1;
            } else {
                i <<= 1;
            }
        }
        l.setElement(14, 0);
        for (i = 0; i < 0x80;) {
            l.setElement(11, i);
            Assert.assertEquals("alm route dev select reply case 4 loop "+i,
                    "Device (Unknown), s/n 0x"+Integer.toHexString(i)+", using addresses 1 thru 1 has been selected for Routes configuration.\n",
                    Almir.interpretAlmRoutes(l));
            if (i == 0) {
                i = 1;
            } else {
                i <<= 1;
            }
        }
 
        l.setElement(11, 0);
        for (i = 0; i < 0x80;) {
            l.setElement(12, i);
            Assert.assertEquals("alm route dev select reply case 5 loop "+i,
                    "Device (Unknown), s/n 0x"+Integer.toHexString(i<<7)+", using addresses 1 thru 1 has been selected for Routes configuration.\n",
                    Almir.interpretAlmRoutes(l));
            if (i == 0) {
                i = 1;
            } else {
                i <<= 1;
            }
        }
        l.setElement(12, 0);
        l.setElement(9, 0x74);
        Assert.assertEquals("alm route dev select reply case 6",
                "Device DS74, s/n 0x0, using addresses 1 thru 4 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 4);
        Assert.assertEquals("alm route dev select reply case 7",
                "Device DS74, s/n 0x0, using addresses 1 thru 4 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 8);
        Assert.assertEquals("alm route dev select reply case 8",
                "Device DS74, s/n 0x0, using addresses 1 thru 4 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 10);
        Assert.assertEquals("alm route dev select reply case 9",
                "Device DS74, s/n 0x0, using addresses 1 thru 8 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 12);
        Assert.assertEquals("alm route dev select reply case 10",
                "Device DS74, s/n 0x0, using addresses 1 thru 4 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));

        l.setElement(10, 0);
        l.setElement(9, 0x7c);
        Assert.assertEquals("alm route dev select reply case 11",
                "Device DS78V, s/n 0x0, using addresses 1 thru 8 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 4);
        Assert.assertEquals("alm route dev select reply case 12",
                "Device DS78V, s/n 0x0, using addresses 1 thru 8 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 8);
        Assert.assertEquals("alm route dev select reply case 13",
                "Device DS78V, s/n 0x0, using addresses 1 thru 8 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 10);
        Assert.assertEquals("alm route dev select reply case 14",
                "Device DS78V, s/n 0x0, using addresses 1 thru 8 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        l.setElement(10, 12);
        Assert.assertEquals("alm route dev select reply case 15",
                "Device DS78V, s/n 0x0, using addresses 1 thru 16 has been selected for Routes configuration.\n",
                Almir.interpretAlmRoutes(l));
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

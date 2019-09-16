package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LnIPLImplementation
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Bob Milhaupt Copyright (C) 2018
 */
public class LnIPLImplementationTest {
    jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;
    jmri.jmrix.loconet.LnTrafficController lnis;
    LnIPLImplementation iplImplementation;
    LocoNetMessage m;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", iplImplementation);
        memo.dispose();
        jmri.InstanceManager.deregister(memo, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);

//        memo.configureManagers();
//        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);

        jmri.InstanceManager.store(memo, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        iplImplementation = new LnIPLImplementation(memo);
    }

    @Test
    public void testCreateQueryAllIplDevices() {
        m = LnIPLImplementation.createQueryAllIplDevicesPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 0, 0, 0, 0, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Byte "+i+ " test", expect[i], m.getElement(i));
        }
    }

    @Test
    public void testCreateQueryHostDevices() {
        m = LnIPLImplementation.createIplSpecificHostQueryPacket(1, 2);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 1, 2, 0, 0, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 1 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(2, 4);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 2; expect[5] = 4;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 2 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(4, 8);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 4; expect[5] = 8;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 3 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(8, 16);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 8; expect[5] = 16;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 4 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(32, 64);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 32; expect[5] = 64;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 5 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(16, 32);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 16; expect[5] = 32;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 6 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(64, 1);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 64; expect[5] = 1;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 7 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(33, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 33; expect[5] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 8 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(128, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 0; expect[5] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 9 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificHostQueryPacket(0, 128);
        Assert.assertEquals("message length",20,  m.getNumDataElements());
        expect[4] = 0; expect[5] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 10 Byte "+i+ " test", expect[i], m.getElement(i));
        }

    }

    @Test
    public void testCreateQuerySlaveDevices() {
        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x01, 0x7e);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 0, 0, 0x7E, 0x01, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 1 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x02, 0x7d);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x7d; expect[7] = 0x2;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 2 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x04, 0x7b);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x7b; expect[7] = 0x04;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 3 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x08, 0x77);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x77; expect[7] = 0x08;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 4 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x10, 0x7f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x7f; expect[7] = 0x10;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 5 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x20, 0x6f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x6f; expect[7] = 0x20;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 6 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x40, 0x5f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x5f; expect[7] = 0x40;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 7 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x0, 0x3f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x3f; expect[7] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 8 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(128, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0; expect[7] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 9 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0, 128);
        Assert.assertEquals("message length",20,  m.getNumDataElements());
        expect[6] = 0; expect[7] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 10 Byte "+i+ " test", expect[i], m.getElement(i));
        }

    }

    @Test
    public void testCreateQueryHostSlaveDevices() {
        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(1, 2, 4, 8);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 1, 2, 8, 4, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 1 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(2, 4, 8, 16);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  2; expect[5] =  4; expect[6] = 16; expect[7] = 8;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 2 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(4, 8, 16, 32);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 4; expect[5] =  8; expect[6] = 32; expect[7] = 16;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 3 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(8, 16, 32, 64);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  8; expect[5] = 16; expect[6] = 64; expect[7] = 32;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 4 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(16, 32, 64, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 16; expect[5] = 32; expect[6] =  0; expect[7] = 64;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 5 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(32, 64, 15, 7);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 32; expect[5] = 64; expect[6] =  7; expect[7] = 15;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 6 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(64, 3, 19, 41);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  64; expect[5] =  3; expect[6] = 41; expect[7] = 19;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 7 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0, 0x7f, 0x12, 0x64);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  0; expect[5] =  0x7f; expect[6] =  0x64; expect[7] =  0x12;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 8 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplSpecificSlaveQueryPacket(0x3d, 0x4e, 0x2a, 0x0d);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  0x3d; expect[5] =  0x4E; expect[6] =  0x0d; expect[7] =  0x2a;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 9 Byte "+i+ " test", expect[i], m.getElement(i));
        }
    }

    @Test
    public void checkSpecificDeviceTypeQueryMessages() {
        m = LnIPLImplementation.createIplUr92QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 0, 92, 0, 0, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type UR92 test Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplDt402QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 42;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type DT402 test Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplUt4QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 4;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type UT4 test Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplDcs51QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 51;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type DCS51 test Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplDcs52QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 52;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type DCS52 test Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = LnIPLImplementation.createIplPr3QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 0x23;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type PR3 test Byte "+i+ " test", expect[i], m.getElement(i));
        }
    }

    @Test
    public void checkIplIdentityReportMethods() {
        int mfg =0 ; int slave = 0; int slaveMfg = 0; int di_hsw = 0;
        int di_f1 = 1; int di_ssw = 0; int di_hs0 = 0; int di_hs1 = 0; int di_hs2 = 0;
        int di_f2 = 0; int di_ss0 = 0; int di_ss1 = 0; int di_ss2 = 0; int di_ss3 = 0;

        int msg[] = {0xE5, 0x14, 0x0f, 0x10, mfg, 0, slave, slaveMfg,
                    di_hsw, di_f1, di_ssw, di_hs0, di_hs1, di_hs2, di_f2, di_ss0,
                    di_ss1, di_ss2, di_ss3, 0};
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query",
                    LnIPLImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report",
                    LnIPLImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device ",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));

            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device ",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg|0x80, dev));
            
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device ",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev|0x80));
            
            Assert.assertEquals("IplIdentity device "+dev+" check is UR92 device", dev == 92,
                    LnIPLImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402 device", dev == 42,
                    LnIPLImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4 device", dev == 4,
                    LnIPLImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS51 device", dev == 51,
                    LnIPLImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS52 device", dev == 52,
                    LnIPLImplementation.isIplDcs52IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR3 device", dev == 35,
                    LnIPLImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is DT402D device",
                    LnIPLImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is UT4D device",
                    LnIPLImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR4 device", dev == 36,
                    LnIPLImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is BXP88 device", dev == 88,
                    LnIPLImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is LNWI device", dev == 99,
                    LnIPLImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS240 device", dev == 28,
                    LnIPLImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS210 device", dev == 27,
                    LnIPLImplementation.isIplDcs210IdentityReportMessage(m));
        }
        mfg = 1;
        msg[4]=mfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query mfg=1",
                    LnIPLImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report mfg=1",
                    LnIPLImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=1",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=1",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=1",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertFalse("IplIdentity device "+dev+" check is UR92 device, mfg=1",
                    LnIPLImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DT402 device, mfg=1",
                    LnIPLImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is UT4 device, mfg=1",
                    LnIPLImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS51 device, mfg=1",
                    LnIPLImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS52 device, mfg=1",
                    LnIPLImplementation.isIplDcs52IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR3 device, mfg=1",
                    LnIPLImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is DT402D device, mfg=1",
                    LnIPLImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is UT4D device, mfg=1",
                    LnIPLImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR4 device, mfg=1",
                    LnIPLImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is BXP88 device, mfg=1",
                    LnIPLImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is LNWI device, mfg=1",
                    LnIPLImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS240 device, mfg=1",
                    LnIPLImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS210 device, mfg=1",
                    LnIPLImplementation.isIplDcs210IdentityReportMessage(m));
        }
        mfg = 127;
        msg[4] = mfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query mfg=127",
                    LnIPLImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report mfg=127",
                    LnIPLImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=127",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=127",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=127",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertFalse("IplIdentity device "+dev+" check is UR92 device, mfg=127",
                    LnIPLImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DT402 device, mfg=127",
                    LnIPLImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is UT4 device, mfg=127",
                    LnIPLImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS51 device, mfg=127",
                    LnIPLImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS52 device, mfg=127",
                    LnIPLImplementation.isIplDcs52IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR3 device, mfg=127",
                    LnIPLImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is DT402D device, mfg=127",
                    LnIPLImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is UT4D device, mfg=127",
                    LnIPLImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR4 device, mfg=127",
                    LnIPLImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is BXP88 device, mfg=127",
                    LnIPLImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is LNWI device, mfg=127",
                    LnIPLImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS240 device, mfg=127",
                    LnIPLImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS210 device, mfg=127",
                    LnIPLImplementation.isIplDcs210IdentityReportMessage(m));
        }

        mfg = 0;
        msg[4] = mfg;
        msg[6] = 24;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query, mfg=0, slave=24",
                    LnIPLImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report, mfg=0, slave=24",
                    LnIPLImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=0, slave=24",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=0, slave=24",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=0, slave=24",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertEquals("IplIdentity device "+dev+" check is UR92 device, mfg=0, slave=24", dev == 92,
                    LnIPLImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402 device, mfg=0, slave=24", dev == 42,
                    LnIPLImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4 device, mfg=0, slave=24", dev == 4,
                    LnIPLImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS51 device, mfg=0, slave=24", dev == 51,
                    LnIPLImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS52 device, mfg=0, slave=24", dev == 52,
                    LnIPLImplementation.isIplDcs52IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR3 device, mfg=0, slave=24", dev == 35,
                    LnIPLImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402D device, mfg=0, slave=24", dev == 42,
                    LnIPLImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4D device, mfg=0, slave=24", dev == 4,
                    LnIPLImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR4 device, mfg=0, slave=24", dev == 36,
                    LnIPLImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is BXP88 device, mfg=0, slave=24", dev == 88,
                    LnIPLImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is LNWI device, mfg=0, slave=24", dev == 99,
                    LnIPLImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS240 device, mfg=0, slave=24", dev == 28,
                    LnIPLImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS210 device, mfg=0, slave=24", dev == 27,
                    LnIPLImplementation.isIplDcs210IdentityReportMessage(m));
        }
        mfg = 0;
        msg[4] = mfg;
        msg[6] = 24;
        slaveMfg = 2;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query, mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report, mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertEquals("IplIdentity device "+dev+" check is UR92 device, mfg=0, slave=24, slaveMfg=2", dev == 92,
                    LnIPLImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402 device, mfg=0, slave=24, slaveMfg=2", dev == 42,
                    LnIPLImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4 device, mfg=0, slave=24, slaveMfg=2", dev == 4,
                    LnIPLImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS51 device, mfg=0, slave=24, slaveMfg=2", dev == 51,
                    LnIPLImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS52 device, mfg=0, slave=24, slaveMfg=2", dev == 52,
                    LnIPLImplementation.isIplDcs52IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR3 device, mfg=0, slave=24, slaveMfg=2", dev == 35,
                    LnIPLImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DT402D device, mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is UT4D device, mfg=0, slave=24, slaveMfg=2",
                    LnIPLImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR4 device, mfg=0, slave=24, slaveMfg=2", dev == 36,
                    LnIPLImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is BXP88 device, mfg=0, slave=24, slaveMfg=2", dev == 88,
                    LnIPLImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is LNWI device, mfg=0, slave=24, slaveMfg=2", dev == 99,
                    LnIPLImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS240 device, mfg=0, slave=24, slaveMfg=2", dev == 28,
                    LnIPLImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS210 device, mfg=0, slave=24, slaveMfg=2", dev == 27,
                    LnIPLImplementation.isIplDcs210IdentityReportMessage(m));
            }

        m = new LocoNetMessage(2);
        m.setElement(0, 0x81); m.setElement(1, 0);
        Assert.assertNull("GPON message does not have extractable IPL Host or Device",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));

        mfg = 0;
        msg[4] = mfg;
        slave = 0;
        msg[6] = slave;
        slaveMfg = 0;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax UT4(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DB210Opto",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DB210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DB220",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS240",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   Assert.assertEquals("extracting slave device from DCS240 IPL report", 
                           "Digitrax (unknown Slave Device)",
                           LnIPLImplementation.extractInterpretedIplSlaveDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax PR3",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax PR4",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DT402(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DT500(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS51",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 52:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS52",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax BXPA1",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax BXP88",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax UR92",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax LNWI",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev, "Unknown Host Manufacturer/Device",
                            LnIPLImplementation.extractInterpretedIplHostDevice(m));
                    break;
            }
        }

        slave = 24;
        msg[6] = slave;
        slaveMfg = 0;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax UT4D",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   Assert.assertEquals("extracting slave device from DCS240 IPL report", 
                           "Digitrax RF24",
                           LnIPLImplementation.extractInterpretedIplSlaveDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DB210Opto",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DB210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DB220",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS240",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax PR3",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax PR4",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DT402D",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DT500D",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS51",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 52:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS52",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax BXPA1",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax BXP88",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax UR92",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax LNWI",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24", "Unknown Host Manufacturer/Device",
                            LnIPLImplementation.extractInterpretedIplHostDevice(m));
                    break;
            }
        }

        slave = 24;
        msg[6] = slave;
        slaveMfg = 5;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UT4(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210Opto",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB220",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS240",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR3",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR4",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT402(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT500(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS51",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 52:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS52",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXPA1",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXP88",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UR92",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax LNWI",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Unknown Host Manufacturer/Device",
                            LnIPLImplementation.extractInterpretedIplHostDevice(m));
                    break;
            }
        }

        slave = 25;
        msg[6] = slave;
        slaveMfg = 0;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UT4(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210Opto",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB220",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS210",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS240",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR3",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR4",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT402(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT500(x)",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS51",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 52:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS52",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXPA1",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXP88",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UR92",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax LNWI",
                           LnIPLImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Unknown Host Manufacturer/Device",
                            LnIPLImplementation.extractInterpretedIplHostDevice(m));
                    break;
            }
        }

    }

    @Test
    public void decodingTest() {
        int original[] = {0xE5, 0x14, 0x0F, 0x10, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 0, 0, 0};
        LocoNetMessage msg = new LocoNetMessage(original);
        Assert.assertFalse("Decoding Test 1 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertTrue("Decoding Test 1 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(0, 0xE4);
        Assert.assertFalse("Decoding Test 2 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 2 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(0, 0xE6);
        Assert.assertFalse("Decoding Test 3 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 3 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(0, 0x64);
        Assert.assertFalse("Decoding Test 4 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 4 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(0, 0x65);
        Assert.assertFalse("Decoding Test 5 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 5 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));

        msg = new LocoNetMessage(original);
        msg.setElement(3, 8);
        Assert.assertTrue("Decoding Test 6 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse ("Decoding Test 6 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0x18);
        Assert.assertFalse("Decoding Test 7 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 7 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0x28);
        Assert.assertFalse("Decoding Test 8 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 8 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0x07);
        Assert.assertFalse("Decoding Test 9 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 9 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0x09);
        Assert.assertFalse("Decoding Test 10 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 10 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));

        msg = new LocoNetMessage(original);
        msg.setElement(2, 0xF);
        Assert.assertFalse("Decoding Test 11 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertTrue ("Decoding Test 11 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0xE);
        Assert.assertFalse("Decoding Test 12 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 12 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0xd);
        Assert.assertFalse("Decoding Test 13 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 13 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0xC);
        Assert.assertFalse("Decoding Test 14 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 14 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0x07);
        Assert.assertFalse("Decoding Test 15 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 15 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));

        msg = new LocoNetMessage(original);
        msg.setElement(2, 0xF);
        msg.setElement(3, 8);
        Assert.assertTrue ("Decoding Test 16 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 16 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0xE);
        Assert.assertFalse("Decoding Test 17 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 17 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0xd);
        Assert.assertFalse("Decoding Test 18 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 18 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0xC);
        Assert.assertFalse("Decoding Test 19 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 19 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(3, 0x07);
        Assert.assertFalse("Decoding Test 20 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 20 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));

        msg = new LocoNetMessage(original);
        msg.setElement(1, 0x10);
        Assert.assertFalse("Decoding Test 21 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 21 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(1, 0x15);
        Assert.assertFalse("Decoding Test 22 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 22 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));

        msg = new LocoNetMessage(original);
        msg.setElement(2, 0x10);
        Assert.assertFalse("Decoding Test 23 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 23 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));
        msg.setElement(1, 0x0E);
        Assert.assertFalse("Decoding Test 24 - isIplIdentityQuery",
                LnIPLImplementation.isIplIdentityQueryMessage(msg));
        Assert.assertFalse("Decoding Test 22 - isIplIdentityReport",
                LnIPLImplementation.isIplIdentityReportMessage(msg));

    }
    
    @Test
    public void checkInterpretHostManufacturerDevice() {
        for (int dev = 0; dev < 128; ++dev) {
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 4:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax UT4(x)",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 20:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DB210Opto",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 21:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DB210",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 22:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DB220",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 27:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DCS210",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 28:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DCS240",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 35:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax PR3",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 36:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax PR4",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 42:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DT402(x)",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 50:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DT500(x)",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 51:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DCS51",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 52:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax DCS52",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 81:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax BXPA1",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 88:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax BXP88",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 92:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax UR92",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                case 99:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                           "Digitrax LNWI",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                   break;
                default:
                   Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                            "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev));
                    break;
            }
        }

        for (int dev = 0; dev < 128; ++dev) {
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 4:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax UT4(x)",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 20:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DB210Opto",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 21:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DB210",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 22:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DB220",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 27:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DCS210",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 28:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DCS240",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 35:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax PR3",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 36:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax PR4",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 42:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DT402(x)",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 50:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DT500(x)",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 51:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DCS51",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 52:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax DCS52",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 81:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax BXPA1",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 88:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax BXP88",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 92:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax UR92",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                case 99:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                           "Digitrax LNWI",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                   break;
                default:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                            "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,0));
                    break;
            }
        }

        for (int dev = 0; dev < 128; ++dev) {
            switch (dev) {
                case 1:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Unknown Host Manufacturer/Device",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 4:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax UT4D",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 20:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DB210Opto",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 21:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DB210",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 22:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DB220",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 27:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DCS210",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 28:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DCS240",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 35:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax PR3",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 36:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax PR4",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 42:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DT402D",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 50:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DT500D",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 51:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DCS51",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 52:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax DCS52",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 81:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax BXPA1",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 88:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax BXP88",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 92:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax UR92",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                case 99:
                   Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,24",
                           "Digitrax LNWI",
                           LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                   break;
                default:
                    Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,"+dev+",0,0",
                            "Unknown Host Manufacturer/Device",
                            LnIPLImplementation.interpretHostManufacturerDevice(0,dev,0,24));
                    break;
            }
        }

        Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,99,128,24",
                "Digitrax LNWI",
                LnIPLImplementation.interpretHostManufacturerDevice(0,99,128,24));

        Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 0,50,0,152",
                "Digitrax DT500D",
                LnIPLImplementation.interpretHostManufacturerDevice(0,50,0,152));

        Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: 128,50,0,24",
                "Digitrax DT500D",
                LnIPLImplementation.interpretHostManufacturerDevice(128,50,0,24));

        for (int mfg = 1; mfg < 128; ++mfg) {
            for (int dev = 0; dev < 128; ++dev) {
                if (mfg == 87) {
                    switch (dev) {
                        case 11:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits TC-64",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 12:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits LNCP",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 21:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits SignalMan",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 22:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits TowerMan",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 23:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits WatchMan",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 24:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits TC-64 Mk-II",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 25:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits MotorMan",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        case 28:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits MotorMan-II",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                        default:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "RR-CirKits (unknown device)",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                    }
                } else {
                    switch (dev) {
                        default:
                           Assert.assertEquals ("Mfg/HostDev/SlvMfg/SlvDev: "+mfg+","+dev+",0,0",
                                    "Unknown Host Manufacturer/Device",
                                   LnIPLImplementation.interpretHostManufacturerDevice(mfg,dev,0,0));
                            break;
                    }
                }
            }
        }
    }

    @Test
    public void checkInterpretSlaveManufacturerDevice() {
        String result;
        for (int dev = 0; dev < 256; ++dev) {
            for (int mfg = 0; mfg < 256; ++mfg) {
                result = LnIPLImplementation.interpretSlaveManufacturerDevice(mfg, dev);
                switch (mfg) {
                    case 0:
                    case 128:
                        switch (dev) {
                            case 24:
                            case 24+128:
                                Assert.assertEquals ("Mfg/HostDev: 0,"+dev,
                                        "Digitrax RF24", result);
                                break;
                            default:
                                Assert.assertEquals("Slave/SlaveMfg: "+dev+"/"+mfg,
                                        "Digitrax (unknown Slave Device)", result);
                                break;
                        }
                        break;
                    case 87:
                    case 87+128:
                        Assert.assertEquals("Slave/SlaveMfg: "+dev+"/"+mfg,
                                "RR-CirKits (unknown Slave Device)", result);
                        break;
                    default:
                        Assert.assertEquals("Slave/SlaveMfg: "+dev+"/"+mfg,
                                "Unknown Slave Manufacturer/Device", result);
                        break;
                }
            }
        }
    }

    @Test
    public void testExtractIplReportInfo() {
        int original[] = {0xE5, 0x14, 0x0F, 0x10, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 0, 0, 0};
        LocoNetMessage msg = new LocoNetMessage(original);

        for (int mfg = 0; mfg < 256; ++ mfg) {
            msg.setElement(4, mfg&0x7f);
            Assert.assertEquals("extractIplMfg "+msg, (mfg&0x7f),
                    (int)LnIPLImplementation.extractIplIdentityHostManufacturer(msg));
        }

        msg = new LocoNetMessage(original);

        for (int dev = 0; dev < 256; ++ dev) {
            msg.setElement(5, dev&0x7f);
            Assert.assertEquals("extractIplDev "+dev, dev&0x7f,
                    (int)LnIPLImplementation.extractIplIdentityHostDevice(msg));
        }

        msg = new LocoNetMessage(original);

        for (int slvMfg = 0; slvMfg < 256; ++ slvMfg) {
            msg.setElement(7, slvMfg&0x7f);
            Assert.assertEquals("extractIplDev "+slvMfg, slvMfg&0x7f,
                    (int)LnIPLImplementation.extractIplIdentitySlaveManufacturer(msg));
        }

        msg = new LocoNetMessage(original);
        for (int slvDev = 0; slvDev < 256; ++ slvDev) {
            msg.setElement(6, slvDev&0x7f);
            Assert.assertEquals("extractIplDev "+slvDev, slvDev&0x7f,
                    (int)LnIPLImplementation.extractIplIdentitySlaveDevice(msg));
        }

        msg = new LocoNetMessage(original);
        for (int fwRev = 0; fwRev < 256; ++ fwRev) {
            msg.setElement(8, fwRev&0x7f);
            Assert.assertEquals("extractIplFwRevString "+Integer.toString(fwRev),
                    Integer.toString((fwRev&0x78)>>3)+"."+Integer.toString(fwRev & 0x7),
                    LnIPLImplementation.extractIplIdentityHostFrimwareRev(msg));
            Assert.assertEquals("extractIplFwRevNum "+Integer.toString(fwRev),
                    fwRev&0x7f, (int)LnIPLImplementation.extractIplIdentityHostFrimwareRevNum(msg));
        }

        msg = new LocoNetMessage(original);
        for (int swRev = 0; swRev < 256; ++ swRev) {
            msg.setElement(9, ((swRev & 0x80) == 0x80) ? 1:0);
            msg.setElement(10, swRev & 0x7f);
            Assert.assertEquals("extractIplSlaveRevNum "+Integer.toString(swRev),
                    swRev, (int)LnIPLImplementation.extractIplIdentitySlaveFrimwareRevNum(msg));
            Assert.assertEquals("extractIplSlaveFwRevString "+Integer.toString(swRev),
                    Integer.toString(swRev>>3)+"."+Integer.toString(swRev & 0x7),
                    LnIPLImplementation.extractIplIdentitySlaveFrimwareRev(msg));
        }

        original[11] = 0;
        for (int hostSn = 0; hostSn < 24; ++ hostSn) {
            msg = new LocoNetMessage(original);
            if (hostSn <8) {
                if (hostSn == 7) {
                    msg.setElement(9, msg.getElement(9) | 0x2);
                } else {
                    msg.setElement(11, 1 << hostSn);
                }
            } else if (hostSn < 16) {
                if (hostSn == 15) {
                    msg.setElement(9, msg.getElement(9) | 0x4);
                } else {
                    msg.setElement(12, 1 << (hostSn - 8));
                }
            } else {
                if (hostSn == 23) {
                    msg.setElement(9, msg.getElement(9) | 0x8);
                } else {
                    msg.setElement(13, 1 << (hostSn - 16));
                }
            }
            long equivSn = 1L << hostSn;
            Assert.assertEquals("extractIplHostSerialNum for bit "+Integer.toString(hostSn),
                    equivSn,
                    (long)LnIPLImplementation.extractIplIdentityHostSerialNumber(msg));
        }

        original[11] = 0;
        for (int swSn = 0; swSn < 32; ++ swSn) {
            msg = new LocoNetMessage(original);
            if (swSn <8) {
                if (swSn == 7) {
                    msg.setElement(14, msg.getElement(14) | 0x1);
                } else {
                    msg.setElement(15, 1 << swSn);
                }
            } else if (swSn < 16) {
                if (swSn == 15) {
                    msg.setElement(14, msg.getElement(14) | 0x2);
                } else {
                    msg.setElement(16, 1 << (swSn - 8));
                }
            } else if (swSn < 24) {
                if (swSn == 23) {
                    msg.setElement(14, msg.getElement(14) | 0x4);
                } else {
                    msg.setElement(17, 1 << (swSn - 16));
                }
            } else {
                if (swSn == 31) {
                    msg.setElement(14, msg.getElement(14) | 0x8);
                } else {
                    msg.setElement(18, 1 << (swSn - 24));
                }
            }
            long equivSn = 1L << swSn;
            Assert.assertEquals("extractIplSlaveSerialNum for bit "+Integer.toString(swSn),
                    equivSn,
                    (long)LnIPLImplementation.extractIplIdentitySlaveSerialNumber(msg));
        }
    }

    @Test
    public void testConnectMethod() {
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if ((e.getPropertyName().equals("IplDeviceTypeQuery"))) {
                    propChangeQueryFlag = true;
                } else if (e.getPropertyName().equals("IplDeviceTypeReport")) {
                    propChangeReportFlag = true;
                }
                propChangeFlag = true;
            }
        };
        iplImplementation.addPropertyChangeListener(l);
        LocoNetMessage m2;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;
        int[] v2 = {0xE5, 0x14, 0x0F, 0x10, 0,
            0, 0, 0, 0, 0,
            0, 1, 0,0,0,
            0,0,0,0,0};
        m2 = new LocoNetMessage(v2);
        Assert.assertFalse("did not see property change flag yet", propChangeFlag);
        iplImplementation.message(m2);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertTrue("saw first property change IPL Report", propChangeReportFlag);
        Assert.assertFalse("didn't see first property change IPL Query", propChangeQueryFlag);
        propChangeFlag = false;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;

        v2[3] = 0x08;
        m2 = new LocoNetMessage(v2);
        Assert.assertFalse("did not see property change flag yet", propChangeFlag);
        iplImplementation.message(m2);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertFalse("didn't see second property change IPL Report", propChangeReportFlag);
        Assert.assertTrue("saw second property change IPL Query", propChangeQueryFlag);
        propChangeFlag = false;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;

        v2[0] = 0xE8;
        m2 = new LocoNetMessage(v2);
        Assert.assertFalse("did not see property change flag yet", propChangeFlag);
        iplImplementation.message(m2);

        Assume.assumeTrue("reply not received",
                jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;}));
        Assert.assertFalse("didn't see third property change IPL Report", propChangeReportFlag);
        Assert.assertFalse("didn't see third property change IPL Query", propChangeQueryFlag);
        propChangeFlag = false;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;
        
        m = new LocoNetMessage(2);
        m.setElement(0, 0x81); m.setElement(1, 0);
        iplImplementation.message(m);
        Assume.assumeTrue("reply2 not received",
                jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;}));
        Assert.assertFalse("didn't see fourth property change IPL Report", propChangeReportFlag);
        Assert.assertFalse("didn't see fourth property change IPL Query", propChangeQueryFlag);
        propChangeFlag = false;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;
        Assert.assertNull("extracting slave device from GPON message", LnIPLImplementation.extractInterpretedIplSlaveDevice(m));

        iplImplementation.removePropertyChangeListener(l);
    }

    boolean propChangeQueryFlag;
    boolean propChangeReportFlag;
    boolean propChangeFlag;

    @Test
    public void checkDispose() {
        propChangeFlag = false;
        Assert.assertFalse("did not see property change flag yet", propChangeFlag);
        iplImplementation.sendIplQueryAllDevices();
        m = new LocoNetMessage(new int[] {0xe5, 0x14, 0x0f, 0x08, 0,
                0,0,0,0,0,
                1,0,0,0,0,
                0,0,0,0,0});
        memo.getLnTrafficController().notify(m);
        Assume.assumeTrue("reply received",
                jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;}));
        Assert.assertTrue("saw property change flag yet", propChangeFlag);
        propChangeFlag = false;
        Assert.assertTrue("isIplQueryTimerRunning is true", iplImplementation.isIplQueryTimerRunning());
        iplImplementation.dispose();
        Assert.assertFalse("did not see property change flag yet", propChangeFlag);
        memo.getLnTrafficController().notify(m);
        Assume.assumeTrue("reply not received",
                jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;}));
        Assert.assertFalse("did not seee property change flag after dispose", propChangeFlag);
        Assert.assertFalse("isIplQueryTimerRunning is false", iplImplementation.isIplQueryTimerRunning());
        iplImplementation = new LnIPLImplementation(memo);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);

        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);

        memo.configureManagers();
        jmri.InstanceManager.store(memo,jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        iplImplementation = new LnIPLImplementation(memo);
    }

    @After
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }
}

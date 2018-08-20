package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.loconet.LocoNetMessage;


/**
 * Test simple functioning of LnIPLImplementation
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Bob Milhaupt Copyright (C) 2018
 */
public class LnIPLImplementationTest {
    jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;
    LnIPLImplementation iplImplementation;
    LocoNetMessage m;        
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", iplImplementation);
        memo.dispose();
    }

    @Test
    public void testCreateQueryAllIplDevices() {
        m = iplImplementation.createQueryAllIplDevicesPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 0, 0, 0, 0, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Byte "+i+ " test", expect[i], m.getElement(i));
        }
    }
    
    @Test
    public void testCreateQueryHostDevices() {
        m = iplImplementation.createIplSpecificHostQueryPacket(1, 2);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 1, 2, 0, 0, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 1 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(2, 4);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 2; expect[5] = 4;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 2 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = iplImplementation.createIplSpecificHostQueryPacket(4, 8);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 4; expect[5] = 8; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 3 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(8, 16);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 8; expect[5] = 16; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 4 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(32, 64);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 32; expect[5] = 64; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 5 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(16, 32);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 16; expect[5] = 32; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 6 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(64, 1);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 64; expect[5] = 1; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 7 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(33, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 33; expect[5] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 8 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(128, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 0; expect[5] = 0; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 9 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificHostQueryPacket(0, 128);
        Assert.assertEquals("message length",20,  m.getNumDataElements());
        expect[4] = 0; expect[5] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Host test 10 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
    }

    @Test
    public void testCreateQuerySlaveDevices() {
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x01, 0x7e);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 0, 0, 0x7E, 0x01, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 1 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x02, 0x7d);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x7d; expect[7] = 0x2;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 2 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x04, 0x7b);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x7b; expect[7] = 0x04; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 3 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x08, 0x77);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x77; expect[7] = 0x08; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 4 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x10, 0x7f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x7f; expect[7] = 0x10; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 5 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x20, 0x6f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x6f; expect[7] = 0x20; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 6 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x40, 0x5f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x5f; expect[7] = 0x40; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 7 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x0, 0x3f);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0x3f; expect[7] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 8 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(128, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[6] = 0; expect[7] = 0; 
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 9 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0, 128);
        Assert.assertEquals("message length",20,  m.getNumDataElements());
        expect[6] = 0; expect[7] = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query Slave test 10 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
    }

    @Test
    public void testCreateQueryHostSlaveDevices() {
        m = iplImplementation.createIplSpecificSlaveQueryPacket(1, 2, 4, 8);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 1, 2, 8, 4, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 1 Byte "+i+ " test", expect[i], m.getElement(i));
        }

        m = iplImplementation.createIplSpecificSlaveQueryPacket(2, 4, 8, 16);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  2; expect[5] =  4; expect[6] = 16; expect[7] = 8;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 2 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(4, 8, 16, 32);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 4; expect[5] =  8; expect[6] = 32; expect[7] = 16;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 3 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(8, 16, 32, 64);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  8; expect[5] = 16; expect[6] = 64; expect[7] = 32;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 4 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(16, 32, 64, 0);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 16; expect[5] = 32; expect[6] =  0; expect[7] = 64;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 5 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(32, 64, 15, 7);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] = 32; expect[5] = 64; expect[6] =  7; expect[7] = 15;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 6 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(64, 3, 19, 41);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  64; expect[5] =  3; expect[6] = 41; expect[7] = 19;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 7 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0, 0x7f, 0x12, 0x64);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  0; expect[5] =  0x7f; expect[6] =  0x64; expect[7] =  0x12;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 8 Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplSpecificSlaveQueryPacket(0x3d, 0x4e, 0x2a, 0x0d);
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[4] =  0x3d; expect[5] =  0x4E; expect[6] =  0x0d; expect[7] =  0x2a;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query HostSlave test 9 Byte "+i+ " test", expect[i], m.getElement(i));
        }
    }       
    
    @Test
    public void checkSpecificDeviceTypeQueryMessages() {
        m = iplImplementation.createIplUr92QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        int expect[] = {0xe5, 0x14, 0x0F, 0x08, 0, 92, 0, 0, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0x00};
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type UR92 test Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplDt402QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 42;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type DT402 test Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplUt4QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 4;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type UT4 test Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplDcs51QueryPacket();
        Assert.assertEquals("message length", 20, m.getNumDataElements());
        expect[5] = 51;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals("Query IPL Device Type DCS51 test Byte "+i+ " test", expect[i], m.getElement(i));
        }
        
        m = iplImplementation.createIplPr3QueryPacket();
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
                    iplImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report", 
                    iplImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device ", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertEquals("IplIdentity device "+dev+" check is UR92 device", dev == 92, 
                    iplImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402 device", dev == 42, 
                    iplImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4 device", dev == 4, 
                    iplImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS51 device", dev == 51, 
                    iplImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR3 device", dev == 35, 
                    iplImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is DT402D device", 
                    iplImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is UT4D device", 
                    iplImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR4 device", dev == 36, 
                    iplImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is BXP88 device", dev == 88, 
                    iplImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is LNWI device", dev == 99, 
                    iplImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS240 device", dev == 28, 
                    iplImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS210 device", dev == 27, 
                    iplImplementation.isIplDcs210IdentityReportMessage(m));
        }
        mfg = 1;
        msg[4]=mfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query mfg=1", 
                    iplImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report mfg=1", 
                    iplImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=1", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=1", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=1", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertFalse("IplIdentity device "+dev+" check is UR92 device, mfg=1", 
                    iplImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DT402 device, mfg=1", 
                    iplImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is UT4 device, mfg=1", 
                    iplImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS51 device, mfg=1",
                    iplImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR3 device, mfg=1",
                    iplImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is DT402D device, mfg=1", 
                    iplImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is UT4D device, mfg=1", 
                    iplImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR4 device, mfg=1",
                    iplImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is BXP88 device, mfg=1",
                    iplImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is LNWI device, mfg=1",
                    iplImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS240 device, mfg=1",
                    iplImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS210 device, mfg=1",
                    iplImplementation.isIplDcs210IdentityReportMessage(m));
        }
        mfg = 127;
        msg[4] = mfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query mfg=127", 
                    iplImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report mfg=127", 
                    iplImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=127", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=127", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=127", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertFalse("IplIdentity device "+dev+" check is UR92 device, mfg=127", 
                    iplImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DT402 device, mfg=127", 
                    iplImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is UT4 device, mfg=127", 
                    iplImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS51 device, mfg=127",
                    iplImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR3 device, mfg=127",
                    iplImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is DT402D device, mfg=127", 
                    iplImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse ("IplIdentity device "+dev+" check is UT4D device, mfg=127", 
                    iplImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is PR4 device, mfg=127",
                    iplImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is BXP88 device, mfg=127",
                    iplImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is LNWI device, mfg=127",
                    iplImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS240 device, mfg=127",
                    iplImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DCS210 device, mfg=127",
                    iplImplementation.isIplDcs210IdentityReportMessage(m));
        }

        mfg = 0;
        msg[4] = mfg;
        msg[6] = 24;
        msg[7] = slaveMfg;
        for (int dev = 0; dev < 128; ++dev) {
            msg[5] = dev;
            m = new LocoNetMessage(msg);
            Assert.assertFalse ("IplIdentity device "+dev+" check is ID query, mfg=0, slave=24", 
                    iplImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report, mfg=0, slave=24", 
                    iplImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=0, slave=24", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=0, slave=24", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=0, slave=24", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertEquals("IplIdentity device "+dev+" check is UR92 device, mfg=0, slave=24", dev == 92, 
                    iplImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402 device, mfg=0, slave=24", dev == 42, 
                    iplImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4 device, mfg=0, slave=24", dev == 4,
                    iplImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS51 device, mfg=0, slave=24", dev == 51, 
                    iplImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR3 device, mfg=0, slave=24", dev == 35, 
                    iplImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402D device, mfg=0, slave=24", dev == 42,
                    iplImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4D device, mfg=0, slave=24", dev == 4,
                    iplImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR4 device, mfg=0, slave=24", dev == 36, 
                    iplImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is BXP88 device, mfg=0, slave=24", dev == 88, 
                    iplImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is LNWI device, mfg=0, slave=24", dev == 99, 
                    iplImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS240 device, mfg=0, slave=24", dev == 28, 
                    iplImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS210 device, mfg=0, slave=24", dev == 27, 
                    iplImplementation.isIplDcs210IdentityReportMessage(m));
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
                    iplImplementation.isIplIdentityQueryMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is ID report, mfg=0, slave=24, slaveMfg=2", 
                    iplImplementation.isIplIdentityReportMessage(m));
            Assert.assertTrue  ("IplIdentity device "+dev+" check is specific IPL host device , mfg=0, slave=24, slaveMfg=2", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for mfg+1, mfg=0, slave=24, slaveMfg=2", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg+1, dev));
            Assert.assertFalse ("IplIdentity device "+dev+" check is specific IPL host device for dev+1, mfg=0, slave=24, slaveMfg=2", 
                    iplImplementation.isIplSpecificIdentityReportMessage(m, mfg, dev+1));
            Assert.assertEquals("IplIdentity device "+dev+" check is UR92 device, mfg=0, slave=24, slaveMfg=2", dev == 92, 
                    iplImplementation.isIplUr92IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DT402 device, mfg=0, slave=24, slaveMfg=2", dev == 42, 
                    iplImplementation.isIplDt402IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is UT4 device, mfg=0, slave=24, slaveMfg=2", dev == 4,
                    iplImplementation.isIplUt4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS51 device, mfg=0, slave=24, slaveMfg=2", dev == 51, 
                    iplImplementation.isIplDcs51IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR3 device, mfg=0, slave=24, slaveMfg=2", dev == 35, 
                    iplImplementation.isIplPr3IdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is DT402D device, mfg=0, slave=24, slaveMfg=2",
                    iplImplementation.isIplDt402DIdentityReportMessage(m));
            Assert.assertFalse("IplIdentity device "+dev+" check is UT4D device, mfg=0, slave=24, slaveMfg=2",
                    iplImplementation.isIplUt4DIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is PR4 device, mfg=0, slave=24, slaveMfg=2", dev == 36, 
                    iplImplementation.isIplPr4IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is BXP88 device, mfg=0, slave=24, slaveMfg=2", dev == 88, 
                    iplImplementation.isIplBxp88IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is LNWI device, mfg=0, slave=24, slaveMfg=2", dev == 99, 
                    iplImplementation.isIplLnwiIdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS240 device, mfg=0, slave=24, slaveMfg=2", dev == 28, 
                    iplImplementation.isIplDcs240IdentityReportMessage(m));
            Assert.assertEquals("IplIdentity device "+dev+" check is DCS210 device, mfg=0, slave=24, slaveMfg=2", dev == 27, 
                    iplImplementation.isIplDcs210IdentityReportMessage(m));
            }

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
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax (unknown device)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax UT4(x)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DB210Opto", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DB210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DB220", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS240", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax PR3", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax PR4", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DT402(x)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DT500(x)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax DCS51", 
                           iplImplementation.extractInterpretedIplHostDevice(m)); 
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax BXPA1", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax BXP88", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax UR92", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev, "Digitrax LNWI", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev, "Digitrax (unknown device)",
                            iplImplementation.extractInterpretedIplHostDevice(m));
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
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax (unknown device)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax UT4D", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DB210Opto", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DB210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DB220", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS240", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax PR3", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax PR4", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DT402D", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DT500D", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax DCS51", 
                           iplImplementation.extractInterpretedIplHostDevice(m)); 
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax BXPA1", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax BXP88", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax UR92", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24", "Digitrax LNWI", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24", "Digitrax (unknown device)",
                            iplImplementation.extractInterpretedIplHostDevice(m));
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
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax (unknown device)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UT4(x)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210Opto", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB220", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS240", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR3", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR4", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT402(x)",
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT500(x)",
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS51", 
                           iplImplementation.extractInterpretedIplHostDevice(m)); 
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXPA1", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXP88", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UR92", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax LNWI", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax (unknown device)",
                            iplImplementation.extractInterpretedIplHostDevice(m));
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
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax (unknown device)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 4:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UT4(x)", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 20:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210Opto", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 21:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 22:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DB220", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 27:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS210", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 28:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS240", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 35:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR3", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 36:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax PR4", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 42:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT402(x)",
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 50:
                   Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DT500(x)",
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 51:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax DCS51", 
                           iplImplementation.extractInterpretedIplHostDevice(m)); 
                   break;
                case 81:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXPA1", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 88:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax BXP88", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 92:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax UR92", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                case 99:
                   Assert.assertEquals ("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax LNWI", 
                           iplImplementation.extractInterpretedIplHostDevice(m));
                   break;
                default:
                    Assert.assertEquals("Ipl Extract host name from device "+dev+", slave=24, slaveMfg=5", "Digitrax (unknown device)",
                            iplImplementation.extractInterpretedIplHostDevice(m));
                    break;
            }
        }

    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        iplImplementation = new LnIPLImplementation(memo);
    }
    
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
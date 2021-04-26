package jmri.jmrix.can.cbus;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Vector;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.junit.jupiter.api.io.TempDir;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young copyright (c) 2019, 2020
 */
public class CbusFilterTest {

    @Test
    public void testCTor() {
        CbusFilter t = new CbusFilter(null);
        Assert.assertNotNull("exists",t);
    }

    private Vector<Integer> _increments;
    private Vector<Integer> _nodes;
    
    public class FtTestFilterFrame extends CbusFilterFrame {
        
        public FtTestFilterFrame() {
        super(null,null);
        }
        
        @Override
        public void passIncrement(int id){ 
            _increments.addElement(id);
        }
        
        @Override
        public void addNode(int nodenum, int position) {
            // log.warn("new node {} position {}",nodenum,position);
            Assert.assertFalse("Node already in list",_nodes.contains(position));
            _nodes.addElement(nodenum);
        }
    }

    @Test
    public void testincrementCount() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FtTestFilterFrame tff = new FtTestFilterFrame();
        CbusFilter t = new CbusFilter(tff);
        Assert.assertNotNull("exists",t);
        Assert.assertNotNull("exists",tff);
        
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_ACON, 0x00, 0x05, 0x00, 0x01},0x12 );
        CanReply r   = new CanReply(   new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x05},0x12 );
        t.filter(m);
        t.filter(r);
        
        // 1, 41, 42, 3, 6, 7, 10, 40, 43, 44, 45, 
        // 0, 41, 42, 4, 5, 7, 10, 40, 43, 44, 46        
        Vector<Integer> expected = new Vector<Integer>(22);
        expected.add(CbusFilterType.CFOUT.ordinal());
        expected.add(CbusFilterType.CFEVENT.ordinal());
        expected.add(CbusFilterType.CFEVENTMIN.ordinal());
        expected.add(CbusFilterType.CFEVENTMAX.ordinal());
        expected.add(CbusFilterType.CFON.ordinal());
        expected.add(CbusFilterType.CFLONG.ordinal());
        expected.add(CbusFilterType.CFSTD.ordinal());
        expected.add(CbusFilterType.CFED0.ordinal());
        expected.add(CbusFilterType.CFNODE.ordinal());
        expected.add(CbusFilter.CFMAXCATS);
        expected.add(CbusFilterType.CFNODEMIN.ordinal());
        expected.add(CbusFilterType.CFNODEMAX.ordinal());
        
        expected.add(CbusFilterType.CFIN.ordinal());
        expected.add(CbusFilterType.CFEVENT.ordinal());
        expected.add(CbusFilterType.CFEVENTMIN.ordinal());
        expected.add(CbusFilterType.CFEVENTMAX.ordinal());
        expected.add(CbusFilterType.CFOF.ordinal());
        expected.add(CbusFilterType.CFSHORT.ordinal());
        expected.add(CbusFilterType.CFSTD.ordinal());
        expected.add(CbusFilterType.CFED0.ordinal());
        expected.add(CbusFilterType.CFNODE.ordinal());
        expected.add(CbusFilter.CFMAXCATS+1);        
        expected.add(CbusFilterType.CFNODEMIN.ordinal());
        expected.add(CbusFilterType.CFNODEMAX.ordinal());
        
        
        Vector<Integer> expectedNodes = new Vector<Integer>(2);
        expectedNodes.add(5);
        expectedNodes.add(0);
        
        // log.warn(" output {}",_increments);
        
       //  _increments.forEach((n) -> log.warn("found {}",convertIntToFilterName(n))); 
        
        // + node
        
        // log.warn(" expctd {}",expected);
        
        JUnitUtil.waitFor(()->{ return(_increments.size()>23); }, "Not all increments passed" + _increments.size());
        // log.warn(" output {}",_increments);
        // log.warn(" node output {}",_nodes);
        Assert.assertTrue("reply increments in",_increments.contains(CbusFilterType.CFIN.ordinal()));
        Assert.assertTrue("message increments out",_increments.contains(CbusFilterType.CFOUT.ordinal()));
        Assert.assertEquals("increment filter id values match nodes", expectedNodes, _nodes);
        Assert.assertEquals("increment filter id values match ",expected , _increments);
        
        t = null;
        tff.dispose();
        tff = null;
    }
    
    // for use in any future debug
    protected String convertIntToFilterName(int number){
        if (number<47) {
            return CbusFilterType.values()[number].toString();
        }
        else {
            return String.valueOf(number);
        }
    }


    // -1 pass the test unfiltered
    // 0 onwards is the CbusFilter static int which has stopped the CanFrame
    // test the in / out filter, remaining tests can just use messages
    @Test
    public void testMainFilterMessageAndReply() {
        CbusFilter t = new CbusFilter(null);
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01},0x12 );
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01},0x12 );
        Assert.assertTrue("message 134",t.filter(m)<0);
        Assert.assertTrue("message 135",t.filter(r)<0);
        t.setFilter(CbusFilterType.CFIN.ordinal(),true);
        Assert.assertTrue("message 136",t.filter(m)<0);
        Assert.assertTrue("message 137",t.filter(r)==CbusFilterType.CFIN.ordinal());
        t.setFilter(CbusFilterType.CFOUT.ordinal(),true);
        Assert.assertTrue("message 138",t.filter(m)==CbusFilterType.CFOUT.ordinal());
        Assert.assertTrue("message 139",t.filter(r)==CbusFilterType.CFIN.ordinal());
        t = null;
        m = null;
        r = null;
    }
    
    // test opc filters
    @Test
    public void testMainFilterOpcs() {
        
        
        
        CbusFilter t = new CbusFilter(null);
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_ACK},0x12 );
        Assert.assertTrue("message 150",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFMISC.ordinal(),true);
        Assert.assertTrue("message 151",t.filter(m)==CbusFilterType.CFMISC.ordinal());
        t.setFilter(CbusFilterType.CFMISC.ordinal(),false);
        t.setFilter(CbusFilterType.CFNETWK.ordinal(),true);
        Assert.assertTrue("message 152",t.filter(m)==CbusFilterType.CFNETWK.ordinal());
        t.setFilter(CbusFilterType.CFNETWK.ordinal(),false);
        
        m.setElement(0, CbusConstants.CBUS_TON);
        Assert.assertTrue("message 153",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 154",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);
        t.setFilter(CbusFilterType.CFCSLC.ordinal(),true);
        Assert.assertTrue("message 155",t.filter(m)==CbusFilterType.CFCSLC.ordinal());
        t.setFilter(CbusFilterType.CFCSLC.ordinal(),false);
        
        m.setElement(0, CbusConstants.CBUS_RSTAT);
        Assert.assertTrue("message 167",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 169",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);
        t.setFilter(CbusFilterType.CFCSC.ordinal(),true);
        Assert.assertTrue("message 172",t.filter(m)==CbusFilterType.CFCSC.ordinal());
        t.setFilter(CbusFilterType.CFCSC.ordinal(),false);
        
        m.setElement(0, CbusConstants.CBUS_STAT);
        Assert.assertTrue("message 176",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 178",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);
        t.setFilter(CbusFilterType.CFCSC.ordinal(),true);
        Assert.assertTrue("message 181",t.filter(m)==CbusFilterType.CFCSC.ordinal());
        t.setFilter(CbusFilterType.CFCSC.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_RQMN);
        Assert.assertTrue("message 185",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),true);
        Assert.assertTrue("message 187",t.filter(m)==CbusFilterType.CFNDCONFIG.ordinal());
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),false);        
        t.setFilter(CbusFilterType.CFNDSETUP.ordinal(),true);
        Assert.assertTrue("message 190",t.filter(m)==CbusFilterType.CFNDSETUP.ordinal());
        t.setFilter(CbusFilterType.CFNDSETUP.ordinal(),false);          
        
        m.setElement(0, CbusConstants.CBUS_WRACK);
        Assert.assertTrue("message 194",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),true);
        Assert.assertTrue("message 196",t.filter(m)==CbusFilterType.CFNDCONFIG.ordinal());
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),false);        
        t.setFilter(CbusFilterType.CFNDSETUP.ordinal(),true);
        Assert.assertTrue("message 199",t.filter(m)==CbusFilterType.CFNDSETUP.ordinal());
        t.setFilter(CbusFilterType.CFNDSETUP.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_QNN);
        Assert.assertTrue("message 203",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),true);
        Assert.assertTrue("message 205",t.filter(m)==CbusFilterType.CFNDCONFIG.ordinal());
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),false);        
        t.setFilter(CbusFilterType.CFNDNUM.ordinal(),true);
        Assert.assertTrue("message 208",t.filter(m)==CbusFilterType.CFNDNUM.ordinal());
        t.setFilter(CbusFilterType.CFNDNUM.ordinal(),false); 
        
        m.setElement(0, CbusConstants.CBUS_NNREL);
        Assert.assertTrue("message 212",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),true);
        Assert.assertTrue("message 214",t.filter(m)==CbusFilterType.CFNDCONFIG.ordinal());
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),false);        
        t.setFilter(CbusFilterType.CFNDNUM.ordinal(),true);
        Assert.assertTrue("message 217",t.filter(m)==CbusFilterType.CFNDNUM.ordinal());
        t.setFilter(CbusFilterType.CFNDNUM.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_QCON);
        Assert.assertTrue("message 221",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 223",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);        
        t.setFilter(CbusFilterType.CFCSAQRL.ordinal(),true);
        Assert.assertTrue("message 226",t.filter(m)==CbusFilterType.CFCSAQRL.ordinal());
        t.setFilter(CbusFilterType.CFCSAQRL.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_DKEEP);
        Assert.assertTrue("message 230",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 232",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);        
        t.setFilter(CbusFilterType.CFCSKA.ordinal(),true);
        Assert.assertTrue("message 235",t.filter(m)==CbusFilterType.CFCSKA.ordinal());
        t.setFilter(CbusFilterType.CFCSKA.ordinal(),false); 
        
        m.setElement(0, CbusConstants.CBUS_DFNOF);
        Assert.assertTrue("message 239",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 241",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);        
        t.setFilter(CbusFilterType.CFCSFUNC.ordinal(),true);
        Assert.assertTrue("message 244",t.filter(m)==CbusFilterType.CFCSFUNC.ordinal());
        t.setFilter(CbusFilterType.CFCSFUNC.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_DSPD);
        Assert.assertTrue("message 248",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 250",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);        
        t.setFilter(CbusFilterType.CFCSDSPD.ordinal(),true);
        Assert.assertTrue("message 253",t.filter(m)==CbusFilterType.CFCSDSPD.ordinal());
        t.setFilter(CbusFilterType.CFCSDSPD.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_EVULN);
        Assert.assertTrue("message 257",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),true);
        Assert.assertTrue("message 259",t.filter(m)==CbusFilterType.CFNDCONFIG.ordinal());
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),false);        
        t.setFilter(CbusFilterType.CFNDEV.ordinal(),true);
        Assert.assertTrue("message 262",t.filter(m)==CbusFilterType.CFNDEV.ordinal());
        t.setFilter(CbusFilterType.CFNDEV.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_RQDAT);
        Assert.assertTrue("message 266",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 268",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false);        
        t.setFilter(CbusFilterType.CFRQDAT.ordinal(),true);
        Assert.assertTrue("message 271",t.filter(m)==CbusFilterType.CFRQDAT.ordinal());
        t.setFilter(CbusFilterType.CFRQDAT.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_RQDDS);
        Assert.assertTrue("message 275",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 277",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false);        
        t.setFilter(CbusFilterType.CFRQDDS.ordinal(),true);
        Assert.assertTrue("message 280",t.filter(m)==CbusFilterType.CFRQDDS.ordinal());
        t.setFilter(CbusFilterType.CFRQDDS.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_NVSET);
        Assert.assertTrue("message 284",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),true);
        Assert.assertTrue("message 286",t.filter(m)==CbusFilterType.CFNDCONFIG.ordinal());
        t.setFilter(CbusFilterType.CFNDCONFIG.ordinal(),false);        
        t.setFilter(CbusFilterType.CFNDVAR.ordinal(),true);
        Assert.assertTrue("message 289",t.filter(m)==CbusFilterType.CFNDVAR.ordinal());
        t.setFilter(CbusFilterType.CFNDVAR.ordinal(),false); 

        m.setElement(0, CbusConstants.CBUS_PCVS);
        Assert.assertTrue("message 293",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFCS.ordinal(),true);
        Assert.assertTrue("message 295",t.filter(m)==CbusFilterType.CFCS.ordinal());
        t.setFilter(CbusFilterType.CFCS.ordinal(),false);        
        t.setFilter(CbusFilterType.CFCSPROG.ordinal(),true);
        Assert.assertTrue("message 298",t.filter(m)==CbusFilterType.CFCSPROG.ordinal());
        t.setFilter(CbusFilterType.CFCSPROG.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACON);
        Assert.assertTrue("message 302",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 304",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 307",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 310",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED0.ordinal(),true);
        Assert.assertTrue("message 313",t.filter(m)==CbusFilterType.CFED0.ordinal());
        t.setFilter(CbusFilterType.CFED0.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACOF);
        Assert.assertTrue("message 317",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 319",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 322",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 325",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED0.ordinal(),true);
        Assert.assertTrue("message 328",t.filter(m)==CbusFilterType.CFED0.ordinal());
        t.setFilter(CbusFilterType.CFED0.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_AREQ);
        Assert.assertTrue("message 332",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 334",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);       
        t.setFilter(CbusFilterType.CFREQUEST.ordinal(),true);
        Assert.assertTrue("message 337",t.filter(m)==CbusFilterType.CFREQUEST.ordinal());
        t.setFilter(CbusFilterType.CFREQUEST.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARON);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 342",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false); 
        Assert.assertTrue("message 344",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 346",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 349",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 352",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_AROF);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 357",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false); 
        Assert.assertTrue("message 359",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 361",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 364",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 367",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ASON);
        Assert.assertTrue("message 371",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 373",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 376",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 379",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED0.ordinal(),true);
        Assert.assertTrue("message 382",t.filter(m)==CbusFilterType.CFED0.ordinal());
        t.setFilter(CbusFilterType.CFED0.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ASOF);
        Assert.assertTrue("message 386",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 388",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 391",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 394",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED0.ordinal(),true);
        Assert.assertTrue("message 397",t.filter(m)==CbusFilterType.CFED0.ordinal());
        t.setFilter(CbusFilterType.CFED0.ordinal(),false);
        
        m.setElement(0, CbusConstants.CBUS_ASRQ);
        Assert.assertTrue("message 401",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 403",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);       
        t.setFilter(CbusFilterType.CFREQUEST.ordinal(),true);
        Assert.assertTrue("message 406",t.filter(m)==CbusFilterType.CFREQUEST.ordinal());
        t.setFilter(CbusFilterType.CFREQUEST.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARSON);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 411",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false); 
        Assert.assertTrue("message 413",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 415",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 418",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 421",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARSOF);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 426",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false); 
        Assert.assertTrue("message 428",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 430",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 433",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 436",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACON1);
        Assert.assertTrue("message 440",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 442",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 445",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 448",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 451",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACOF1);
        Assert.assertTrue("message 455",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 457",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 460",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 463",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 466",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARON1);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 471",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false); 
        Assert.assertTrue("message 473",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 475",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 478",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 481",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_AROF1);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 486",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false); 
        Assert.assertTrue("message 488",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 490",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 493",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 496",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);
        
        m.setElement(0, CbusConstants.CBUS_ASON1);
        Assert.assertTrue("message 500",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 502",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 505",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 508",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 511",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ASOF1);
        Assert.assertTrue("message 515",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 517",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 520",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 523",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 526",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);        
        
        m.setElement(0, CbusConstants.CBUS_ARSON1);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 531",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false); 
        Assert.assertTrue("message 533",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 535",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 538",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 541",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARSOF1);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 546",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false); 
        Assert.assertTrue("message 548",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),true);
        Assert.assertTrue("message 550",t.filter(m)==CbusFilterType.CFSHORT.ordinal());
        t.setFilter(CbusFilterType.CFSHORT.ordinal(),false);
        t.setFilter(CbusFilterType.CFED1.ordinal(),true);
        Assert.assertTrue("message 553",t.filter(m)==CbusFilterType.CFED1.ordinal());
        t.setFilter(CbusFilterType.CFED1.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 556",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);        

        m.setElement(0, CbusConstants.CBUS_CABDAT);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 561",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false); 
        Assert.assertTrue("message 563",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFCABDAT.ordinal(),true);
        Assert.assertTrue("message 565",t.filter(m)==CbusFilterType.CFCABDAT.ordinal());
        t.setFilter(CbusFilterType.CFCABDAT.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_FCLK);
        t.setFilter(CbusFilterType.CFMISC.ordinal(),true);
        Assert.assertTrue("message 570",t.filter(m)==CbusFilterType.CFMISC.ordinal());
        t.setFilter(CbusFilterType.CFMISC.ordinal(),false); 
        Assert.assertTrue("message 572",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFCLOCK.ordinal(),true);
        Assert.assertTrue("message 574",t.filter(m)==CbusFilterType.CFCLOCK.ordinal());
        t.setFilter(CbusFilterType.CFCLOCK.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACON2);
        Assert.assertTrue("message 578",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 580",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 583",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 586",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED2.ordinal(),true);
        Assert.assertTrue("message 589",t.filter(m)==CbusFilterType.CFED2.ordinal());
        t.setFilter(CbusFilterType.CFED2.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACOF2);
        Assert.assertTrue("message 593",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 595",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 598",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 601",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED2.ordinal(),true);
        Assert.assertTrue("message 604",t.filter(m)==CbusFilterType.CFED2.ordinal());
        t.setFilter(CbusFilterType.CFED2.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARON2);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 609",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false); 
        Assert.assertTrue("message 611",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 613",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFED2.ordinal(),true);
        Assert.assertTrue("message 616",t.filter(m)==CbusFilterType.CFED2.ordinal());
        t.setFilter(CbusFilterType.CFED2.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 619",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_AROF2);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 624",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false); 
        Assert.assertTrue("message 626",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 628",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFED2.ordinal(),true);
        Assert.assertTrue("message 631",t.filter(m)==CbusFilterType.CFED2.ordinal());
        t.setFilter(CbusFilterType.CFED2.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 634",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACON3);
        Assert.assertTrue("message 638",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 640",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 643",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 646",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED3.ordinal(),true);
        Assert.assertTrue("message 649",t.filter(m)==CbusFilterType.CFED3.ordinal());
        t.setFilter(CbusFilterType.CFED3.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACOF3);
        Assert.assertTrue("message 653",t.filter(m)<0);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 655",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 658",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFSTD.ordinal(),true);
        Assert.assertTrue("message 661",t.filter(m)==CbusFilterType.CFSTD.ordinal());
        t.setFilter(CbusFilterType.CFSTD.ordinal(),false);        
        t.setFilter(CbusFilterType.CFED3.ordinal(),true);
        Assert.assertTrue("message 664",t.filter(m)==CbusFilterType.CFED3.ordinal());
        t.setFilter(CbusFilterType.CFED3.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARON3);
        t.setFilter(CbusFilterType.CFON.ordinal(),true);
        Assert.assertTrue("message 669",t.filter(m)==CbusFilterType.CFON.ordinal());
        t.setFilter(CbusFilterType.CFON.ordinal(),false); 
        Assert.assertTrue("message 671",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 673",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFED3.ordinal(),true);
        Assert.assertTrue("message 676",t.filter(m)==CbusFilterType.CFED3.ordinal());
        t.setFilter(CbusFilterType.CFED3.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 679",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_AROF3);
        t.setFilter(CbusFilterType.CFOF.ordinal(),true);
        Assert.assertTrue("message 684",t.filter(m)==CbusFilterType.CFOF.ordinal());
        t.setFilter(CbusFilterType.CFOF.ordinal(),false); 
        Assert.assertTrue("message 686",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFLONG.ordinal(),true);
        Assert.assertTrue("message 688",t.filter(m)==CbusFilterType.CFLONG.ordinal());
        t.setFilter(CbusFilterType.CFLONG.ordinal(),false);
        t.setFilter(CbusFilterType.CFED3.ordinal(),true);
        Assert.assertTrue("message 691",t.filter(m)==CbusFilterType.CFED3.ordinal());
        t.setFilter(CbusFilterType.CFED3.ordinal(),false);
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),true);
        Assert.assertTrue("message 694",t.filter(m)==CbusFilterType.CFRESPONSE.ordinal());
        t.setFilter(CbusFilterType.CFRESPONSE.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ACDAT);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 699",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false); 
        Assert.assertTrue("message 701",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFACDAT.ordinal(),true);
        Assert.assertTrue("message 703",t.filter(m)==CbusFilterType.CFACDAT.ordinal());
        t.setFilter(CbusFilterType.CFACDAT.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_ARDAT);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 708",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false); 
        Assert.assertTrue("message 710",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFARDAT.ordinal(),true);
        Assert.assertTrue("message 712",t.filter(m)==CbusFilterType.CFARDAT.ordinal());
        t.setFilter(CbusFilterType.CFARDAT.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_DDES);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 717",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false); 
        Assert.assertTrue("message 719",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFDDES.ordinal(),true);
        Assert.assertTrue("message 721",t.filter(m)==CbusFilterType.CFDDES.ordinal());
        t.setFilter(CbusFilterType.CFDDES.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_DDRS);
        t.setFilter(CbusFilterType.CFDATA.ordinal(),true);
        Assert.assertTrue("message 726",t.filter(m)==CbusFilterType.CFDATA.ordinal());
        t.setFilter(CbusFilterType.CFDATA.ordinal(),false); 
        Assert.assertTrue("message 728",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFDDRS.ordinal(),true);
        Assert.assertTrue("message 730",t.filter(m)==CbusFilterType.CFDDRS.ordinal());
        t.setFilter(CbusFilterType.CFDDRS.ordinal(),false);

        m.setElement(0, CbusConstants.CBUS_EXTC2);
        t.setFilter(CbusFilterType.CFMISC.ordinal(),true);
        Assert.assertTrue("message 735",t.filter(m)==CbusFilterType.CFMISC.ordinal());
        t.setFilter(CbusFilterType.CFMISC.ordinal(),false); 
        Assert.assertTrue("message 737",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFOTHER.ordinal(),true);
        Assert.assertTrue("message 739",t.filter(m)==CbusFilterType.CFOTHER.ordinal());
        t.setFilter(CbusFilterType.CFOTHER.ordinal(),false);

        m.setElement(0, 0x0f); // needs to be an unused opc, 0 is used.
        t.setFilter(CbusFilterType.CFMISC.ordinal(),true);
        Assert.assertTrue("message 744",t.filter(m)==CbusFilterType.CFMISC.ordinal());
        t.setFilter(CbusFilterType.CFMISC.ordinal(),false); 
        Assert.assertTrue("message 746",t.filter(m)<0);        
        t.setFilter(CbusFilterType.CFUNKNOWN.ordinal(),true);
        Assert.assertTrue("message 748",t.filter(m)==CbusFilterType.CFUNKNOWN.ordinal());
        t.setFilter(CbusFilterType.CFUNKNOWN.ordinal(),false);
        
        m.setExtended(true);
        t.setFilter(CbusFilterType.CFEXTRTR.ordinal(),true);
        Assert.assertTrue("message 760",t.filter(m)==CbusFilterType.CFEXTRTR.ordinal());
        t.setFilter(CbusFilterType.CFEXTRTR.ordinal(),false);
        Assert.assertTrue("message 762",t.filter(m)<0); 

    }
    
    @Test
    public void testSetMinMax(){
        CbusFilter t = new CbusFilter(null);
        t.setMinMax(CbusFilterType.CFOTHER, 0);
        t.setMinMax(CbusFilterType.CFEVENTMIN, 11);
        Assert.assertTrue("event min 11",t.getEvMin()==11);
        
        t.setMinMax(CbusFilterType.CFEVENTMAX, 22);
        Assert.assertTrue("event max 22",t.getEvMax()==22);
        
        t.setMinMax(CbusFilterType.CFNODEMIN, 33);
        Assert.assertTrue("node min 33",t.getNdMin()==33);
        
        t.setMinMax(CbusFilterType.CFNODEMAX, 44);
        Assert.assertTrue("node max 44",t.getNdMax()==44);
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        _increments = new Vector<>(22);
        _nodes = new Vector<>();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        _increments = null;
        _nodes = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusFilterTest.class);

}

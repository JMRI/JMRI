package jmri.jmrix.can.cbus;

import java.awt.GraphicsEnvironment;
import java.util.Vector;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young copyright (c) 2019
 */
public class CbusFilterTest {

    @Test
    public void testCTor() {
        CbusFilter t = new CbusFilter(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testgetTtip() {
        CbusFilter t = new CbusFilter(null);
        Assert.assertNull("no tip",t.getTtip(444));
        Assert.assertNull("a category without a tip",t.getTtip(CbusFilter.CFIN));
        //    log.warn("tip: {}",t.getTtip(CbusFilter.CFIN));
        
        Assert.assertTrue("tip found ", (t.getTtip(CbusFilter.CFON).length() > 100 ) );
        
        // tip chosen at random, testing text retrieval mechanism, not the text
        Assert.assertEquals("tip found ", "<html>DDES : Device Data Event Short : " + 
            "Sent from single Node, Device addressing sends data from multiple " + 
            "attachments, eg. several RFID readers.<br></html>", t.getTtip(CbusFilter.CFDDES)
        );
        
    }

    Vector<Integer> _increments;
    Vector<Integer> _nodes;
    
    public class FtTestFilterFrame extends CbusFilterFrame {
        @Override
        public void passIncrement(int id){ 
            _increments.addElement(id);
            // log.info("passIncrement id {} vector {}",id,_increments);
        }
        
        @Override
        public void addNode(int nodenum, int position) {
            Assert.assertFalse("Node already in list",_nodes.contains(position));
            _nodes.addElement(nodenum);
            // log.info("nodenum position {} {} vector {}",nodenum,position,_nodes);
        }
    }

    @Test
    public void testincrementCount() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FtTestFilterFrame tff = new FtTestFilterFrame();
        CbusFilter t = new CbusFilter(tff);
        Assert.assertNotNull("exists",t);
        Assert.assertNotNull("exists",tff);
        
        // technically invalid cbus frames due to opc / node, 
        // these are supposed to still pass filter unchanged, we'll test proper ones later
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_ACON, 0x00, 0x00, 0x00, 0x01},0x12 );
        CanReply r   = new CanReply(   new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x05, 0x00, 0x05},0x12 );
        t.filter(m);
        t.filter(r);
        
        // 1, 41, 42, 3, 6, 7, 10, 40, 43, 44, 45, 
        // 0, 41, 42, 4, 5, 7, 10, 40, 43, 44, 46        
        Vector<Integer> expected = new Vector<Integer>(22);
        expected.add(CbusFilter.CFOUT);
        expected.add(CbusFilter.CFEVENT);
        expected.add(CbusFilter.CFEVENTMIN);
        expected.add(CbusFilter.CFEVENTMAX);
        expected.add(CbusFilter.CFON);
        expected.add(CbusFilter.CFLONG);
        expected.add(CbusFilter.CFSTD);
        expected.add(CbusFilter.CFED0);
        expected.add(CbusFilter.CFNODE);
        expected.add(CbusFilter.CFNODEMIN);
        expected.add(CbusFilter.CFNODEMAX);
        expected.add(CbusFilter.CFMAXCATS);
        
        expected.add(CbusFilter.CFIN);
        expected.add(CbusFilter.CFEVENT);
        expected.add(CbusFilter.CFEVENTMIN);
        expected.add(CbusFilter.CFEVENTMAX);
        expected.add(CbusFilter.CFOF);
        expected.add(CbusFilter.CFSHORT);
        expected.add(CbusFilter.CFSTD);
        expected.add(CbusFilter.CFED0);
        expected.add(CbusFilter.CFNODE);
        expected.add(CbusFilter.CFNODEMIN);
        expected.add(CbusFilter.CFNODEMAX);
        expected.add(CbusFilter.CFMAXCATS+1);
        
        Vector<Integer> expectedNodes = new Vector<Integer>(2);
        expectedNodes.add(0);
        expectedNodes.add(5);
        
        JUnitUtil.waitFor(()->{ return(_increments.size()>23); }, "Not all increments passed");
        // log.warn(" output {}",_increments);
        // log.warn(" node output {}",_nodes);
        Assert.assertTrue("reply increments in",_increments.contains(CbusFilter.CFIN));
        Assert.assertTrue("message increments out",_increments.contains(CbusFilter.CFOUT));
        Assert.assertEquals("increment filter id values match nodes", expectedNodes, _nodes);
        Assert.assertEquals("increment filter id values match ",expected , _increments);
        
        t = null;
        tff.dispose();
        tff = null;
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
        t.setFilter(CbusFilter.CFIN,true);
        Assert.assertTrue("message 136",t.filter(m)<0);
        Assert.assertTrue("message 137",t.filter(r)==CbusFilter.CFIN);
        t.setFilter(CbusFilter.CFOUT,true);
        Assert.assertTrue("message 138",t.filter(m)==CbusFilter.CFOUT);
        Assert.assertTrue("message 139",t.filter(r)==CbusFilter.CFIN);
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
        t.setFilter(CbusFilter.CFMISC,true);
        Assert.assertTrue("message 151",t.filter(m)==CbusFilter.CFMISC);
        t.setFilter(CbusFilter.CFMISC,false);
        t.setFilter(CbusFilter.CFNETWK,true);
        Assert.assertTrue("message 152",t.filter(m)==CbusFilter.CFNETWK);
        t.setFilter(CbusFilter.CFNETWK,false);
        
        m.setElement(0, CbusConstants.CBUS_TON);
        Assert.assertTrue("message 153",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 154",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);
        t.setFilter(CbusFilter.CFCSLC,true);
        Assert.assertTrue("message 155",t.filter(m)==CbusFilter.CFCSLC);
        t.setFilter(CbusFilter.CFCSLC,false);
        
        m.setElement(0, CbusConstants.CBUS_RSTAT);
        Assert.assertTrue("message 167",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 169",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);
        t.setFilter(CbusFilter.CFCSC,true);
        Assert.assertTrue("message 172",t.filter(m)==CbusFilter.CFCSC);
        t.setFilter(CbusFilter.CFCSC,false);
        
        m.setElement(0, CbusConstants.CBUS_STAT);
        Assert.assertTrue("message 176",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 178",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);
        t.setFilter(CbusFilter.CFCSC,true);
        Assert.assertTrue("message 181",t.filter(m)==CbusFilter.CFCSC);
        t.setFilter(CbusFilter.CFCSC,false);

        m.setElement(0, CbusConstants.CBUS_RQMN);
        Assert.assertTrue("message 185",t.filter(m)<0);
        t.setFilter(CbusFilter.CFNDCONFIG,true);
        Assert.assertTrue("message 187",t.filter(m)==CbusFilter.CFNDCONFIG);
        t.setFilter(CbusFilter.CFNDCONFIG,false);        
        t.setFilter(CbusFilter.CFNDSETUP,true);
        Assert.assertTrue("message 190",t.filter(m)==CbusFilter.CFNDSETUP);
        t.setFilter(CbusFilter.CFNDSETUP,false);          
        
        m.setElement(0, CbusConstants.CBUS_WRACK);
        Assert.assertTrue("message 194",t.filter(m)<0);
        t.setFilter(CbusFilter.CFNDCONFIG,true);
        Assert.assertTrue("message 196",t.filter(m)==CbusFilter.CFNDCONFIG);
        t.setFilter(CbusFilter.CFNDCONFIG,false);        
        t.setFilter(CbusFilter.CFNDSETUP,true);
        Assert.assertTrue("message 199",t.filter(m)==CbusFilter.CFNDSETUP);
        t.setFilter(CbusFilter.CFNDSETUP,false); 

        m.setElement(0, CbusConstants.CBUS_QNN);
        Assert.assertTrue("message 203",t.filter(m)<0);
        t.setFilter(CbusFilter.CFNDCONFIG,true);
        Assert.assertTrue("message 205",t.filter(m)==CbusFilter.CFNDCONFIG);
        t.setFilter(CbusFilter.CFNDCONFIG,false);        
        t.setFilter(CbusFilter.CFNDNUM,true);
        Assert.assertTrue("message 208",t.filter(m)==CbusFilter.CFNDNUM);
        t.setFilter(CbusFilter.CFNDNUM,false); 
        
        m.setElement(0, CbusConstants.CBUS_NNREL);
        Assert.assertTrue("message 212",t.filter(m)<0);
        t.setFilter(CbusFilter.CFNDCONFIG,true);
        Assert.assertTrue("message 214",t.filter(m)==CbusFilter.CFNDCONFIG);
        t.setFilter(CbusFilter.CFNDCONFIG,false);        
        t.setFilter(CbusFilter.CFNDNUM,true);
        Assert.assertTrue("message 217",t.filter(m)==CbusFilter.CFNDNUM);
        t.setFilter(CbusFilter.CFNDNUM,false); 

        m.setElement(0, CbusConstants.CBUS_QCON);
        Assert.assertTrue("message 221",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 223",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);        
        t.setFilter(CbusFilter.CFCSAQRL,true);
        Assert.assertTrue("message 226",t.filter(m)==CbusFilter.CFCSAQRL);
        t.setFilter(CbusFilter.CFCSAQRL,false); 

        m.setElement(0, CbusConstants.CBUS_DKEEP);
        Assert.assertTrue("message 230",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 232",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);        
        t.setFilter(CbusFilter.CFCSKA,true);
        Assert.assertTrue("message 235",t.filter(m)==CbusFilter.CFCSKA);
        t.setFilter(CbusFilter.CFCSKA,false); 
        
        m.setElement(0, CbusConstants.CBUS_DFNOF);
        Assert.assertTrue("message 239",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 241",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);        
        t.setFilter(CbusFilter.CFCSFUNC,true);
        Assert.assertTrue("message 244",t.filter(m)==CbusFilter.CFCSFUNC);
        t.setFilter(CbusFilter.CFCSFUNC,false); 

        m.setElement(0, CbusConstants.CBUS_DSPD);
        Assert.assertTrue("message 248",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 250",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);        
        t.setFilter(CbusFilter.CFCSDSPD,true);
        Assert.assertTrue("message 253",t.filter(m)==CbusFilter.CFCSDSPD);
        t.setFilter(CbusFilter.CFCSDSPD,false); 

        m.setElement(0, CbusConstants.CBUS_EVULN);
        Assert.assertTrue("message 257",t.filter(m)<0);
        t.setFilter(CbusFilter.CFNDCONFIG,true);
        Assert.assertTrue("message 259",t.filter(m)==CbusFilter.CFNDCONFIG);
        t.setFilter(CbusFilter.CFNDCONFIG,false);        
        t.setFilter(CbusFilter.CFNDEV,true);
        Assert.assertTrue("message 262",t.filter(m)==CbusFilter.CFNDEV);
        t.setFilter(CbusFilter.CFNDEV,false); 

        m.setElement(0, CbusConstants.CBUS_RQDAT);
        Assert.assertTrue("message 266",t.filter(m)<0);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 268",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false);        
        t.setFilter(CbusFilter.CFRQDAT,true);
        Assert.assertTrue("message 271",t.filter(m)==CbusFilter.CFRQDAT);
        t.setFilter(CbusFilter.CFRQDAT,false); 

        m.setElement(0, CbusConstants.CBUS_RQDDS);
        Assert.assertTrue("message 275",t.filter(m)<0);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 277",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false);        
        t.setFilter(CbusFilter.CFRQDDS,true);
        Assert.assertTrue("message 280",t.filter(m)==CbusFilter.CFRQDDS);
        t.setFilter(CbusFilter.CFRQDDS,false); 

        m.setElement(0, CbusConstants.CBUS_NVSET);
        Assert.assertTrue("message 284",t.filter(m)<0);
        t.setFilter(CbusFilter.CFNDCONFIG,true);
        Assert.assertTrue("message 286",t.filter(m)==CbusFilter.CFNDCONFIG);
        t.setFilter(CbusFilter.CFNDCONFIG,false);        
        t.setFilter(CbusFilter.CFNDVAR,true);
        Assert.assertTrue("message 289",t.filter(m)==CbusFilter.CFNDVAR);
        t.setFilter(CbusFilter.CFNDVAR,false); 

        m.setElement(0, CbusConstants.CBUS_PCVS);
        Assert.assertTrue("message 293",t.filter(m)<0);
        t.setFilter(CbusFilter.CFCS,true);
        Assert.assertTrue("message 295",t.filter(m)==CbusFilter.CFCS);
        t.setFilter(CbusFilter.CFCS,false);        
        t.setFilter(CbusFilter.CFCSPROG,true);
        Assert.assertTrue("message 298",t.filter(m)==CbusFilter.CFCSPROG);
        t.setFilter(CbusFilter.CFCSPROG,false);

        m.setElement(0, CbusConstants.CBUS_ACON);
        Assert.assertTrue("message 302",t.filter(m)<0);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 304",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 307",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 310",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED0,true);
        Assert.assertTrue("message 313",t.filter(m)==CbusFilter.CFED0);
        t.setFilter(CbusFilter.CFED0,false);

        m.setElement(0, CbusConstants.CBUS_ACOF);
        Assert.assertTrue("message 317",t.filter(m)<0);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 319",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 322",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 325",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED0,true);
        Assert.assertTrue("message 328",t.filter(m)==CbusFilter.CFED0);
        t.setFilter(CbusFilter.CFED0,false);

        m.setElement(0, CbusConstants.CBUS_AREQ);
        Assert.assertTrue("message 332",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 334",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);       
        t.setFilter(CbusFilter.CFREQUEST,true);
        Assert.assertTrue("message 337",t.filter(m)==CbusFilter.CFREQUEST);
        t.setFilter(CbusFilter.CFREQUEST,false);

        m.setElement(0, CbusConstants.CBUS_ARON);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 342",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false); 
        Assert.assertTrue("message 344",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 346",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 349",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 352",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_AROF);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 357",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false); 
        Assert.assertTrue("message 359",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 361",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 364",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 367",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_ASON);
        Assert.assertTrue("message 371",t.filter(m)<0);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 373",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 376",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 379",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED0,true);
        Assert.assertTrue("message 382",t.filter(m)==CbusFilter.CFED0);
        t.setFilter(CbusFilter.CFED0,false);

        m.setElement(0, CbusConstants.CBUS_ASOF);
        Assert.assertTrue("message 386",t.filter(m)<0);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 388",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 391",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 394",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED0,true);
        Assert.assertTrue("message 397",t.filter(m)==CbusFilter.CFED0);
        t.setFilter(CbusFilter.CFED0,false);
        
        m.setElement(0, CbusConstants.CBUS_ASRQ);
        Assert.assertTrue("message 401",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 403",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);       
        t.setFilter(CbusFilter.CFREQUEST,true);
        Assert.assertTrue("message 406",t.filter(m)==CbusFilter.CFREQUEST);
        t.setFilter(CbusFilter.CFREQUEST,false);

        m.setElement(0, CbusConstants.CBUS_ARSON);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 411",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false); 
        Assert.assertTrue("message 413",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 415",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 418",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 421",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_ARSOF);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 426",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false); 
        Assert.assertTrue("message 428",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 430",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 433",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 436",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_ACON1);
        Assert.assertTrue("message 440",t.filter(m)<0);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 442",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 445",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 448",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 451",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);

        m.setElement(0, CbusConstants.CBUS_ACOF1);
        Assert.assertTrue("message 455",t.filter(m)<0);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 457",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 460",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 463",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 466",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);

        m.setElement(0, CbusConstants.CBUS_ARON1);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 471",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false); 
        Assert.assertTrue("message 473",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 475",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 478",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 481",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_AROF1);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 486",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false); 
        Assert.assertTrue("message 488",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 490",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 493",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 496",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);
        
        m.setElement(0, CbusConstants.CBUS_ASON1);
        Assert.assertTrue("message 500",t.filter(m)<0);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 502",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 505",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 508",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 511",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);

        m.setElement(0, CbusConstants.CBUS_ASOF1);
        Assert.assertTrue("message 515",t.filter(m)<0);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 517",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 520",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 523",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 526",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);        
        
        m.setElement(0, CbusConstants.CBUS_ARSON1);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 531",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false); 
        Assert.assertTrue("message 533",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 535",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 538",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 541",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_ARSOF1);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 546",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false); 
        Assert.assertTrue("message 548",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFSHORT,true);
        Assert.assertTrue("message 550",t.filter(m)==CbusFilter.CFSHORT);
        t.setFilter(CbusFilter.CFSHORT,false);
        t.setFilter(CbusFilter.CFED1,true);
        Assert.assertTrue("message 553",t.filter(m)==CbusFilter.CFED1);
        t.setFilter(CbusFilter.CFED1,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 556",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);        

        m.setElement(0, CbusConstants.CBUS_CABDAT);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 561",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false); 
        Assert.assertTrue("message 563",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFCABDAT,true);
        Assert.assertTrue("message 565",t.filter(m)==CbusFilter.CFCABDAT);
        t.setFilter(CbusFilter.CFCABDAT,false);

        m.setElement(0, CbusConstants.CBUS_FCLK);
        t.setFilter(CbusFilter.CFMISC,true);
        Assert.assertTrue("message 570",t.filter(m)==CbusFilter.CFMISC);
        t.setFilter(CbusFilter.CFMISC,false); 
        Assert.assertTrue("message 572",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFCLOCK,true);
        Assert.assertTrue("message 574",t.filter(m)==CbusFilter.CFCLOCK);
        t.setFilter(CbusFilter.CFCLOCK,false);

        m.setElement(0, CbusConstants.CBUS_ACON2);
        Assert.assertTrue("message 578",t.filter(m)<0);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 580",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 583",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 586",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED2,true);
        Assert.assertTrue("message 589",t.filter(m)==CbusFilter.CFED2);
        t.setFilter(CbusFilter.CFED2,false);

        m.setElement(0, CbusConstants.CBUS_ACOF2);
        Assert.assertTrue("message 593",t.filter(m)<0);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 595",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 598",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 601",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED2,true);
        Assert.assertTrue("message 604",t.filter(m)==CbusFilter.CFED2);
        t.setFilter(CbusFilter.CFED2,false);

        m.setElement(0, CbusConstants.CBUS_ARON2);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 609",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false); 
        Assert.assertTrue("message 611",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 613",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFED2,true);
        Assert.assertTrue("message 616",t.filter(m)==CbusFilter.CFED2);
        t.setFilter(CbusFilter.CFED2,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 619",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_AROF2);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 624",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false); 
        Assert.assertTrue("message 626",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 628",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFED2,true);
        Assert.assertTrue("message 631",t.filter(m)==CbusFilter.CFED2);
        t.setFilter(CbusFilter.CFED2,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 634",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_ACON3);
        Assert.assertTrue("message 638",t.filter(m)<0);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 640",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 643",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 646",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED3,true);
        Assert.assertTrue("message 649",t.filter(m)==CbusFilter.CFED3);
        t.setFilter(CbusFilter.CFED3,false);

        m.setElement(0, CbusConstants.CBUS_ACOF3);
        Assert.assertTrue("message 653",t.filter(m)<0);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 655",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 658",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFSTD,true);
        Assert.assertTrue("message 661",t.filter(m)==CbusFilter.CFSTD);
        t.setFilter(CbusFilter.CFSTD,false);        
        t.setFilter(CbusFilter.CFED3,true);
        Assert.assertTrue("message 664",t.filter(m)==CbusFilter.CFED3);
        t.setFilter(CbusFilter.CFED3,false);

        m.setElement(0, CbusConstants.CBUS_ARON3);
        t.setFilter(CbusFilter.CFON,true);
        Assert.assertTrue("message 669",t.filter(m)==CbusFilter.CFON);
        t.setFilter(CbusFilter.CFON,false); 
        Assert.assertTrue("message 671",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 673",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFED3,true);
        Assert.assertTrue("message 676",t.filter(m)==CbusFilter.CFED3);
        t.setFilter(CbusFilter.CFED3,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 679",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_AROF3);
        t.setFilter(CbusFilter.CFOF,true);
        Assert.assertTrue("message 684",t.filter(m)==CbusFilter.CFOF);
        t.setFilter(CbusFilter.CFOF,false); 
        Assert.assertTrue("message 686",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFLONG,true);
        Assert.assertTrue("message 688",t.filter(m)==CbusFilter.CFLONG);
        t.setFilter(CbusFilter.CFLONG,false);
        t.setFilter(CbusFilter.CFED3,true);
        Assert.assertTrue("message 691",t.filter(m)==CbusFilter.CFED3);
        t.setFilter(CbusFilter.CFED3,false);
        t.setFilter(CbusFilter.CFRESPONSE,true);
        Assert.assertTrue("message 694",t.filter(m)==CbusFilter.CFRESPONSE);
        t.setFilter(CbusFilter.CFRESPONSE,false);

        m.setElement(0, CbusConstants.CBUS_ACDAT);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 699",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false); 
        Assert.assertTrue("message 701",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFACDAT,true);
        Assert.assertTrue("message 703",t.filter(m)==CbusFilter.CFACDAT);
        t.setFilter(CbusFilter.CFACDAT,false);

        m.setElement(0, CbusConstants.CBUS_ARDAT);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 708",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false); 
        Assert.assertTrue("message 710",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFARDAT,true);
        Assert.assertTrue("message 712",t.filter(m)==CbusFilter.CFARDAT);
        t.setFilter(CbusFilter.CFARDAT,false);

        m.setElement(0, CbusConstants.CBUS_DDES);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 717",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false); 
        Assert.assertTrue("message 719",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFDDES,true);
        Assert.assertTrue("message 721",t.filter(m)==CbusFilter.CFDDES);
        t.setFilter(CbusFilter.CFDDES,false);

        m.setElement(0, CbusConstants.CBUS_DDRS);
        t.setFilter(CbusFilter.CFDATA,true);
        Assert.assertTrue("message 726",t.filter(m)==CbusFilter.CFDATA);
        t.setFilter(CbusFilter.CFDATA,false); 
        Assert.assertTrue("message 728",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFDDRS,true);
        Assert.assertTrue("message 730",t.filter(m)==CbusFilter.CFDDRS);
        t.setFilter(CbusFilter.CFDDRS,false);

        m.setElement(0, CbusConstants.CBUS_EXTC2);
        t.setFilter(CbusFilter.CFMISC,true);
        Assert.assertTrue("message 735",t.filter(m)==CbusFilter.CFMISC);
        t.setFilter(CbusFilter.CFMISC,false); 
        Assert.assertTrue("message 737",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFOTHER,true);
        Assert.assertTrue("message 739",t.filter(m)==CbusFilter.CFOTHER);
        t.setFilter(CbusFilter.CFOTHER,false);

        m.setElement(0, 170); // needs to be an unused opc, 0 is used.
        t.setFilter(CbusFilter.CFMISC,true);
        Assert.assertTrue("message 744",t.filter(m)==CbusFilter.CFMISC);
        t.setFilter(CbusFilter.CFMISC,false); 
        Assert.assertTrue("message 746",t.filter(m)<0);        
        t.setFilter(CbusFilter.CFUNKNOWN,true);
        Assert.assertTrue("message 748",t.filter(m)==CbusFilter.CFUNKNOWN);
        t.setFilter(CbusFilter.CFUNKNOWN,false);
        
        t = null;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        _increments = new Vector<Integer>(22);
        _nodes = new Vector<Integer>();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        _increments = null;
        _nodes = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusFilterTest.class);

}

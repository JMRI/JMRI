package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractXNetSerialConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new AbstractXNetSerialConnectionConfigXml(){
           @Override
           public void register(){
           }
           @Override
           public void getInstance(){
           }
        };
    }

    @After
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractXNetSerialConnectionConfigXmlTest.class);

}

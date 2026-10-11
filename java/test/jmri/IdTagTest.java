package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the IdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class IdTagTest {

    @Test
    public void testStateConstants() {

        assertTrue( (IdTag.SEEN != IdTag.UNSEEN), "Seen and Unseen differ");
        assertTrue( (IdTag.SEEN != IdTag.UNKNOWN), "Seen and Unknown differ");
        assertTrue( (IdTag.SEEN != IdTag.INCONSISTENT), "Seen and Inconsistent differ");

        assertTrue( (IdTag.UNSEEN != IdTag.UNKNOWN), "Unseen and Unknown differ");
        assertTrue( (IdTag.UNSEEN != IdTag.INCONSISTENT), "Unseen and Inconsistent differ");

        assertTrue( (IdTag.UNKNOWN != IdTag.INCONSISTENT), "Unknown and Inconsistent differ");

    }

    @Test
    public void testReportableIdTag() {
       TestIdTag t = new TestIdTag("ID1234"); 
       assertNotNull( t.toReportString(), "default toReporterStringImplementation");
       assertEquals( "ID1234",t.toReportString(), "default toReporterStringImplementation");

    }

    private static class TestIdTag extends jmri.implementation.AbstractNamedBean implements IdTag,Reportable {

       private TestIdTag(String systemName){
           super(systemName);
       }

       @Override
       public String getBeanType(){
           return "IDTAG";
       }

       @Override
       public int getState(){
          return IdTag.UNSEEN;
       }

       @Override
       public void setState(int state){
       }

       @Override
       public String getTagID(){
          return "1234";
       }

       @Override
       public void setWhereLastSeen(Reporter reporter){
       }

       @Override
       public Reporter getWhereLastSeen(){
          return null;
       }

       @Override
       public java.util.Date getWhenLastSeen(){
          return null;
       }

       @Override
       public org.jdom2.Element store(boolean storeState){
          return null;
       }
  
       @Override
       public void load(org.jdom2.Element e){
       }

       // don't override toReporterString so we can test the default
       // implementation. 

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

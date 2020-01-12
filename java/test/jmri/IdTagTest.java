package jmri;

import org.junit.*;

/**
 * Tests for the IdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class IdTagTest {

    @Test
    public void testStateConstants() {

        Assert.assertTrue("Seen and Unseen differ", (IdTag.SEEN != IdTag.UNSEEN));
        Assert.assertTrue("Seen and Unknown differ", (IdTag.SEEN != IdTag.UNKNOWN));
        Assert.assertTrue("Seen and Inconsistent differ", (IdTag.SEEN != IdTag.INCONSISTENT));

        Assert.assertTrue("Unseen and Unknown differ", (IdTag.UNSEEN != IdTag.UNKNOWN));
        Assert.assertTrue("Unseen and Inconsistent differ", (IdTag.UNSEEN != IdTag.INCONSISTENT));

        Assert.assertTrue("Unknown and Inconsistent differ", (IdTag.UNKNOWN != IdTag.INCONSISTENT));

    }

    @Test
    public void testReportableIdTag() {
       TestIdTag t = new TestIdTag("ID1234"); 
       Assert.assertNotNull("default toReporterStringImplementation",t.toReportString());
       Assert.assertEquals("default toReporterStringImplementation","ID1234",t.toReportString());

    }

    class TestIdTag extends jmri.implementation.AbstractNamedBean implements IdTag,Reportable {

       public TestIdTag(String systemName){
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

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}

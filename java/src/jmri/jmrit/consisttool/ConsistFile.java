// ConsistFile.java

package jmri.jmrit.consisttool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.XmlFile;
import jmri.DccLocoAddress;
import jmri.Consist;
import java.io.File;

import java.util.ArrayList;
import jmri.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;


/**
 * Handle saving/restoring consist information to XML files.
 * This class manipulates files conforming to the consist-roster-config DTD.
 *
 * @author      Paul Bender Copyright (C) 2008
 * @version     $Revision$
 */

public class ConsistFile extends XmlFile {

       protected jmri.ConsistManager consistMan = null;   

       public ConsistFile(){
          super();
          consistMan=jmri.InstanceManager.consistManagerInstance();
          // set the location to a subdirectory of the defined roster
          // directory
          setFileLocation(jmri.jmrit.roster.Roster.getFileLocation()+
                          "roster"+File.separator+"consist");
       }

	/**
         * Load a Consist from the consist elements in the file.
         * @param consist a JDOM element containing a consist
         */
       @SuppressWarnings("unchecked")
       private void ConsistFromXML(Element consist){
    	   org.jdom.Attribute type,cnumber,isCLong,cID;
    	   jmri.Consist newConsist = null;

    	   // Read the consist address from the file and create the 
    	   // consisit in memory if it doesn't exist already.
    	   cnumber=consist.getAttribute("consistNumber");
    	   isCLong=consist.getAttribute("longAddress");
    	   DccLocoAddress consistAddress = null;
    	   if(isCLong!=null) {
    		   if(log.isDebugEnabled()) log.debug("adding consist "+cnumber +" with longAddress set to " +isCLong.getValue());
    		   try {
    			   int number = Integer.parseInt(cnumber.getValue());
    			   consistAddress=new DccLocoAddress(number, isCLong.getValue().equals("yes"));
    		   } catch (NumberFormatException e) {
    			   if(log.isDebugEnabled())
    				   log.debug("Consist number not an integer");
    			   return;
    		   }

    	   } else {
    		   if(log.isDebugEnabled()) log.debug("adding consist "+cnumber +" with default long address setting.");
    		   consistAddress=new DccLocoAddress(
    				   Integer.parseInt(cnumber.getValue()),
    				   false);
    	   }
    	   newConsist=consistMan.getConsist(consistAddress);
    	   if(!(newConsist.getConsistList().isEmpty())) {
    		   if(log.isDebugEnabled())
    			   log.debug("Consist " + consistAddress.toString() + " is not empty.  Using version in memory.");
    		   return;
    	   }

    	   // read and set the consist type
    	   type=consist.getAttribute("type");
    	   if(type!=null) {
    		   // use the value read from the file
    		   newConsist.setConsistType((type.getValue().equals("CSAC"))?jmri.Consist.CS_CONSIST:jmri.Consist.ADVANCED_CONSIST);
    	   } else {
    		   // use the default (DAC)
    		   newConsist.setConsistType(jmri.Consist.ADVANCED_CONSIST);
    	   }

    	   // Read the consist ID from the file;
    	   cID=consist.getAttribute("id");
    	   if(cID!=null) {
    		   // use the value read from the file
    		   newConsist.setConsistID(cID.getValue());
    	   }

    	   // read each child of locomotive in the consist from the file
    	   // and restore it's information to memory.
    	   java.util.Iterator<Element> childIterator=consist.getDescendants(new org.jdom.filter.ElementFilter("loco"));
    	   try {
    		   org.jdom.Element e;
    		   do {
    			   e=childIterator.next();
    			   org.jdom.Attribute number,isLong,direction,position;
    			   number=e.getAttribute("dccLocoAddress");
    			   isLong=e.getAttribute("longAddress");
    			   direction=e.getAttribute("locoDir");
    			   position=e.getAttribute("locoName");
    			   if(log.isDebugEnabled())log.debug("adding Loco "+number);
    			   // Use restore so we DO NOT cause send any commands
    			   // to the command station as we recreate the consist.
    			   DccLocoAddress address;
    			   if(isLong!=null && direction !=null) {
    				   // use the values from the file
    				   if(log.isDebugEnabled())log.debug("using direction from file "+direction.getValue());
    				   address=new DccLocoAddress(
    						   Integer.parseInt(number.getValue()),
    						   isLong.getValue().equals("yes"));
    				   newConsist.restore(address,
    						   direction.getValue().equals("normal"));
    			   } else if(isLong==null && direction !=null) {
    				   // use the direction from the file
    				   // but set as long address
    				   if(log.isDebugEnabled())log.debug("using direction from file "+direction.getValue());
    				   address=new DccLocoAddress(
    						   Integer.parseInt(number.getValue()),
    						   true);
    				   newConsist.restore(address,
    						   direction.getValue().equals("normal"));
    			   } else if(isLong!=null && direction==null) {
    				   // use the default direction
    				   // but the long/short value from the file
    				   address=new DccLocoAddress(
    						   Integer.parseInt(number.getValue()),
    						   isLong.getValue().equals("yes"));
    				   newConsist.restore(address,true);
    			   } else { 
    				   // use the default values long address
    				   // and normal direction
    				   address=new DccLocoAddress(
    						   Integer.parseInt(number.getValue()),
    						   true);
    				   newConsist.restore(address,true);
    			   }
    			   if(position!=null && !position.getValue().equals("mid")){
    				   if(position.getValue().equals("lead")) {
    					   newConsist.setPosition(address,Consist.POSITION_LEAD);
    				   } else if(position.getValue().equals("rear")) {
    					   newConsist.setPosition(address,Consist.POSITION_TRAIL);
    				   } 
    			   } else {
    				   org.jdom.Attribute midNumber=e.getAttribute("locoMidNumber");
    				   if(midNumber!=null) {
    					   int pos=Integer.parseInt(midNumber.getValue());
    					   newConsist.setPosition(address,pos);
    				   }
    			   } 
    		   }while(true); 
    	   }catch(java.util.NoSuchElementException nse){
    		   if(log.isDebugEnabled()) log.debug("end of loco list");
    	   }
       }

	/**
         * convert a Consist to XML.
         * @param consist a jmri.Consist object to write to the file
         * @return an org.jdom.Element representing the consist.
 	 */
	private org.jdom.Element ConsistToXML(jmri.Consist consist){
              org.jdom.Element e = new org.jdom.Element("consist");
              e.setAttribute("id", consist.getConsistID());
              e.setAttribute("consistNumber",""+consist.getConsistAddress()
                                                    .getNumber());
              e.setAttribute("longAddress",consist.getConsistAddress()
                                                .isLongAddress()?"yes":"no");
              e.setAttribute("type",consist.getConsistType()==jmri.Consist.ADVANCED_CONSIST?"DAC":"CSAC");
              ArrayList<DccLocoAddress> addressList = consist.getConsistList();

	      for(int i=0;i<addressList.size();i++)
              {
                 DccLocoAddress locoaddress=addressList.get(i);
                 org.jdom.Element eng = new org.jdom.Element("loco");
                 eng.setAttribute("dccLocoAddress",""+locoaddress.getNumber());
                 eng.setAttribute("longAddress",locoaddress.isLongAddress()?"yes":"no");
                 eng.setAttribute("locoDir",consist.getLocoDirection(locoaddress)?"normal":"reverse");
                 int position=consist.getPosition(locoaddress);
                 if(position==Consist.POSITION_LEAD) {
                    eng.setAttribute("locoName","lead");
                 } else if(position==Consist.POSITION_TRAIL) {
                    eng.setAttribute("locoName","rear");
                 } else {
                    eng.setAttribute("locoName","mid");
                    eng.setAttribute("locoMidNumber",""+position);
                 }                 
                 e.addContent(eng);
              }
              return(e);
	}

        /**
         * Read all consists from the default file name
         * @throws org.jdom.JDOMException
         * @throws java.io.IOException
         */
        public void ReadFile() throws org.jdom.JDOMException, java.io.IOException {
            ReadFile(defaultConsistFilename());
        }

        /**
         * Read all consists from a file.
         * @param fileName - with location and file type
         * @throws org.jdom.JDOMException
         * @throws java.io.IOException
         */
	@SuppressWarnings("unchecked")
	public void ReadFile(String fileName) throws org.jdom.JDOMException, java.io.IOException {
           if(checkFile(fileName))
           {
	      Element root=rootFromName(fileName);
              Element roster = null;
              if(root==null) {
	  	   log.warn("consist file could not be read");
                   return;
              }
              roster=root.getChild("roster");
              if(roster==null) {
	  	   if(log.isDebugEnabled()) log.debug("consist file does not contain a roster entry");
                   return;
              }
              java.util.Iterator<Element> consistIterator=root.getDescendants(new org.jdom.filter.ElementFilter("consist"));
              try {
                 org.jdom.Element consist;
                 do {
                    consist=consistIterator.next();
                    ConsistFromXML(consist);
                 } while(consist!=null);
              } catch(java.util.NoSuchElementException nde){
                  if(log.isDebugEnabled()) log.debug("end of consist list");
              }
           } else log.info("Consist file does not exist.  One will be created if necessary.");
           
        }

        /**
         * Write all consists to the default file name
         * @param consistList
         * @throws java.io.IOException
         */
        public void WriteFile(ArrayList<jmri.DccLocoAddress> consistList) throws java.io.IOException {
            WriteFile(consistList, defaultConsistFilename());
        }

        /**
         * Write all consists to a file.
         * @param consistList an ArrayList of consists to write
         * @param fileName - with location and file type
         * @throws java.io.IOException
         */
	public void WriteFile(ArrayList<jmri.DccLocoAddress> consistList, String fileName) throws java.io.IOException {
           // create root element
           Element root = new Element("consist-roster-config");
           Document doc = newDocument(root, dtdLocation+"consist-roster-config.dtd");

           // add XSLT processing instruction
           java.util.Map<String,String> m = new java.util.HashMap<String,String>();
           m.put("type", "text/xsl");
           m.put("href", xsltLocation+"consistRoster.xsl");
           org.jdom.ProcessingInstruction p = new org.jdom.ProcessingInstruction("xml-stylesheet", m);
           doc.addContent(0,p);
           
           Element roster = new Element("roster");

           for(int i=0; i<consistList.size();i++)
           {
                jmri.Consist newConsist=consistMan.getConsist(consistList.get(i));
		roster.addContent(ConsistToXML(newConsist));
           }
           root.addContent(roster);
           try {   
           if(!checkFile(fileName))
           {
               //The file does not exist, create it before writing
               java.io.File file=new java.io.File(fileName);
               java.io.File parentDir=file.getParentFile();
               if(!parentDir.exists())
               {
                  if(!parentDir.mkdir())
                      throw(new java.io.IOException());
               }
               if(!file.createNewFile())
                  throw(new java.io.IOException());
           }
           writeXML(findFile(fileName),doc);
           } catch(java.io.IOException ioe) {
                log.error("IO Exception " +ioe);
                throw(ioe);
           } 
	}

    /**
     * Defines the preferences subdirectory in which LocoFiles are kept
     * by default.
     */
    static private String fileLocation = FileUtil.getUserFilesPath()+"roster"+File.separator+"consist";
                                         
    
    static public String getFileLocation() { return fileLocation; }
         
    static public void setFileLocation(String loc) {
        fileLocation = loc;
        if (!fileLocation.endsWith(File.separator))
	     fileLocation = fileLocation+File.separator;
    }

    /**
     * Return the filename String for the default Consist file, including 
location.
     */
    public static String defaultConsistFilename() { return getFileLocation()+"consist.xml";}

       
    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConsistFile.class.getName());

}

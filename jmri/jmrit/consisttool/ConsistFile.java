// ConsistFile.java

package jmri.jmrit.consisttool;

import jmri.jmrit.XmlFile;
import jmri.DccLocoAddress;
import jmri.ConsistManager;
import jmri.Consist;
import java.io.File;

import java.util.List;
import java.util.ArrayList;
import org.jdom.Document;
import org.jdom.Element;


/**
 * Handle saving/restoring consist information to XML files.
 * This class manipulates files conforming to the consist-roster-config DTD.
 *
 * @author      Paul Bender Copyright (C) 2008
 * @version     $Revision: 1.1 $
 */

class ConsistFile extends jmri.jmrit.XmlFile {

       private jmri.ConsistManager ConsistMan = null;   

       public ConsistFile(){
          super();
          ConsistMan=jmri.InstanceManager.consistManagerInstance();
          // set the location to a subdirectory of the defined roster
          // directory
          setFileLocation(jmri.jmrit.roster.Roster.instance().getFileLocation()+
                          "roster"+File.separator+"consist");
       }

	/**
         * Load a Consist from the consist elements in the file.
         * @param consist a JDOM element containing a consist
         */
	private void ConsistFromXML(Element consist){
                org.jdom.Element e;
                org.jdom.Attribute type,number,isLong,direction;
                jmri.Consist newConsist;

                // Read the consist address from the file and create the 
                // consisit in memory if it doesn't exist already.
                number=consist.getAttribute("consistNumber");
                isLong=consist.getAttribute("longAddress");
                DccLocoAddress consistAddress;
                if(!(isLong==null)) {
                   consistAddress=new DccLocoAddress(
                                             Integer.parseInt(number.getValue()),
                                             isLong.equals("yes"));
                } else {
                   consistAddress=new DccLocoAddress(
                                             Integer.parseInt(number.getValue()),
                                             false);
                }
                newConsist=ConsistMan.getConsist(consistAddress);
                if(newConsist.getConsistList().size()!=0) {
                     if(log.isDebugEnabled())
                            log.debug("Consist " + consistAddress.toString() + " is not empty.  Using version in memory.");
                     return;
                }

                // read and set the consist type
                type=consist.getAttribute("type");
                if(type!=null) {
                   // use the value read from the file
                   newConsist.setConsistType((type.getValue()=="CSAC")?jmri.Consist.CS_CONSIST:jmri.Consist.ADVANCED_CONSIST);
                } else {
                   // use the default (DAC)
                   newConsist.setConsistType(jmri.Consist.ADVANCED_CONSIST);
                }

                // read each child of locomotive in the consist from the file
                // and restore it's information to memory.
		while((e=consist.getChild("loco"))!=null)
		{
                        number=consist.getAttribute("dccLocoAddress");
                        isLong=consist.getAttribute("longAddress");
                        direction=consist.getAttribute("locoDir");
                        // Use restore so we DO NOT cause send any commands
                        // to the command station as we recreate the consist.
                        if(isLong!=null && direction !=null) {
                           // use the values from the file
			   newConsist.restore(new DccLocoAddress(
                                            Integer.parseInt(number.getValue()),
                                            isLong.equals("yes")),
                                            direction.equals("normal"));
                        } else if(isLong==null && direction !=null) {
                           // use the direction from the file
                           // but set as long address
			   newConsist.restore(new DccLocoAddress(
                                            Integer.parseInt(number.getValue()),
                                            true),
                                            direction.equals("normal"));
                        } else if(isLong!=null && direction ==null) {
                           // use the default direction
                           // but the long/short value from the file
			   newConsist.restore(new DccLocoAddress(
                                            Integer.parseInt(number.getValue()),
                                            isLong.equals("yes")),
                                            true);
                        } else { 
                           // use the default values long address
                           // and normal direction
			   newConsist.restore(new DccLocoAddress(
                                            Integer.parseInt(number.getValue()),
                                            true), 
                                            true);
                        }
                 } 
        }

	/**
         * convert a Consist to XML.
         * @param consist a jmri.Consist object to write to the file
         * @return an org.jdom.Element representing the consist.
 	 */
	private org.jdom.Element ConsistToXML(jmri.Consist consist){
              org.jdom.Element e = new org.jdom.Element("consist");
              e.setAttribute("id", consist.getConsistAddress().toString());
              e.setAttribute("consistNumber",""+consist.getConsistAddress()
                                                    .getNumber());
              e.setAttribute("longAddress",consist.getConsistAddress()
                                                .isLongAddress()?"yes":"no");
              e.setAttribute("type",consist.getConsistType()==jmri.Consist.ADVANCED_CONSIST?"DAC":"CSAC");
              ArrayList addressList = consist.getConsistList();

	      for(int i=0;i<addressList.size();i++)
              {
                 DccLocoAddress locoaddress=(DccLocoAddress)addressList.get(i);
                 org.jdom.Element eng = new org.jdom.Element("loco");
                 eng.setAttribute("dccLocoAddress",""+locoaddress.getNumber());
                 eng.setAttribute("longAddress",locoaddress.isLongAddress()?"yes":"no");
                 eng.setAttribute("locoDir",consist.getLocoDirection(locoaddress)?"normal":"reverse");
                 // for now, just set the first loco as lead, the last as
                 // rear and the rest as mid.
                 if(i==0)
                    eng.setAttribute("locoName","lead");
                 else if(i==addressList.size()-1)
                    eng.setAttribute("locoName","rear");
                 else {
                    eng.setAttribute("locoName","mid");
                    eng.setAttribute("locoMidNumber",""+i);
                 }                 
                 e.addContent(eng);
              }
              return(e);
	}

        /**
         * Read all consists from a file.
         * @param file a file to write to a consist
         * @throws org.jdom.JDOMException
         * @throws java.io.FileNotFoundException
         * @param consistList an ArrayList of consist IDs to write to the file
         */
	public void ReadFile() throws org.jdom.JDOMException, java.io.IOException {
           if(checkFile(defaultConsistFilename()))
           {
	      Element root=rootFromName(defaultConsistFilename());
              Element roster = null;
              Element consist = null;
              if(root==null) {
	  	   log.warn("consist file could not be read");
                   return;
              }
              roster=root.getChild("consist");
              if(roster==null) {
	  	   if(log.isDebugEnabled()) log.info("consist file does not contain a roster entry");
                   return;
              }
              while((consist=roster.getChild("consist"))!=null) {
                  ConsistFromXML(consist);
              }
           }

        }

	/**
         * Write all consists to a file.
	 * @param consistList an ArrayList of consists to write
         * @throws org.jdom.JDOMException
         * @param consistList an ArrayList of consist IDs to write to the file
 	 */
	public void WriteFile(ArrayList consistList) throws org.jdom.JDOMException, java.io.IOException {
           // create root element
           Element root = new Element("consist-roster-config");
           Document doc = newDocument(root, dtdLocation+"consist-roster-config.dtd");

           // add XSLT processing instruction
           java.util.Map m = new java.util.HashMap();
           m.put("type", "text/xsl");
           m.put("href", "http://jmri.sourceforge.net/xml/XSLT/consistRoster.xsl");
           org.jdom.ProcessingInstruction p = new org.jdom.ProcessingInstruction("xml-stylesheet", m);
           doc.addContent(0,p);
           
           Element roster = new Element("roster");

           for(int i=0; i<consistList.size();i++)
           {
                jmri.Consist newConsist=ConsistMan.getConsist((jmri.DccLocoAddress)consistList.get(i));
		roster.addContent(ConsistToXML(newConsist));
           }
           root.addContent(roster);
           try {   
           if(!checkFile(defaultConsistFilename()))
           {
               //The file does not exist, create it before writing
               java.io.File file=new java.io.File(defaultConsistFilename());
               file.createNewFile();
           }
           writeXML(findFile(defaultConsistFilename()),doc);
           } catch(java.io.IOException ioe) {
                log.error("IO Exception " +ioe);
                throw(ioe);
           } catch(org.jdom.JDOMException jde) {
                log.error("JDOM Exception " +jde);
                throw(jde);
           }
	}

    /**
     * Defines the preferences subdirectory in which LocoFiles are kept
     * by default.
     */
    static private String fileLocation = XmlFile.prefsDir()+File.separator+"roster"+File.separator+"consist";
                                         
    
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConsistFile.class.getName());

}

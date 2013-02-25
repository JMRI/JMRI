package jmri.jmrix.lenz.xntcp.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.lenz.xntcp.ConnectionConfig;
import jmri.jmrix.lenz.xntcp.XnTcpAdapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the XnTcpAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the XnTcpAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author	Giorgio Terdina Copyright (C) 2008, based on LI100 Action by Bob Jacobsen, Copyright (C) 2003
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if(adapter == null) adapter=new XnTcpAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter=((ConnectionConfig) object).getAdapter();
    }

   @Override
   public boolean load(org.jdom.Element e) throws Exception {
      boolean result=true; 
      try {
          result = super.load(e);
	  if(log.isDebugEnabled()) log.debug("result " +result);
      } catch (NullPointerException ex) {
	 // If the standard configuration fails, try the original 
         // original configurations method for XnTcp which used a
         // string name as the port name to designate automatic or
         // manual configuration.  
         // If manual was manual, option 1 contained the host name
         // and option 2 contained the port.  We now use option 1 to 
         // designate the manual option.
	 if(log.isDebugEnabled()) log.debug("Null Pointer Exception Occured");
	 String manualOption=null;
	 String hostName=null;
	 int portNumber=0;
	 try {
	     manualOption=e.getAttribute("port").getValue();
	     adapter.configureOption1(manualOption);
           } catch (NullPointerException e1) {
		// it is considered normal if this fails when the 
                //attributes are not present.
           }
         
	 try {
	     hostName=e.getAttribute("option1").getValue();
	     adapter.setHostName(hostName);
         } catch (NullPointerException e1) {
             // it is considered normal if this fails when the 
             //attributes are not present.
         }

	 try {
	     portNumber=e.getAttribute("option2").getIntValue();
	     adapter.setPort(portNumber);
         } catch (org.jdom.DataConversionException e2) {
             log.warn("Could not parse port attribute");
         } catch (NullPointerException e1) {
             // it is considered normal if this fails when the 
             //attributes are not present.
         }


        String manufacturer;
        try {
            manufacturer = e.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(manufacturer);
        } catch ( NullPointerException e1) { //Considered normal if not present

        }


        if (adapter.getSystemConnectionMemo()!=null){
            if (e.getAttribute("userName")!=null){
                adapter.getSystemConnectionMemo().setUserName(e.getAttribute("userName").getValue());
            }

            if (e.getAttribute("systemPrefix")!=null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(e.getAttribute("systemPrefix").getValue());
            }
        }

        if (e.getAttribute("disabled")!=null) {
            String yesno = e.getAttribute("disabled").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("no")) adapter.setDisabled(false);
                    else if (yesno.equals("yes")) adapter.setDisabled(true);
                }
        }
        // register, so can be picked up next time
        register();


        if (adapter.getDisabled()){
            unpackElement(e);
            return result;
        }
        try{
            adapter.connect();
        } catch (Exception e1) {
            jmri.configurexml.ConfigXmlManager.creationErrorEncountered(
                                        null, "opening connection",
                                        e1.getMessage(),
                                        null,null,null
                                    );
            return false;
        }

        // if successful so far, go ahead and configure
        adapter.configure();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(e);


      }
      return result;
   }


    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}

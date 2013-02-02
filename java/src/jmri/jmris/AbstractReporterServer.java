//AbstractReporterServer.java

package jmri.jmris;

import org.apache.log4j.Logger;
import java.io.*;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Reporter;

/**
 * Abstract interface between the a JMRI reporter and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

abstract public class AbstractReporterServer {

   public AbstractReporterServer(){
      reporters= new ArrayList<String>();
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendReport(String reporter, Object r) throws IOException; 
     abstract public void sendErrorStatus(String reporter) throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;

    synchronized protected void addReporterToList(java.lang.String reporterName) {
         if (!reporters.contains(reporterName) ) {
             reporters.add(reporterName);
             InstanceManager.reporterManagerInstance().provideReporter(reporterName)
                     .addPropertyChangeListener(new ReporterListener(reporterName));
         }
    }

    synchronized protected void removeReporterFromList(java.lang.String reporterName) {
         if (reporters.contains(reporterName) ) {
             reporters.remove(reporterName);
         }
    }

    /*
     * Set the report state of a reporter
     * 
     * @parm reporterName - the name of a reporter
     * @parm r - the object containing the report (currently a string).
     */
    public void setReporterReport(java.lang.String reporterName,Object r) {
                Reporter reporter = null;
		// load address from reporterAddrTextField
		try {
			addReporterToList(reporterName);
			reporter= InstanceManager.reporterManagerInstance().provideReporter(reporterName);
			if (reporter == null) {
				log.error("Reporter " + reporterName
						+ " is not available");
			} else {

			        if (log.isDebugEnabled())
					log.debug("about to set reporter State");
				reporter.setReport(r);
			}
		} catch (Exception ex) {
			log.error("set reporter report, exception: "
							+ ex.toString());
		}
	}

    class ReporterListener implements java.beans.PropertyChangeListener {

       ReporterListener(String reporterName) {
          name=reporterName;
          reporter= InstanceManager.reporterManagerInstance().provideReporter(reporterName);
       }

       // update state as state of reporter changes
       public void propertyChange(java.beans.PropertyChangeEvent e) {
    	 // If the Commanded State changes, show transition state as "<inconsistent>" 
        if (e.getPropertyName().equals("currentReport")) {
            String now = null;
	    try {
              now = e.getNewValue().toString();
            } catch (java.lang.NullPointerException npe) {
		// current report is null, which is expected.
		now = null;
            }
            try {
               sendReport(name,now);
            } catch(java.io.IOException ie) {
                  log.debug("Error Sending Status");
                  // if we get an error, de-register
                  reporter.removePropertyChangeListener(this);
                  removeReporterFromList(name);
            }
         }
      }

      String name = null;
      Reporter reporter=null;
 
    }

    protected ArrayList<String> reporters = null;

    String newState = "";


    static Logger log = Logger.getLogger(AbstractReporterServer.class.getName());

}

/**
 * LI100XNetProgrammer.java
 */

 // Convert the jmri.Programmer interface into commands for the Lenz XpressNet

package jmri.jmrix.lenz.li100;

import jmri.Programmer;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Programmer support for Lenz XpressNet.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode /Direct Mode read request
 * <LI>Wait for Broadcast Service Mode Entry message
 * <LI>Send Request for Service Mode Results request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 * @author Bob Jacobsen     Copyright (c) 2002, 2007
 * @author Paul Bender      Copyright (c) 2003, 2004, 2005, 2009
 * @author Giorgio Terdina  Copyright (c) 2007
 * @version $Revision$
 */
public class LI100XNetProgrammer extends XNetProgrammer implements XNetListener {

	static private final int RETURNSENT = 3;

        // save the last XPressNet message for retransmission after a 
        // communication error..
        private XNetMessage lastRequestMessage = null;

        private int _error=0;

        public LI100XNetProgrammer(XNetTrafficController tc){
           super(tc);
        }

      // programming interface
        synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
                if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
                useProgrammer(p);
                _progRead = false;
                // set new state & save values
                progState = REQUESTSENT;
                _val = val;
                _cv = 0xff & CV;

                try {
                   // start the error timer
                   restartTimer(XNetProgrammerTimeout);

                   // format and send message to go to program mode
                   if (_mode == Programmer.PAGEMODE) {
                       XNetMessage msg = XNetMessage.getWritePagedCVMsg(CV,val);
		       msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                       lastRequestMessage = new XNetMessage(msg);
                       controller().sendXNetMessage(msg, this);
                   } else if (_mode == Programmer.DIRECTBITMODE || _mode == Programmer.DIRECTBYTEMODE) {
                       XNetMessage msg = XNetMessage.getWriteDirectCVMsg(CV,val);
		       msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                       lastRequestMessage = new XNetMessage(msg);
                       controller().sendXNetMessage(msg, this);
                   } else  { // register mode by elimination
                       XNetMessage msg = XNetMessage.getWriteRegisterMsg(registerFromCV(CV),val);
		       msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                       lastRequestMessage = new XNetMessage(msg);
                       controller().sendXNetMessage(msg,this);
                   }
                } catch (jmri.ProgrammerException e) {
                  progState = NOTPROGRAMMING;
                  throw e;
                }
        }


      synchronized public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
                readCV(CV, p);
        }

     synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
                if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);

				if(!getCanRead()) {
				    // should not invoke this if cant read, but if done anyway set NotImplemented error
					p.programmingOpReply(CV,jmri.ProgListener.NotImplemented);
					return;
				} 

                useProgrammer(p);
                _progRead = true;
                // set new state
                progState = REQUESTSENT;
                _cv = 0xff & CV;
                try {
                  // start the error timer
                   restartTimer(XNetProgrammerTimeout);

                   // format and send message to go to program mode
                   if (_mode == Programmer.PAGEMODE) {
                       XNetMessage msg=XNetMessage.getReadPagedCVMsg(CV);
		       msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                       lastRequestMessage = new XNetMessage(msg);
                       controller().sendXNetMessage(msg, this);
                   } else if (_mode == Programmer.DIRECTBITMODE || _mode == Programmer.DIRECTBYTEMODE) {
                       XNetMessage msg=XNetMessage.getReadDirectCVMsg(CV);
		       msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                       lastRequestMessage = new XNetMessage(msg);
                       controller().sendXNetMessage(msg, this);
                   } else { // register mode by elimination
                       XNetMessage msg=XNetMessage.getReadRegisterMsg(registerFromCV(CV));
		       msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                       lastRequestMessage = new XNetMessage(msg);
                       controller().sendXNetMessage(msg, this);
                   }
                } catch (jmri.ProgrammerException e) {
                  progState = NOTPROGRAMMING;
                  throw e;
                }
        }


	synchronized public void message(XNetReply m) {
            	if (m.getElement(0)==XNetConstants.CS_INFO && 
                     m.getElement(1)==XNetConstants.BC_SERVICE_MODE_ENTRY) {
                     if(_service_mode == false) {
		        // the command station is in service mode.  An "OK" 
		        // message can trigger a request for service mode 
		        // results if progrstate is REQUESTSENT.
		   	if (log.isDebugEnabled()) log.debug("change _service_mode to true");
		        _service_mode = true; 
                     }
                     else if(_service_mode == true) {
			// Since we get this message as both a broadcast and
                        // a directed message, ignore the message if we're
                        //already in the indicated mode
		   	if (log.isDebugEnabled()) log.debug("_service_mode already true");
                        return;	   
                     }
		}
		if(m.getElement(0)==XNetConstants.CS_INFO &&
		   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
                     if(_service_mode == true ) {
		        // the command station is not in service mode.  An 
		        // "OK" message can not trigger a request for service 
		        // mode results if progrstate is REQUESTSENT.
		   	if (log.isDebugEnabled()) log.debug("change _service_mode to false");
		        _service_mode = false;
                     }
                     else if(_service_mode == false) {
			// Since we get this message as both a broadcast and
                        // a directed message, ignore the message if we're
                        //already in the indicated mode
		   	if (log.isDebugEnabled()) log.debug("_service_mode already false");
		        return;
                     }
		}

		if (progState == NOTPROGRAMMING) {
			// we get the complete set of replies now, so ignore these
			return;

		} else if (progState == REQUESTSENT) {
		   	if (log.isDebugEnabled()) log.debug("reply in REQUESTSENT state");
			// see if reply is the acknowledge of program mode; if not, wait for next
            		if ( (_service_mode && m.isOkMessage()) || 
            		     (m.getElement(0)==XNetConstants.CS_INFO && 
                	   (m.getElement(1)==XNetConstants.BC_SERVICE_MODE_ENTRY ||
		            m.getElement(1)==XNetConstants.PROG_CS_READY )) ) {
			       stopTimer();

					if(!getCanRead()) { 
                                         // on systems like the Roco MultiMaus
                                         // (which does not support reading)
                                         // let a timeout occur so the system
                                         // has time to write data to the
                                         // decoder
                                                restartTimer(SHORT_TIMEOUT);
                                                return; 
					}

			       // here ready to request the results
			       progState = INQUIRESENT;
                	       //start the error timer
			       restartTimer(XNetProgrammerTimeout);

			       controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                                                            this);
                               return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
                           m.getElement(1)==XNetConstants.CS_NOT_SUPPORTED) {
                           // programming operation not supported by this command station
			   progState = RETURNSENT;
			   _error = jmri.ProgListener.NotImplemented;
			   // create a request to exit service mode and 
			   // send the message to the command station
			   controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
		           this);
                           return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
			   // We Exited Programming Mode early
			   //log.error("Service mode exited before sequence complete.");
			   progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
			   m.getElement(1)==XNetConstants.PROG_SHORT_CIRCUIT) {
			   // We experienced a short Circuit on the Programming Track
			   log.error("Short Circuit While Programming Decoder");
			   progState = RETURNSENT;
			   _error = jmri.ProgListener.ProgrammingShort;
			   // create a request to exit service mode and 
			   // send the message to the command station
			   controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                            this);
			   //notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
                        } else if(m.isCommErrorMessage()) {
                           // We experienced a communicatiosn error
                           // If this is a Timeslot error, ignore it,
                           //otherwise report it as an error
                           if(m.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)
                                   return;
                           else if (!_service_mode){
                             log.error("Communications error in REQUESTSENT state before entering service mode.  Error: " + m.toString());
                             controller().sendXNetMessage(lastRequestMessage, this);
                           } else {
                              log.error("Communications error in REQUESTSENT state after entering service mode.  Error: " + m.toString());
                                 progState = RETURNSENT;
                                 _error=jmri.ProgListener.CommError;
			         // create a request to exit service mode and 
			         // send the message to the command station
			         controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                            this);
                          } 
   			}
		} else if (progState == INQUIRESENT) {
			if (log.isDebugEnabled()) log.debug("reply in INQUIRESENT state");
            		// check for right message, else return
            		if (m.isPagedModeResponse()) {
                	    // valid operation response, but does it belong to us?
                            try {
                               // we always save the cv number, but if 
                               // we are using register mode, there is 
                               // at least one case (CV29) where the value
                               // returned does not match the value we saved.
                               if(m.getServiceModeCVNumber()!=_cv &&
                                  m.getServiceModeCVNumber()!=registerFromCV(_cv)) {
                                   log.debug(" result for CV " + m.getServiceModeCVNumber() +
                                             " expecting " + _cv);
                                   return;
                               }
                            } catch (jmri.ProgrammerException e) {
                                progState = NOTPROGRAMMING;
                                notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
                            }
			    // see why waiting
			    if (_progRead) {
			        // read was in progress - get return value
				_val = m.getServiceModeCVValue();
			    }
			    progState = RETURNSENT;
			    _error=jmri.ProgListener.OK;
			    // create a request to exit service mode and 
			    // send the message to the command station
			    controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                            this);
                	    return;
            		} else if ( m.isDirectModeResponse() ) {
                	    // valid operation response, but does it belong to us?
                            if(m.getServiceModeCVNumber()!=_cv) {
                                log.debug(" result for CV " + m.getServiceModeCVNumber() +
                                          " expecting " + _cv);
                                return;
                            }

			    // see why waiting
			    if (_progRead) {
				// read was in progress - get return value
				_val = m.getServiceModeCVValue();
			    }
			    progState = RETURNSENT;
                            _error=jmri.ProgListener.OK;
			    stopTimer();
			   // create a request to exit service mode and 
			   // send the message to the command station
			   controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                            this);
                	    return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.PROG_BYTE_NOT_FOUND) {
                    	   // "data byte not found", e.g. no reply
		    	   progState = RETURNSENT;
		           _error=jmri.ProgListener.NoLocoDetected;
			   // create a request to exit service mode and 
			   // send the message to the command station
			   controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                            this);
               	    	   return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
			   m.getElement(1)==XNetConstants.PROG_SHORT_CIRCUIT) {
			   // We experienced a short Circuit on the Programming Track
			   log.error("Short Circuit While Programming Decoder");
			   progState = RETURNSENT;
                           _error=jmri.ProgListener.ProgrammingShort;
			   // create a request to exit service mode and 
			   // send the message to the command station
			   controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                            this);
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
			   // We Exited Programming Mode early
			   log.error("Service mode exited before sequence complete.");
			   progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
                        } else if(m.isCommErrorMessage()) {
                           // We experienced a communicatiosn error
                           // If this is a Timeslot error, ignore it
                           if(m.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR){
                                   return;
                           } else if (_service_mode){
                               // If we're in service mode, retry sending the 
                               // result request.
                               log.error("Communications error in INQUIRESENT state while in service mode.  Error: " + m.toString());
			       controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                                                            this);          
	                       return; 
                           } else {
                           //otherwise report it as an error
                           log.error("Communications error in INQUIRESENT state after exiting service mode.  Error: " + m.toString());
                           progState = RETURNSENT;
                           _error=jmri.ProgListener.CommError;
			   // create a request to exit service mode and 
			   // send the message to the command station
			   controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                           this);
	                   return; 
                           }
			} else {
                           // nothing important, ignore
                           return;
		   	}
	    
	    } else if (progState == RETURNSENT) {
	       if (log.isDebugEnabled()) log.debug("reply in RETURNSENT state");
               if (m.getElement(0)==XNetConstants.CS_INFO && 
	          m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
		  progState = NOTPROGRAMMING;
		  stopTimer();

                  // We've exited service mode.  Notify the programmer of any 
                  // the results.  
                  notifyProgListenerEnd(_val, _error);

               	  return;
	       }
	    } else {
		if (log.isDebugEnabled()) log.debug("reply in un-decoded state");
	    }
	}

 	// listen for the messages to the LI100/LI101
    	synchronized public void message(XNetMessage l) {
    	}

        /**
         * Internal routine to handle a timeout
         */
        @Override
        synchronized protected void timeout() {
                // if a timeout occurs, and we are not 
                // finished programming, we need to exit
                // service mode.
                if (progState != NOTPROGRAMMING) {
                        // we're programming, time to stop
                        if (log.isDebugEnabled()) log.debug("timeout!");

                        progState = RETURNSENT;
                        if(!getCanRead())
                           _error = jmri.ProgListener.OK;  //MultiMaus etc.
                        else
                           // perhaps no loco present?
                           _error=jmri.ProgListener.FailedTimeout;
                                                
                        controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                                                this);
                } 
        }




   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LI100XNetProgrammer.class.getName());

}


/* @(#)XNetProgrammer.java */

// SE8cSignalHead.java

package jmri.jmrix.loconet;

import jmri.AbstractSignalHead;

/**
 * Extend jmri.SignalHead for signals implemented by an SE8c
 * <P>
 * This implementation writes out to the physical signal when
 * it's commanded to change appearance, and updates its internal state
 * when it hears commands from other places.
 *
 * <P>The algorithms in this class are a collaborative effort of Digitrax, Inc
 * and Bob Jacobsen.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.9 $
 */public class SE8cSignalHead extends AbstractSignalHead implements LocoNetListener {

    public SE8cSignalHead(int pNumber, String sys) {
        super(""+pNumber, sys);
        init(pNumber);
    }

    public SE8cSignalHead(int pNumber) {
        super(""+pNumber);
        init(pNumber);
    }

    void init(int pNumber) {
        mNumber = pNumber;
        mAppearance = RED;  // start turned off
        // At construction, register for messages
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.warn("No LocoNet connection, signal head won't update");
    }

    public int getAppearance() { return mAppearance; }
    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
        if (oldAppearance != newAppearance) {
            forwardCommandChangeToLayout(mAppearance);
            // notify listeners, if any
            firePropertyChange("Appearance", new Integer(oldAppearance), new Integer(newAppearance));
        }

    }

    public int getNumber() { return mNumber; }
    public String getSystemName() { return "LH"+getNumber(); }

    // Handle a request to change state by sending a LocoNet command
    protected void forwardCommandChangeToLayout(int s)  {
         // send SWREQ for close
         LocoNetMessage l = new LocoNetMessage(4);
         l.setOpCode(LnConstants.OPC_SW_REQ);

         int address = 0;
         boolean closed = false;
         // which of the four states?
         switch (s) {
            case RED:
                address = mNumber;
                closed = false;
                break;
            case YELLOW:
                address = mNumber+1;
                closed = false;
                break;
            case FLASHGREEN:
            case FLASHYELLOW:
            case FLASHRED:
            case DARK:
                address = mNumber+1;
                closed = true;
                break;
            case GREEN:
                address = mNumber;
                closed = true;
                break;
            default:
                log.error("Invalid state request: "+s);
                return;
         }

         // compute address fields
         int hiadr = (address-1)/128;
         int loadr = (address-1)-hiadr*128;
         if (closed) hiadr |= 0x20;

         // store and send
         l.setElement(1,loadr);
         l.setElement(2,hiadr);
         LnTrafficController.instance().sendLocoNetMessage(l);
     }

     // implementing classes will typically have a function/listener to get
     // updates from the layout, which will then call
     //		public void firePropertyChange(String propertyName,
     //						Object oldValue,
     //						Object newValue)
     // _once_ if anything has changed state (or set the commanded state directly)
     public void message(LocoNetMessage l) {
        int oldAppearance = mAppearance;
         // parse message type
         switch (l.getOpCode()) {
         case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
             int sw1 = l.getElement(1);
             int sw2 = l.getElement(2);
             if (myAddress(sw1, sw2)) {
                 if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0) {
                     // was set CLOSED
                     mAppearance = GREEN;
                 } else {
                     // was set THROWN
                     mAppearance = RED;
                 }
             }
             if (myAddressPlusOne(sw1, sw2)) {
                 if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0) {
                     // was set CLOSED
                     // don't change if one of the possibilities already
                     if ( ! (mAppearance==FLASHYELLOW|| mAppearance==DARK
                            || mAppearance==FLASHGREEN || mAppearance==FLASHRED))
                     mAppearance = FLASHYELLOW;
                 } else {
                     // was set THROWN
                     mAppearance = YELLOW;
                 }
             }
             break;
         }
         case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */
             int sw1 = l.getElement(1);
             int sw2 = l.getElement(2);
             if (myAddress(sw1, sw2)) {
                 // see if its a turnout state report
                 if ((sw2 & LnConstants.OPC_SW_REP_INPUTS)==0) {
                    // sort out states
                    if ((sw2 & LnConstants.OPC_SW_REP_CLOSED) != 0) {
                        // was set CLOSED
                        mAppearance = GREEN;
                    }
                    if ((sw2 & LnConstants.OPC_SW_REP_THROWN) != 0) {
                        // was set THROWN
                        mAppearance = RED;
                    }
                 }
             }
             if (myAddressPlusOne(sw1, sw2)) {
                 // see if its a turnout state report
                 if ((sw2 & LnConstants.OPC_SW_REP_INPUTS)==0) {
                    // sort out states
                    if ((sw2 & LnConstants.OPC_SW_REP_CLOSED) != 0) {
                        // was set CLOSED
                        mAppearance = FLASHYELLOW;
                    }
                    if ((sw2 & LnConstants.OPC_SW_REP_THROWN) != 0) {
                        // was set THROWN
                        mAppearance = YELLOW;
                    }
                 }
             }
         }
         default:
             return;
         }
         // reach here if the state has updated
         if (oldAppearance != mAppearance) {
            firePropertyChange("Appearance", new Integer(oldAppearance), new Integer(mAppearance));
         }
     }

     public void dispose() {
         LnTrafficController.instance().removeLocoNetListener(~0, this);
     }

     // data members
     int mNumber;   // loconet turnout number with lower address (0 based)
     int mAppearance;   // current value of appearance variable

     private boolean myAddress(int a1, int a2) {
         // the "+ 1" in the following converts to throttle-visible numbering
         return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == mNumber;
     }
     private boolean myAddressPlusOne(int a1, int a2) {
         // the "+ 1" in the following converts to throttle-visible numbering
         return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == mNumber+1;
     }
     static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SE8cSignalHead.class.getName());

 }


/* @(#)SE8cSignalHead.java */

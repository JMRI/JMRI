// PaneServiceProgFrame.java

package jmri.jmrit.symbolicprog.tabbedframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import javax.swing.JPanel;

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;

/**
 * Extend the PaneProgFrame to handle service mode operations.
 *<p>
 * If no programmer is provided, the programmer parts of the GUI are suppressed.
 *
 * @author	   Bob Jacobsen   Copyright (C) 2002, 2008
 * @version	   $Revision$
 */
public class PaneServiceProgFrame extends PaneProgFrame
                         implements java.beans.PropertyChangeListener  {

    jmri.jmrit.progsupport.ProgModeSelector  modePane;


    /**
     * Provide the programming mode selection pane for inclusion
     */
    protected JPanel getModePane() {
        // ensure initialization, even if invoked in ctor
        if (modePane == null) modePane = new jmri.jmrit.progsupport.ProgServiceModeComboBox();
        log.debug("invoked getModePane");
        return modePane;
    }


    /**
     * This invokes the parent ctor to do the real work. That will
     * call back to get the programming mode panel (provided) and to
     * hear if there is read mode (depends). Then, this sets the
     * programming mode for the service programmer based on what's
     * in the decoder file.
     *
     * @param decoderFile XML file defining the decoder contents
     * @param r RosterEntry for information on this locomotive
     * @param name
     * @param file
     */
    @SuppressWarnings("unchecked")
    public PaneServiceProgFrame(DecoderFile decoderFile, RosterEntry r,
                                String name, String file, Programmer pProg) {
        super(decoderFile, r, name, file, pProg, false);

        // set the programming mode
        if (pProg != null)
            if (jmri.InstanceManager.programmerManagerInstance() != null) {
                // go through in preference order, trying to find a mode
                // that exists in both the programmer and decoder.
                // First, get attributes. If not present, assume that
                // all modes are usable
                Element programming = null;
                boolean paged = true;
                boolean directbit= true;
                boolean directbyte= true;
                boolean register= true;
                if (decoderRoot != null
                    && (programming = decoderRoot.getChild("decoder").getChild("programming"))!= null) {
                    
                    // set the programming attributes for DCC
                    Attribute a;
                    if ( (a = programming.getAttribute("paged")) != null )
                        if (a.getValue().equals("no")) paged = false;
                    if ( (a = programming.getAttribute("direct")) != null ) {
                        if (a.getValue().equals("no")) { directbit = false; directbyte = false; }
                        else if (a.getValue().equals("bitOnly")) { directbit = true; directbyte = false; }
                        else if (a.getValue().equals("byteOnly")) { directbit = false; directbyte = true; }
                    }
                    if ( (a = programming.getAttribute("register")) != null )
                        if (a.getValue().equals("no")) register = false;
                        
                    // iterate over any facades and add them
                    List<Element> facades = (List<Element>)(programming.getChildren("capability"));
                    if (log.isDebugEnabled()) log.debug("Found "+facades.size()+" capability elements");
                    for (Element facade : facades) {
                        String fname = facade.getChild("name").getText();
                        if (log.isDebugEnabled()) log.debug("Process capability facade: "+fname);


                        List<Element> parameters = (List<Element>)(facade.getChildren("parameter"));
                        if (log.isDebugEnabled()) log.debug("Found "+facades.size()+" capability parameters");
                        for (Element parameter : parameters) {
                            String pval = parameter.getText();
                            if (log.isDebugEnabled()) log.debug("Process parameter value: "+pval);
                        }

                        if (fname.equals("High Access via Double Index")) {
                            // going to create a specific one
                            String top          = parameters.get(0).getText();
                            String addrCVhigh   = parameters.get(1).getText();
                            String addrCVlow    = parameters.get(2).getText();
                            String valueCV      = parameters.get(3).getText();
                            String modulo       = parameters.get(4).getText();

                            jmri.implementation.AddressedHighCvProgrammerFacade pf =
                                new jmri.implementation.AddressedHighCvProgrammerFacade(mProgrammer, top, addrCVhigh, addrCVlow, valueCV, modulo);
                            
                            log.debug("new programmer "+pf);
                            mProgrammer = pf;
                            cvModel.setProgrammer(pf);
                            iCvModel.setProgrammer(pf);
                            resetModel.setProgrammer(pf);
                            log.debug("Found programmers: "+cvModel.getProgrammer()+" "+iCvModel.getProgrammer());
                            
                        } else if (fname.equals("High Access via Partial Index")) {
                            // going to create a specific one
                            String top          = parameters.get(0).getText();
                            String addrCV       = parameters.get(1).getText();
                            String factor       = parameters.get(2).getText();
                            String modulo       = parameters.get(3).getText();

                            jmri.implementation.OffsetHighCvProgrammerFacade pf =
                                new jmri.implementation.OffsetHighCvProgrammerFacade(mProgrammer, top, addrCV, factor, modulo);
                            
                            log.debug("new programmer "+pf);
                            mProgrammer = pf;
                            cvModel.setProgrammer(pf);
                            iCvModel.setProgrammer(pf);
                            resetModel.setProgrammer(pf);
                            log.debug("Found programmers: "+cvModel.getProgrammer()+" "+iCvModel.getProgrammer());
                            
                        } else {
                            log.error("Cannot create programmer capability named: "+fname);
                        }
                    }
                }
    
                // is the current mode OK?
                int currentMode = mProgrammer.getMode();
                log.debug("XML specifies modes: P "+paged+" DBi "+directbit+" Dby "+directbyte+" R "+register+" now "+currentMode);

                // find a mode to set it to
                if (mProgrammer.hasMode(Programmer.DIRECTBITMODE)&&directbit)
                    mProgrammer.setMode(jmri.Programmer.DIRECTBITMODE);
                else if (mProgrammer.hasMode(Programmer.DIRECTBYTEMODE)&&directbyte)
                    mProgrammer.setMode(jmri.Programmer.DIRECTBYTEMODE);
                else if (mProgrammer.hasMode(Programmer.PAGEMODE)&&paged)
                    mProgrammer.setMode(jmri.Programmer.PAGEMODE);
                else if (mProgrammer.hasMode(Programmer.REGISTERMODE)&&register)
                    mProgrammer.setMode(jmri.Programmer.REGISTERMODE);
                else log.warn("No acceptable mode found, leave as found");
                
            } else {
                log.error("Can't set programming mode, no programmer instance");
            }

        pack();

        if (log.isDebugEnabled()) log.debug("PaneServiceProgFrame \""+name
                                            +"\" constructed");
    }

    /**
     * local dispose, which also invokes parent. Note that
     * we remove the components (removeAll) before taking those
     * apart.
     */
    public void dispose() {

        if (log.isDebugEnabled()) log.debug("dispose local");
        super.dispose();

    }

    static Logger log = LoggerFactory.getLogger(PaneServiceProgFrame.class.getName());

}


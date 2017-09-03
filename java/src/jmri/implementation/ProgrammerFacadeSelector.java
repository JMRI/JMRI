package jmri.implementation;

import java.util.List;
import jmri.AddressedProgrammer;
import jmri.Programmer;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to load a specific ProgrammerFacade from an XML element
 * <P>
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class ProgrammerFacadeSelector {

    /**
     * Add facades specified in an XML decoder definition element to the front
     * of a programmer.
     *
     * @param element    Contains "capability" elements that define the Facades
     * @param programmer Programmer implementation to decorate
     * @param allowCache Passed to facades that optionally cache
     * @return the programmer with added facades
     */
    public static Programmer loadFacadeElements(Element element, Programmer programmer, boolean allowCache) {
        // iterate over any facades and add them
        List<Element> facades = element.getChildren("capability");
        if (log.isDebugEnabled()) {
            log.debug("Found " + facades.size() + " capability elements");
        }
        for (Element facade : facades) {
            String fname = facade.getChild("name").getText();
            if (log.isDebugEnabled()) {
                log.debug("Process capability facade: " + fname);
            }

            List<Element> parameters = facade.getChildren("parameter");
            if (log.isDebugEnabled()) {
                log.debug("Found " + facades.size() + " capability parameters");
            }
            for (Element parameter : parameters) {
                String pval = parameter.getText();
                if (log.isDebugEnabled()) {
                    log.debug("Process parameter value: " + pval);
                }
            }

            if (fname.equals("High Access via Double Index")) {
                // going to create a specific one
                String top = parameters.get(0).getText();
                String addrCVhigh = parameters.get(1).getText();
                String addrCVlow = parameters.get(2).getText();
                String valueCV = parameters.get(3).getText();
                String modulo = parameters.get(4).getText();

                jmri.implementation.AddressedHighCvProgrammerFacade pf
                        = new jmri.implementation.AddressedHighCvProgrammerFacade(programmer, top, addrCVhigh, addrCVlow, valueCV, modulo);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (fname.equals("High Access via Partial Index")) {
                // going to create a specific one
                String top = parameters.get(0).getText();
                String addrCV = parameters.get(1).getText();
                String factor = parameters.get(2).getText();
                String modulo = parameters.get(3).getText();

                jmri.implementation.OffsetHighCvProgrammerFacade pf
                        = new jmri.implementation.OffsetHighCvProgrammerFacade(programmer, top, addrCV, factor, modulo);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (fname.equals("High Access via Partial Index with Reset")) {
                // going to create a specific one
                String top = parameters.get(0).getText();
                String addrCV = parameters.get(1).getText();
                String factor = parameters.get(2).getText();
                String modulo = parameters.get(3).getText();
                String indicator = parameters.get(4).getText();

                jmri.implementation.ResettingOffsetHighCvProgrammerFacade pf
                        = new jmri.implementation.ResettingOffsetHighCvProgrammerFacade(programmer, top, addrCV, factor, modulo, indicator);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (fname.equals("Indexed CV access")) {
                // going to create a specific one
                String PI = parameters.get(0).getText();
                String SI = (parameters.size() > 1) ? parameters.get(1).getText() : null;
                boolean cvFirst = (parameters.size() > 2) ? (parameters.get(2).getText().equals("false") ? false : true) : true;
                boolean skipDupIndexWrite = (parameters.size() > 3) ? (parameters.get(3).getText().equals("false") ? false : allowCache) : allowCache; // if not present, use default

                jmri.implementation.MultiIndexProgrammerFacade pf
                        = new jmri.implementation.MultiIndexProgrammerFacade(programmer, PI, SI, cvFirst, skipDupIndexWrite);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (fname.equals("TCS 4 CV access")) {

                jmri.implementation.TwoIndexTcsProgrammerFacade pf
                        = new jmri.implementation.TwoIndexTcsProgrammerFacade(programmer);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (fname.equals("Ops Mode Accessory Programming")) {
                if (programmer instanceof AddressedProgrammer) {  // create if relevant to current mode, otherwise silently ignore
                    String addrType = "decoder";
                    if (!parameters.isEmpty()) {
                        addrType = parameters.get(0).getText();
                    }

                    jmri.implementation.AccessoryOpsModeProgrammerFacade pf
                            = new jmri.implementation.AccessoryOpsModeProgrammerFacade((AddressedProgrammer) programmer, addrType);

                    log.debug("new programmer " + pf);
                    programmer = pf; // to go around and see if there are more
                }

            } else {
                log.error("Cannot create programmer capability named: " + fname);
            }
        }

        return programmer;
    }

    private final static Logger log = LoggerFactory.getLogger(ProgrammerFacadeSelector.class);
}

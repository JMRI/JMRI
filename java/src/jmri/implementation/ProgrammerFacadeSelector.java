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
 * @author	Bob Jacobsen Copyright (C) 2013
 */
public class ProgrammerFacadeSelector {

    public static Programmer loadFacadeElements(Element element, Programmer programmer) {
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

                jmri.implementation.MultiIndexProgrammerFacade pf
                        = new jmri.implementation.MultiIndexProgrammerFacade(programmer, PI, SI, cvFirst);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (fname.equals("TCS 4 CV access")) {

                jmri.implementation.TwoIndexTcsProgrammerFacade pf
                        = new jmri.implementation.TwoIndexTcsProgrammerFacade(programmer);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else if (programmer instanceof AddressedProgrammer && fname.equals("Ops Mode Accessory Programming")) {

                jmri.implementation.AccessoryOpsModeProgrammerFacade pf
                        = new jmri.implementation.AccessoryOpsModeProgrammerFacade((AddressedProgrammer) programmer);

                log.debug("new programmer " + pf);
                programmer = pf; // to go around and see if there are more

            } else {
                log.error("Cannot create programmer capability named: " + fname);
            }
        }

        return programmer;
    }

    private final static Logger log = LoggerFactory.getLogger(ProgrammerFacadeSelector.class.getName());
}

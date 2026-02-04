package jmri.jmrit.logixng.configurexml2;

import org.jdom2.Element;

/**
 * Outer class for TransitScaffold.MyEntryExitPairsXml.
 * @author Daniel Bergqvist (C) 2023
 */
public class TransitScaffold {

    public static class MyEntryExitPairsXml extends jmri.configurexml.AbstractXmlAdapter {

        @Override
        public Element store(Object o) {
            // No need to store anything here
            return null;
        }

    }

}

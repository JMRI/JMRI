package jmri.jmrit.logixng.implementation.configurexml;

import org.jdom2.Element;

/**
 * XML class for the DefaultClipboard.MaleRootManager class.
 * @author Daniel Bergqvist (C) 2026
 */
public class DefaultClipboard {

    public static class MaleRootManagerXml extends AbstractManagerXml {

        @Override
        public Element store(Object o) {
            // Never store anything.
            return null;
        }

    }

}

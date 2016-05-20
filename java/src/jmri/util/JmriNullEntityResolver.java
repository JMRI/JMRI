// JmriNullEntityResolver.java

package jmri.util;

/**
 * Entity Resolver to return a null DTD content, used to 
 * bypass verification.
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version $Revision$
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Return a content-less DTD
 */
public class JmriNullEntityResolver implements EntityResolver {
    public InputSource resolveEntity (String publicId, String systemId) {
        log.debug("resolves "+systemId);
        return new InputSource(new java.io.StringReader(""));
    }

    static private Logger log = LoggerFactory.getLogger(JmriNullEntityResolver.class.getName());

}
 

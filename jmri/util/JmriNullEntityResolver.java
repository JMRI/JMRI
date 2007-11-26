// JmriNullEntityResolver.java

package jmri.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Entity Resolver to return a null DTD content, used to 
 * bypass verification.
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version $Revision: 1.1 $
 */

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

    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriNullEntityResolver.class.getName());

}
 
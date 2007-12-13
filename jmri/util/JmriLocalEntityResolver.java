// JmriLocalEntityResolver.java

package jmri.util;

import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.net.URI;

/**
 * Entity Resolver to locate JMRI DTDs in the local space.
 *<P>
 * For historical reasons, JMRI xml files might have DTD definitions
 * of three forms:
 *<OL>
 *<LI>SYSTEM "../DTD/decoder-config.dtd"
 *<LI>SYSTEM "layout-config.dtd"
 *<LI>SYSTEM "http://jmri.sourceforce.net/xml/DTD/layout-config.dtd"
 *</OL>
 * Only the last of these is preferred now. The first two refer to
 * local files within the JMRI distributions in the xml/DTD directory.
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version $Revision: 1.4 $
 */

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class JmriLocalEntityResolver implements EntityResolver {
    public InputSource resolveEntity (String publicId, String systemId) {
        if (log.isDebugEnabled()) log.debug(" got DTD request "+systemId);
        
        // find local file first
        try {
            URI uri = new URI(systemId);
            if (log.isDebugEnabled()) log.debug("systemId: "+systemId);
            String scheme = uri.getScheme();
            String source = uri.getSchemeSpecificPart();
            String path = uri.getPath();
            if (log.isDebugEnabled()) log.debug("scheme: "+scheme);
            if (log.isDebugEnabled()) log.debug("source: "+source);
            if (log.isDebugEnabled()) log.debug("path: "+path);

            // figure out which form we have
            if (scheme.equals("http")) {
                // type 3 - find local file if we can
                String filename = path.substring(1);  // drop leading slash
                try {
                    return new InputSource(new java.io.FileReader(new File(filename)));
                } catch (java.io.FileNotFoundException e2) {
                    log.error("did not find type 3 DTD file: "+filename);
                    return null;  // use default, which is to find on web
                }
            } else if (path.startsWith("../DTD")) {
                // type 1
                String filename = "xml"+File.separator+"DTD"+File.separator+path;
                try {
                    return new InputSource(new java.io.FileReader(new File(filename)));
                } catch (java.io.FileNotFoundException e2) {
                    log.error("did not find type 1 DTD file: "+filename);
                    return null;
                }
            } else if (path.indexOf("/")==-1) {  // path doesn't contain "/", so is just name
                // type 2
                String filename = "xml"+File.separator+"DTD"+File.separator+path;
                try {
                    return new InputSource(new java.io.FileReader(new File(filename)));
                } catch (java.io.FileNotFoundException e2) {
                    log.error("did not find type 2 DTD file: "+filename);
                    return null;
                }
            } else if (scheme.equals("file")) {
                // still looking for a local file, this must be absolute or full relative path
                try {
                    return new InputSource(new java.io.FileReader(new File(path)));
                } catch (java.io.FileNotFoundException e2) {
                    log.error("did not find direct DTD file: "+path);
                    return null;
                }
            } else {
                // not recognized type, return null to use default
                log.error("could not parse systemId: "+systemId);
                return null;
            }
        } catch (Exception e1) { // was java.net.URISyntaxException, but that's not in Java 1.3.1
            log.warn(e1);
            return null;
        }
    }

    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriLocalEntityResolver.class.getName());

}
 

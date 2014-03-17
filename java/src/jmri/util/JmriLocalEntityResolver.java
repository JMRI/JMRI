// JmriLocalEntityResolver.java

package jmri.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

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
 * @author Bob Jacobsen  Copyright 2007, 2009
 * @version $Revision$
 */
public class JmriLocalEntityResolver implements EntityResolver {
    public InputSource resolveEntity (String publicId, String systemId) {
        log.debug("-- got entity request {}", systemId);
        
        // find local file first
        try {
            URI uri = new URI(systemId);
            InputStream stream;
            log.debug("systemId: {}", systemId);
            String scheme = uri.getScheme();
            String source = uri.getSchemeSpecificPart();
            String path = uri.getPath();

            log.debug("scheme: {}", scheme);
            log.debug("source: {}", source);
            log.debug("path: {}", path);

            // figure out which form we have
            if (scheme.equals("http")) {
                // type 3 - find local file if we can
                String filename = path.substring(1);  // drop leading slash
                log.debug("http finds filename: {}", filename);
                stream = FileUtil.findInputStream(filename);
                if (stream != null) {
                    return new InputSource(stream);
                } else {
                    log.debug("did not find local type 3 DTD file: {}", filename);
                    // try to find on web
                    return null;  // tell parser to use default, which is to find on web
                }
            } else if (path != null && path.startsWith("../DTD")) {
                // type 1
                String filename = "xml"+File.separator+"DTD"+File.separator+path;
                log.debug("starts with ../DTD finds filename: {}", filename);
                stream = FileUtil.findInputStream(filename);
                if (stream != null) {
                    return new InputSource(stream);
                } else {
                    log.error("did not find type 1 DTD file: {}", filename);
                    return null;
                }
            } else if (path != null && path.indexOf("/")==-1) {  // path doesn't contain "/", so is just name
                // type 2
                String filename = "xml"+File.separator+"DTD"+File.separator+path;
                log.debug("doesn't contain / finds filename: {}", filename);
                stream = FileUtil.findInputStream(filename);
                if (stream != null) {
                    return new InputSource(stream);
                } else {
                    log.error("did not find type 2 entity file: {}", filename);
                    return null;
                }
            } else if (scheme.equals("file")) {
                if (path != null ) {
                    // still looking for a local file, this must be absolute or full relative path
                    log.debug("scheme file finds path: {}", path);
                    // now we see if we've got a valid path
                    stream = FileUtil.findInputStream(path);
                    if (stream != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("file exists, used");
                        }
                        return new InputSource(stream);
                    } else { // file not exist
                        // now do special case for Windows, which might use "/" or "\"
                        // regardless of what File.separator says
                        String realSeparator = File.separator;
                        // guess! first form is right one
                        if (SystemType.isWindows()) {
                            int forIndex = path.indexOf("/");
                            int backIndex = path.indexOf("\\");
                            if (forIndex >= 0 && backIndex < 0) {
                                realSeparator = "/";
                            } else if (forIndex < 0 && backIndex >= 0) {
                                realSeparator = "\\";
                            } else if (forIndex > 0 && backIndex >= forIndex) {
                                realSeparator = "\\";
                            } else if (backIndex > 0 && forIndex >= backIndex) {
                                realSeparator = "/";
                            }
                            log.debug(" forIndex {} backIndex {}", forIndex, backIndex);
                        }
                        log.debug("File.separator {} realSeparator {}", File.separator, realSeparator);
                        // end special case
                        if (path.lastIndexOf(realSeparator + "DTD" + realSeparator) >= 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("file not exist, DTD in name, insert xml directory");
                            }
                            String modifiedPath = realSeparator + "xml"
                                    + path.substring(path.lastIndexOf(realSeparator + "DTD" + realSeparator), path.length());
                            path = modifiedPath;
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("file not exist, no DTD, insert xml/DTD directory");
                            }
                            String modifiedPath = realSeparator + "xml" + realSeparator + "DTD"
                                    + path.substring(path.lastIndexOf(realSeparator), path.length());
                            path = modifiedPath;
                        }
                        stream = FileUtil.findInputStream(path);
                        log.debug("attempting : {}", path);
                        if (stream != null) {
                            return new InputSource(stream);
                        } else {
                            log.error("did not find direct entity path: " + path);
                            return null;
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("schema file with null path");
                    }
                    try {
                        return new InputSource(new FileReader(new File(source)));
                    } catch (FileNotFoundException e2) {
                        log.error("did not find direct entity file: " + source);
                        return null;
                    }
                }
            } else {
                // not recognized type, return null to use default
                log.error("could not parse systemId: "+systemId);
                return null;
            }
        } catch (Exception e1) { // was java.net.URISyntaxException, but that's not in Java 1.3.1
            log.warn(e1.getLocalizedMessage(), e1);
            return null;
        } catch (NoClassDefFoundError e2) { // working on an old version of java, go with default quietly
            if (!toldYouOnce) log.info("Falling back to defailt resolver due to JVM version");
            toldYouOnce = true;
            return null;
        }
    }

    static private boolean toldYouOnce = false;
    static private Logger log = LoggerFactory.getLogger(JmriLocalEntityResolver.class.getName());

}
 

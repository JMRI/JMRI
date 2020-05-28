package jmri.web.servlet.directory;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override
 * {@link org.eclipse.jetty.util.resource.Resource#getListHTML(java.lang.String, boolean, java.lang.String)}
 * in {@link org.eclipse.jetty.util.resource.Resource} so that directory
 * listings can include the complete JMRI look and feel.
 *
 * @author Randall Wood Copright 2016, 2020
 */
public class DirectoryResource extends PathResource {

    private final Locale locale;

    public DirectoryResource(Locale locale, Resource resource) throws IOException {
        super(resource.getFile());
        this.locale = locale;
    }

    @Override
    public String getListHTML(String base, boolean parent, String query)
            throws IOException {
        String basePath = URIUtil.canonicalPath(base);
        if (basePath == null || !isDirectory()) {
            return null;
        }

        String[] ls = list();
        if (ls == null) {
            return null;
        }
        Arrays.sort(ls);

        String decodedBase = URIUtil.decodePath(basePath);
        String title = Bundle.getMessage(this.locale, "DirectoryTitle", StringUtil.sanitizeXmlString(decodedBase)); // NOI18N

        StringBuilder table = new StringBuilder(4096);
        String row = Bundle.getMessage(this.locale, "TableRow"); // NOI18N
        if (parent) {
            table.append(String.format(this.locale, row,
                    URIUtil.addPaths(basePath, "../"),
                    Bundle.getMessage(this.locale, "ParentDirectory"),
                    "",
                    ""));
        }

        String encodedBase = hrefEncodeURI(basePath);

        DateFormat dfmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, this.locale);
        for (String l : ls) {
            Resource item = addPath(l);
            String itemPath = URIUtil.addPaths(encodedBase, URIUtil.encodePath(l));
            if (item.isDirectory() && !itemPath.endsWith("/")) {
                itemPath += URIUtil.SLASH;
            }
            table.append(String.format(this.locale, row,
                    itemPath,
                    StringUtil.sanitizeXmlString(l),
                    Bundle.getMessage(this.locale, "SizeInBytes", item.length()),
                    dfmt.format(new Date(item.lastModified())))
            );
        }

        return String.format(this.locale,
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(this.locale, "Directory.html"))), // NOI18N
                String.format(this.locale,
                        Bundle.getMessage(this.locale, "HtmlTitle"), // NOI18N
                        InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                        title
                ),
                InstanceManager.getDefault(ServletUtil.class).getNavBar(this.locale, basePath),
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                InstanceManager.getDefault(ServletUtil.class).getFooter(this.locale, basePath),
                title,
                table
        );
    }

    @Override
    public boolean equals(Object other) {
        // spotbugs errors if equals is not overridden, so override and call super
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        // spotbugs errors if equals is present, but not hashCode, so override and call super
        return super.hashCode();
    }

    /*
     * Originally copied from private static method of org.eclipse.jetty.util.resource.Resource
     */
    /**
     * Encode any characters that could break the URI string in an HREF. Such as
     * <a
     * href="/path/to;<script>Window.alert("XSS"+'%20'+"here");</script>">Link</a>
     *
     * The above example would parse incorrectly on various browsers as the "<"
     * or '"' characters would end the href attribute value string prematurely.
     *
     * @param raw the raw text to encode.
     * @return the defanged text.
     */
    private static String hrefEncodeURI(String raw) {
        StringBuilder buf = null;

        loop:
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\'':
                case '"':
                case '<':
                case '>':
                    buf = new StringBuilder(raw.length() << 1);
                    break loop;
                default:
                    log.debug("Unhandled code: {}", c);
                    break;
            }
        }
        if (buf == null) {
            return raw;
        }

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"':
                    buf.append("%22");
                    break;
                case '\'':
                    buf.append("%27");
                    break;
                case '<':
                    buf.append("%3C");
                    break;
                case '>':
                    buf.append("%3E");
                    break;
                default:
                    buf.append(c);
            }
        }

        return buf.toString();
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(DirectoryResource.class);
}

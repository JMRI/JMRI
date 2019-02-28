package jmri.web.servlet.directory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override
 * {@link org.eclipse.jetty.util.resource.Resource#getListHTML(java.lang.String, boolean)}
 * in {@link org.eclipse.jetty.util.resource.Resource} so that directory
 * listings can include the complete JMRI look and feel.
 *
 * @author Randall Wood (C) 2016
 */
public class DirectoryResource extends Resource {

    private final Resource resource;
    private final Locale locale;

    public DirectoryResource(Locale locale, Resource resource) {
        this.resource = resource;
        this.locale = locale;
    }

    @Override
    public String getListHTML(String base, boolean parent)
            throws IOException {
        String resource = URIUtil.canonicalPath(base);
        if (resource == null || !isDirectory()) {
            return null;
        }

        String[] ls = list();
        if (ls == null) {
            return null;
        }
        Arrays.sort(ls);

        String decodedBase = URIUtil.decodePath(resource);
        String title = Bundle.getMessage(this.locale, "DirectoryTitle", deTag(decodedBase)); // NOI18N

        StringBuilder table = new StringBuilder(4096);
        String row = Bundle.getMessage(this.locale, "TableRow"); // NOI18N
        if (parent) {
            table.append(String.format(this.locale, row,
                    URIUtil.addPaths(resource, "../"),
                    Bundle.getMessage(this.locale, "ParentDirectory"),
                    "",
                    ""));
        }

        String encodedBase = hrefEncodeURI(resource);

        DateFormat dfmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, this.locale);
        for (String l : ls) {
            Resource item = addPath(l);
            String path = URIUtil.addPaths(encodedBase, URIUtil.encodePath(l));
            if (item.isDirectory() && !path.endsWith("/")) {
                path += URIUtil.SLASH;
            }
            table.append(String.format(this.locale, row,
                    path,
                    deTag(l),
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
                InstanceManager.getDefault(ServletUtil.class).getNavBar(this.locale, resource),
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                InstanceManager.getDefault(ServletUtil.class).getFooter(this.locale, resource),
                title,
                table
        );
    }

    /*
     * TODO: Do we already have something like this elsewhere in the JMRI code or in a required library?
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
        StringBuffer buf = null;

        loop:
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\'':
                case '"':
                case '<':
                case '>':
                    buf = new StringBuffer(raw.length() << 1);
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
                    continue;
                case '\'':
                    buf.append("%27");
                    continue;
                case '<':
                    buf.append("%3C");
                    continue;
                case '>':
                    buf.append("%3E");
                    continue;
                default:
                    buf.append(c);
            }
        }

        return buf.toString();
    }

    /*
     * TODO: Do we already have something like this elsewhere in the JMRI code or in a required library?
     */
    private static String deTag(String raw) {
        return StringUtil.replace(StringUtil.replace(raw, "<", "&lt;"), ">", "&gt;");
    }

    /*
     * Abstract methods of Resource that must be overridden.
     */
    @Override
    public boolean isContainedIn(Resource rsrc) throws MalformedURLException {
        return this.resource.isContainedIn(rsrc);
    }

    @Override
    public void close() {
        this.resource.close();
    }

    @Override
    public boolean exists() {
        return this.resource.exists();
    }

    @Override
    public boolean isDirectory() {
        return this.resource.isDirectory();
    }

    @Override
    public long lastModified() {
        return this.resource.lastModified();
    }

    @Override
    public long length() {
        return this.resource.length();
    }

    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public URL getURL() {
        return this.resource.getURL();
    }

    @Override
    public File getFile() throws IOException {
        return this.resource.getFile();
    }

    @Override
    public String getName() {
        return this.resource.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.resource.getInputStream();
    }

    @Override
    public boolean delete() throws SecurityException {
        return this.resource.delete();
    }

    @Override
    public boolean renameTo(Resource rsrc) throws SecurityException {
        return this.resource.renameTo(rsrc);
    }

    @Override
    public String[] list() {
        return this.resource.list();
    }

    @Override
    public Resource addPath(String string) throws IOException, MalformedURLException {
        return this.resource.addPath(string);
    }

    @Override
    public ReadableByteChannel getReadableByteChannel() throws IOException {
        return this.resource.getReadableByteChannel();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DirectoryResource.class);
}

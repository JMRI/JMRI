// RosterServlet.java
package jmri.web.servlet.roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.jmris.json.JSON;
import static jmri.jmris.json.JSON.ADDRESS;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.DECODER_FAMILY;
import static jmri.jmris.json.JSON.DECODER_MODEL;
import static jmri.jmris.json.JSON.GROUP;
import static jmri.jmris.json.JSON.ID;
import static jmri.jmris.json.JSON.MFG;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.NUMBER;
import static jmri.jmris.json.JSON.ROAD;
import jmri.jmris.json.JsonUtil;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
import jmri.web.servlet.ServletUtil;
import static jmri.web.servlet.ServletUtil.APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.APPLICATION_XML;
import static jmri.web.servlet.ServletUtil.IMAGE_PNG;
import static jmri.web.servlet.ServletUtil.TEXT_HTML;
import static jmri.web.servlet.ServletUtil.UTF8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide roster data to HTTP clients.
 *
 * Each method of this Servlet responds to a unique URL pattern.
 *
 * @author Randall Wood
 */
/*
 * TODO: Implement an XSLT that respects newlines in comments.
 * TODO: Include decoder defs and CVs in roster entry response.
 *
 */
public class RosterServlet extends HttpServlet {

    private ObjectMapper mapper;

    static Logger log = LoggerFactory.getLogger(RosterServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        this.mapper = new ObjectMapper();
    }

    /**
     * Parse all HTTP GET requests and pass to appropriate method
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo().length() == 1) {
            this.doList(request, response, true);
        } else {
            // split the path after removing the leading /
            String[] pathInfo = request.getPathInfo().substring(1).split("/");
            if (pathInfo[0].equals("list")) {
                this.doList(request, response, false);
            } else {
                this.doEntry(request, response);
            }
        }
    }

    /**
     * Handle POST requests. POST requests are treated as GET requests.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @see #doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    /**
     * List roster entries.
     *
     * Lists roster entries and return an XML document conforming to the JMRI
     * Roster XML schema. This method can be passed multiple filter filter
     * matching the filter in {@link jmri.jmrit.roster.Roster#getEntriesMatchingCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * }. <b>Note:</b> Any given filter can be specified only once.
     *
     * This method responds to the following GET URL patterns: <ul>
     * <li>/roster/</li> <li>/roster/list</li>
     * <li>/roster/list?filter=filter[&filter=filter]</li> </ul>
     *
     * This method responds to POST URLs <code>/roster</code> and
     * <code>/roster/list</code> with a JSON payload for the filter.
     *
     * @param request
     * @param response
     * @param groups
     * @throws ServletException
     * @throws IOException
     */
    protected void doList(HttpServletRequest request, HttpServletResponse response, Boolean groups) throws ServletException, IOException {
        JsonNode data;
        if (request.getContentType() != null && request.getContentType().contains(APPLICATION_JSON)) {
            data = this.mapper.readTree(request.getReader());
            if (!data.path(DATA).isMissingNode()) {
                data = data.path(DATA);
            }
        } else {
            data = this.mapper.createObjectNode();
            for (String filter : request.getParameterMap().keySet()) {
                if (filter.equals(GROUP)) {
                    String group = URLDecoder.decode(request.getParameter(filter), UTF8);
                    if (!group.equals(Roster.ALLENTRIES)) {
                        ((ObjectNode) data).put(GROUP, group);
                    }
                } else if (filter.equals(ID)) {
                    ((ObjectNode) data).put(NAME, URLDecoder.decode(request.getParameter(filter), UTF8));
                } else {
                    ((ObjectNode) data).put(filter, URLDecoder.decode(request.getParameter(filter), UTF8));
                }
            }
        }
        this.doRoster(request, response, data, groups);
    }

    /**
     * Provide the XML representation of a roster entry given its ID.
     *
     * Lists roster entries and return an XML document conforming to the JMRI
     * Roster XML schema. Requests for roster entry images and icons can include
     * width and height specifiers, and always return PNG images.
     *
     * This method responds to the following URL patterns: <ul>
     * <li>/roster/&lt;ID&gt;</li> <li>/roster/entry/&lt;ID&gt;</li>
     * <li>/roster/&lt;ID&gt;/image</li> <li>/roster/&lt;ID&gt;/icon</li></ul>
     * <b>Note:</b> The use of the term <em>entry</em> in URLs is optional.
     *
     * Images and icons can be rescaled using the following parameters:<ul>
     * <li>height</li> <li>maxHeight</li> <li>minHeight</li> <li>width</li>
     * <li>maxWidth</li> <li>minWidth</li></ul>
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doEntry(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] pathInfo = request.getPathInfo().substring(1).split("/");
        String id = pathInfo[0];
        String type = null;
        if (pathInfo[0].equals("entry")) {
            if (pathInfo.length == 1) {
                // path must be /roster/<id> or /roster/entry/<id>
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            id = pathInfo[1];
        }
        if (pathInfo.length > 1 && !pathInfo[pathInfo.length - 1].equals(id)) {
            type = pathInfo[pathInfo.length - 1];
        }
        RosterEntry re = Roster.instance().getEntryForId(id);
        if (re == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find roster entry " + id);
        } else if (type == null || type.equals("entry")) {
            // this should be an entirely different format than the table
            this.doRoster(request, response, this.mapper.createObjectNode().put(ID, id), false);
        } else if (type.equals("image")) {
            this.doImage(request, response, new File(FileUtil.getAbsoluteFilename(re.getImagePath())));
        } else if (type.equals("icon")) {
            this.doImage(request, response, new File(FileUtil.getAbsoluteFilename(re.getIconPath())));
        } else if (type.equals("file")) {
            this.doFile(request, response, new File(Roster.getFileLocation(), "roster" + File.separator + re.getFileName())); // NOI18N
        } else if (type.equals("throttle")) {
            this.doFile(request, response, new File(FileUtil.getUserFilesPath(), "trottle" + File.separator + id + ".xml")); // NOI18N
        } else {
            // don't know what to do
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Generate the XML output specified by {@link #doList(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Boolean)
     * }
     * and {@link #doEntry(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     * }.
     *
     * @param request
     * @param response
     * @param filter
     * @param groups
     * @throws ServletException
     * @throws IOException
     */
    void doRoster(HttpServletRequest request, HttpServletResponse response, JsonNode filter, Boolean groups) throws ServletException, IOException {
        ServletUtil.getHelper().setNonCachingHeaders(response);
        String group = (!filter.path(GROUP).isMissingNode()) ? filter.path(GROUP).asText() : null;
        if (JSON.JSON.equals(request.getParameter("format"))) { // NOI18N
            response.setContentType(APPLICATION_JSON);
            response.getWriter().print(JsonUtil.getRoster(request.getLocale(), filter));
        } else if (("html").equals(request.getParameter("format"))) {
            String row;
            if ("simple".equals(request.getParameter("view"))) {
                row = FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "SimpleTableRow.html")));
            } else {
                row = FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "TableRow.html")));
            }
            StringBuilder builder = new StringBuilder();
            response.setContentType(TEXT_HTML); // NOI18N
            if (Roster.ALLENTRIES.equals(group)) {
                group = null;
            }
            List<RosterEntry> entries = Roster.instance().getEntriesMatchingCriteria(
                    (!filter.path(ROAD).isMissingNode()) ? filter.path(ROAD).asText() : null,
                    (!filter.path(NUMBER).isMissingNode()) ? filter.path(NUMBER).asText() : null,
                    (!filter.path(ADDRESS).isMissingNode()) ? filter.path(ADDRESS).asText() : null,
                    (!filter.path(MFG).isMissingNode()) ? filter.path(MFG).asText() : null,
                    (!filter.path(DECODER_MODEL).isMissingNode()) ? filter.path(DECODER_MODEL).asText() : null,
                    (!filter.path(DECODER_FAMILY).isMissingNode()) ? filter.path(DECODER_FAMILY).asText() : null,
                    (!filter.path(NAME).isMissingNode()) ? filter.path(NAME).asText() : null,
                    group
            );
            for (RosterEntry entry : entries) {
                // NOTE: changing the following order will break JavaScript and HTML code
                builder.append(String.format(request.getLocale(), row,
                        entry.getId(),
                        entry.getRoadName(),
                        entry.getRoadNumber(),
                        entry.getMfg(),
                        entry.getModel(),
                        entry.getOwner(),
                        entry.getDccAddress(),
                        entry.getDecoderModel(),
                        entry.getDecoderFamily(),
                        entry.getDecoderComment(),
                        entry.getComment(),
                        entry.getURL(),
                        entry.getMaxSpeedPCT(),
                        entry.getFileName(),
                        StringUtil.escapeString(entry.getId())
                // get function buttons in a formatting loop
                // get attributes in a formatting loop
                ));
            }
            response.getWriter().print(builder.toString());
        } else {
            response.setContentType(TEXT_HTML); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Roster.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            ServletUtil.getHelper().getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "RosterTitle")
                    ),
                    ServletUtil.getHelper().getNavBar(request.getLocale(), request.getContextPath()),
                    ServletUtil.getHelper().getRailroadName(false),
                    ServletUtil.getHelper().getFooter(request.getLocale(), request.getContextPath()),
                    group
            ));
        }
    }

    /**
     * Process the image for a roster entry image or icon request. This always
     * returns a PNG image.
     *
     * @param request
     * @param response
     * @param file {@link java.io.File} object containing an image
     * @throws ServletException
     * @throws IOException
     */
    void doImage(HttpServletRequest request, HttpServletResponse response, File file) throws ServletException, IOException {
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            // file not found or unreadable
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String fname = file.getName();
        int height = image.getHeight();
        int width = image.getWidth();
        int pWidth = width;
        int pHeight = height;
        if (request.getParameter("maxWidth") != null) {
            pWidth = Integer.parseInt(request.getParameter("maxWidth"));
            if (pWidth < width) {
                width = pWidth;
            }
            log.debug("{} @maxWidth: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        if (request.getParameter("minWidth") != null) {
            pWidth = Integer.parseInt(request.getParameter("minWidth"));
            if (pWidth > width) {
                width = pWidth;
            }
            log.debug("{} @minWidth: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        if (request.getParameter("width") != null) {
            width = Integer.parseInt(request.getParameter("width"));
        }
        if (width != image.getWidth()) {
            height = (int) (height * (1.0 * width / image.getWidth()));
            pHeight = height;
            log.debug("{} @adjusting height: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        if (request.getParameter("maxHeight") != null) {
            pHeight = Integer.parseInt(request.getParameter("maxHeight"));
            if (pHeight < height) {
                height = pHeight;
            }
            log.debug("{} @maxHeight: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        if (request.getParameter("minHeight") != null) {
            pHeight = Integer.parseInt(request.getParameter("minHeight"));
            if (pHeight > height) {
                height = pHeight;
            }
            log.debug("{} @minHeight: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        if (request.getParameter("height") != null) {
            height = Integer.parseInt(request.getParameter("height"));
            log.debug("{} @height: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        if (height != image.getHeight() && width == image.getWidth()) {
            width = (int) (width * (1.0 * height / image.getHeight()));
            log.debug("{} @adjusting width: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        }
        log.debug("{} @responding: width: {}, pWidth: {}, height: {}, pHeight: {}", fname, width, pWidth, height, pHeight);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (height != image.getHeight() || width != image.getWidth()) {
            BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
            g.dispose();
            // ImageIO needs the simple type ("jpeg", "png") instead of the mime type ("image/jpeg", "image/png")
            ImageIO.write(resizedImage, "png", baos);
        } else {
            ImageIO.write(image, "png", baos);
        }
        baos.close();
        response.setContentType(IMAGE_PNG);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLength(baos.size());
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().close();
    }

    private void doFile(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
        log.debug("Getting roster file {}", file.getPath());
        if (file.exists()) {
            if (file.canRead()) {
                response.setContentType(APPLICATION_XML);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength((int) file.length());
                FileInputStream fileInputStream = new FileInputStream(file);
                int bytes;
                while ((bytes = fileInputStream.read()) != -1) {
                    response.getOutputStream().write(bytes);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

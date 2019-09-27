package jmri.web.servlet.roster;

import static jmri.server.json.JSON.ADDRESS;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.DECODER_FAMILY;
import static jmri.server.json.JSON.DECODER_MODEL;
import static jmri.server.json.JSON.FORMAT;
import static jmri.server.json.JSON.GROUP;
import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.LIST;
import static jmri.server.json.JSON.MFG;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.NUMBER;
import static jmri.server.json.JSON.ROAD;
import static jmri.web.servlet.ServletUtil.IMAGE_PNG;
import static jmri.web.servlet.ServletUtil.UTF8;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_XML;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.roster.JsonRosterServiceFactory;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide roster data to HTTP clients.
 * <p>
 * Each method of this Servlet responds to a unique URL pattern.
 *
 * @author Randall Wood
 */
/*
 * TODO: Implement an XSLT that respects newlines in comments.
 * TODO: Include decoder defs and CVs in roster entry response.
 *
 */
@MultipartConfig
@WebServlet(name = "RosterServlet",
        urlPatterns = {
            "/roster", // default
            "/prefs/roster.xml", // redirect to /roster?format=xml since ~ 9 Apr 2012
        })
@ServiceProvider(service = HttpServlet.class)
public class RosterServlet extends HttpServlet {

    private transient ObjectMapper mapper;

    private final static Logger log = LoggerFactory.getLogger(RosterServlet.class);

    @Override
    public void init() throws ServletException {
        if (this.getServletContext().getContextPath().equals("/roster")) { // NOI18N
            this.mapper = new ObjectMapper();
        }
    }

    /**
     * Route the request and response to the appropriate methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws java.io.IOException if communications is cut with client
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().startsWith("/prefs/roster.xml")) { // NOI18N
            response.sendRedirect("/roster?format=xml"); // NOI18N
            return;
        }
        if (request.getPathInfo().length() == 1) {
            this.doList(request, response);
        } else {
            // split the path after removing the leading /
            String[] pathInfo = request.getPathInfo().substring(1).split("/"); // NOI18N
            switch (pathInfo[0]) {
                case LIST:
                    this.doList(request, response);
                    break;
                case GROUP:
                    if (pathInfo.length == 2) {
                        this.doGroup(request, response, pathInfo[1]);
                    } else {
                        this.doList(request, response);
                    }
                    break;
                default:
                    this.doEntry(request, response);
                    break;
            }
        }
    }

    /**
     * Handle any POST request as an upload of a roster file from client.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if unable to process uploads
     * @throws java.io.IOException            if communications is cut with
     *                                        client
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        OutputStream out = null;
        InputStream fileContent = null;
        File rosterFolder = new File(Roster.getDefault().getRosterLocation(), "roster");
        if (!rosterFolder.exists()) { //insure roster folder exists
            if (rosterFolder.mkdir()) {
                log.debug("Roster folder not found, created '{}'", rosterFolder.getPath());
            } else {
                log.error("Could not create roster directory: '{}'", rosterFolder.getPath());
            }
        }
        File tempFolder = new File(System.getProperty("java.io.tmpdir"));
        Locale rl = request.getLocale();

        //get the uploaded file(s)
        List<FileMeta> files = MultipartRequestHandler.uploadByJavaServletAPI(request);

        List<String> msgList = new ArrayList<>();

        //loop thru files returned and validate and (if ok) save each
        for (FileMeta fm : files) {
            log.debug("processing uploaded '{}' file '{}' ({}), group='{}', roster='{}', temp='{}'", fm.getFileType(), fm.getFileName(),
                    fm.getFileSize(), fm.getRosterGroup(), rosterFolder, tempFolder);

            //only allow xml files or image files
            if (!fm.getFileType().equals("text/xml")
                    && !fm.getFileType().startsWith("image")) {
                String m = String.format(rl, Bundle.getMessage(rl, "ErrorInvalidFileType"), fm.getFileName(), fm.getFileType());
                log.error(m);
                msgList.add(m);
                break; //stop processing this one
            }
            //save received file to temporary folder
            File fileTemp = new File(tempFolder, fm.getFileName());
            try {
                out = new FileOutputStream(fileTemp);
                fileContent = fm.getContent();
                int read;
                final byte[] bytes = new byte[1024];
                while ((read = fileContent.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                log.debug("file '{}' of type '{}' temp saved to {}", fm.getFileType(), fm.getFileName(), tempFolder);
            } catch (IOException e) {
                String m = String.format(rl, Bundle.getMessage(rl, "ErrorSavingFile"), fm.getFileName());
                log.error(m);
                msgList.add(m);
                break; //stop processing this one
            } finally {
                if (out != null) {
                    out.close();
                }
                if (fileContent != null) {
                    fileContent.close();
                }
            } //finally

            //reference to target file name and location
            File fileNew = new File(rosterFolder, fm.getFileName());

            //save image file, replacing if parm is set that way. return appropriate message
            if (fm.getFileType().startsWith("image")) {
                if (fileNew.exists()) {
                    if (!fm.getFileReplace()) {
                        String m = String.format(rl, Bundle.getMessage(rl, "ErrorFileExists"), fm.getFileName());
                        log.error(m);
                        msgList.add(m);
                        if (!fileTemp.delete()) { //get rid of temp file
                            log.error("Unable to delete {}", fileTemp);
                        }
                    } else {
                        if (!fileNew.delete()) { //delete the old file
                            String m = String.format(rl, Bundle.getMessage(rl, "ErrorDeletingFile"), fileNew.getName());
                            log.debug(m);
                            msgList.add(m);
                        }
                        if (fileTemp.renameTo(fileNew)) {
                            String m = String.format(rl, Bundle.getMessage(rl, "FileReplaced"), fm.getFileName());
                            log.debug(m);
                            msgList.add(m);
                        } else {
                            String m = String.format(rl, Bundle.getMessage(rl, "ErrorRenameFailed"), fm.getFileName());
                            log.error(m);
                            msgList.add(m);
                            if (!fileTemp.delete()) { //get rid of temp file
                                log.error("Unable to delete {}", fileTemp);
                            }
                        }
                    }
                } else {
                    if (fileTemp.renameTo(fileNew)) {
                        String m = String.format(rl, Bundle.getMessage(rl, "FileAdded"), fm.getFileName());
                        log.debug(m);
                        msgList.add(m);
                    } else {
                        String m = String.format(rl, Bundle.getMessage(rl, "ErrorRenameFailed"), fm.getFileName());
                        log.error(m);
                        msgList.add(m);
                        if (!fileTemp.delete()) { //get rid of temp file
                            log.error("Unable to delete {}", fileTemp);
                        }
                    }

                }
            } else {
                RosterEntry reTemp; // create a temp rosterentry to check, based on uploaded file
                try {
                    reTemp = RosterEntry.fromFile(new File(tempFolder, fm.getFileName()));
                } catch (JDOMException e) { //handle XML failures
                    String m = String.format(rl, Bundle.getMessage(rl, "ErrorInvalidXML"), fm.getFileName(), e.getMessage());
                    log.error(m);
                    msgList.add(m);
                    if (!fileTemp.delete()) { //get rid of temp file
                        log.error("Unable to delete {}", fileTemp);
                    }
                    break;
                }
                RosterEntry reOld = Roster.getDefault().getEntryForId(reTemp.getId()); //get existing entry if found
                if (reOld != null) {
                    if (!fm.getFileReplace()) {
                        String m = String.format(rl, Bundle.getMessage(rl, "ErrorFileExists"), fm.getFileName());
                        log.error(m);
                        msgList.add(m);
                        if (!fileTemp.delete()) { //get rid of temp file
                            log.error("Unable to delete {}", fileTemp);
                        }
                    } else { //replace specified
                        Roster.getDefault().removeEntry(reOld); //remove the old entry from roster
                        reTemp.updateFile(); //saves XML file to roster folder and makes backup
                        Roster.getDefault().addEntry(reTemp); //add the new entry to roster
                        Roster.getDefault().writeRoster(); //save modified roster.xml file
                        String m = String.format(rl, Bundle.getMessage(rl, "RosterEntryReplaced"), fm.getFileName(), reTemp.getDisplayName());
                        log.debug(m);
                        msgList.add(m);
                        if (!fileTemp.delete()) { //get rid of temp file
                            log.error("Unable to delete {}", fileTemp);
                        }
                    }
                } else {
                    if (fileTemp.renameTo(fileNew)) { //move the file to proper roster location
                        Roster.getDefault().addEntry(reTemp);
                        Roster.getDefault().writeRoster();
                        String m = String.format(rl, Bundle.getMessage(rl, "RosterEntryAdded"), fm.getFileName(), reTemp.getId());
                        log.debug(m);
                        msgList.add(m);
                    } else {
                        String m = String.format(rl, Bundle.getMessage(rl, "ErrorMoveFailed"), fm.getFileName(), reTemp.getPathName());
                        log.error(m);
                        msgList.add(m);                        
                    }
                }

            }

        } //for FileMeta

        //respond with a json list of messages from the upload attempts
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), msgList);
    }

    /**
     * Get a roster group.
     * <p>
     * Lists roster entries in the specified group and return an XML document
     * conforming to the JMRI JSON schema. This method can be passed multiple
     * filters matching the filter in {@link jmri.jmrit.roster.Roster#getEntriesMatchingCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * }. <b>Note:</b> Any given filter can be specified only once.
     * <p>
     * This method responds to the following GET URL patterns: <ul>
     * <li>{@code/roster/group/<group name>}</li>
     * <li>{@code/roster/group/<group name>?filter=filter[&filter=filter]}</li>
     * </ul>
     * <p>
     * This method responds to the POST URL {@code/roster/group/<group name>}
     * with a JSON payload for the filter.
     *
     * @param request  servlet request
     * @param response servlet response
     * @param group    The group name
     * @throws java.io.IOException if communications is cut with client
     */
    protected void doGroup(HttpServletRequest request, HttpServletResponse response, String group) throws IOException {
        log.debug("Getting group {}", group);
        ObjectNode data;
        if (request.getContentType() != null && request.getContentType().contains(UTF8_APPLICATION_JSON)) {
            data = (ObjectNode) this.mapper.readTree(request.getReader());
            if (data.path(DATA).isObject()) {
                data = (ObjectNode) data.path(DATA);
            }
        } else {
            data = this.mapper.createObjectNode();
            for (String filter : request.getParameterMap().keySet()) {
                if (filter.equals(ID)) {
                    data.put(NAME, URLDecoder.decode(request.getParameter(filter), UTF8));
                } else {
                    data.put(filter, URLDecoder.decode(request.getParameter(filter), UTF8));
                }
            }
        }
        data.put(GROUP, URLDecoder.decode(group, UTF8));
        log.debug("Getting roster with {}", data);
        this.doRoster(request, response, data);
    }

    /**
     * List roster entries.
     * <p>
     * Lists roster entries and return an XML document conforming to the JMRI
     * Roster XML schema. This method can be passed multiple filter filter
     * matching the filter in
     * {@link jmri.jmrit.roster.Roster#getEntriesMatchingCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     * <b>Note:</b> Any given filter can be specified only once.
     * <p>
     * This method responds to the following GET URL patterns: <ul>
     * <li>{@code/roster/}</li> <li>{@code/roster/list}</li>
     * <li>{@code/roster/list?filter=filter[&filter=filter]}</li> </ul>
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws java.io.IOException if communications is cut with client
     */
    protected void doList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectNode data;
        if (request.getContentType() != null && request.getContentType().contains(UTF8_APPLICATION_JSON)) {
            data = (ObjectNode) this.mapper.readTree(request.getReader());
            if (data.path(DATA).isObject()) {
                data = (ObjectNode) data.path(DATA);
            }
        } else {
            data = this.mapper.createObjectNode();
            for (String filter : request.getParameterMap().keySet()) {
                switch (filter) {
                    case GROUP:
                        String group = URLDecoder.decode(request.getParameter(filter), UTF8);
                        if (!group.equals(Roster.allEntries(request.getLocale()))) {
                            data.put(GROUP, group);
                        }
                        break;
                    case ID:
                        data.put(NAME, URLDecoder.decode(request.getParameter(filter), UTF8));
                        break;
                    default:
                        data.put(filter, URLDecoder.decode(request.getParameter(filter), UTF8));
                        break;
                }
            }
        }
        this.doRoster(request, response, data);
    }

    /**
     * Provide the XML representation of a roster entry given its ID.
     * <p>
     * Lists roster entries and return an XML document conforming to the JMRI
     * Roster XML schema. Requests for roster entry images and icons can include
     * width and height specifiers, and always return PNG images.
     * <p>
     * This method responds to the following URL patterns: <ul>
     * <li>{@code/roster/<ID>}</li> <li>{@code/roster/entry/<ID>}</li>
     * <li>{@code/roster/<ID>/image}</li> <li>{@code/roster/<ID>/icon}</li></ul>
     * <b>Note:</b> The use of the term <em>entry</em> in URLs is optional.
     * <p>
     * Images and icons can be rescaled using the following parameters:<ul>
     * <li>height</li> <li>maxHeight</li> <li>minHeight</li> <li>width</li>
     * <li>maxWidth</li> <li>minWidth</li></ul>
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws java.io.IOException if communications is cut with client
     */
    protected void doEntry(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] pathInfo = request.getRequestURI().substring(1).split("/");
        int idOffset = 1;
        String type = null;
        if (pathInfo[1].equals("entry")) {
            if (pathInfo.length == 2) {
                // path must be /roster/<id> or /roster/entry/<id>
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            idOffset = 2;
        }
        String id = URLDecoder.decode(pathInfo[idOffset], UTF8);
        if (pathInfo.length > (1 + idOffset)) {
            type = pathInfo[pathInfo.length - 1];
        }
        RosterEntry re = Roster.getDefault().getEntryForId(id);
        try {
            if (re == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find roster entry " + id);
            } else if (type == null || type.equals("entry")) {
                // this should be an entirely different format than the table
                this.doRoster(request, response, this.mapper.createObjectNode().put(ID, id));
            } else if (type.equals(JSON.IMAGE)) {
                if (re.getImagePath() != null) {
                    this.doImage(request, response, FileUtil.getFile(re.getImagePath()));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (type.equals(JSON.ICON)) {
                int function = -1;
                if (pathInfo.length != (2 + idOffset)) {
                    function = Integer.parseInt(pathInfo[pathInfo.length - 2].substring(1));
                }
                if (function == -1) {
                    if (re.getIconPath() != null) {
                        this.doImage(request, response, FileUtil.getFile(re.getIconPath()));
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else if (re.getFunctionImage(function) != null) {
                    this.doImage(request, response, FileUtil.getFile(re.getFunctionImage(function)));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (type.equals(JSON.SELECTED_ICON)) {
                if (pathInfo.length != (2 + idOffset)) {
                    int function = Integer.parseInt(pathInfo[pathInfo.length - 2].substring(1));
                    this.doImage(request, response, FileUtil.getFile(re.getFunctionSelectedImage(function)));
                }
            } else if (type.equals("file")) {
                InstanceManager.getDefault(ServletUtil.class).writeFile(response, new File(Roster.getDefault().getRosterLocation(), "roster" + File.separator + re.getFileName()), ServletUtil.UTF8_APPLICATION_XML); // NOI18N
            } else if (type.equals("throttle")) {
                InstanceManager.getDefault(ServletUtil.class).writeFile(response, new File(FileUtil.getUserFilesPath(), "throttle" + File.separator + id + ".xml"), ServletUtil.UTF8_APPLICATION_XML); // NOI18N
            } else {
                // don't know what to do
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NullPointerException ex) {
            // triggered by instanciating a File with null path
            // this would happen when an image or icon is requested for a
            // rosterEntry that has no such image or icon associated with it
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Generate the JSON, XML, or HTML output specified by {@link #doList(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)},
     * {@link #doGroup(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)},
     * or
     * {@link #doEntry(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     *
     * @param request  servlet request with format and locale for response
     * @param response servlet response
     * @param filter   a JSON object with name-value pairs of parameters for
     *                 {@link jmri.jmrit.roster.Roster#getEntriesMatchingCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     * @throws java.io.IOException if communications is cut with client
     */
    protected void doRoster(HttpServletRequest request, HttpServletResponse response, JsonNode filter) throws IOException {
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
        log.debug("Getting roster with filter {}", filter);
        String group = (!filter.path(GROUP).isMissingNode()) ? filter.path(GROUP).asText() : null;
        log.debug("Group {} was in filter", group);

        String format = request.getParameter(FORMAT);
        if (format == null) {
            format = "";
        }
        switch (format) {
            case JSON.JSON:
                response.setContentType(UTF8_APPLICATION_JSON);
                JsonRosterServiceFactory factory = InstanceManager.getOptionalDefault(JsonRosterServiceFactory.class).orElseGet(() -> {
                    return InstanceManager.setDefault(JsonRosterServiceFactory.class, new JsonRosterServiceFactory());
                });
                try {
                    response.getWriter().print(factory.getHttpService(mapper).getRoster(request.getLocale(), filter, 0));
                } catch (JsonException ex) {
                    response.sendError(ex.getCode(), mapper.writeValueAsString(ex.getJsonMessage()));
                }
                break;
            case JSON.XML:
                response.setContentType(UTF8_APPLICATION_XML);
                File roster = new File(Roster.getDefault().getRosterIndexPath());
                if (roster.exists()) {
                    response.getWriter().print(FileUtil.readFile(roster));
                }
                break;
            case "html":
                String row;
                if ("simple".equals(request.getParameter("view"))) {
                    row = FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "SimpleTableRow.html")));
                } else {
                    row = FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "TableRow.html")));
                }
                StringBuilder builder = new StringBuilder();
                response.setContentType(UTF8_TEXT_HTML); // NOI18N
                if (Roster.allEntries(request.getLocale()).equals(group)) {
                    group = null;
                }
                List<RosterEntry> entries = Roster.getDefault().getEntriesMatchingCriteria(
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
                    try {
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
                                URLEncoder.encode(entry.getId(), UTF8)
                        // get function buttons in a formatting loop
                        // get attributes in a formatting loop
                        ));
                    } catch (UnsupportedEncodingException ex) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to encode entry Id in UTF-8."); // NOI18N
                    }
                }
                response.getWriter().print(builder.toString());
                break;
            default:
                if (group == null) {
                    group = Roster.allEntries(request.getLocale());
                }
                response.setContentType(UTF8_TEXT_HTML); // NOI18N
                response.getWriter().print(String.format(request.getLocale(),
                        FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Roster.html"))),
                        String.format(request.getLocale(),
                                Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                                Bundle.getMessage(request.getLocale(), "RosterTitle")
                        ),
                        InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), request.getContextPath()),
                        InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                        InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), request.getContextPath()),
                        group
                ));
                break;
        }
    }

    /**
     * Process the image for a roster entry image or icon request.
     *
     * @param file     {@link java.io.File} object containing an image
     * @param request  contains parameters for drawing the image
     * @param response sends a PNG image or a 403 Not Found error.
     * @throws java.io.IOException if communications is cut with client
     */
    void doImage(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
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
}

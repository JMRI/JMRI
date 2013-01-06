// RosterServlet.java
package jmri.web.servlet.roster;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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

    static Logger log = Logger.getLogger(RosterServlet.class.getName());

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
            } else if (pathInfo[0].equals("groups")) {
                this.doGroups(request, response);
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
     * This method responds to the following URL patterns: <ul>
     * <li>/roster/</li> <li>/roster/list</li>
     * <li>/roster/list?filter=filter[&filter=filter]</li> </ul>
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doList(HttpServletRequest request, HttpServletResponse response, Boolean groups) throws ServletException, IOException {
        String group = null;
        String roadName = null;
        String roadNumber = null;
        String dccAddress = null;
        String mfg = null;
        String decoderMfgID = null;
        String decoderVersionID = null;
        String id = null;
        String criteria = "";
        for (String filter : request.getParameterMap().keySet()) {
            if (filter.equals("group")) {
                group = URLDecoder.decode(request.getParameter(filter), "UTF-8");
                if (!group.equals(Roster.ALLENTRIES)) {
                    criteria += ", Group: " + group;
                }
            } else if (filter.equals("roadName")) {
                roadName = URLDecoder.decode(request.getParameter(filter), "UTF-8");
                criteria += ", RoadName: " + roadName;
            } else if (filter.equals("roadNumber")) {
                roadNumber = request.getParameter(filter);
                criteria += ", RoadNumber: " + roadNumber;
            } else if (filter.equals("dccAddress")) {
                dccAddress = request.getParameter(filter);
                criteria += ", DCC Address: " + dccAddress;
            } else if (filter.equals("mfg")) {
                mfg = URLDecoder.decode(request.getParameter(filter), "UTF-8");
                criteria += ", Manufacturer: " + mfg;
            } else if (filter.equals("decoderMfgID")) {
                decoderMfgID = request.getParameter(filter);
                criteria += ", Decoder Manufacturer Id: " + decoderMfgID;
            } else if (filter.equals("decoderVersionID")) {
                decoderVersionID = request.getParameter(filter);
                criteria += ", Decoder Version Id: " + decoderVersionID;
            } else if (filter.equals("id")) {
                id = request.getParameter(filter);
                criteria += ", Id: " + id;
            }
        }
        if (criteria.startsWith(", ")) {
            criteria = criteria.substring(2);
        }
        List<RosterEntry> list = Roster.instance().getEntriesMatchingCriteria(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id, group);
        this.doRoster(request, response, list, (criteria.isEmpty() ? null : criteria), groups);
    }

    /**
     * Placeholder to list groups. Currently returns a HTTP 501 NOT IMPLEMENTED
     * status message.
     *
     * This method responds to the following URL patterns: <ul>
     * <li>/roster/groups</li> </ul>
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGroups(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRoster(request, response, null, null, true);
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
            List<RosterEntry> list = new ArrayList<RosterEntry>();
            list.add(re);
            this.doRoster(request, response, list, "Entry " + id, false);
        } else if (type.equals("image")) {
            this.doImage(request, response, new File(FileUtil.getAbsoluteFilename(re.getImagePath())));
        } else if (type.equals("icon")) {
            this.doImage(request, response, new File(FileUtil.getAbsoluteFilename(re.getIconPath())));
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
     * @param list A list of {@link jmri.jmrit.roster.RosterEntry} objects
     * @throws ServletException
     * @throws IOException
     */
    void doRoster(HttpServletRequest request, HttpServletResponse response, List<RosterEntry> list, String filter, Boolean groups) throws ServletException, IOException {
        XMLOutputter fmt = new XMLOutputter(Format.getPrettyFormat());
        Element root = new Element("roster-config");
        root.setAttribute("noNamespaceSchemaLocation",
                "http://jmri.org/xml/schema/roster" + Roster.schemaVersion + ".xsd",
                Namespace.getNamespace("xsi",
                "http://www.w3.org/2001/XMLSchema-instance"));
        Document doc = XmlFile.newDocument(root);
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl");
        if (request.getParameter("simple") != null && request.getParameter("simple").equals("yes")) {
            m.put("href", Roster.xsltLocation + "roster4web-simple.xsl");
        } else {
            m.put("href", Roster.xsltLocation + "roster4web.xsl");
        }
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);
        if (list != null) {
            Element values = new Element("roster");
            if (filter != null) {
                values.setAttribute("filter", filter);
            }
            for (RosterEntry re : list) {
                values.addContent(re.store());
            }
            root.addContent(values);
        }
        if (groups) {
            Element rosterGroup = new Element("rosterGroup");
            rosterGroup.addContent(new Element("group").addContent(Roster.ALLENTRIES));
            if (!Roster.instance().getRosterGroupList().isEmpty()) {
                for (String group : Roster.instance().getRosterGroupList()) {
                    rosterGroup.addContent(new Element("group").addContent(group));
                }
                root.addContent(rosterGroup);
            }
        }
        response.setContentType("text/xml");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(fmt.outputString(doc));
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
        } catch (Exception ex) {
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
            if (log.isDebugEnabled()) {
                log.debug(fname + " @maxWidth: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (request.getParameter("minWidth") != null) {
            pWidth = Integer.parseInt(request.getParameter("minWidth"));
            if (pWidth > width) {
                width = pWidth;
            }
            if (log.isDebugEnabled()) {
                log.debug(fname + " @minWidth: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (request.getParameter("width") != null) {
            width = Integer.parseInt(request.getParameter("width"));
        }
        if (width != image.getWidth()) {
            height = (int) (height * (1.0 * width / image.getWidth()));
            pHeight = height;
            if (log.isDebugEnabled()) {
                log.debug(fname + " @adjusting height: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (request.getParameter("maxHeight") != null) {
            pHeight = Integer.parseInt(request.getParameter("maxHeight"));
            if (pHeight < height) {
                height = pHeight;
            }
            if (log.isDebugEnabled()) {
                log.debug(fname + " @maxHeight: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (request.getParameter("minHeight") != null) {
            pHeight = Integer.parseInt(request.getParameter("minHeight"));
            if (pHeight > height) {
                height = pHeight;
            }
            if (log.isDebugEnabled()) {
                log.debug(fname + " @minHeight: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (request.getParameter("height") != null) {
            height = Integer.parseInt(request.getParameter("height"));
            if (log.isDebugEnabled()) {
                log.debug(fname + " @height: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (height != image.getHeight() && width == image.getWidth()) {
            width = (int) (width * (1.0 * height / image.getHeight()));
            if (log.isDebugEnabled()) {
                log.debug(fname + " @adjusting width: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(fname + " @responding: width: " + width + ", pWidth: " + pWidth + ", height: " + height + ", pHeight: " + pHeight);
        }
        response.setContentType("image/png");
        response.setStatus(HttpServletResponse.SC_OK);
        if (height != image.getHeight() || width != image.getWidth()) {
            BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
            g.dispose();
            // ImageIO needs the simple type ("jpeg", "png") instead of the mime type ("image/jpeg", "image/png")
            ImageIO.write(resizedImage, "png", response.getOutputStream());
        } else {
            ImageIO.write(image, "png", response.getOutputStream());
        }
    }
}

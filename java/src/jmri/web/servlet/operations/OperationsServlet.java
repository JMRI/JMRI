package jmri.web.servlet.operations;

import static jmri.web.servlet.ServletUtil.APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.JsonManifest;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JSON;
import jmri.server.json.operations.JsonOperations;
import jmri.server.json.operations.JsonUtil;
import jmri.util.FileUtil;
import jmri.web.server.WebServer;
import jmri.web.servlet.ServletUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2014
 * @author Steve Todd (C) 2013
 */
@WebServlet(name = "OperationsServlet",
        urlPatterns = {
            "/operations", // default
            "/web/operationsConductor.html", // redirect to default since ~ 13 May 2014
            "/web/operationsManifest.html", // redirect to default since ~ 13 May 2014
            "/web/operationsTrains.html" // redirect to default since ~ 13 May 2014
        })
@ServiceProvider(service = HttpServlet.class)
public class OperationsServlet extends HttpServlet {

    private ObjectMapper mapper;

    private final static Logger log = LoggerFactory.getLogger(OperationsServlet.class);

    @Override
    public void init() throws ServletException {
        // only do complete initialization for default path, not redirections
        if (this.getServletContext().getContextPath().equals("/operations")) { // NOI18N
            this.mapper = new ObjectMapper();
            // ensure all operations managers are functional before handling first request
            InstanceManager.getDefault(OperationsManager.class);
        }
    }

    /*
     * Valid paths are:
     * /operations/trains -or- /operations - get a list of trains for operations
     * /operations/manifest/id - get the manifest for train with Id "id"
     * /operations/conductor/id - get the conductor's screen for train with Id "id"
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().equals("/web/operationsConductor.html") // NOI18N
                || request.getRequestURI().equals("/web/operationsManifest.html") // NOI18N
                || request.getRequestURI().equals("/web/operationsTrains.html")) { // NOI18N
            response.sendRedirect("/operations"); // NOI18N
            return;
        }
        String[] pathInfo = request.getPathInfo().substring(1).split("/");
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        if (pathInfo[0].equals("") || (pathInfo[0].equals(JsonOperations.TRAINS) && pathInfo.length == 1)) {
            this.processTrains(request, response);
        } else {
            if (pathInfo.length == 1) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String id = pathInfo[1];
                String report = pathInfo[0];
                if (report.equals(JsonOperations.TRAINS) && pathInfo.length == 3) {
                    report = pathInfo[2];
                }
                log.debug("Handling {} with id {}", report, id);
                switch (report) {
                    case "manifest":
                        this.processManifest(id, request, response);
                        break;
                    case "conductor":
                        this.processConductor(id, request, response);
                        break;
                    case "trains":
                        // TODO: allow for editing/building/reseting train
                        log.warn("Unhandled request for \"trains\"");
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        break;
                    default:
                        // Don't know what to do
                        log.warn("Unparsed request for \"{}\"", report);
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        break;
                }
            }
        }
    }

    protected void processTrains(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (JSON.JSON.equals(request.getParameter("format"))) {
            response.setContentType(UTF8_APPLICATION_JSON);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            JsonUtil utilities = new JsonUtil(this.mapper);
            response.getWriter().print(utilities.getTrains(request.getLocale()));
        } else if ("html".equals(request.getParameter("format"))) {
            response.setContentType(UTF8_TEXT_HTML);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            boolean showAll = ("all".equals(request.getParameter("show")));
            StringBuilder html = new StringBuilder();
            String format = FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "TrainsSnippet.html")));
            for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
                if (showAll || !InstanceManager.getDefault(CarManager.class).getByTrainDestinationList(train).isEmpty()) {
                    html.append(String.format(request.getLocale(), format,
                            train.getIconName(),
                            StringEscapeUtils.escapeHtml4(train.getDescription()),
                            train.getLeadEngine() != null ? train.getLeadEngine().toString() : "",
                            StringEscapeUtils.escapeHtml4(train.getTrainDepartsName()),
                            train.getDepartureTime(),
                            train.getStatus(),
                            StringEscapeUtils.escapeHtml4(train.getCurrentLocationName()),
                            StringEscapeUtils.escapeHtml4(train.getTrainTerminatesName()),
                            StringEscapeUtils.escapeHtml4(train.getTrainRouteName()),
                            train.getId()
                    ));
                }
            }
            response.getWriter().print(html.toString());
        } else {
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Operations.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "TrainsTitle")
                    ),
                    InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), request.getContextPath()),
                    InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                    InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), request.getContextPath()),
                    "" // no train Id
            ));
        }
    }

    private void processManifest(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
        if ("html".equals(request.getParameter("format"))) {
            log.debug("Getting manifest HTML code for train {}", id);
            HtmlManifest manifest = new HtmlManifest(request.getLocale(), train);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "ManifestSnippet.html"))),
                    train.getIconName(),
                    StringEscapeUtils.escapeHtml4(train.getDescription()),
                    Setup.isPrintValidEnabled() ? manifest.getValidity() : "",
                    StringEscapeUtils.escapeHtml4(train.getComment()),
                    Setup.isPrintRouteCommentsEnabled() ? train.getRoute().getComment() : "",
                    manifest.getLocations()
            ));
        } else if (JSON.JSON.equals(request.getParameter("format"))) {
            log.debug("Getting manifest JSON code for train {}", id);
            JsonNode manifest = this.mapper.readTree(new JsonManifest(train).getFile());
            if (manifest.path(JSON.IMAGE).isTextual()) {
                ((ObjectNode) manifest).put(JSON.IMAGE, WebServer.URIforPortablePath(FileUtil.getPortableFilename(manifest.path(JSON.IMAGE).asText())));
            }
            String content = this.mapper.writeValueAsString(manifest);
            response.setContentType(ServletUtil.UTF8_APPLICATION_JSON);
            response.setContentLength(content.getBytes(UTF8).length);
            response.getWriter().print(content);
        } else {
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Operations.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                            String.format(request.getLocale(),
                                    Bundle.getMessage(request.getLocale(), "ManifestTitle"),
                                    train.getIconName(),
                                    StringEscapeUtils.escapeHtml4(train.getDescription())
                            )
                    ),
                    InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), request.getContextPath()),
                    !train.getRailroadName().equals("") ? train.getRailroadName() : InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                    InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), request.getContextPath()),
                    train.getId()
            ));
        }
    }

    private void processConductor(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
        JsonNode data;
        if (request.getContentType() != null && request.getContentType().contains(APPLICATION_JSON)) {
            data = this.mapper.readTree(request.getReader());
            if (!data.path(JSON.DATA).isMissingNode()) {
                data = data.path(JSON.DATA);
            }
        } else {
            data = this.mapper.createObjectNode();
            ((ObjectNode) data).put("format", request.getParameter("format"));
        }
        if (data.path("format").asText().equals("html")) {
            JsonNode location = data.path(JsonOperations.LOCATION);
            if (!location.isMissingNode()) {
                if (location.isNull() || train.getNextLocationName().equals(location.asText())) {
                    train.move();
                    return; // done; property change will cause update to client
                }
            }
            log.debug("Getting conductor HTML code for train {}", id);
            HtmlConductor conductor = new HtmlConductor(request.getLocale(), train);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(conductor.getLocation());
        } else {
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Operations.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                            String.format(request.getLocale(),
                                    Bundle.getMessage(request.getLocale(), "ConductorTitle"),
                                    train.getIconName(),
                                    StringEscapeUtils.escapeHtml4(train.getDescription())
                            )
                    ),
                    InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), request.getContextPath()),
                    !train.getRailroadName().equals("") ? train.getRailroadName() : InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                    InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), request.getContextPath()),
                    train.getId()
            ));
        }
    }
// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Operations Servlet";
    }// </editor-fold>

}

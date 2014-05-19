package jmri.web.servlet.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.jmris.json.JSON;
import static jmri.jmris.json.JSON.CODE;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.LOCATION;
import static jmri.jmris.json.JSON.NULL;
import jmri.jmris.json.JsonException;
import jmri.jmris.json.JsonUtil;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.JsonManifest;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.FileUtil;
import jmri.web.server.WebServer;
import jmri.web.servlet.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2014
 * @author Steve Todd (C) 2013
 */
public class OperationsServlet extends HttpServlet {

    private ObjectMapper mapper;

    private final static Logger log = LoggerFactory.getLogger(OperationsServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        this.mapper = new ObjectMapper();
        // ensure all operations managers are functional before receiving first request
        OperationsManager.getInstance();
    }

    /*
     * Valid paths are:
     * /operations/trains -or- /operations - get a list of trains for operations
     * /operations/manifest/id - get the manifest for train with Id "id"
     * /operations/conductor/id - get the conductor's screen for train with Id "id"
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] pathInfo = request.getPathInfo().substring(1).split("/");
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        if (pathInfo[0].equals("") || (pathInfo[0].equals(JSON.TRAINS) && pathInfo.length == 1)) {
            this.processTrains(request, response);
        } else {
            if (pathInfo.length == 1) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String id = pathInfo[1];
                String report = pathInfo[0];
                if (report.equals(JSON.TRAINS) && pathInfo.length == 3) {
                    report = pathInfo[2];
                }
                log.debug("Handling {} with id {}", report, id);
                if (report.equals("manifest")) {
                    this.processManifest(id, request, response);
                } else if (report.equals("conductor")) {
                    this.processConductor(id, request, response);
                } else if (report.equals("trains")) {
                    // TODO: allow for editing/building/reseting train
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    // Don't know what to do
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }

    protected void processTrains(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (JSON.JSON.equals(request.getParameter("format"))) {
            response.setContentType("application/json"); // NOI18N
            ServletUtil.getInstance().setNonCachingHeaders(response);
            try {
                response.getWriter().print(JsonUtil.getTrains(request.getLocale()));
            } catch (JsonException ex) {
                int code = ex.getJsonMessage().path(DATA).path(CODE).asInt(200);
                response.sendError(code, (new ObjectMapper()).writeValueAsString(ex.getJsonMessage()));
            }
        } else if ("html".equals(request.getParameter("format"))) {
            response.setContentType("text/html"); // NOI18N
            ServletUtil.getInstance().setNonCachingHeaders(response);
            boolean showAll = ("all".equals(request.getParameter("show")));
            StringBuilder html = new StringBuilder();
            String format = FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "TrainsSnippet.html")));
            for (Train train : TrainManager.instance().getTrainsByNameList()) {
                if (showAll || !CarManager.instance().getByTrainDestinationList(train).isEmpty()) {
                    html.append(String.format(request.getLocale(), format,
                            train.getIconName(),
                            train.getDescription(),
                            train.getLeadEngine() != null ? train.getLeadEngine().toString() : "",
                            train.getTrainDepartsName(),
                            train.getDepartureTime(),
                            train.getStatus(),
                            train.getCurrentLocationName(),
                            train.getTrainTerminatesName(),
                            train.getRoute(),
                            train.getId()
                    ));
                }
            }
            response.getWriter().print(html.toString());
        } else {
            response.setContentType("text/html"); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Operations.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            ServletUtil.getInstance().getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "TrainsTitle")
                    ),
                    ServletUtil.getInstance().getNavBar(request.getLocale(), request.getContextPath()),
                    ServletUtil.getInstance().getRailroadName(false),
                    ServletUtil.getInstance().getFooter(request.getLocale(), request.getContextPath()),
                    "" // no train Id
            ));
        }
    }

    private void processManifest(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Train train = TrainManager.instance().getTrainById(id);
        if ("html".equals(request.getParameter("format"))) {
            log.debug("Getting manifest HTML code for train {}", id);
            HtmlManifest manifest = new HtmlManifest(request.getLocale(), train);
            ServletUtil.getInstance().setNonCachingHeaders(response);
            response.setContentType("text/html"); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "ManifestSnippet.html"))),
                    train.getIconName(),
                    train.getDescription(),
                    Setup.isPrintValidEnabled() ? manifest.getValidity() : "",
                    train.getComment(),
                    Setup.isPrintRouteCommentsEnabled() ? train.getRoute().getComment() : "",
                    manifest.getLocations()
            ));
            train.setModified(false);
        } else if (JSON.JSON.equals(request.getParameter("format"))) {
            log.debug("Getting manifest JSON code for train {}", id);
            JsonNode manifest = this.mapper.readTree(new JsonManifest(train).getFile());
            if (manifest.path(JSON.IMAGE_FILE_NAME).isTextual()) {
                ((ObjectNode) manifest).put(JSON.IMAGE_FILE_NAME, WebServer.URIforPortablePath(FileUtil.getPortableFilename(manifest.path(JSON.IMAGE_FILE_NAME).asText())));
            }
            String content = this.mapper.writeValueAsString(manifest);
            response.setContentType(ServletUtil.APPLICATION_JSON);
            response.setContentLength(content.length());
            response.getWriter().print(content);
        } else {
            response.setContentType("text/html"); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Operations.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            ServletUtil.getInstance().getRailroadName(false),
                            String.format(request.getLocale(),
                                    Bundle.getMessage(request.getLocale(), "ManifestTitle"),
                                    train.getIconName(),
                                    train.getDescription()
                            )
                    ),
                    ServletUtil.getInstance().getNavBar(request.getLocale(), request.getContextPath()),
                    !train.getRailroadName().equals("") ? train.getRailroadName() : ServletUtil.getInstance().getRailroadName(false),
                    ServletUtil.getInstance().getFooter(request.getLocale(), request.getContextPath()),
                    train.getId()
            ));
        }
    }

    private void processConductor(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Train train = TrainManager.instance().getTrainById(id);
        JsonNode data;
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            data = this.mapper.readTree(request.getReader());
            if (!data.path(DATA).isMissingNode()) {
                data = data.path(DATA);
            }
        } else {
            data = this.mapper.createObjectNode();
            ((ObjectNode) data).put("format", request.getParameter("format"));
        }
        if (data.path("format").asText().equals("html")) {
            if (!data.path(LOCATION).isMissingNode()) {
                String location = data.path(LOCATION).asText();
                if (location.equals(NULL)) {
                    train.terminate();
                } else if (!train.move(location)) {
                    response.sendError(412, Bundle.getMessage(request.getLocale(), "ErrorTrainMovement", id, location));
                }
            }
            log.debug("Getting conductor HTML code for train {}", id);
            HtmlConductor conductor = new HtmlConductor(request.getLocale(), train);
            ServletUtil.getInstance().setNonCachingHeaders(response);
            response.setContentType("text/html"); // NOI18N
            response.getWriter().print(conductor.getLocation());
        } else {
            response.setContentType("text/html"); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Operations.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            ServletUtil.getInstance().getRailroadName(false),
                            String.format(request.getLocale(),
                                    Bundle.getMessage(request.getLocale(), "ConductorTitle"),
                                    train.getIconName(),
                                    train.getDescription()
                            )
                    ),
                    ServletUtil.getInstance().getNavBar(request.getLocale(), request.getContextPath()),
                    !train.getRailroadName().equals("") ? train.getRailroadName() : ServletUtil.getInstance().getRailroadName(false),
                    ServletUtil.getInstance().getFooter(request.getLocale(), request.getContextPath()),
                    train.getId()
            ));
        }
    }
// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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

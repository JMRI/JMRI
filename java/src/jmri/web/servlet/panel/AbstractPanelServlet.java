/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.jmrit.display.Editor;
import jmri.util.JmriJFrame;
import jmri.web.server.WebServer;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 *
 * @author rhwood
 */
abstract class AbstractPanelServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3134679703461026038L;
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	protected static final String XML_CONTENT_TYPE = "application/xml; charset=utf-8";
    static Logger log = Logger.getLogger(AbstractPanelServlet.class.getName());

    abstract protected String getPanelType();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Handling GET request for " + request.getRequestURI());
        }
        if (request.getRequestURI().endsWith("/")) {
            listPanels(request, response);
        } else {
        	boolean useXML = true;
        	if ("json".equals(request.getParameter("format"))) {
        		useXML = false;
        	}
            String[] path = request.getRequestURI().split("/");
            response.setContentType(XML_CONTENT_TYPE);
            String panel = getPanel(path[path.length - 1].replaceAll("%20", " ").replaceAll("%23", "#").replaceAll("%26", "&"), useXML);
            if (panel == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See the JMRI console for details.");
            } else if (panel.startsWith("ERROR")) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, panel.substring(5).trim());
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength(panel.length());
                response.getWriter().print(panel);
            }
        }
    }

    protected void listPanels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if ("json".equals(request.getParameter("format"))) {
    		response.sendRedirect("/json/panels");
    	}
        response.sendRedirect("/xmlio/list?type=panel");
    }

    protected String getPanel(String name, boolean useXML) {
    	if (useXML) {
    		return getXmlPanel(name);
    	} else {
    		return getJsonPanel(name);
    	}
    }
    
    abstract protected String getJsonPanel(String name);
    
    abstract protected String getXmlPanel(String name);

    protected Editor getEditor(String name) {
        List<JmriJFrame> frames = JmriJFrame.getFrameList(Editor.class);
        for (JmriJFrame frame : frames) {
            if (((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle().equals(name)) {
                return (Editor) frame;
            }
        }
        return null;
    }

    protected void parsePortableURIs(Element element) {
        if (element != null) {
            for (Object child : element.getChildren()) {
                parsePortableURIs((Element) child);
                for (Object attr : ((Element) child).getAttributes()) {
                    if (((Attribute) attr).getName().equals("url")) {
                        String url = WebServer.URIforPortablePath(((Attribute) attr).getValue());
                        if (url != null) {
                            ((Attribute) attr).setValue(url);
                        } else {
                            ((Element) child).removeAttribute("url");
                        }
                    }
                }
            }

        }
    }
}

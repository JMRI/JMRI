/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.xmlio;

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.jmrit.XmlFile;
import jmri.web.xmlio.XmlIOFactory;
import jmri.web.xmlio.XmlIORequestor;
import jmri.web.xmlio.XmlIOServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author rhwood
 */
public class XmlIOServlet extends HttpServlet implements XmlIORequestor {

    static XmlIOFactory factory = null;
//    Thread thread = null;
    static ResourceBundle htmlStrings = ResourceBundle.getBundle("jmri.web.server.Html");
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XmlIOServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameterNames().hasMoreElements()) {
            Document doc = new Document(new Element("xmlio"));
            Element e = new Element(request.getPathInfo().substring(request.getPathInfo().indexOf("/") + 1));
            Enumeration<String> parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String parameter = parameters.nextElement();
                e.setAttribute(parameter, request.getParameter(parameter));
            }
            doc.getRootElement().addContent(e);
            this.doXmlIO(request, response, doc);
        } else {
            response.sendRedirect("/help/en/html/web/XMLIO.shtml");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        SAXBuilder builder = XmlFile.getBuilder(false);
        try {
            Document doc = builder.build(request.getInputStream());
            this.doXmlIO(request, response, doc);
        } catch (JDOMException e1) {
            log.error("JDOMException on input: " + e1, e1);
        }
    }

    protected void doXmlIO(HttpServletRequest request, HttpServletResponse response, Document doc) throws ServletException, IOException {
        XMLOutputter fmt = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());

        Element root = doc.getRootElement();

        // start processing reply
        if (factory == null) {
            factory = new XmlIOFactory();
        }
        XmlIOServer srv = factory.getServer();

        // if list or throttle elements present, or item elements that do set, do immediate operation
        boolean immediate = false;
        if (root.getChild("list") != null
                || root.getChild("throttle") != null) {
            immediate = true;
        }
        if (!immediate) {
            for (Object e : root.getChildren()) {
                if (((Element) e).getAttributeValue("set") != null
                        || ((Element) e).getChild("set") != null) {
                    immediate = true;
                }
                break;
            }
        }
    	String client = request.getRemoteHost()+":"+request.getRemotePort();
        try {
            if (immediate) {
                if (log.isDebugEnabled()) log.debug("immediate reply to "+client);
                srv.immediateRequest(root);  // modifies 'doc' in place
            } else {
            	Thread thread = Thread.currentThread();
            	if (log.isDebugEnabled()) log.debug("stalling thread, waiting to reply to " + client);
                srv.monitorRequest(root, this, client, thread);

                try {
                    //Thread.sleep(10000000000000L);  // really long
                    Thread.sleep(300000);  // not quite so long (5 minutes)
                    if (log.isDebugEnabled()) log.debug("Thread sleep completed for " + client);
                } catch (InterruptedException e) {
                	if (log.isDebugEnabled()) log.debug("Thread sleep interrupted for " + client);
                }
                if (log.isDebugEnabled()) log.debug("thread resumes and replies to " + client);
            }
        } catch (jmri.JmriException e1) {
            log.error("JmriException while creating reply: " + e1, e1);
        }

        response.setContentType("text/xml");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(fmt.outputString(doc));
    }

    @Override
    public void monitorReply(Element e, Thread thread) {
    	if (log.isDebugEnabled()) log.debug("Interrupting thread " + thread.getName() + " (" + thread.getState() + ")");
    	thread.interrupt();
    }
}

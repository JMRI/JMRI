/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

/**
 *
 * @author rhwood
 */
public class ControlPanelServlet extends AbstractPanelServlet {

    @Override
    protected String getPanelType() {
        return "ControlPanel";
    }

    @Override
    protected String getPanel(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}

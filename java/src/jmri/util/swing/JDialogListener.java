package jmri.util.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;

/**
 * JDialogListener can be used to link JDialog instances with Frames.
 * If the JMRI Web Frame Server, JmriJFrameServlet encounters a Frame with
 * this listener attached, the Dialog will be accessible via the server.
 * This listener must be removed when the Dialog is closed.
 * @since 5.5.6
 * @author Steve Young Copyright (C) 2023
 */
public class JDialogListener implements PropertyChangeListener {

    private final JDialog thisDialog;

    public JDialogListener(JDialog dialog){
        super();
        thisDialog = dialog;
    }

    public JDialog getDialog(){
        return thisDialog;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

}

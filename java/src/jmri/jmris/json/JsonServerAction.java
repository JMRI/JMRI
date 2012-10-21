package jmri.jmris.json;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class JsonServerAction extends AbstractAction {

	public JsonServerAction(String s) {
		super(s);
	}

    public JsonServerAction() {
    	this("Start JMRI JSON Server");
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		JsonServerManager.getJsonServer().start();
	}

}

package jmri.jmrit.jython;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class JynstrumentPopupMenu extends JPopupMenu {
	private static final ResourceBundle jythonBundle = ResourceBundle.getBundle("jmri/jmrit/jython/JythonBundle");

	Jynstrument jynstrument;
	Container container;

	public JynstrumentPopupMenu(Jynstrument it, Container cont) {
		super(it.getName());
		jynstrument = it;
		container = cont;
		initMenu();
		it.addMouseListener(new MouseAdapter(){
    		public void mousePressed(MouseEvent e) {
    	        maybeShowPopup(e);
    	    }
    	    public void mouseReleased(MouseEvent e) {
    	        maybeShowPopup(e);
    	    }
    	    private void maybeShowPopup(MouseEvent e) {
    	    	if (! (e.getComponent() instanceof Jynstrument)) {
    	    		return;
    	    	}
	        	Jynstrument it = (Jynstrument) e.getComponent();
    	        if (e.isPopupTrigger()) {
    	        	it.getPopUpMenu().show(e.getComponent(),e.getX(), e.getY());
    	        }
    	    }
    	});
		
	}
	
	private void initMenu() {
		// Quit option
		JMenuItem quitMenuItem = new JMenuItem(jythonBundle.getString("JynstrumentPopupMenuQuit"));
		quitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jynstrument.quit();
				container.remove(jynstrument);
				jynstrument.setPopUpMenu(null);
				jynstrument = null;
				container.repaint();
			} 
		} );
		add(quitMenuItem);  		
		// Edit option
		JMenuItem editMenuItem = new JMenuItem(jythonBundle.getString("JynstrumentPopupMenuEdit"));
		editMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//			if (!java.awt.Desktop.isDesktopSupported()) //TODO: Need Java 6
//				    return;
				log.debug("Not implemented");
			} 
		} );
		editMenuItem.setEnabled(false);
		add(editMenuItem);  		
		// Reload option
		JMenuItem reloadMenuItem = new JMenuItem(jythonBundle.getString("JynstrumentPopupMenuReload"));
		reloadMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.debug("Not implemented"); // TODO
			} 
		} );
		reloadMenuItem.setEnabled(false);
		add(reloadMenuItem);  		
		// Debug option
		JMenuItem debugMenuItem = new JMenuItem(jythonBundle.getString("JynstrumentPopupMenuDebug"));
		debugMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.debug("Not implemented"); // TODO
			} 
		} );
		debugMenuItem.setEnabled(false);
		add(debugMenuItem);
	}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JynstrumentPopupMenu.class.getName());
}


package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom.Element;

import jmri.DccThrottle;
import jmri.jmrit.catalog.NamedIcon;


public class ThrottlesListPanel extends JPanel implements AddressListener {
	private static final ResourceBundle throttleBundle = ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle");

	private DefaultListModel throttleFramesLM;
	
	public ThrottlesListPanel() {
		super();
		throttleFramesLM = new DefaultListModel();
		initGUI();
	}
	
	private void initGUI() {
		JList throttleFrames;
		throttleFrames = new JList(throttleFramesLM);
		throttleFrames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		throttleFrames.setCellRenderer(new ThrottlesListCellRenderer());
		throttleFrames.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if ( ((JList)evt.getSource()).getSelectedValue() != null )
					((ThrottleFrame)((JList)evt.getSource()).getSelectedValue()).toFront();
			}			
		});

	    JScrollPane scrollPane1 = new JScrollPane(throttleFrames);
	    setLayout(new BorderLayout());
	    setPreferredSize(new Dimension(320,200));
	    
	    JToolBar throttleToolBar = new JToolBar("Throttles list toolbar");
	    JButton jbNew = new JButton();
	    jbNew.setIcon(new NamedIcon("resources/icons/throttles/Add24.gif","resources/icons/throttles/Add24.gif"));
	    jbNew.setToolTipText(throttleBundle.getString("ThrottleToolBarNewWindowToolTip"));
	    jbNew.setVerticalTextPosition(JButton.BOTTOM);
	    jbNew.setHorizontalTextPosition(JButton.CENTER);
	    jbNew.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
	    		tf.toFront();
	    	}
	    });
	    throttleToolBar.add(jbNew);
	    throttleToolBar.addSeparator();
	    throttleToolBar.add(new StopAllButton());
	    throttleToolBar.add(new LargePowerManagerButton());

	    add( throttleToolBar, BorderLayout.PAGE_START);
	    add( scrollPane1, BorderLayout.CENTER);
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<ThrottleFrame> getEnumeration() {
		return  (Enumeration<ThrottleFrame>) throttleFramesLM.elements() ;
	}
	
	public void addThrottleFrame(ThrottleFrame tf) {
		throttleFramesLM.addElement(tf);
	}
	
	public void removeThrottleFrame(ThrottleFrame tf) {
		throttleFramesLM.removeElement(tf);
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottlesListPanel.class.getName());

	public void notifyAddressChosen(int newAddress, boolean isLong) {		
	}

	public void notifyAddressReleased(int address, boolean isLong) {
		repaint();
	}

	public void notifyAddressThrottleFound(DccThrottle throttle) {
		repaint();
	}

	public Element getXml() {
		Element me  = new Element("ThrottlesListPanel");
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);        
        children.add(WindowPreferences.getPreferences(this.getTopLevelAncestor()));       
        me.setContent(children);        
        return me;
	}

	public void setXml(Element tlp) {
        Element window = tlp.getChild("window");
        if (window!=null)
        	WindowPreferences.setPreferences(this.getTopLevelAncestor(), window);		
	}
}

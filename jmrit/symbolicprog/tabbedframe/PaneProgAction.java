/** 
 * PaneProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.symbolicprog.KnownLocoSelPane;
import jmri.jmrit.symbolicprog.NewLocoSelPane;

import java.awt.event.ActionEvent;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import org.jdom.*;
import org.jdom.input.*;

public class PaneProgAction 			extends AbstractAction {

	public PaneProgAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create the initial frame that steers
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

		String[] decoderLabels = {"<none>", "Lenz LE230", "Digitrax DH142", "Digitrax DH121"};
		String[] locoLabels = {"<none>", "UP 775", "UP 777", "SP 4738"};

		// new Loco on programming track
		JLabel last;
		JPanel pane1 = new NewLocoSelPane();
		
		// Known loco on programming track
		JPanel pane2 = new KnownLocoSelPane();
			
		// Known loco on main
		JPanel pane3 = new JPanel();
			pane3.setLayout(new BoxLayout(pane3, BoxLayout.Y_AXIS));
			pane3.add(last = new JLabel("Known locomotive on main track"));
			last.setBorder(new EmptyBorder(6,0,6,0));
				JPanel pane3a = new JPanel();
				pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.X_AXIS));
				pane3a.add(new JLabel("Locomotive address"));
				JTextField j = new JTextField(6);
				j.setMaximumSize(j.getPreferredSize());
				pane3a.add(j);
				pane3a.add(new JLabel("Select from roster:"));
				pane3a.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			pane3.add(pane3a);
			pane3.add(new JComboBox(locoLabels));
			JButton go3 = new JButton("Open programmer");
			pane3.add(go3);
			pane3.setBorder(new EmptyBorder(6,6,6,6));
			
		JPanel pane4 = new JPanel();
			pane4.add(new JButton("Update Roster"));
			pane4.setBorder(new EmptyBorder(6,6,6,6));
			pane4.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			
		// load primary frame
		f.getContentPane().add(pane1);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		f.getContentPane().add(pane2);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		f.getContentPane().add(pane3);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		f.getContentPane().add(pane4);
		
		f.pack();	
		f.show();	
		
		// now pop a the pane frame for show
		testFrame();	
	}

	public void testFrame() {
		// Open and parse decoder file
		log.info("start decoder file");
		File dfile = new File("xml"+File.separator+"decoders"+File.separator+"NMRA_All.xml");
		Namespace dns = Namespace.getNamespace("decoder",
										"http://jmri.sourceforge.net/xml/decoder");
		SAXBuilder dbuilder = new SAXBuilder(true);  // argument controls validation, on for now
		Document ddoc = null;
		log.info("ctors done, do build");
		try {
			ddoc = dbuilder.build(new BufferedInputStream(new FileInputStream(dfile), 40000),"xml"+File.separator);
		}
		catch (Exception e) {
			log.error("Exception in SAXBuilder "+e);
		}
		// find root
		log.info("get root");
		Element droot = ddoc.getRootElement();

		// Open and parse programmer file
		log.info("start programmer file");
		File pfile = new File("xml"+File.separator+"programmers"+File.separator+"MultiPane.xml");
		Namespace pns = Namespace.getNamespace("programmer",
										"http://jmri.sourceforge.net/xml/programmer");
		SAXBuilder pbuilder = new SAXBuilder(true);  // argument controls validation, on for now
		Document pdoc = null;
		try {
			pdoc = pbuilder.build(new FileInputStream(pfile),"xml"+File.separator);
		}
		catch (Exception e) {
			log.error("Exception in programmer SAXBuilder "+e);
		}
		// find root
		Element proot = pdoc.getRootElement();

		// create the pane programmer
		PaneProgFrame p = new PaneProgFrame();
			
		// load its variables from decoder tree
		p.loadVariables(droot.getChild("decoder", dns), dns);
		
		// load its programmer config from programmer tree
		p.readConfig(proot, pns);
		
		p.pack();
		p.show();
		
	}	

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgAction.class.getName());

}


/* @(#)PanecProgAction.java */

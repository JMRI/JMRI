// BeanEditAction.java

package jmri.jmrit.beantable.beanedit;

import jmri.jmrit.beantable.AbstractTableAction;
import jmri.NamedBean;
import javax.swing.AbstractAction;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.ArrayList;
import jmri.util.JmriJFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;

/**
 * Provides the basic information and structure for
 * for a editing the details of a bean object
 *
 * @author	    Kevin Dickerson Copyright (C) 2011
 * @version		$Revision: 17977 $	
 */

abstract class BeanEditAction extends AbstractAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");
    
	public BeanEditAction(String s) {
        super(s);
    }
    
    public BeanEditAction() {
        super("Bean Edit");
    }
    
    jmri.NamedBean bean;
    
    public void setBean(jmri.NamedBean bean){
        this.bean = bean;
    }
    
    protected void createPanels(){
        bei.add(basicDetails());
    }

    JTextField userNameField = new JTextField(20);
    JTextArea commentField = new JTextArea(3,30);
    JScrollPane commentFieldScroller = new JScrollPane(commentField);
    
    EditBeanItem basicDetails(){
    
        ArrayList<Item> items = new ArrayList<Item>();
    
        items.add(new Item(new JLabel(bean.getSystemName()), Bundle.getMessage("ColumnSystemName"), null));
        
        EditBeanItem basic = new EditBeanItem();
        basic.setName("Basic");
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));

        userNameField.setText(bean.getUserName());
        items.add(new Item(userNameField, Bundle.getMessage("ColumnUserName"), null));
        
        items.add(new Item(commentFieldScroller, Bundle.getMessage("ColumnComment"), null));

        basic.setSaveItem(new AbstractAction(){
                public void actionPerformed(ActionEvent e) {
                    if(bean.getUserName()==null && !userNameField.getText().equals("")){
                        renameBean(userNameField.getText());
                    } else if(bean.getUserName()!=null && !bean.getUserName().equals(userNameField.getText())){
                        if(userNameField.getText().equals("")){
                            removeName();
                        } else {
                            renameBean(userNameField.getText());
                        }
                    }
                    bean.setComment(commentField.getText());
            }
        });
        basic.setResetItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                userNameField.setText(bean.getUserName());
                commentField.setText(bean.getComment());
            }
        });
        addToPanel(basic, items);
        return basic;
    }
    
    ArrayList<EditBeanItem> bei = new ArrayList<EditBeanItem>(5);
    JmriJFrame f;
    public void actionPerformed(ActionEvent e) {
        if(bean==null){
            log.error("No bean set so unable to edit a null bean");  //IN18N
            return;
        }
        if(f==null){
            f = new JmriJFrame("Edit " + getBeanType() + " " + bean.getDisplayName(), false,false);
            
            java.awt.Container containerPanel = f.getContentPane();
            createPanels();
            JTabbedPane detailsTab = new JTabbedPane();
            for(EditBeanItem bi:bei){
                detailsTab.addTab(bi.getName(), bi);
            }
            
            containerPanel.add(detailsTab, BorderLayout.CENTER);
            JPanel buttons = new JPanel();
            JButton applyBut = new JButton(Bundle.getMessage("ButtonApply"));
            applyBut.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    save();
                }
            });
            JButton okBut = new JButton(Bundle.getMessage("ButtonOK"));
            okBut.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    save();
                    f.dispose();
                }
            });
            JButton cancelBut = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelBut.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    f.dispose();
                }
            });
            buttons.add(applyBut);
            buttons.add(okBut);
            buttons.add(cancelBut);
            containerPanel.add(buttons, BorderLayout.SOUTH);
            /*try {
                f.initComponents(bean);
                }
            catch (Exception ex) {
                log.error("Exception: "+ex.toString());// NOI18N
                ex.printStackTrace();
                }*/
        } else {
            for(EditBeanItem bi:bei){
                bi.resetField();
            }
        }
        f.pack();
		f.setVisible(true);
	}
    
    protected void addToPanel(JPanel panel, ArrayList<Item> items){
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cD = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        cL.fill = GridBagConstraints.HORIZONTAL;
        cL.insets = new Insets(2, 0, 0, 15);
        cR.insets = new Insets(2, 0, 15, 15);
        cD.insets = new Insets(2, 0, 0, 0);
        cD.anchor = GridBagConstraints.NORTHWEST;
        cL.anchor = GridBagConstraints.NORTHWEST;
        
        //Dimension minFieldDim = new Dimension(30, 20);
        int y = 0;
        JPanel p = new JPanel();
        
        for(Item it:items){
            if(it.getDescription()!=null && it.getComponent()!=null){
                JLabel decript = new JLabel(it.getDescription() + ":", JLabel.LEFT);
                cL.gridx = 0;
                cL.gridy = y;
                cL.ipadx = 3;
                
                gbLayout.setConstraints(decript, cL);
                p.setLayout(gbLayout);
                p.add(decript, cL);
                
                cD.gridx = 1;
                cD.gridy = y;

                //it.getComponent().setMinimumSize(minFieldDim);
                gbLayout.setConstraints(it.getComponent(), cD);

                p.add(it.getComponent(), cD);
                
                cR.gridx = 2;
                cR.gridwidth = 1;
                cR.anchor = GridBagConstraints.WEST;
            
            } else {
                cR.anchor = GridBagConstraints.CENTER;
                cR.gridx = 0;
                cR.gridwidth = 3;
            }
            cR.gridy = y;
            if(it.getHelp()!=null){
                JTextPane help = new JTextPane();
                help.setText(it.getHelp());
                //help.setMinimumSize(minFieldDim);
                gbLayout.setConstraints(help, cR);
                formatTextAreaAsLabel(help);
                p.add(help, cR);
            }
            y++;
        }

        panel.add(p);
    }
    
    void formatTextAreaAsLabel(JTextPane pane) {
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(null);
    }
    
    public void save(){
        for(EditBeanItem bi:bei){
            bi.saveItem();
        }
    }
    
    static boolean validateNumericalInput(String text){
        if (text.length()!=0){
           try{
                Integer.parseInt(text);
            } catch (java.lang.NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }
    
    jmri.NamedBeanHandleManager nbMan = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    
    abstract protected String getBeanType();
    abstract protected NamedBean getBySystemName(String name);
    abstract protected NamedBean getByUserName(String name);
    
    public void renameBean(String _newName){
        NamedBean nBean = bean;
        String oldName = nBean.getUserName();

        String value = _newName;
        
        if(value.equals(oldName)){
            //name not changed.
            return;
        }
        else {
            NamedBean nB = getByUserName(value);
            if (nB != null) {
                log.error("User name is not unique " + value); // NOI18N
                String msg;
                msg = java.text.MessageFormat.format(Bundle.getMessage("WarningUserName"),
                        new Object[] { ("" + value) });
                JOptionPane.showMessageDialog(null, msg,
                        AbstractTableAction.rb.getString("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        nBean.setUserName(value);
        if(!value.equals("")){
            if(oldName==null || oldName.equals("")){
                if(!nbMan.inUse(nBean.getSystemName(), nBean))
                    return;
                String msg = java.text.MessageFormat.format(Bundle.getMessage("UpdateToUserName"),
                        new Object[] { getBeanType(),value,nBean.getSystemName() });
                int optionPane = JOptionPane.showConfirmDialog(null,
                    msg, Bundle.getMessage("UpdateToUserNameTitle"), 
                    JOptionPane.YES_NO_OPTION);
                if(optionPane == JOptionPane.YES_OPTION){
                    //This will update the bean reference from the systemName to the userName
                    try {
                        nbMan.updateBeanFromSystemToUser(nBean);
                    } catch (jmri.JmriException ex){
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }
                
            } else {
                nbMan.renameBean(oldName, value, nBean);
            }
            
        }
        else {
            //This will update the bean reference from the old userName to the SystemName
            nbMan.updateBeanFromUserToSystem(nBean);
        }
    }

    public void removeName(){
        String msg = java.text.MessageFormat.format(Bundle.getMessage("UpdateToSystemName"),
                new Object[] { getBeanType()});
        int optionPane = JOptionPane.showConfirmDialog(null,
            msg, Bundle.getMessage("UpdateToSystemNameTitle"), 
            JOptionPane.YES_NO_OPTION);
        if(optionPane == JOptionPane.YES_OPTION){
            nbMan.updateBeanFromUserToSystem(bean);
        }
        bean.setUserName(null);
    }
    
    static class Item {
        String help;
        String description;
        JComponent component;
        Item (JComponent component, String description, String help){
            this.component = component;
            this.description = description;
            this.help = help;
        }
        
        String getDescription(){
            return description;
        }
    
        String getHelp(){
            return help;
        }
        
        JComponent getComponent(){
            return component;
        }
    
    
    }
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BeanEditAction.class.getName());
}
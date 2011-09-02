// JMenuUtil.java

package jmri.util.swing;

import java.io.File;
import javax.swing.*;
import org.jdom.*;
import java.beans.PropertyChangeListener;

/**
 * Common utility methods for working with JMenus.
 * <P>
 * Chief among these is the loadMenu method, for
 * creating a set of menus from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision$
 * @since 2.9.4
 */

public class JMenuUtil extends GuiUtilBase {

    static public JMenu[] loadMenu(File file, WindowInterface wi, Object context) {
        Element root = rootFromFile(file);

        int n = root.getChildren("node").size();
        JMenu[] retval = new JMenu[n];
        
        int i = 0;
        for (Object child : root.getChildren("node")) {
            JMenu menuItem = jMenuFromElement((Element)child, wi, context);
            retval[i++] = menuItem;
            if(((Element)child).getChild("mnemonic")!=null && menuItem!=null){
                menuItem.setMnemonic(convertStringToKeyEvent(((Element)child).getChild("mnemonic").getText()));
            }
        }
        return retval;
    }
    
    static JMenu jMenuFromElement(Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();
        JMenu menu = new JMenu(name);
        
        for (Object item : main.getChildren("node")) {
            JMenuItem menuItem = null;
            Element child = (Element) item;
            if (child.getChildren("node").size() == 0) {  // leaf
                if ((child.getText().trim()).equals("separator"))
                    menu.addSeparator();
                else {
                    Action act = actionFromNode(child, wi, context);
                    menu.add(menuItem = new JMenuItem(act));
                }
            } else {
                if((child.getText().trim()).equals("group")){
                    //A seperate method is required for creating radio button groups
                    menu.add(createMenuGroupFromElement(child, wi, context));
                } else {
                    menu.add(menuItem = jMenuFromElement(child, wi, context)); // not leaf
                }
            }
            if(menuItem!=null && child.getChild("current") != null){
                setMenuItemInterAction(context, child.getChild("current").getText(), menuItem);
            
            }
            if(menuItem!=null && child.getChild("mnemonic")!=null){
               menuItem.setMnemonic(convertStringToKeyEvent(child.getChild("mnemonic").getText()));
            }
        }
        return menu;
    }
    
    static JMenu createMenuGroupFromElement(Element main, WindowInterface wi, Object context){
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();
        JMenu menu = new JMenu(name);
        ButtonGroup group = new ButtonGroup();
        for (Object item : main.getChildren("node")) {
            Element elem = (Element) item;
            Action act = actionFromNode(elem, wi, context);
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(act);
            group.add(menuItem);
            menu.add(menuItem);
            if(menuItem!=null && elem.getChild("current") != null){
                setMenuItemInterAction(context, elem.getChild("current").getText(), menuItem);
            }
        }

        return menu;
    }
    
    static void setMenuItemInterAction(Object context, final String ref, final JMenuItem menuItem){
        java.lang.reflect.Method methodListener = null;
        try{
            methodListener = context.getClass().getMethod("addPropertyChangeListener", java.beans.PropertyChangeListener.class);
        } catch (java.lang.NullPointerException e) {
            log.error("Null object passed");
            return;
        } catch (SecurityException e) {
            log.error("security exception unable to find remoteCalls for " + context.getClass().getName());
            return;
        } catch (NoSuchMethodException e) {
            log.error("No such method remoteCalls for " + context.getClass().getName());
            return;
        }
        if (methodListener!=null){
            try{
                methodListener.invoke(context, new PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        if(e.getPropertyName().equals(ref)){
                            String method = (String)e.getOldValue();
                            if(method.equals("setSelected")){
                                menuItem.setSelected(true);
                            } else if (method.equals("setEnabled")){
                                menuItem.setEnabled((Boolean)e.getNewValue());
                            }
                        }
                    }
                });
            } catch (IllegalArgumentException ex) {
                System.out.println("IllegalArgument " + ex);
            } catch (IllegalAccessException ex) {
                System.out.println("IllegalAccess " + ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                System.out.println("InvocationTarget " + ref + " " + ex.getCause());
            } catch (java.lang.NullPointerException ex){
                System.out.println("NPE " + context.getClass().getName() + " " +ex.toString());
            }
        }
    
    }
    
    static int convertStringToKeyEvent(String st){
    	char a = (st.toLowerCase()).charAt(0);
        int kcode = a - 32;
        return kcode;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMenuUtil.class.getName());
}
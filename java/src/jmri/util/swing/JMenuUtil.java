package jmri.util.swing;

import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import jmri.util.SystemType;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with JMenus.
 * <P>
 * Chief among these is the loadMenu method, for creating a set of menus from an
 * XML definition
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @since 2.9.4
 */
public class JMenuUtil extends GuiUtilBase {

    static public JMenu[] loadMenu(String path, WindowInterface wi, Object context) {
        Element root = rootFromName(path);

        int n = root.getChildren("node").size();
        JMenu[] retval = new JMenu[n];

        int i = 0;
        ArrayList<Integer> mnemonicList = new ArrayList<Integer>();
        for (Object child : root.getChildren("node")) {
            JMenu menuItem = jMenuFromElement((Element) child, wi, context);
            retval[i++] = menuItem;
            if (((Element) child).getChild("mnemonic") != null) {
                int mnemonic = convertStringToKeyEvent(((Element) child).getChild("mnemonic").getText());
                if (mnemonicList.contains(mnemonic)) {
                    log.error("Menu item '" + menuItem.getText() + "' Mnemonic '" + ((Element) child).getChild("mnemonic").getText() + "' has already been assigned");
                } else {
                    menuItem.setMnemonic(mnemonic);
                    mnemonicList.add(mnemonic);
                }
            }
        }
        return retval;
    }

    static @Nonnull
    JMenu jMenuFromElement(@CheckForNull Element main, WindowInterface wi, Object context) {
        boolean addSep = false;
        String name = "<none>";
        if (main == null) {
            log.warn("Menu from element called without an element");
            return new JMenu(name);
        }
        name = LocaleSelector.getAttribute(main, "name");
        //Next statement left in if the xml file hasn't been converted
        if ((name == null) || (name.equals(""))) {
            if (main.getChild("name") != null) {
                name = main.getChild("name").getText();
            }
        }
        JMenu menu = new JMenu(name);
        ArrayList<Integer> mnemonicList = new ArrayList<Integer>();
        for (Object item : main.getChildren("node")) {
            JMenuItem menuItem = null;
            Element child = (Element) item;
            if (child.getChildren("node").size() == 0) {  // leaf
                if ((child.getText().trim()).equals("separator")) {
                    addSep = true;
                } else {
                    if (!(SystemType.isMacOSX()
                            && UIManager.getLookAndFeel().isNativeLookAndFeel()
                            && ((child.getChild("adapter") != null
                            && child.getChild("adapter").getText().equals("apps.gui3.TabbedPreferencesAction"))
                            || (child.getChild("current") != null
                            && child.getChild("current").getText().equals("quit"))))) {
                        if (addSep) {
                            menu.addSeparator();
                            addSep = false;
                        }
                        Action act = actionFromNode(child, wi, context);
                        menu.add(menuItem = new JMenuItem(act));
                    }
                }
            } else {
                if (addSep) {
                    menu.addSeparator();
                    addSep = false;
                }
                if (child.getChild("group") != null && child.getChild("group").getText().equals("yes")) {
                    //A seperate method is required for creating radio button groups
                    menu.add(createMenuGroupFromElement(child, wi, context));
                } else {
                    menu.add(menuItem = jMenuFromElement(child, wi, context)); // not leaf
                }
            }
            if (menuItem != null && child.getChild("current") != null) {
                setMenuItemInterAction(context, child.getChild("current").getText(), menuItem);
            }
            if (menuItem != null && child.getChild("mnemonic") != null) {
                int mnemonic = convertStringToKeyEvent(child.getChild("mnemonic").getText());
                if (mnemonicList.contains(mnemonic)) {
                    log.error("Menu Item '" + menuItem.getText() + "' Mnemonic '" + child.getChild("mnemonic").getText() + "' has already been assigned");
                } else {
                    menuItem.setMnemonic(mnemonic);
                    mnemonicList.add(mnemonic);
                }
            }
        }
        return menu;
    }

    static @Nonnull
    JMenu createMenuGroupFromElement(@CheckForNull Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        if (main == null) {
            log.warn("Menu from element called without an element");
            return new JMenu(name);
        }
        name = LocaleSelector.getAttribute(main, "name");
        //Next statement left in if the xml file hasn't been converted
        if ((name == null) || (name.equals(""))) {
            if (main.getChild("name") != null) {
                name = main.getChild("name").getText();
            }
        }
        JMenu menu = new JMenu(name);
        ButtonGroup group = new ButtonGroup();
        for (Object item : main.getChildren("node")) {
            Element elem = (Element) item;
            Action act = actionFromNode(elem, wi, context);
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(act);
            group.add(menuItem);
            menu.add(menuItem);
            if (elem.getChild("current") != null) {
                setMenuItemInterAction(context, elem.getChild("current").getText(), menuItem);
            }
        }

        return menu;
    }

    static void setMenuItemInterAction(@Nonnull Object context, final String ref, final JMenuItem menuItem) {
        java.lang.reflect.Method methodListener = null;
        try {
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

        try {
            methodListener.invoke(context, new PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals(ref)) {
                        String method = (String) e.getOldValue();
                        if (method.equals("setSelected")) {
                            menuItem.setSelected(true);
                        } else if (method.equals("setEnabled")) {
                            menuItem.setEnabled((Boolean) e.getNewValue());
                        }
                    }
                }
            });
        } catch (IllegalArgumentException ex) {
            log.error("IllegalArgument in setMenuItemInterAction ", ex);
        } catch (IllegalAccessException ex) {
            log.error("IllegalAccess in setMenuItemInterAction ", ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            log.error("InvocationTarget {} in setMenuItemInterAction ", ref, ex);
        } catch (java.lang.NullPointerException ex) {
            log.error("NPE {} in setMenuItemInterAction ", context.getClass().getName(), ex);
        }

    }

    static int convertStringToKeyEvent(@Nonnull String st) {
        char a = (st.toLowerCase()).charAt(0);
        int kcode = a - 32;
        return kcode;
    }

    /**
     * replace a menu item in its parent with another menu item
     * <p>
     * (at the same position in the parent menu)
     *
     * @param orginalMenuItem     the original menu item to be replaced
     * @param replacementMenuItem the menu item to replace it with
     * @return true if the original menu item was found and replaced
     */
    public static boolean replaceMenuItem(
            @Nonnull JMenuItem orginalMenuItem,
            @Nonnull JMenuItem replacementMenuItem) {
        boolean result = false; // assume failure (pessimist!)
        Container container = orginalMenuItem.getParent();
        if (container != null) {
            int index = container.getComponentZOrder(orginalMenuItem);
            if (index > -1) {
                container.remove(orginalMenuItem);
                container.add(replacementMenuItem, index);
                result = true;
            }
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(JMenuUtil.class);
}   // class JMenuUtil

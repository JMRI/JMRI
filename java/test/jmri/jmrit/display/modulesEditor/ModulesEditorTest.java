/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import jmri.jmrit.display.layoutEditor.LayoutEditor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author geowar
 */
public class ModulesEditorTest {

    public ModulesEditorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of init method, of class ModulesEditor.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        String name = "";
        ModulesEditor instance = new ModulesEditor();
        instance.init(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDrawGrid method, of class ModulesEditor.
     */
    @Test
    public void testIsDrawGrid() {
        System.out.println("isDrawGrid");
        ModulesEditor instance = new ModulesEditor();
        boolean expResult = false;
        boolean result = instance.isDrawGrid();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDrawGrid method, of class ModulesEditor.
     */
    @Test
    public void testSetDrawGrid() {
        System.out.println("setDrawGrid");
        boolean b = false;
        ModulesEditor instance = new ModulesEditor();
        instance.setDrawGrid(b);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSnapToGridOnAdd method, of class ModulesEditor.
     */
    @Test
    public void testIsSnapToGridOnAdd() {
        System.out.println("isSnapToGridOnAdd");
        ModulesEditor instance = new ModulesEditor();
        boolean expResult = false;
        boolean result = instance.isSnapToGridOnAdd();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSnapToGridOnAdd method, of class ModulesEditor.
     */
    @Test
    public void testSetSnapToGridOnAdd() {
        System.out.println("setSnapToGridOnAdd");
        boolean b = false;
        ModulesEditor instance = new ModulesEditor();
        instance.setSnapToGridOnAdd(b);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSnapToGridOnMove method, of class ModulesEditor.
     */
    @Test
    public void testIsSnapToGridOnMove() {
        System.out.println("isSnapToGridOnMove");
        ModulesEditor instance = new ModulesEditor();
        boolean expResult = false;
        boolean result = instance.isSnapToGridOnMove();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSnapToGridOnMove method, of class ModulesEditor.
     */
    @Test
    public void testSetSnapToGridOnMove() {
        System.out.println("setSnapToGridOnMove");
        boolean b = false;
        ModulesEditor instance = new ModulesEditor();
        instance.setSnapToGridOnMove(b);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGridSize method, of class ModulesEditor.
     */
    @Test
    public void testSetGridSize() {
        System.out.println("setGridSize");
        int newSize = 0;
        ModulesEditor instance = new ModulesEditor();
        int expResult = 0;
        int result = instance.setGridSize(newSize);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGridSize method, of class ModulesEditor.
     */
    @Test
    public void testGetGridSize() {
        System.out.println("getGridSize");
        ModulesEditor instance = new ModulesEditor();
        int expResult = 0;
        int result = instance.getGridSize();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGridSize2nd method, of class ModulesEditor.
     */
    @Test
    public void testSetGridSize2nd() {
        System.out.println("setGridSize2nd");
        int newSize = 0;
        ModulesEditor instance = new ModulesEditor();
        int expResult = 0;
        int result = instance.setGridSize2nd(newSize);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGridSize2nd method, of class ModulesEditor.
     */
    @Test
    public void testGetGridSize2nd() {
        System.out.println("getGridSize2nd");
        ModulesEditor instance = new ModulesEditor();
        int expResult = 0;
        int result = instance.getGridSize2nd();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultTextColor method, of class ModulesEditor.
     */
    @Test
    public void testGetDefaultTextColor() {
        System.out.println("getDefaultTextColor");
        ModulesEditor instance = new ModulesEditor();
        String expResult = "";
        String result = instance.getDefaultTextColor();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultTextColor method, of class ModulesEditor.
     */
    @Test
    public void testSetDefaultTextColor() {
        System.out.println("setDefaultTextColor");
        Color color = null;
        ModulesEditor instance = new ModulesEditor();
        instance.setDefaultTextColor(color);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultBackgroundColor method, of class ModulesEditor.
     */
    @Test
    public void testSetDefaultBackgroundColor() {
        System.out.println("setDefaultBackgroundColor");
        Color color = null;
        ModulesEditor instance = new ModulesEditor();
        instance.setDefaultBackgroundColor(color);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of backgroundPopUp method, of class ModulesEditor.
     */
    @Test
    public void testBackgroundPopUp() {
        System.out.println("backgroundPopUp");
        MouseEvent event = null;
        ModulesEditor instance = new ModulesEditor();
        instance.backgroundPopUp(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mousePressed method, of class ModulesEditor.
     */
    @Test
    public void testMousePressed() {
        System.out.println("mousePressed");
        MouseEvent event = null;
        ModulesEditor instance = new ModulesEditor();
        instance.mousePressed(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseDragged method, of class ModulesEditor.
     */
    @Test
    public void testMouseDragged() {
        System.out.println("mouseDragged");
        MouseEvent event = null;
        ModulesEditor instance = new ModulesEditor();
        instance.mouseDragged(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseReleased method, of class ModulesEditor.
     */
    @Test
    public void testMouseReleased() {
        System.out.println("mouseReleased");
        MouseEvent event = null;
        ModulesEditor instance = new ModulesEditor();
        instance.mouseReleased(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showAddItemPopUp method, of class ModulesEditor.
     */
    @Test
    public void testShowAddItemPopUp() {
        System.out.println("showAddItemPopUp");
        MouseEvent event = null;
        JPopupMenu popup = null;
        ModulesEditor instance = new ModulesEditor();
        instance.showAddItemPopUp(event, popup);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addItemPopUp method, of class ModulesEditor.
     */
    @Test
    public void testAddItemPopUp() {
        System.out.println("addItemPopUp");
        LayoutEditor layoutEditor = null;
        JMenu menu = null;
        ModulesEditor instance = new ModulesEditor();
        instance.addItemPopUp(layoutEditor, menu);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of paintTargetPanel method, of class ModulesEditor.
     */
    @Test
    public void testPaintTargetPanel() {
        System.out.println("paintTargetPanel");
        Graphics g = null;
        ModulesEditor instance = new ModulesEditor();
        instance.paintTargetPanel(g);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}

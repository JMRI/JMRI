package jmri;


import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test Category.
 *
 * @author Daniel Bergqvist 2018
 */
public class CategoryTest {

    @Test
    public void testCategory() {
        Category myCategory1 = Category.registerCategory(
                new MyCategory("MyCategory", "MyDescription", 130));

        assertNotNull( myCategory1, "object exists");
        assertEquals( "MyCategory", myCategory1.name(), "Correct name");
        assertEquals( "MyDescription", myCategory1.toString(), "Correct description");
        assertEquals( 130, myCategory1.order(), "Correct order");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Category.registerCategory(new MyCategory("MyCategory", "MyDescription", 130));
        });
        assertEquals("Category 'MyCategory' with description 'MyDescription' is already registered",
                thrown.getMessage());

        thrown = assertThrows(IllegalArgumentException.class, () -> {
            Category.registerCategory(new MyCategory("MyOtherCategory", "MyDescription", 130));
        });
        assertEquals("Category 'MyOtherCategory' with description 'MyDescription' is already registered",
                thrown.getMessage());

        thrown = assertThrows(IllegalArgumentException.class, () -> {
            Category.registerCategory(new MyCategory("MyCategory", "MyOtherDescription", 130));
        });
        assertEquals("Category 'MyCategory' with description 'MyOtherDescription' is already registered",
                thrown.getMessage());

        Category myCategory2 = Category.registerCategory(
                new MyCategory("MyOtherCategory", "MyOtherDescription", 130));
        assertNotNull( myCategory2, "object exists");
        assertEquals( "MyOtherCategory", myCategory2.name(), "Correct name");
        assertEquals( "MyOtherDescription", myCategory2.toString(), "Correct description");
        assertEquals( 130, myCategory2.order(), "Correct order");

        Category myCategory3 = Category.registerCategory(
                new MyCategory("MyOtherOtherCategory", "MyOtherOtherDescription", 220));
        assertNotNull( myCategory3, "object exists");
        assertEquals( "MyOtherOtherCategory", myCategory3.name(), "Correct name");
        assertEquals( "MyOtherOtherDescription", myCategory3.toString(), "Correct description");
        assertEquals( 220, myCategory3.order(), "Correct order");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }


    private static final class MyCategory extends Category {

        MyCategory(String name, String description, int order) {
            super(name, description, order);
        }
    }

}

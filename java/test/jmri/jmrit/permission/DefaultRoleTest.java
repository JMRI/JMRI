package jmri.jmrit.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import jmri.BooleanPermission;
import jmri.Permission;
import jmri.PermissionOwner;
import jmri.PermissionValue;
import jmri.Role;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for DefaultRole.
 * <p>
 * The permission manager adds each role's permissions on the layout thread (Swing thread) after start up, while
 * another thread may be copying the role, as the Preferences window does (DefaultPermissionManager.getTemporaryInstance)
 * or going through its permissions, as storing them does.
 */
public class DefaultRoleTest {

    private static final int PERMISSIONS = 20000;

    private static final PermissionOwner OWNER = () -> "Test owner";

    private static Permission permission(String name) {
        return new BooleanPermission() {
            @Override
            public PermissionOwner getOwner() {
                return OWNER;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public BooleanValue getDefaultPermission(Role role) {
                return BooleanValue.FALSE;
            }
        };
    }

    private List<Permission> permissions;

    @Test
    public void testCopyWhilePermissionsAreAdded() throws InterruptedException {
        DefaultRole role = new DefaultRole("Test role");
        AtomicReference<Throwable> failed = new AtomicReference<>();

        Thread adder = new Thread(() -> {
            for (Permission p : permissions) {
                role.setPermissionWithoutCheck(p, BooleanPermission.BooleanValue.TRUE);
            }
        }, "DefaultRoleTest adder");
        adder.start();
        try {
            while (adder.isAlive() && failed.get() == null) {
                try {
                    new DefaultRole(role);
                    for (Map.Entry<Permission, PermissionValue> e : role.getPermissions().entrySet()) {
                        e.getValue();
                    }
                } catch (RuntimeException e) {
                    failed.set(e);
                }
            }
        } finally {
            adder.join();
        }

        assertNull(failed.get(), "copying the role, or going through its permissions, while they were added");
        assertEquals(PERMISSIONS, new DefaultRole(role).getPermissions().size(), "the copy has every permission");
    }

    @Test
    public void testPermissionsAreASnapshot() {
        DefaultRole role = new DefaultRole("Test role");
        role.setPermissionWithoutCheck(permissions.get(0), BooleanPermission.BooleanValue.TRUE);
        Map<Permission, PermissionValue> before = role.getPermissions();
        role.setPermissionWithoutCheck(permissions.get(1), BooleanPermission.BooleanValue.TRUE);

        assertEquals(1, before.size(), "permissions read before a change don't change under the reader");
        assertEquals(2, role.getPermissions().size());
        assertEquals(BooleanPermission.BooleanValue.TRUE, role.getPermissionValue(permissions.get(1)));
        assertEquals(BooleanPermission.BooleanValue.FALSE, role.getPermissionValue(permissions.get(2)),
                "a permission not set has its default");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        permissions = new ArrayList<>();
        for (int i = 0; i < PERMISSIONS; i++) {
            permissions.add(permission(String.format("Permission %05d", i)));
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.FileAsFlag;
import jmri.jmrit.logixng.expressions.FileAsFlag.DeleteOrKeep;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.util.FileUtil;

/**
 * Configures an FileAsFlag object with a Swing JPanel.
 *
 * @author Daniel Bergqvist 2023
 */
public class FileAsFlagSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectStringSwing _selectFilenameSwing;
    private LogixNG_SelectEnumSwing<DeleteOrKeep> _selectDeleteOrKeepSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        FileAsFlag expression = (FileAsFlag)object;

        if (expression == null) {
            // Create a temporary expression
            expression = new FileAsFlag("IQDE1", null);
        }

        _selectFilenameSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectDeleteOrKeepSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel tabbedPaneNamedBean;
        JPanel tabbedPaneState;

        tabbedPaneNamedBean = _selectFilenameSwing.createFilenamePanel(expression.getSelectFilename(), FileUtil.getUserFilesPath());
        tabbedPaneState = _selectDeleteOrKeepSwing.createPanel(expression.getSelectDeleteOrKeep(), DeleteOrKeep.values());

        JComponent[] components = new JComponent[]{
            tabbedPaneNamedBean,
            tabbedPaneState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("FileAsFlag_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        FileAsFlag expression = new FileAsFlag("IQDE1", null);

        _selectFilenameSwing.validate(expression.getSelectFilename(), errorMessages);
        _selectDeleteOrKeepSwing.validate(expression.getSelectDeleteOrKeep(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        FileAsFlag expression = new FileAsFlag(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof FileAsFlag)) {
            throw new IllegalArgumentException("object must be an FileAsFlag but is a: "+object.getClass().getName());
        }
        FileAsFlag expression = (FileAsFlag)object;

        _selectFilenameSwing.updateObject(expression.getSelectFilename());
        _selectDeleteOrKeepSwing.updateObject(expression.getSelectDeleteOrKeep());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("FileAsFlag_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileAsFlagSwing.class);

}

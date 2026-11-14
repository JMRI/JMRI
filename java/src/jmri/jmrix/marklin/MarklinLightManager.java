package jmri.jmrix.marklin;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Light;
import jmri.NamedBean;
import jmri.managers.AbstractLightManager;

/**
 * Light manager for Marklin connections.
 * System names accept {@code MLDCC45}, {@code MLMM123}, {@code MLCAN7168},
 * or legacy {@code ML45} (DCC). CAN addresses are raw wire addresses.
 */
public class MarklinLightManager extends AbstractLightManager {

    public MarklinLightManager(MarklinSystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MarklinSystemConnectionMemo getMemo() {
        return (MarklinSystemConnectionMemo) memo;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        try {
            MarklinAccessoryAddress.toCANAddress(curAddress);
        } catch (IllegalArgumentException ex) {
            throw new JmriException("Invalid Marklin light address '" + curAddress
                    + "'. Use MM<n>, DCC<n>, CAN<n>, or plain <n> (defaults to DCC).", ex);
        }
        return prefix + typeLetter() + curAddress;
    }

    /** {@inheritDoc} */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        if (!systemName.startsWith(getSystemNamePrefix())) {
            return NameValidity.INVALID;
        }
        try {
            MarklinAccessoryAddress.toCANAddress(systemName.substring(getSystemNamePrefix().length()));
            return NameValidity.VALID;
        } catch (IllegalArgumentException ex) {
            return NameValidity.INVALID;
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        systemName = validateSystemNamePrefix(systemName, locale);
        if (validSystemNameFormat(systemName) != NameValidity.VALID) {
            throw new NamedBean.BadSystemNameException(locale, "InvalidSystemName", systemName);
        }
        return systemName;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Light createNewLight(@Nonnull String systemName, String userName) {
        return new MarklinLight(systemName, userName, getSystemPrefix(), getMemo().getTrafficController());
    }

    /** {@inheritDoc} */
    @Override
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        return validSystemNameFormat(systemName) == NameValidity.VALID;
    }

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("LightEntryToolTip");
    }
}

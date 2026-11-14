package jmri.jmrix.marklin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Protocol-prefixed accessory addresses shared by turnouts and lights.
 */
final class MarklinAccessoryAddress {

    static final Pattern ADDRESS_PATTERN = Pattern.compile("^(MM|DCC|CAN)?(\\d+)$");

    private MarklinAccessoryAddress() {
    }

    static int toCANAddress(String address) {
        Matcher matcher = ADDRESS_PATTERN.matcher(address);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Marklin accessory address '" + address + "'");
        }
        int number = Integer.parseInt(matcher.group(2));
        if ("CAN".equals(matcher.group(1))) {
            return number;
        }
        int base = "MM".equals(matcher.group(1))
                ? MarklinConstants.MM1ACCSTART : MarklinConstants.DCCACCSTART;
        return number + base - 1;
    }
}

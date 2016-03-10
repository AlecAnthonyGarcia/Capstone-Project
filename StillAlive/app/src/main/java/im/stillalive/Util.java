package im.stillalive;

import java.util.Locale;

public class Util {

    public static String getFormattedTime(int hourOfDay, int minute) {
        return String.format(Locale.getDefault(), "%1d:%02d",
                (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12, minute)
                + ((hourOfDay >= 12) ? " PM" : " AM");
    }

}

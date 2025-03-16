package edu.cwu.catmap.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    // Independent method that formats a date to "Mon 11"
    public static String formatDateToDayNumber(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE d", Locale.US);

            Date date = inputFormat.parse(rawDate);
            return outputFormat.format(date);  // Returns "Mon 11" format
        } catch (Exception e) {
            e.printStackTrace();
            return "";  // Return empty string if formatting fails
        }
    }
}


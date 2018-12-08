/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.date;

import java.util.Date;

/**
 * Date-related utility class full of date-related utitlity stuff!
 */

public class DateUtils {

    public static boolean isLessThanXDaysInTheFuture(Date date, int x) {

        Date now = new Date();

        // get the time difference between the (future) date and now
        long diffTime = date.getTime() - now.getTime();

        // if the future date is earlier than now, then the date is not in the future at all!
        if (diffTime < 0) {
            return false;
        }

        long diffDays = diffTime / (1000 * 60 * 60 * 24);

        // the final result depends on whether the difference is less than x - or not (yet)
        return diffDays < x;
    }

}

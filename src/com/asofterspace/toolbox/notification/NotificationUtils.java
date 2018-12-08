/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.notification;

/**
 * This class provides utilities for handling notifications.
 */

public class NotificationUtils {

    final public static String KEY_NOTIFICATION_ID = "NOTIFICATION_ID";

    final public static String KEY_RESULT = "RESULT";

    private static int latestNotification = 0;

    /**
     * Generates a UNIQUE notification id!
     * @return A notification id
     */
    public static synchronized int generateNotificationId() {

        latestNotification++;

        return latestNotification;
    }
}

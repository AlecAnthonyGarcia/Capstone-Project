package im.stillalive;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class Util {

    public static void showToastMessage(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static String getHumanFormattedTime(String timeString) {
        String time[] = timeString.split(":");
        int hourOfDay = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);
        return String.format(Locale.getDefault(), "%1d:%02d",
                (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12, minute)
                + ((hourOfDay >= 12) ? " PM" : " AM");
    }

    public static String getFormattedTime(int hourOfDay, int minute) {
        return String.format(Locale.getDefault(), "%1d:%02d", hourOfDay, minute);
    }

    public static void setAlarm(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void sendReminderNotification(Context context, String contactName,
                                                int reminderId) {
        int notificationId = 001;
        Intent intent = new Intent(context, MessageComposerActivity.class);
        intent.putExtra("reminderId", reminderId);
        intent.putExtra("notificationId", notificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.reminder_notification_title))
                .setContentText(contactName + " " +
                        context.getString(R.string.reminder_notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_textsms_black_24dp,
                        context.getString(R.string.reminder_notification_action_still_alive),
                        pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notification);

    }

    public static void sendTextMessage(Context context, String number,
                                       String message) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent("SMS_SENT"), 0);
        SmsManager.getDefault().sendTextMessage(number, null, message, pendingIntent, null);
    }

}

package im.stillalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class AlarmReceiver extends BroadcastReceiver {

    private Realm realm;
    private ArrayList<Integer> reminderDeliveryDays = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        realm = Realm.getDefaultInstance();
        RealmResults<Reminder> reminders = realm.where(Reminder.class).findAll();
        Reminder reminder;
        String deliveryTime[];
        JSONObject deliveryDays;

        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < reminders.size(); i++) {
            reminder = reminders.get(i);
            deliveryTime = reminder.getDeliveryTime().split(":");
            try {
                deliveryDays = new JSONObject(reminder.getDeliveryDays());
                reminderDeliveryDays.clear();

                if (deliveryDays.getBoolean("Sunday")) {
                    reminderDeliveryDays.add(1);
                }
                if (deliveryDays.getBoolean("Monday")) {
                    reminderDeliveryDays.add(2);
                }
                if (deliveryDays.getBoolean("Tuesday")) {
                    reminderDeliveryDays.add(3);
                }
                if (deliveryDays.getBoolean("Wednesday")) {
                    reminderDeliveryDays.add(4);
                }
                if (deliveryDays.getBoolean("Thursday")) {
                    reminderDeliveryDays.add(5);
                }
                if (deliveryDays.getBoolean("Friday")) {
                    reminderDeliveryDays.add(6);
                }
                if (deliveryDays.getBoolean("Saturday")) {
                    reminderDeliveryDays.add(7);
                }

                if (Integer.parseInt(deliveryTime[0]) == hour
                        && Integer.parseInt(deliveryTime[1]) == minute
                        && reminderDeliveryDays.contains(day)) {
                    Util.sendReminderNotification(context, reminder.getContactName(),
                            reminder.getId());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
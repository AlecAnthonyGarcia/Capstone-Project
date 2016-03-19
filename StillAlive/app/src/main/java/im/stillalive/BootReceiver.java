package im.stillalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.realm.Realm;
import io.realm.RealmResults;

public class BootReceiver extends BroadcastReceiver {

    private Realm realm;

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            realm = Realm.getDefaultInstance();
            RealmResults<Reminder> reminders = realm.where(Reminder.class).findAll();
            Reminder reminder;
            String deliveryTime[];

            for (int i = 0; i < reminders.size(); i++) {
                reminder = reminders.get(i);
                deliveryTime = reminder.getDeliveryTime().split(":");
                int hour = Integer.parseInt(deliveryTime[0]);
                int minute = Integer.parseInt(deliveryTime[1]);
                Util.setAlarm(context, hour, minute);
            }
        }
    }
}
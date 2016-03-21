package im.stillalive;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class StillAliveRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private Realm realm;
    private List<Reminder> mReminders;

    public StillAliveRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        initReminders();
    }

    @Override
    public void onDataSetChanged() {
        initReminders();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mReminders.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.item_reminder);

        Reminder reminder = mReminders.get(position);

        String reminderDeliveryDaysStr = "";
        try {
            JSONObject messageDeliveryDaysObj = new JSONObject(
                    reminder.getDeliveryDays());
            Iterator<String> Iterator = messageDeliveryDaysObj.keys();
            while (Iterator.hasNext()) {
                String day = Iterator.next();
                boolean shouldDeliver = (Boolean) messageDeliveryDaysObj.get(day);
                if (shouldDeliver) {
                    reminderDeliveryDaysStr = reminderDeliveryDaysStr + " "
                            + day.substring(0, 3).toUpperCase(Locale.getDefault());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        row.setTextViewText(R.id.contact_name, reminder.getContactName());
        if (reminder.getContactPhoto() != null) {
            row.setImageViewUri(R.id.contact_photo, Uri.parse(reminder.getContactPhoto()));
        }
        row.setTextViewText(R.id.reminder_text, reminder.getText());
        row.setTextViewText(R.id.reminder_delivery_time, Util.getHumanFormattedTime(reminder.getDeliveryTime()));
        row.setTextViewText(R.id.reminder_delivery_days, reminderDeliveryDaysStr);

        Bundle extras = new Bundle();
        extras.putInt("reminderId", reminder.getId());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.contact_photo, fillInIntent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void initReminders() {
        mReminders = new ArrayList<>();
        realm = Realm.getInstance(mContext);
        realm.refresh();

        // workaround for accessing Realm objects on a different thread
        List<Reminder> remindersList = realm.where(Reminder.class).findAll();
        List<Reminder> reminderListClone = new ArrayList<>();
        for (Reminder reminder : remindersList) {
            Reminder tempReminder = new Reminder();
            tempReminder.setId(reminder.getId());
            tempReminder.setText(reminder.getText());
            tempReminder.setContactName(reminder.getContactName());
            tempReminder.setContactNumber(reminder.getContactNumber());
            tempReminder.setContactPhoto(reminder.getContactPhoto());
            tempReminder.setDeliveryTime(reminder.getDeliveryTime());
            tempReminder.setDeliveryDays(reminder.getDeliveryDays());
            reminderListClone.add(tempReminder);
        }

        mReminders = reminderListClone;
    }

}
package im.stillalive;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

import io.realm.RealmResults;

public class RemindersAdapter extends
        RecyclerView.Adapter<RemindersAdapter.ViewHolder> {

    private static RealmResults<Reminder> mReminders;

    public RemindersAdapter(RealmResults<Reminder> reminders) {
        mReminders = reminders;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public Context context;
        public TextView contactName;
        public ImageView contactPhoto;
        public TextView reminderText;
        public TextView reminderDeliveryTime;
        public TextView reminderDeliveryDays;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            contactPhoto = (ImageView) itemView.findViewById(R.id.contact_photo);
            reminderText = (TextView) itemView.findViewById(R.id.reminder_text);
            reminderDeliveryTime = (TextView) itemView.findViewById(R.id.reminder_delivery_time);
            reminderDeliveryDays = (TextView) itemView.findViewById(R.id.reminder_delivery_days);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Reminder reminder = mReminders.get(getLayoutPosition());
            Intent intent = new Intent(context, ReminderComposerActivity.class);
            intent.putExtra("reminderId", reminder.getId());
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            Reminder reminder = mReminders.get(getLayoutPosition());
            Intent intent = new Intent(context, MessageComposerActivity.class);
            intent.putExtra("reminderId", reminder.getId());
            context.startActivity(intent);
            return false;
        }
    }

    @Override
    public RemindersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_reminder, parent, false);
        ViewHolder viewHolder = new ViewHolder(context, contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RemindersAdapter.ViewHolder viewHolder, int position) {
        Reminder reminder = mReminders.get(position);

        viewHolder.contactName.setText(reminder.getContactName());
        viewHolder.reminderText.setText(reminder.getText());

        if (reminder.getContactPhoto() != null) {
            viewHolder.contactPhoto.setImageURI(Uri.parse(reminder.getContactPhoto()));
        }

        viewHolder.reminderDeliveryTime.setText(Util.getHumanFormattedTime(reminder.getDeliveryTime()));

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

        viewHolder.reminderDeliveryDays.setText(reminderDeliveryDaysStr);

    }

    @Override
    public int getItemCount() {
        return mReminders.size();
    }
}
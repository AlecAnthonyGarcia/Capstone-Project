package im.stillalive;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class ReminderComposerActivity extends AppCompatActivity {

    private Intent intentExtras;
    private FloatingActionButton saveReminderFAB;
    private static Button reminderTimePickerButton;
    private EditText reminderMessage;
    private CheckBox reminderDeliveryDaySundayButton;
    private CheckBox reminderDeliveryDayMondayButton;
    private CheckBox reminderDeliveryDayTuesdayButton;
    private CheckBox reminderDeliveryDayWednesdayButton;
    private CheckBox reminderDeliveryDayThursdayButton;
    private CheckBox reminderDeliveryDayFridayButton;
    private CheckBox reminderDeliveryDaySaturdayButton;

    private static String reminderDeliveryTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_composer);

        intentExtras = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("contactName"));
        setSupportActionBar(toolbar);

        ImageView contactPhoto = (ImageView) findViewById(R.id.contact_photo);
        String contactPhotoUri = intentExtras.getStringExtra("contactPhoto");
        if (contactPhotoUri != null) {
            contactPhoto.setImageURI(Uri.parse(contactPhotoUri));
        }

        saveReminderFAB = (FloatingActionButton) findViewById(R.id.save_reminder_fab);
        saveReminderFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMessageCompositionValid()) {
                    // TODO: save reminder to Realm
                }
            }
        });

        reminderMessage = (EditText) findViewById(R.id.reminder_message);

        reminderDeliveryDaySundayButton = (CheckBox) findViewById(R.id.message_delivery_day_sunday_button);
        reminderDeliveryDayMondayButton = (CheckBox) findViewById(R.id.message_delivery_day_monday_button);
        reminderDeliveryDayTuesdayButton = (CheckBox) findViewById(R.id.message_delivery_day_tuesday_button);
        reminderDeliveryDayWednesdayButton = (CheckBox) findViewById(R.id.message_delivery_day_wednesday_button);
        reminderDeliveryDayThursdayButton = (CheckBox) findViewById(R.id.message_delivery_day_thursday_button);
        reminderDeliveryDayFridayButton = (CheckBox) findViewById(R.id.message_delivery_day_friday_button);
        reminderDeliveryDaySaturdayButton = (CheckBox) findViewById(R.id.message_delivery_day_saturday_button);

        reminderDeliveryDaySundayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);
        reminderDeliveryDayMondayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);
        reminderDeliveryDayTuesdayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);
        reminderDeliveryDayWednesdayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);
        reminderDeliveryDayThursdayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);
        reminderDeliveryDayFridayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);
        reminderDeliveryDaySaturdayButton
                .setOnCheckedChangeListener(onReminderDeliveryDayCheckChanged);

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        reminderTimePickerButton = (Button) findViewById(R.id.reminder_time_picker_button);
        reminderTimePickerButton.setText(Util.getFormattedTime(hour, minute));
        reminderTimePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment timerPickerFragment = new TimePickerFragment();
                timerPickerFragment.show(getFragmentManager(), "timePicker");
            }
        });
    }

    public CompoundButton.OnCheckedChangeListener onReminderDeliveryDayCheckChanged = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (!isChecked) {
                buttonView.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                buttonView.setBackgroundResource(R.drawable.reminder_day_background_transparent);
                buttonView.setTypeface(Typeface.DEFAULT);
            } else {
                buttonView.setTextColor(getResources().getColor(android.R.color.white));
                buttonView.setBackgroundResource(R.drawable.reminder_day_background);
                buttonView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }
    };

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            reminderDeliveryTime = String.format(Locale.getDefault(),
                    "%1d:%02d", hourOfDay, minute);
            reminderTimePickerButton.setText(Util.getFormattedTime(hourOfDay, minute));
        }
    }

    public boolean isMessageCompositionValid() {
        String messageTextStr = reminderMessage.getText().toString();
        String contactName = intentExtras.getStringExtra("contactName");
        if (!(reminderDeliveryDaySundayButton.isChecked()
                || reminderDeliveryDayMondayButton.isChecked()
                || reminderDeliveryDayTuesdayButton.isChecked()
                || reminderDeliveryDayWednesdayButton.isChecked()
                || reminderDeliveryDayThursdayButton.isChecked()
                || reminderDeliveryDayFridayButton.isChecked() || reminderDeliveryDaySaturdayButton
                .isChecked())) {
            Snackbar.make(saveReminderFAB, getString(R.string.error_invalid_reminder_day)
                    + " " + contactName, Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (!(messageTextStr.length() > 0)) {
            Snackbar.make(saveReminderFAB, getString(R.string.error_invalid_reminder_message)
                    + " " + contactName, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}

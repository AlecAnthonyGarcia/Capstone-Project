package im.stillalive;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import io.realm.Realm;

public class MessageComposerActivity extends AppCompatActivity {

    private Realm realm;
    private Bundle intentExtras;
    private FloatingActionButton sendMessageFAB;
    private EditText aliveMessage;
    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composer);

        realm = Realm.getDefaultInstance();
        intentExtras = getIntent().getExtras();

        reminder = realm.where(Reminder.class).equalTo("id", intentExtras.getInt("reminderId", -1)).findFirst();

        ImageView contactPhotoIv = (ImageView) findViewById(R.id.contact_photo);
        sendMessageFAB = (FloatingActionButton) findViewById(R.id.send_message_fab);
        aliveMessage = (EditText) findViewById(R.id.alive_message);

        String contactPhoto = reminder.getContactPhoto();

        aliveMessage.setText(reminder.getText());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(reminder.getContactName());
        setSupportActionBar(toolbar);

        if (contactPhoto != null) {
            contactPhotoIv.setImageURI(Uri.parse(contactPhoto));
        }

        sendMessageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.sendTextMessage(MessageComposerActivity.this, reminder.getContactNumber(),
                        aliveMessage.getText().toString());
                Util.showToastMessage(getApplicationContext(), getString(R.string.message_sent_message)
                        + "" + reminder.getContactName());
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(intentExtras.getInt("notificationId"));
                finish();
            }
        });

    }

}
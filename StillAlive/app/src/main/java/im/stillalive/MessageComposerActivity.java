package im.stillalive;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.realm.Realm;

public class MessageComposerActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int PLACE_PICKER_REQUEST = 1;
    private Realm realm;
    private Bundle intentExtras;
    private FloatingActionButton sendMessageFAB;
    private EditText aliveMessage;
    private Reminder reminder;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composer);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

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
                        + " " + reminder.getContactName());
                if (intentExtras.containsKey("notificationId")) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(intentExtras.getInt("notificationId"));
                }
                finish();
            }
        });

        Button selectLocationButton = (Button) findViewById(R.id.alive_message_select_location_button);
        selectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLocation();
            }
        });

        Button attachWeatherButton = (Button) findViewById(R.id.alive_message_attach_weather_button);
        attachWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachWeather();
            }
        });

    }

    private void selectLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void attachWeather() {
        progressDialog = ProgressDialog.show(this, getString(R.string.progress_dialog_weather_title),
                getString(R.string.progress_dialog_weather_message), true);
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                int count = 0;
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    if(count == 0) {
                        LatLng latLng = placeLikelihood.getPlace().getLatLng();
                        new FetchWeatherTask().execute(latLng.latitude, latLng.longitude);
                        count++;
                        break;
                    }
                }
                likelyPlaces.release();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                aliveMessage.setText(aliveMessage.getText().toString() + " @ " + place.getName());
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Adapted from https://github.com/udacity/Sunshine-Version-2/
    public class FetchWeatherTask extends AsyncTask<Double, Void, Double> {

        @Override
        protected Double doInBackground(Double... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String currentWeatherJsonStr = null;

            String format = "json";
            String units = "imperial";
            String appId = "b1b15e88fa797225412429c1c50c122a";

            try {
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/weather?";
                final String LAT_PARAM = "lat";
                final String LON_PARAM = "lon";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String APPID_PARAM = "appid";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LAT_PARAM, String.valueOf(params[0]))
                        .appendQueryParameter(LON_PARAM, String.valueOf(params[1]))
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(APPID_PARAM, appId)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    currentWeatherJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    currentWeatherJsonStr = null;
                }
                currentWeatherJsonStr = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
                currentWeatherJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return getCurrentTemperatureFromJson(currentWeatherJsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(final Double currentTemperature) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    aliveMessage.setText(aliveMessage.getText().toString() + " " +
                            getString(R.string.alive_message_weather_suffix) + " " +
                            String.format("%.0f", currentTemperature) + "\u00B0" + "F");
                    progressDialog.dismiss();
                }
            });
        }

        private Double getCurrentTemperatureFromJson(String weatherJsonStr)
                throws JSONException {
            final String OWM_TEMPERATURE = "temp";
            final String OWM_DESCRIPTION = "main";
            JSONObject weatherObj = new JSONObject(weatherJsonStr);
            return weatherObj.getJSONObject(OWM_DESCRIPTION).getDouble(OWM_TEMPERATURE);
        }
    }

}
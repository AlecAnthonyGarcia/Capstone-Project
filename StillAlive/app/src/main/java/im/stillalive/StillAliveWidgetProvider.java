package im.stillalive;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class StillAliveWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_ACTION = "im.stillalive.WIDGET_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WIDGET_ACTION)) {
            Intent reminderComposerActivity = new Intent(context, ReminderComposerActivity.class);
            reminderComposerActivity.putExtra("reminderId", intent.getIntExtra("reminderId", -1));
            reminderComposerActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(reminderComposerActivity);
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, StillAliveWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_layout);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.reminders_list, intent);
            rv.setEmptyView(R.id.reminders_list, R.id.empty_view);

            Intent broadcastIntent = new Intent(context, StillAliveWidgetProvider.class);
            broadcastIntent.setAction(WIDGET_ACTION);
            broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.reminders_list, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

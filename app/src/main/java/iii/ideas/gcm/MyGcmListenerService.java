package iii.ideas.gcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import iii.ideas.mdmconsole.MainActivity;
import iii.ideas.mdmconsole.R;
import iii.ideas.service.MainService;
import sdk.ideas.common.Logs;
import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService
{

	private static final String TAG = "MyGcmListenerService";

	/**
	 * Called when message is received.
	 *
	 * @param from
	 *            SenderID of the sender.
	 * @param data
	 *            Data bundle containing message data as key/value pairs. For
	 *            Set of keys use data.keySet().
	 */

	@Override
	public void onMessageReceived(String from, Bundle data)
	{
		String message = data.getString("message");
		
		
		Log.d(TAG, "From: " + from);
		Log.d(TAG, "Message: " + message);

		if (!isMyServiceRunning(MainService.class))
		{
			Intent intent = new Intent(this, MainService.class);
			startService(intent);
		//	setForegroundNotification(message);
		}
		else
		{
			Logs.showTrace("Main Service is already running");
		}
		//sendNotification(message);
		
	}
	
	
	

	private boolean isMyServiceRunning(Class<?> serviceClass)
	{

		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if (serviceClass.getName().equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Create and show a simple notification containing the received GCM
	 * message.
	 *
	 * @param message
	 *            GCM message received.
	 */
	private void sendNotification(String message)
	{
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("GCM Message")
				.setContentText(message)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}
	
	
	private void setForegroundNotification(String message)
	{
		if(null == message)
		{
			message = "just test";
		}
		
		Intent intent = new Intent(this, MainService.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("GCM Message")
				.setWhen(System.currentTimeMillis())
				.setContentText(message)
				.setAutoCancel(false)
				.setPriority(Notification.PRIORITY_HIGH)
				.setContentIntent(pendingIntent);
		
		Notification notification = notificationBuilder.build();
		
		notification.flags |= Notification.FLAG_NO_CLEAR;
		
		startForeground(1, notification);
		
		startService(intent);
	}
	


	
	
	
}
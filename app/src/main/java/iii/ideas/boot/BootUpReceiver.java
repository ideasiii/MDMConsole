package iii.ideas.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import iii.ideas.service.MainService;
import sdk.ideas.common.Logs;

public class BootUpReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
		{
			Logs.showTrace("裝置已重新開機");
			Intent startServiceIntent = new Intent(context, MainService.class);
			context.startService(startServiceIntent);
		}
	}
}
package iii.ideas.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import com.google.android.gms.iid.InstanceID;

public class RegistrationIntentService extends IntentService
{

	private static final String TAG = "RegIntentService";
	ResultReceiver rec = null;
	public RegistrationIntentService()
	{
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		try
		{
			rec = intent.getParcelableExtra("receiverTag");
			
			String token = InstanceID.getInstance(this).getToken(MDMParameterSetting.GCM_SENDER_ID,"GCM");
			Log.i(TAG, "GCM Registration Token: " + token);
			
			//Global.theApplication.saveInSharedPref(MDMParameterSetting.GCM_REGISTER_ID, token);
			
			Bundle b= new Bundle();
			b.putString(MDMParameterSetting.GCM_REGISTER_ID_String, token);
			
			rec.send(Global.GCM_RESULT_CODE, b);
		
		}
		catch (Exception e)
		{
			Log.d(TAG, "Failed to complete token refresh", e);
			
		}
		
		
	}

	
	

}
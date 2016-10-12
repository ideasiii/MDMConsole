package iii.ideas.mdmconsole;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import iii.ideas.service.AppEventScheduler;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.Logs;
import sdk.ideas.common.OnCallbackResult;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.app.ApplicationHandler;

public class AppActivity extends Activity
{
    private ApplicationHandler mApplicationHandler = null;
    private static boolean isStartActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Logs.showTrace("##[AppActivity] onCreate##");
        AppActivity.setIsStartActivity(true);

        Intent mIntent = getIntent();

        if (null != mIntent)
        {
            try
            {
                JSONObject mJsonAppEvent = new JSONObject(mIntent.getExtras().getString("AppEventJson"));

                mApplicationHandler = new ApplicationHandler(this);

                final boolean isInstall = mJsonAppEvent.getBoolean("isInstall");
                mApplicationHandler.setOnCallbackResultListener(new OnCallbackResult()
                {
                    @Override
                    public void onCallbackResult(int result, int what, int from, HashMap<String, String> message)
                    {
                        Logs.showTrace("AppHandler result: " + String.valueOf(result) + " from: " + String.valueOf(from)
                                + " message: " + message);

                        if (isInstall)
                        {
                            if (from == ResponseCode.METHOD_APPLICATION_INSTALL_SYSTEM && result == ResponseCode.ERR_SUCCESS)
                            {
                                //可加一些判斷
                            }
                        }
                        else
                        {
                            if (from == ResponseCode.METHOD_APPLICATION_UNINSTALL_SYSTEM)
                            {
                                if (result == ResponseCode.ERR_SUCCESS || result == ResponseCode.ERR_PACKAGE_NOT_FIND)
                                {
                                    //可加一些判斷

                                }
                            }

                        }
                        mApplicationHandler.stopListenAction();
                        AppActivity.this.finish();
                    }
                });
                mApplicationHandler.startListenAction();
                if (isInstall)
                {
                    mApplicationHandler.installApplication(mJsonAppEvent.getString("savePath"), mJsonAppEvent.getString("fileName"),
                            mJsonAppEvent.getInt("appID"));
                }
                else
                {
                    mApplicationHandler.unInstallApplication(mJsonAppEvent.getString("packageName"), mJsonAppEvent.getInt("appID"));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void finish()
    {
        AppActivity.setIsStartActivity(false);

        ResultReceiver receiver = getIntent().getParcelableExtra(MDMParameterSetting.APP_EVENT_CLOSE_RECEIVER);
        Logs.showTrace("[AppActivity] Send Finish Receiver Back!");
        Bundle mBundle = new Bundle();
        receiver.send(MDMParameterSetting.APP_EVENT_RESULT_CODE, mBundle);

        super.finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        int i = Activity.RESULT_OK;
        super.onActivityResult(requestCode, resultCode, data);
        Logs.showTrace("[AppActivity] requestCode: " + String.valueOf(requestCode)
                + " resultCode: " + String.valueOf(resultCode));

        mApplicationHandler.onActivityResult(requestCode, resultCode, data);
    }

    private static synchronized void setIsStartActivity(boolean isStart)
    {
        isStartActivity = isStart;
    }

    public static synchronized boolean getActivityState()
    {
        return isStartActivity;
    }

    @Override
    protected void onResume()
    {
        Logs.showTrace("##[AppActivity] onResume##");
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        // AppActivity.setIsStartActivity(false);
        Logs.showTrace("##[AppActivity] onStop##");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


}

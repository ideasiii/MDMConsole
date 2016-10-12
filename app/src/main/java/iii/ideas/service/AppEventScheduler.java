package iii.ideas.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

import iii.ideas.global.AppEvent;
import iii.ideas.global.MDMParameterSetting;
import iii.ideas.mdmconsole.AppActivity;
import sdk.ideas.common.Logs;


public class AppEventScheduler
{


    private AppEvent cacheAppEvent = null;
    private static Queue<AppEvent> appEventQueue = new LinkedList<AppEvent>();

    private Context mContext = null;

    public AppEventScheduler(Context mContext)
    {
        if (null != mContext)
        {
            this.mContext = mContext;
            IntentFilter filter = new IntentFilter();
            filter.addAction(MDMParameterSetting.ACTION_ADD_APP_EVENT);
            mContext.registerReceiver(mBroadcastReceiver, filter);
            //  Thread t = new Thread(new SchedulerRunnable());
            // t.start();


        }
    }

    public void unRegisterReceiver()
    {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }


    // handler for received AppEvent from MainService
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(MDMParameterSetting.ACTION_ADD_APP_EVENT))
            {
                Logs.showTrace("[AppEventScheduler] get action from service!");
                try
                {
                    JSONObject mJSONObject = new JSONObject(intent.getExtras().getString("AppEventJson"));
                    addOrGetAppEventQueue(true, AppEvent.jsonToAppEvent(mJSONObject));

                    //當AppActivity未執行時，可以將App Event 丟給他
                    if (!AppActivity.getActivityState())
                    {
                        startAppActivity(addOrGetAppEventQueue(false, null));
                    }

                }
                catch (JSONException e)
                {
                    Logs.showError("[AppEventScheduler] Cannot covert AppEvent to Json!");
                }


            }
        }
    };

    class AppEventCloseReceiver extends ResultReceiver
    {
        public AppEventCloseReceiver()
        {
            super(null);
        }

        /**
         * Called when there's a result available.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData)
        {
            Logs.showTrace("[AppEventScheduler] resultCode" + String.valueOf(resultCode));
            if (resultCode != MDMParameterSetting.APP_EVENT_RESULT_CODE)
            {
                return;
            }
            cacheAppEvent = null;
            AppEvent mAppEvent = addOrGetAppEventQueue(false, null);

            if (null != mAppEvent)
            {
                try
                {
                    if (!AppActivity.getActivityState())
                    {
                        startAppActivity(mAppEvent);

                        Logs.showTrace("[AppEventScheduler] AppActivity state" +
                                String.valueOf(AppActivity.getActivityState()));
                    }
                    else
                    {
                        Logs.showTrace("[AppEventScheduler] AppActivity still Alive!");
                    }
                }
                catch (JSONException e)
                {
                    Logs.showError("[AppEventScheduler] Cannot Covert AppEvent to Json!");
                }
                catch (Exception e)
                {
                    Logs.showError("[AppEventScheduler] ERROR Message" + e.toString());
                }

            }

        }


    }


    private synchronized static AppEvent addOrGetAppEventQueue(boolean isAdd, AppEvent mAppEvent)
    {
        AppEvent out = null;
        if (isAdd)
        {
            if (null != mAppEvent)
            {
                appEventQueue.add(mAppEvent);
            }
        }
        else
        {
            out = appEventQueue.poll();
        }
        appEventQueuePrint();

        return out;
    }

    private void startAppActivity(AppEvent data) throws JSONException
    {

        Intent mIntent = new Intent(mContext, AppActivity.class);
        mIntent.putExtra("AppEventJson", AppEvent.appEventToJson(data).toString());
        mIntent.putExtra(MDMParameterSetting.APP_EVENT_CLOSE_RECEIVER, new AppEventCloseReceiver());
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        Logs.showTrace("[AppEventScheduler] now start AppActivity!");
        mContext.startActivity(mIntent);
    }

    public static void appEventQueuePrint()
    {
        Logs.showTrace("Print AppEvent Queue Start!");
        for (AppEvent element : appEventQueue)
        {
            Logs.showTrace(element.toString());
        }
        Logs.showTrace("Print AppEvent Queue End!");

    }



    //僅以用thread 來去run run 看
    private class SchedulerRunnable implements Runnable
    {

        @Override
        public void run()
        {
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    if (cacheAppEvent == null)
                    {
                        cacheAppEvent = addOrGetAppEventQueue(false, null);
                        if (cacheAppEvent != null)
                        {
                            startAppActivity(cacheAppEvent);
                        }
                    }
                    else
                    {
                        startAppActivity(cacheAppEvent);
                    }
                    Thread.sleep(MDMParameterSetting.APPEVENT_WATCH_TIMES);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch (JSONException e2)
                {
                    e2.printStackTrace();
                }
            }


        }


    }


}





package iii.ideas.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import iii.ideas.global.AppEvent;
import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import iii.ideas.global.UnitTestCase;
import iii.ideas.mdmconsole.AppActivity;
import iii.ideas.mdmconsole.MainActivity;
import iii.ideas.mdmconsole.R;
import iii.ideas.mdmconsole.RestoreActivity;
import sdk.ideas.common.CommonClass;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.DeviceInfo;
import sdk.ideas.common.Logs;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.applist.ApplicationList;
import sdk.ideas.ctrl.content.DocumentWebViewHandler;

public class MainService extends Service
{
    private int count = 0;
    //為true: 需做login
    //為false: 不需做login

    public boolean loginFlag = true;
    private CommunicateServer mCommunicateServer = null;

    private Handler mainHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            Logs.showTrace("MainService Handler Get Message");
            Logs.showTrace("[MainService]what: " + String.valueOf(msg.what) + " arg1:" + String.valueOf(msg.arg1) + " arg2:" + String.valueOf(msg.arg2)
                    + "obj" + msg.obj);
            switch (msg.what)
            {
                case Global.MSG_RESPONSE_COMMUNICATE_SERVER:
                    switch (msg.arg2)
                    {
                        case Global.MSG_LOGIN:
                            if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                            {
                                onLoginSuccess();
                            }
                            else
                            {
                                onLoginFailed(msg.arg1, (String) msg.obj);
                            }

                            break;
                        case Global.MSG_UPDATE_STATE:


                            break;

                        case Global.MSG_GET_COMMAND:


                            Logs.showTrace("@@isUnitTest: " + String.valueOf(MDMParameterSetting.isUnitTest) + " @@");

                            if (MDMParameterSetting.isUnitTest)
                            {
                                /**
                                 * @@Unit
                                 * @@test
                                 * @@case
                                 * @@Start
                                 * */
                                Logs.showTrace("Unit test case Start!!");
                                Message testMessage = new Message();
                                testMessage.what = Global.MSG_RESPONSE_COMMUNICATE_SERVER;
                                testMessage.arg1 = ResponseCode.ERR_SUCCESS;
                                testMessage.arg2 = Global.MSG_GET_COMMAND;
                                testMessage.obj = UnitTestCase.noCommandTest;
                                /*if (count == 0)
                                {
                                    testMessage.obj = UnitTestCase.documentViewer_PDF;
                                }
                                else
                                {
                                    testMessage.obj = UnitTestCase.documentViewer_DOC;
                                }*/
                                count++;
                                Global.mGetCommander.getSendCommand(testMessage);
                            }

                            /**
                             * Unit test case End
                             * */
                            else
                            {
                                Global.mGetCommander.getSendCommand(msg);
                            }
                            break;

                        default:
                            break;
                    }

                    break;
                case Global.MSG_RESPONSE_GET_COMMANDER:

                    switch (msg.arg1)
                    {
                        case Global.MSG_GET_COMMANDER_RE_LOGIN:

                            stopAllThread();
                            //RE Login
                            login();

                            break;
                        case Global.MSG_GET_COMMANDER_STOPPED:


                            break;
                        case Global.MSG_GET_COMMANDER_UPDATE_STATE:

                            mCommunicateServer.sendEvent((String) msg.obj, Global.MSG_UPDATE_STATE);

                            break;
                        case Global.MSG_GET_COMMANDER_EXECUTE_COMMAND:

                            Message operateMessage = new Message();
                            operateMessage.what = Global.MSG_GET_COMMANDER_EXECUTE_COMMAND;
                            operateMessage.arg1 = msg.arg2;
                            operateMessage.obj = msg.obj;
                            Global.mExecuteCommander.sendMessage(operateMessage);

                            break;


                        default:

                            break;

                    }


                    break;
                case Global.MSG_RESPONSE_STATE_UPDATER:
                    Logs.showTrace("From: MSG_RESPONSE_STATE_UPDATER ERROR: Result:" + msg.arg1 + " Which:" + msg.arg2 + "ERROR message: " + msg.obj);
                    break;
                case Global.MSG_RESPONSE_EXECUTE_COMMANDER:

                    //處理Device Admin錯誤訊息
                    if (msg.arg1 == Global.ERROR_INVALID_PRIVILEGE)
                    {
                        //跳至main Activity 頁面
                        Logs.showTrace("[MainService]Jump to Main Activity!!");
                        Intent intent = new Intent(MainService.this, MainActivity.class);
                        startActivity(intent);
                        Logs.showTrace("[MainService]@@now stop MainService@@");
                        stopService();
                    }
                    //處理一些功能service context無法做到的，需另外啟動Activity來執行命令
                    else
                    {
                        if (msg.arg1 == Global.COMMAND_SERVICE_EXECUTE)
                        {
                            switch (msg.arg2)
                            {
                                case Global.COMMAND_RESTORE_CONTROL:


                                    //stopAllThread();
                                    if (!RestoreActivity.isActivityActive())
                                    {
                                        startRestoreActivity();
                                    }
                                    else
                                    {
                                        Logs.showTrace("[MainService] Restore Activity is still Running!");
                                    }
                                    break;
                                case Global.COMMAND_APPLICATION_INSTALL_CONTROL:

                                    int appID = Global.getInstallAppID();

                                    //download apk
                                    Global.mApplicationHandler.downloadApplicationFile((String) msg.obj, null
                                            , String.valueOf(appID) + ".apk", appID);


                                    break;
                                case Global.COMMAND_APPLICATION_UNINSTALL_CONTROL:
                                    try
                                    {
                                        String uninstallPackName = (String) msg.obj;
                                        int uAppID = Global.getUninstallAppID();

                                        Intent myIntent = new Intent(MDMParameterSetting.ACTION_ADD_APP_EVENT);
                                        AppEvent mAppEvent = new AppEvent(false, uninstallPackName
                                                , uAppID);

                                        myIntent.putExtra("AppEventJson",
                                                AppEvent.appEventToJson(mAppEvent).toString());
                                        MainService.this.sendBroadcast(myIntent);
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }


                                    break;
                                case Global.COMMAND_CONTENT_CONTROL:
                                    Logs.showTrace("[MainService]Jump to DocumentViewer Activity!!");
                                    if (DocumentWebViewHandler.getActivityState())
                                    {
                                        Global.mDocumentWebViewHandler.closeActivity();
                                    }
                                    Global.mDocumentWebViewHandler.startIntent((String) msg.obj);


                                    break;


                                default:
                                    break;


                            }


                        }
                    }

                    break;

                case CtrlType.MSG_RESPONSE_APPLICATION_HANDLER:
                    HashMap<String, String> message = (HashMap<String, String>) msg.obj;
                    switch (msg.arg2)
                    {
                        case ResponseCode.METHOD_APPLICATION_DOWNLOAD_APP:


                            int appID = Integer.valueOf(message.get("appID"));
                            int downloadState = -1;
                            if (null != message.get("downloadState"))
                            {
                                downloadState = Integer.valueOf(message.get("downloadState"));
                            }
                            switch (downloadState)
                            {
                                case 0:
                                    setDownloadingNotification(appID, null);
                                    break;
                                case 1:

                                    if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                                    {
                                        setFinishDownloadNotification(appID, null);
                                        try
                                        {
                                            Intent myIntent = new Intent(MDMParameterSetting.ACTION_ADD_APP_EVENT);
                                            AppEvent mAppEvent = new AppEvent(true,
                                                    message.get("savePath"), message.get("fileName"), appID);
                                            myIntent.putExtra("AppEventJson",
                                                    AppEvent.appEventToJson(mAppEvent).toString());
                                            MainService.this.sendBroadcast(myIntent);
                                        }
                                        catch (JSONException e)
                                        {
                                            Logs.showError("[MainService]Cannot Convert AppEvent to Json!");
                                        }


                                    }
                                    break;
                                case -1:
                                    //當下載因不明原因失敗後，如何解決方法


                                    break;
                            }

                            break;
                        case ResponseCode.METHOD_APPLICATION_INSTALL_SYSTEM:
                        case ResponseCode.METHOD_APPLICATION_INSTALL_USER:
                            String appName = ApplicationList.getAppNameFromPackageName(MainService.this, message.get("packageName"));
                            Global.setGetUpdateAppStateData(true, true, message.get("packageName"), appName);
                            Global.printAppUpdateStateData();
                            break;
                        case ResponseCode.METHOD_APPLICATION_UNINSTALL_SYSTEM:
                        case ResponseCode.METHOD_APPLICATION_UNINSTALL_USER:
                            Global.setGetUpdateAppStateData(true, false, message.get("packageName"), "");
                            Global.printAppUpdateStateData();
                            break;


                    }


                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Logs.showTrace("[MainService]Start MainService");

        if (isValid(intent))
        {
            //test case start
            if (MDMParameterSetting.isUnitTest)
            {
                onLoginSuccess();
            }
            //test case end
            else
            {
                if (loginFlag)
                {
                    login();
                }
                else
                {
                    onLoginSuccess();
                }
            }
        }
        else
        {
            Logs.showError("some param is null!");
        }


        return Service.START_NOT_STICKY;
    }

    private void startRestoreActivity()
    {
        Intent mIntent = new Intent(this, RestoreActivity.class);
        mIntent.putExtra(MDMParameterSetting.RESTORE_EVENT_CLOSE_RECEIVER, new RestoreCloseReceiver());
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        Logs.showTrace("[MainService] now start RestoreActivity!");
        this.startActivity(mIntent);
    }

    class RestoreCloseReceiver extends ResultReceiver
    {
        public RestoreCloseReceiver()
        {
            super(null);
        }

        /**
         * Called when there's a result available.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData)
        {
            Logs.showTrace("resultCode = " + String.valueOf(resultCode));
            if (resultCode == MDMParameterSetting.RESTORE_EVENT_RESULT_CODE)
            {
                Logs.showTrace("[MainService] Success to Restore!");
                //startAllThread();


            }
        }
    }

    private boolean isMyActivityExist(String packageName)
    {

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        for (ActivityManager.RunningTaskInfo info : list)
        {
            if (info.topActivity.getPackageName().equals(packageName) || info.baseActivity.getPackageName().equals(packageName))
            {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }


    public boolean isValid(Intent mIntent)
    {
        String isLogin = mIntent.getStringExtra("isLogin");
        if (null != isLogin)
        {
            if (isLogin.equals("true"))
            {
                loginFlag = false;
            }
        }
        return Global.theApplication.isValidSharedPreferenceData();
    }

    public void onLoginFailed(int errorCode, String errorMessage)
    {
        Logs.showTrace("ERROR Code: " + String.valueOf(errorCode) + "ERROR Message: " + errorMessage);
        //this.stopSelf();
    }

    public void onLoginSuccess()
    {

        setForegroundNotification(null);
        startAllThread();
    }

    public void startAllThread()
    {
        Logs.showTrace("[MainService] now START all thread!");
        Global.mStateUpdater.start();
        Global.mGetCommander.start();
        Global.mAppEventScheduler = new AppEventScheduler(this);
    }

    public void stopAllThread()
    {
        Logs.showTrace("[MainService] now STOP all thread!");
        Global.mStateUpdater.stop();
        Global.mGetCommander.stop();
        Global.mExecuteCommander.stopThread();
        Global.mAppEventScheduler.unRegisterReceiver();
    }

    public void stopService()
    {
        Logs.showTrace("[MainService] now STOP MainService");
        stopAllThread();
        this.stopForeground(true);
        this.stopSelf();
    }


    public void login()
    {
        JSONObject LoginData = new JSONObject();
        try
        {
            LoginData.put(MDMParameterSetting.JSON_ACCOUNT_String, Global.theApplication.getAccountData());
            LoginData.put(MDMParameterSetting.JSON_PASSWORD_String, Global.theApplication.getAccountPasswordData());
            LoginData.put(MDMParameterSetting.JSON_GCM_ID_String, Global.theApplication.getGCMIDData());
            LoginData.put(MDMParameterSetting.JSON_BRAND_String, DeviceInfo.getBrand());
            LoginData.put(MDMParameterSetting.JSON_MODEL_String, DeviceInfo.getModel());
            LoginData.put(MDMParameterSetting.JSON_DEVICE_String, 0);
            LoginData.put(MDMParameterSetting.JSON_DEVICE_ID_String, Global.theApplication.getDeviceIDData());
            Logs.showTrace("Login Json Data: " + LoginData.toString());

            mCommunicateServer.sendEvent(LoginData.toString(), Global.MSG_LOGIN);

        }
        catch (JSONException e)
        {
            Logs.showTrace(e.toString());
        }
        catch (Exception e)
        {
            Logs.showTrace(e.toString());
        }
    }


    private void setForegroundNotification(String message)
    {
        if (null == message)
        {
            message = "You Are Controlled By MORE MDM SYSTEM";
        }

        Intent intent = new Intent(this, MainService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("MORE MDM Controlling")
                .setWhen(System.currentTimeMillis()).setContentText(message).setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH).setContentIntent(pendingIntent);

        Notification notification = notificationBuilder.build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1, notification);

    }

    private void setDownloadingNotification(int appID, String message)
    {
        if (null == message)
        {
            message = "Download in Progress";
        }
        NotificationManager mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("MDM App Download")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setProgress(0, 0, true);
        // Issues the notification
        mNotifyManager.notify(appID, mBuilder.build());


    }

    private void setFinishDownloadNotification(int appID, String message)
    {
        if (null == message)
        {
            message = "Download Finish!";
        }

        NotificationManager mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("MDM APP Download")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setProgress(0, 0, false);
        // Issues the notification
        mNotifyManager.notify(appID, mBuilder.build());
        mNotifyManager.cancel(appID);
    }


    //初始化
    public void init()
    {

        ArrayList<CommonClass.AppInfo> appInfoList = ApplicationList.getInstalledApps(this, false);

        for (int i = 0; i < appInfoList.size(); i++)
        {
            Global.setGetUpdateAppStateData(true, true, appInfoList.get(i).appPackageName, appInfoList.get(i).appName);
        }
        Global.printAppUpdateStateData();

        mCommunicateServer = new CommunicateServer(this.mainHandler);
        Global.mStateUpdater = StateUpdater.getInstance(MainService.this, mainHandler);
        Global.mExecuteCommander = ExecuteCommander.getInstance(MainService.this, mainHandler);
        Global.mGetCommander = GetCommander.getInstance(MainService.this, mainHandler);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        init();

        Logs.showTrace("MainService onCreate");
    }

    @Override
    public void onDestroy()
    {
        Logs.showTrace("MainService onDestroy");
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
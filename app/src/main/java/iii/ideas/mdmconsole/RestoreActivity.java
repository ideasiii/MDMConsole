package iii.ideas.mdmconsole;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


import java.util.HashMap;

import iii.ideas.global.MDMParameterSetting;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.Logs;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.app.ApplicationHandler;
import sdk.ideas.ctrl.restore.RestoreHandler;


public class RestoreActivity extends AppCompatActivity
{
    private RestoreHandler mRestoreHandler = null;
    private ApplicationHandler mApplicationHandler = null;
    private boolean restoreAppSuccess = false;
    private boolean restoreFileSuccess = false;
    private ProgressDialog progressDialog = null;
    private static boolean isRestoreActivityActive = false;

    private Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            Logs.showTrace("[RestoreActivity]what: " + String.valueOf(msg.what) + " arg1:" + String.valueOf(msg.arg1) + " arg2:" + String.valueOf(msg.arg2)
                    + "obj" + msg.obj);
            switch (msg.what)
            {
                case CtrlType.MSG_RESPONSE_RESTORE_HANDLER:
                    if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                    {
                        if (msg.arg2 == ResponseCode.METHOD_RESTORE_APPLICATION && MDMParameterSetting.FLAG_RESTORE_APP)
                        {
                            restoreAppSuccess = true;
                        }
                        if (msg.arg2 == ResponseCode.METHOD_RESTORE_SDCARD_FILE && MDMParameterSetting.FLAG_RESTORE_SDCARD)
                        {
                            restoreFileSuccess = true;
                        }
                        if (MDMParameterSetting.FLAG_RESTORE_APP && MDMParameterSetting.FLAG_RESTORE_SDCARD)
                        {
                            if (restoreAppSuccess && restoreFileSuccess)
                            {
                                progressDialog.dismiss();
                                Toast.makeText(RestoreActivity.this, "Restore Success!", Toast.LENGTH_LONG).show();
                                isRestoreActivityActive = false;
                                RestoreActivity.this.finish();
                            }
                        }
                        //只回復單筆
                        else
                        {
                            if (MDMParameterSetting.FLAG_RESTORE_APP)
                            {
                                if (restoreAppSuccess)
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(RestoreActivity.this, "Restore Success!", Toast.LENGTH_LONG).show();
                                    isRestoreActivityActive = false;
                                    RestoreActivity.this.finish();
                                }
                            }
                            else
                            {
                                if (MDMParameterSetting.FLAG_RESTORE_SDCARD)
                                {
                                    if (restoreFileSuccess)
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(RestoreActivity.this, "Restore Success!", Toast.LENGTH_LONG).show();
                                        isRestoreActivityActive = false;
                                        RestoreActivity.this.finish();
                                    }
                                }

                            }
                        }
                    }
                    break;
                case CtrlType.MSG_RESPONSE_APPLICATION_HANDLER:
                    switch (msg.arg2)
                    {
                        case ResponseCode.METHOD_APPLICATION_DOWNLOAD_APP:
                            HashMap<String, String> message = (HashMap<String, String>) msg.obj;
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
                                    setFinishDownloadNotification(appID, null);
                                    break;
                                case -1:
                                    Logs.showTrace("[RestoreActivity]ERROR occur while downloading app AppID:"
                                            + String.valueOf(appID));
                                    setFinishDownloadNotification(appID, null);


                                    break;


                                // break;
                            }


                        default:


                            break;

                    }
            }
        }


    };

    public static boolean isActivityActive()
    {
        return isRestoreActivityActive;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_page);
        isRestoreActivityActive = true;
        init();
        mRestoreHandler.restore();
        progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Restoring...");
        progressDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        mApplicationHandler.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setDownloadingNotification(int appID, String message)
    {
        if (null == message)
        {
            message = "Download " + String.valueOf(appID) + ".apk in Progress";
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


    private void init()
    {
        mApplicationHandler = new ApplicationHandler(this);
        mApplicationHandler.setHandler(mHandler);
        mApplicationHandler.startListenAction();

        mRestoreHandler = new RestoreHandler(this);

        mRestoreHandler.setHandler(mHandler);
        mRestoreHandler.setAppDownloadServerURL(MDMParameterSetting.URL_MDM_APP_DOWNLOAD);
        mRestoreHandler.setOnApplicationHandler(mApplicationHandler);
        mRestoreHandler.setLocalProfileReadPath(MDMParameterSetting.RECORD_DATA_INTERNAL_MEMORY,
                MDMParameterSetting.INIT_LOCAL_MDM_SDCARD_PATH, MDMParameterSetting.INIT_LOCAL_MDM_APP_PATH,
                MDMParameterSetting.RECORD_DATA_WRITE_EXTERNAL_PATH);
        mRestoreHandler.setRestoreFlag(MDMParameterSetting.FLAG_RESTORE_APP,
                MDMParameterSetting.FLAG_RESTORE_SDCARD);
    }


    @Override
    public void finish()
    {
        mApplicationHandler.stopListenAction();

        ResultReceiver receiver = getIntent().getParcelableExtra(MDMParameterSetting.RESTORE_EVENT_CLOSE_RECEIVER);
        Logs.showTrace("[RestoreActivity] Send Finish Receiver Back!");
        Bundle mBundle = new Bundle();
        receiver.send(MDMParameterSetting.RESTORE_EVENT_RESULT_CODE, mBundle);


        super.finish();
    }

    @Override
    protected void onStop()
    {
        Logs.showTrace("[RestoreActivity] onStop!!");
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        Logs.showTrace("[RestoreActivity] onResume!!");
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        Logs.showTrace("[RestoreActivity] onDestroy!!");

        super.onDestroy();
    }
}

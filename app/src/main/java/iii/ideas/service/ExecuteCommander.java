package iii.ideas.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import sdk.ideas.common.Common;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.Logs;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.common.StringUtility;
import sdk.ideas.ctrl.admin.DeviceAdminHandler;
import sdk.ideas.ctrl.app.ApplicationHandler;
import sdk.ideas.ctrl.camera.CameraHandler;
import sdk.ideas.ctrl.content.DocumentWebViewHandler;
import sdk.ideas.ctrl.lock.LockHandler;
import sdk.ideas.ctrl.mute.MuteVolumeHandler;
import sdk.ideas.ctrl.record.RecordHandler;
import sdk.ideas.ctrl.restore.RestoreHandler;
import sdk.ideas.ctrl.wifi.WifiHandler;

public class ExecuteCommander extends HandlerThread
{

    private final static String TagName = "ExecuteCommanderThread";
    private static ExecuteCommander mExecuteCommander = null;

    //檢查權限是否被關閉
    private boolean lockExecute = false;

    private Handler selfHandler = null;

    // parent handler
    private Handler mHandler = null;
    private Context mContext = null;

    private ExecuteCommander(Context mContext, Handler mHandler)
    {
        super(TagName);
        if (null != mHandler && null != mContext)
        {
            this.mHandler = mHandler;
            this.mContext = mContext;
        }
        else
        {
            Logs.showTrace("In Execute Commander: null mHandler");
        }
        init();
    }

    public static ExecuteCommander getInstance(Context mContext, Handler mHandler)
    {
        if (null == mExecuteCommander)
        {
            mExecuteCommander = new ExecuteCommander(mContext, mHandler);
            mExecuteCommander.start();
        }
        return mExecuteCommander;
    }


    private void init()
    {

        if (null == Global.mDeviceAdminHandler)
        {
            Global.mDeviceAdminHandler = new DeviceAdminHandler(mContext);

            //當裝置權限被使用者移除的話，則回報給Main Service 去處理問題
            if (!Global.mDeviceAdminHandler.isActive())
            {
                //回傳錯誤訊息
                Logs.showError("Device Admin is NOT enable!");
                Common.postMessage(mHandler, Global.MSG_RESPONSE_EXECUTE_COMMANDER, 0, 0, "Device Admin is not enable!");
                lockExecute = true;
                return;
            }
        }
        if (null == Global.mCameraHandler)
        {
            Global.mCameraHandler = new CameraHandler(Global.mDeviceAdminHandler.getPolicyData());
            Global.mCameraHandler.setHandler(mHandler);
        }
        if (null == Global.mLockHandler)
        {
            Global.mLockHandler = new LockHandler(Global.mDeviceAdminHandler.getPolicyData());
            Global.mLockHandler.setHandler(mHandler);
        }
        if (null == Global.mDocumentWebViewHandler)
        {
            Global.mDocumentWebViewHandler = new DocumentWebViewHandler(mContext);
            Global.mDocumentWebViewHandler.setHandler(mHandler);

        }
        if (null == Global.mMuteVolumeHandler)
        {
            Global.mMuteVolumeHandler = new MuteVolumeHandler(mContext);
            Global.mMuteVolumeHandler.setHandler(mHandler);
        }
        if (null == Global.mRecordHandler)
        {
            Global.mRecordHandler = new RecordHandler(mContext);
            Global.mRecordHandler.setHandler(mHandler);
            Global.mRecordHandler.setRecordFlag(MDMParameterSetting.FLAG_RECORD_APP, MDMParameterSetting.FLAG_RECORD_SDCARD);
            Global.mRecordHandler.setWritePathAndFileName(MDMParameterSetting.RECORD_DATA_INTERNAL_MEMORY,
                    MDMParameterSetting.RECORD_DATA_WRITE_EXTERNAL_PATH, MDMParameterSetting.INIT_LOCAL_MDM_APP_PATH,
                    MDMParameterSetting.INIT_LOCAL_MDM_SDCARD_PATH);
        }
        if (null == Global.mApplicationHandler)
        {
            Global.mApplicationHandler = new ApplicationHandler(mContext);
            Global.mApplicationHandler.setHandler(mHandler);
            Global.mApplicationHandler.startListenAction();
        }

        if (null == Global.mWifiHandler)
        {
            Global.mWifiHandler = new WifiHandler(mContext);
            Global.mWifiHandler.setHandler(mHandler);
        }


    }


    @Override
    protected void onLooperPrepared()
    {
        super.onLooperPrepared();

        Logs.showTrace("****OnLooperPrepared****");

		/* 當指令到來時，會透過此selfHandler來做MDM Commander 判斷 */
        selfHandler = new Handler(getLooper())
        {

            @Override
            public void handleMessage(Message msg)
            {
                // debug use

                Logs.showTrace("ExecuteCommander:");
                Logs.showTrace("what: " + String.valueOf(msg.what) + " arg1: " + String.valueOf(msg.arg1)
                        + " arg2: " + String.valueOf(msg.arg2) + " obj: " + String.valueOf(msg.obj));


                //當使用者將權限關閉時，就無法執行命令
                if (lockExecute)
                {
                    Logs.showError("!!權限未給，無法執行!!");
                    return;
                }

                if (msg.what == Global.MSG_GET_COMMANDER_EXECUTE_COMMAND)
                {
                    try
                    {
                        JSONObject operateCommand = new JSONObject((String) msg.obj);
                        switch (msg.arg1)
                        {
                            case Global.COMMAND_CAMERA_CONTROL:
                                if (operateCommand.getInt("value") == 0)
                                {
                                    Global.mCameraHandler.setCameraDisable(false);
                                }
                                else
                                {
                                    Global.mCameraHandler.setCameraDisable(true);
                                }
                                break;
                            case Global.COMMAND_SCREEN_LOCK_CONTROL:
                                if (operateCommand.getInt("value") == 0)
                                {
                                    Global.mLockHandler.setSceenLockPassword("");
                                }
                                else
                                {
                                    String password = operateCommand.getString("password");
                                    if (StringUtility.isValid(password))
                                    {
                                        Global.mLockHandler.setSceenLockPassword(password);
                                    }
                                    int lockNow = operateCommand.getInt("lock-now");
                                    if (lockNow == 1)
                                    {
                                        Global.mLockHandler.lockSceenNow();
                                    }

                                }

                                break;
                            case Global.COMMAND_CONTENT_CONTROL:
                                String contentURL = operateCommand.getString("content-URL");
                                if (StringUtility.isValid(contentURL))
                                {
                                    //傳給MainService去幫忙
                                    Common.postMessage(mHandler, Global.MSG_RESPONSE_EXECUTE_COMMANDER, Global.COMMAND_SERVICE_EXECUTE, Global.COMMAND_CONTENT_CONTROL, contentURL);
                                }


                                break;
                            case Global.COMMAND_APPLICATION_INSTALL_CONTROL:

                                if (operateCommand.getInt("count") != 0)
                                {
                                    JSONArray installAppList = operateCommand.getJSONArray("list");
                                    if (null != installAppList)
                                    {
                                        for (int i = 0; i < installAppList.length(); i++)
                                        {
                                            JSONObject tmp = installAppList.getJSONObject(i);
                                            String appURL = tmp.getString("app-URL");


                                            //需要時間想
                                            // int appID = 0;
                                            // Global.mApplicationHandler.installApplicationThread(appURL, null, "tmp.apk", appID);
                                            Common.postMessage(mHandler, Global.MSG_RESPONSE_EXECUTE_COMMANDER, Global.COMMAND_SERVICE_EXECUTE, Global.COMMAND_APPLICATION_INSTALL_CONTROL, appURL);
                                        }
                                    }
                                }

                                break;
                            case Global.COMMAND_APPLICATION_UNINSTALL_CONTROL:
                                if (operateCommand.getInt("count") != 0)
                                {
                                    JSONArray unInstallPackageList = operateCommand.getJSONArray("list");
                                    if (null != unInstallPackageList)
                                    {
                                        for (int i = 0; i < unInstallPackageList.length(); i++)
                                        {
                                            JSONObject tmp = unInstallPackageList.getJSONObject(i);
                                            String packName = tmp.getString("app-packageName");

                                            //需要時間想
                                            //   int appID = 0;
                                            //   Global.mApplicationHandler.unInstallApplication(packName, appID);
                                            Common.postMessage(mHandler, Global.MSG_RESPONSE_EXECUTE_COMMANDER, Global.COMMAND_SERVICE_EXECUTE, Global.COMMAND_APPLICATION_UNINSTALL_CONTROL, packName);
                                        }


                                    }


                                }

                                break;
                            case Global.COMMAND_MUTE_CONTROL:
                                if (operateCommand.getInt("value") == 0)
                                {
                                    Global.mMuteVolumeHandler.stopMute();
                                }
                                else
                                {
                                    Global.mMuteVolumeHandler.startMute();
                                }

                                break;
                            case Global.COMMAND_RECORD_CONTROL:

                                Global.mRecordHandler.record();

                                break;
                            case Global.COMMAND_RESTORE_CONTROL:

                                //傳給Service 去restore
                                Common.postMessage(mHandler, Global.MSG_RESPONSE_EXECUTE_COMMANDER, Global.COMMAND_SERVICE_EXECUTE, Global.COMMAND_RESTORE_CONTROL, null);

                                break;
                            case Global.COMMAND_WIFI_CONTROL:
                                String ssid = operateCommand.getString("ssid");

                                int encryptionType = operateCommand.getInt("encryption-type");
                                if (StringUtility.isValid(ssid))
                                {
                                    /*尚可加入是否要直接連線此ssid，為最後一個參數 false為只存不連接，
                                    * true為存入並連接*/
                                    if (encryptionType == 1)
                                    {
                                        Global.mWifiHandler.saveWIFIConfig(ssid, null, encryptionType, false);
                                    }
                                    else
                                    {
                                        String password = operateCommand.getString("password");
                                        if (StringUtility.isValid(password))
                                        {
                                            Global.mWifiHandler.saveWIFIConfig(ssid, password, encryptionType, false);
                                        }
                                    }
                                }


                                break;
                            default:
                                Logs.showTrace("Unknown COMMAND　ID: " + String.valueOf(msg.arg1));
                                break;
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }


                }
                else
                {
                    switch (msg.what)
                    {
                        case CtrlType.MSG_RESPONSE_APPLICATION_HANDLER:
                            //會回傳app add or remove，需抓住此資訊並傳給state updater cache

                            break;
                        default:
                            //如果command 執行有問題時，回傳給 MainService
                            if (msg.arg1 != ResponseCode.ERR_SUCCESS)
                            {
                                Common.postMessage(mHandler, Global.MSG_RESPONSE_EXECUTE_COMMANDER, msg.what, msg.arg1, msg.obj);
                            }
                            break;
                    }
                }
            }
        };
    }

    public void stopThread()
    {
        try
        {
            this.quitSafely();
            this.interrupt();
        }
        catch (Exception e)
        {
            Logs.showTrace("[ExecuteCommander] Exception: " + e.toString());
        }
    }

    public void sendMessage(Message msg)
    {
        if (null != msg)
        {
            if (null != selfHandler)
            {
                selfHandler.sendMessage(msg);
            }
            else
            {
                Logs.showTrace("selfHandler is null!!");
            }
        }
    }

}

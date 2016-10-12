package iii.ideas.global;

import android.app.DownloadManager;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

import iii.ideas.mdmconsole.MainApplication;
import iii.ideas.service.AppEventScheduler;
import iii.ideas.service.ExecuteCommander;
import iii.ideas.service.GetCommander;
import iii.ideas.service.StateUpdater;
import sdk.ideas.common.Logs;
import sdk.ideas.ctrl.admin.DeviceAdminHandler;
import sdk.ideas.ctrl.app.ApplicationHandler;
import sdk.ideas.ctrl.battery.BatteryHandler;
import sdk.ideas.ctrl.camera.CameraHandler;
import sdk.ideas.ctrl.content.DocumentWebViewHandler;
import sdk.ideas.ctrl.lock.LockHandler;
import sdk.ideas.ctrl.mute.MuteVolumeHandler;
import sdk.ideas.ctrl.record.RecordHandler;
import sdk.ideas.ctrl.restore.RestoreHandler;
import sdk.ideas.ctrl.space.StorageSpaceHandler;
import sdk.ideas.ctrl.wifi.WifiHandler;
import sdk.ideas.tool.googleapi.gps.FusedLocationHandler;

public class Global
{
    public static MainApplication theApplication = null;

    public static StateUpdater mStateUpdater = null;
    public static ExecuteCommander mExecuteCommander = null;
    public static GetCommander mGetCommander = null;

    public static AppEventScheduler mAppEventScheduler = null;
    //device state tracker
    public static BatteryHandler mBatteryHandler = null;
    public static StorageSpaceHandler mStorageSpaceHandler = null;
    public static FusedLocationHandler mFusedLocationHandler = null;

    //device controller
    public static DocumentWebViewHandler mDocumentWebViewHandler = null;
    public static CameraHandler mCameraHandler = null;
    public static DeviceAdminHandler mDeviceAdminHandler = null;
    public static ApplicationHandler mApplicationHandler = null;
    public static LockHandler mLockHandler = null;
    public static MuteVolumeHandler mMuteVolumeHandler = null;
    public static RecordHandler mRecordHandler = null;
    // public static RestoreHandler mRestoreHandler = null;
    public static WifiHandler mWifiHandler = null;


    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 6554;

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 8000;

    public static final int GCM_RESULT_CODE = 9520;

    public final static int REQUEST_CODE_INSTALL_APP = 3334;
    public final static int REQUEST_CODE_UNINSTALL_APP = 3335;

    public static final int MSG_RESPONSE_COMMUNICATE_SERVER = 9000;
    public static final int MSG_LOGIN = 9001;
    public static final int MSG_LOGOUT = 9002;
    public static final int MSG_GET_COMMAND = 9003;
    public static final int MSG_UPDATE_STATE = 9004;


    /*get commander define*/
    /* set time that get command from server */
    public static final int MSG_RESPONSE_GET_COMMANDER = 9500;
    public static final int MSG_GET_COMMANDER_RE_LOGIN = 9501;
    public static final int MSG_GET_COMMANDER_STOPPED = 9502;
    public static final int MSG_GET_COMMANDER_UPDATE_STATE = 9503;
    public static final int MSG_GET_COMMANDER_EXECUTE_COMMAND = 9504;

    public static final int MSG_RESULT_SUCCESS = 0;
    public static final int MSG_RESULT_SYSTEM_EXCEPTION = 1;
    public static final int MSG_RESULT_INVALID_PARAMETER = 2;
    public static final int MSG_RESULT_SYSTEM_BUSY = 3;
    public static final int MSG_RESULT_UNKNOWN_ERROR = 4;
    public static final int MSG_RESULT_INVALID_AUTHORIZATION = 5;


    /*execute command define */
    public static final int MSG_RESPONSE_EXECUTE_COMMANDER = 9800;
    public static final int ERROR_INVALID_PRIVILEGE = 9801;
    public static final int COMMAND_SERVICE_EXECUTE = 9802;


    /* state updater define */
    public static final int MSG_RESPONSE_STATE_UPDATER = 9900;

    public static final int COMMAND_CAMERA_CONTROL = 1;
    public static final int COMMAND_SCREEN_LOCK_CONTROL = 2;
    public static final int COMMAND_APPLICATION_INSTALL_CONTROL = 3;
    public static final int COMMAND_APPLICATION_UNINSTALL_CONTROL = 4;
    public static final int COMMAND_CONTENT_CONTROL = 5;
    public static final int COMMAND_MUTE_CONTROL = 6;
    public static final int COMMAND_WIFI_CONTROL = 7;
    public static final int COMMAND_RECORD_CONTROL = 8;
    public static final int COMMAND_RESTORE_CONTROL = 9;

    private static int installAppID = 11110;
    private static int uninstallAppID = 22220;

    private static SparseArray<String> updateStateData = new SparseArray<String>();

    public synchronized static SparseArray<String> setGetUpdateStateData(boolean isSet, int key, String value)
    {
        if (isSet)
        {
            updateStateData.put(key, value);
            Logs.showTrace("Print Update State!");
            Logs.showTrace("Key: " + String.valueOf(key) + " Value: " + value);
            //printUpdateStateData();
        }
        else
        {
            SparseArray<String> data = updateStateData.clone();

            updateStateData.clear();

            return data;
        }
        return null;
    }

    private static JSONObject updateAppStateData = null;

    public synchronized static JSONObject setGetUpdateAppStateData(boolean isSet, boolean isInstall, String packageName, String appName)
    {
        try
        {

            if (isSet)
            {

                if (null == updateAppStateData)
                {
                    updateAppStateData = new JSONObject();
                    updateAppStateData.put("type", MDMParameterSetting.TYPE_STATE_APP);
                    updateAppStateData.put("addlistcount", 0);
                    updateAppStateData.put("removelistcount", 0);
                }

                //update install /uninstall app
                int originAddListCount = updateAppStateData.getInt("addlistcount");
                int originRemoveListCount = updateAppStateData.getInt("removelistcount");


                if (isInstall)
                {
                    boolean isInRemoveList = false;
                    JSONObject addObject = new JSONObject();
                    addObject.put("packagename", packageName);
                    addObject.put("appname", appName);


                    if (originRemoveListCount > 0)
                    {
                        JSONArray tmp = updateAppStateData.getJSONArray("removelist");
                        for (int i = 0; i < originRemoveListCount; i++)
                        {
                            if (tmp.getJSONObject(i).getString("packagename").equals(packageName))
                            {
                                updateAppStateData.getJSONArray("removelist").remove(i);
                                updateAppStateData.remove("removelistcount");
                                updateAppStateData.put("removelistcount", --originRemoveListCount);
                                isInRemoveList = true;
                                break;
                            }
                        }
                    }
                    if (!isInRemoveList)
                    {
                        if (originAddListCount > 0)
                        {
                            updateAppStateData.getJSONArray("addlist").put(addObject);
                        }
                        else
                        {
                            JSONArray addList = new JSONArray();
                            addList.put(addObject);
                            updateAppStateData.put("addlist", addList);
                        }

                        updateAppStateData.remove("addlistcount");
                        updateAppStateData.put("addlistcount", ++originAddListCount);
                    }
                }
                else
                {
                    boolean isInAddList = false;
                    JSONObject removeObject = new JSONObject();
                    removeObject.put("packagename", packageName);
                    removeObject.put("appname", appName);
                    if (originAddListCount > 0)
                    {
                        JSONArray tmp = updateAppStateData.getJSONArray("addlist");
                        for (int i = 0; i < originAddListCount; i++)
                        {
                            if (tmp.getJSONObject(i).getString("packagename").equals(packageName))
                            {
                                updateAppStateData.getJSONArray("addlist").remove(i);
                                updateAppStateData.remove("addlistcount");
                                updateAppStateData.put("addlistcount", --originAddListCount);
                                isInAddList = true;
                                break;
                            }
                        }
                    }
                    if (!isInAddList)
                    {
                        if (originRemoveListCount > 0)
                        {
                            updateAppStateData.getJSONArray("removelist").put(removeObject);
                        }
                        else
                        {
                            JSONArray removeList = new JSONArray();
                            removeList.put(removeObject);
                            updateAppStateData.put("removelist", removeList);
                        }

                        updateAppStateData.remove("removelistcount");
                        updateAppStateData.put("removelistcount", ++originRemoveListCount);
                    }

                }
            }
            else
            {
                if (null != updateAppStateData)
                {
                    JSONObject returnData = new JSONObject(updateAppStateData.toString());
                    updateAppStateData = null;
                    return returnData;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void printAppUpdateStateData()
    {
        if (null != updateAppStateData)
        {
            Logs.showTrace("Start Print App Update State Data...");
            Logs.showTrace(updateAppStateData.toString());
            Logs.showTrace("END Print App Update State Data...");
        }
    }

    /**
     * debug use
     * print the data of the update state cache
     */
    public static void printUpdateStateData()
    {
        Logs.showTrace("Print Update State Data...");

        for (int i = 0; i < updateStateData.size(); i++)
        {
            Logs.showTrace("key: " + updateStateData.keyAt(i) + " value: " + updateStateData.valueAt(i));
        }
    }

    public synchronized static int getInstallAppID()
    {
        return installAppID++;
    }

    public synchronized static int getUninstallAppID()
    {
        return uninstallAppID--;
    }


}

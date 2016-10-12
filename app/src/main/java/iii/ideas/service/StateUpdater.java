package iii.ideas.service;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import sdk.ideas.common.Common;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.Logs;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.battery.BatteryHandler;
import sdk.ideas.ctrl.space.StorageSpaceHandler;
import sdk.ideas.tool.googleapi.gps.FusedLocationHandler;

public class StateUpdater
{
    private static StateUpdater mStateUpdater = null;
    private Context mContext = null;

    private Handler mHandler = null;
    private Thread mStateUpdaterRunnable = null;


    private Handler selfHandler = new Handler(Looper.myLooper())
    {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg)
        {
            // debug use
            // Logs.showTrace("what: " + msg.what + " result: " +
            // String.valueOf(msg.arg1) + "from: "
            // + String.valueOf(msg.arg2) + " message: " + msg.obj);

            // send problem message to outside handler hope to deal with it
            if (msg.arg1 != ResponseCode.ERR_SUCCESS)
            {
                Common.postMessage(mHandler, Global.MSG_RESPONSE_STATE_UPDATER, msg.arg1, msg.what, msg.obj);
            }
            else
            {
                if (null == msg.obj)
                {
                    Logs.showTrace("ERROR what: " + msg.what + " the message is NULL!");
                }

                else
                {
                    JSONObject jsonData = new JSONObject();
                    HashMap<String, String> datas = null;
                    try
                    {
                        datas = (HashMap<String, String>) msg.obj;

                        switch (msg.what)
                        {
                            case CtrlType.MSG_RESPONSE_BATTERY_HANDLER:
                                // debug use
                                // Logs.showTrace("Battery INFO: ");
                                jsonData.put("level", datas.get("level"));

                                break;
                            case CtrlType.MSG_RESPONSE_STORAGE_SPACE_HANDLER:

                                // debug use
                                // Logs.showTrace("Storage INFO: ");

                                DecimalFormat df = new DecimalFormat("##.00");
                                double availablePercent = (Double.parseDouble(datas.get("availablememory")) * 100.0)
                                        / Double.parseDouble(datas.get("totalmemory"));

                                jsonData.put("availablememory", df.format(availablePercent));
                                break;
                            case CtrlType.MSG_RESPONSE_FUSED_LOCATION_HANDLER:

                                // debug use
                                //Logs.showTrace("Fused Location INFO: ");
                                jsonData.put("lat", datas.get("lat"));
                                jsonData.put("lng", datas.get("lng"));

                            default:

                                break;
                        }
                    }
                    catch (ClassCastException e)
                    {
                        Logs.showTrace(e.toString());
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    // save to global state updater cache
                    Global.setGetUpdateStateData(true, msg.what, jsonData.toString());
                }
            }
        }

    };

    private StateUpdater(Context mContext, Handler mHandler)
    {
        if (null != mContext)
        {
            this.mHandler = mHandler;
            this.mContext = mContext;
            mStateUpdaterRunnable = new Thread(new StateUpdaterRunnable());

        }
    }

    public void start()
    {
        mStateUpdaterRunnable.start();
    }

    public void stop()
    {
        stopListenAction();
        mStateUpdaterRunnable.interrupt();
    }

    public static StateUpdater getInstance(Context mContext, Handler mHandler)
    {
        if (null == mStateUpdater)
        {
            mStateUpdater = new StateUpdater(mContext, mHandler);
        }
        return mStateUpdater;
    }


    private void init()
    {
        if (null == Global.mBatteryHandler)
        {
            Global.mBatteryHandler = new BatteryHandler(mContext);

            Global.mBatteryHandler.setDiffBatteryLevel(MDMParameterSetting.diffBatteryLevel);

            Global.mBatteryHandler.setHandler(selfHandler);

        }

        if (null == Global.mStorageSpaceHandler)
        {
            Global.mStorageSpaceHandler = new StorageSpaceHandler(mContext);

            Global.mStorageSpaceHandler.setCheckTime(MDMParameterSetting.storageSpaceCheckTime);
            Global.mStorageSpaceHandler.setDiffStorageSpace(MDMParameterSetting.diffStorageSpaceMB);

            Global.mStorageSpaceHandler.setHandler(selfHandler);

        }
        if (null == Global.mFusedLocationHandler)
        {
            Global.mFusedLocationHandler = new FusedLocationHandler(mContext);

            Global.mFusedLocationHandler.setUpdateTime(MDMParameterSetting.locationCheckTime);
            Global.mFusedLocationHandler.setGPSAccuracy(MDMParameterSetting.locationAccuracy);

            Global.mFusedLocationHandler.setHandler(selfHandler);
        }

    }

    private void startListenAction()
    {
        Global.mBatteryHandler.startListenAction();
        Global.mStorageSpaceHandler.startListenAction();
        Global.mFusedLocationHandler.startListenAction();

    }

    private void stopListenAction()
    {
        Global.mBatteryHandler.stopListenAction();
        Global.mStorageSpaceHandler.stopListenAction();
        Global.mFusedLocationHandler.stopListenAction();
    }

    class StateUpdaterRunnable implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                init();
                startListenAction();
            }
            catch (Exception e)
            {
                Logs.showError("[StateUpdaterRunnable]: " + e.toString());
            }
        }

    }

}

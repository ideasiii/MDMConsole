package iii.ideas.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import sdk.ideas.common.Common;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.Logs;
import sdk.ideas.common.ResponseCode;

public class GetCommander
{
    private static GetCommander mGetCommander = null;
    private Handler mHandler = null;
    private Thread mGetCommandRunnable = null;
    private CommunicateServer mCommunicateServer = null;

    private GetCommander(Context mContext, Handler mHandler)
    {
        this.mHandler = mHandler;
        init();
    }

    public static GetCommander getInstance(Context mContext, Handler mHandler)
    {
        if (null == mGetCommander)
        {
            mGetCommander = new GetCommander(mContext, mHandler);
        }
        return mGetCommander;
    }


    private void init()
    {
        mCommunicateServer = new CommunicateServer(mHandler);
        mGetCommandRunnable = new Thread(new GetCommandRunnable());
    }

    public void start()
    {
        mGetCommandRunnable.start();
    }

    public void stop()
    {
        mGetCommandRunnable.interrupt();
    }

    public void getSendCommand(Message msg)
    {
        try
        {
            if (msg.arg1 == ResponseCode.ERR_SUCCESS)
            {
                JSONObject responseOperateData = new JSONObject((String) msg.obj);

                //result Table
                // 0    Success
                // 1	Fail，System Exception
                // 2	Fail，Invalid Parameter
                // 3	Fail，System Busy
                // 4	Fail，Unknown Error
                // 5	Fail，Invalid Authorization
                switch (responseOperateData.getInt("result"))
                {
                    case Global.MSG_RESULT_SUCCESS:
                        JSONObject jsonOperate = responseOperateData.getJSONObject("control");


                        //當沒指令時，將目前狀態上傳給MDM Controller
                        if (jsonOperate.getInt("count") == 0)
                        {
                            //start to check states(such as GPS, Power, Storage...) and update it
                            SparseArray<String> mUpdateData = Global.setGetUpdateStateData(false, 0, null);

                            if (null != mUpdateData)
                            {
                                JSONObject jsonUpdateData = new JSONObject();

                                jsonUpdateData.put(MDMParameterSetting.JSON_DEVICE_ID_String, Global.theApplication.getDeviceIDData());
                                jsonUpdateData.put("count", mUpdateData.size());
                                JSONArray mJsonArray = new JSONArray();

                                for (int i = 0; i < mUpdateData.size(); i++)
                                {
                                    int key = mUpdateData.keyAt(i);
                                    String data = mUpdateData.valueAt(i);
                                    JSONObject tmp = new JSONObject(data);
                                    switch (key)
                                    {
                                        case CtrlType.MSG_RESPONSE_BATTERY_HANDLER:
                                            tmp.put("type", MDMParameterSetting.TYPE_STATE_BATTERY);
                                            break;
                                        case CtrlType.MSG_RESPONSE_STORAGE_SPACE_HANDLER:
                                            tmp.put("type", MDMParameterSetting.TYPE_STATE_STORAGE_SPACE);
                                            break;
                                        case CtrlType.MSG_RESPONSE_FUSED_LOCATION_HANDLER:
                                            tmp.put("type", MDMParameterSetting.TYPE_STATE_LOCATION);
                                            break;

                                    }

                                    mJsonArray.put(tmp);
                                }


                                JSONObject appStateUpdater = Global.setGetUpdateAppStateData(false, false, null, null);
                                if (null != appStateUpdater)
                                {
                                    int countsUpdate = jsonUpdateData.getInt("count");
                                    jsonUpdateData.remove("count");
                                    jsonUpdateData.put("count", ++countsUpdate);

                                    mJsonArray.put(appStateUpdater);
                                }

                                jsonUpdateData.put("list", mJsonArray);

                                Logs.showTrace("[GetCommander] No Command! Update state! Data: " + jsonUpdateData.toString());
                                Common.postMessage(mHandler, Global.MSG_RESPONSE_GET_COMMANDER, Global.MSG_GET_COMMANDER_UPDATE_STATE, 0, jsonUpdateData.toString());
                            }


                        }
                        //有指令時，開始執行指令
                        else
                        {
                            //get operate, send
                            JSONArray operateList = jsonOperate.getJSONArray("list");
                            for (int i = 0; i < operateList.length(); i++)
                            {
                                JSONObject operate = operateList.getJSONObject(i);
                                Common.postMessage(mHandler, Global.MSG_RESPONSE_GET_COMMANDER,
                                        Global.MSG_GET_COMMANDER_EXECUTE_COMMAND, operate.getInt("type"), operate.toString());

                            }

                        }

                        break;
                    //可能為使用者未將網路開啟，突然開啟後，MDM Controller 早已logout出去，才會發生非法的取得指令
                    //something Error,such as MDM Controller delete our login data, reLogin!
                    case Global.MSG_RESULT_INVALID_AUTHORIZATION:

                        Common.postMessage(mHandler, Global.MSG_RESPONSE_GET_COMMANDER,
                                Global.MSG_GET_COMMANDER_RE_LOGIN, 0, "please RE login Again!");
                        break;

                    default:


                        break;


                }

            }
            else
            {
                //handled error message such as server GG or Client network ERROR
                Logs.showTrace("[GetCommander] msg.what: " + String.valueOf(msg.what) + " msg.arg1: "
                        + String.valueOf(msg.arg1) + "msg.arg2: " + String.valueOf(msg.arg2) + "msg.obj: " + String.valueOf(msg.obj));

                //解決一些小問題，MDM Server ERROR 或是網路ERROR 大型問題則丟給Main Service
                //Common.postMessage(mHandler,,,,,);


            }


        }
        catch (JSONException e)
        {
            Logs.showError(e.toString());
        }
        catch (Exception e)
        {
            Logs.showError(e.toString());
        }
    }


    class GetCommandRunnable implements Runnable
    {

        @Override
        public void run()
        {
            while (!Thread.currentThread().isInterrupted())
            {
                if (MDMParameterSetting.isUnitTest)
                {
                    if (MDMParameterSetting.maxUnitTestCount <= MDMParameterSetting.unitTestCount)
                    {
                        break;
                    }
                    else
                    {
                        MDMParameterSetting.unitTestCount++;
                    }
                }

                JSONObject requestOperateData = new JSONObject();
                try
                {
                    Thread.sleep(MDMParameterSetting.GET_COMMAND_UPDATE_TIMES);
                    /*
                    Message msg = new Message();
                    msg.what = Global.COMMAND_CAMERA_CONTROL;

                    Logs.showTrace("send message!");
                    mExecuteCommander.sendMessage(msg);
                    */
                    requestOperateData.put(MDMParameterSetting.JSON_DEVICE_ID_String, Global.theApplication.getDeviceIDData());

                    mCommunicateServer.sendEvent(requestOperateData.toString(), Global.MSG_GET_COMMAND);


                    Logs.showTrace("[GetCommander] Sleep " + String.valueOf(MDMParameterSetting.GET_COMMAND_UPDATE_TIMES) + " seconds!");


                }
                catch (JSONException e)
                {
                    Logs.showError(e.toString());
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch (Exception e)
                {
                    Logs.showError(e.toString());
                }
            }
        }

        public GetCommandRunnable()
        {
        }


    }


}

package iii.ideas.global;

import org.json.JSONException;
import org.json.JSONObject;



public  class AppEvent
{
    private boolean isInstall;
    private String savePath = null;
    private String fileName = null;
    private int appID = -1;
    private String packageName = null;

    @Override
    public String toString()
    {
        if (isInstall)
        {
            return "AppEvent:[Install]" + "appID: " + String.valueOf(appID) + " fileName: " + fileName + " savePath: " + savePath;
        }
        else
        {
            return "AppEvent:[Uninstall]" + "appID: " + String.valueOf(appID) + " packageName: " + packageName;
        }
    }

    public AppEvent(boolean isInstall, String packageName, int appID)
    {
        this.isInstall = isInstall;
        this.appID = appID;
        this.packageName = packageName;
    }

    public AppEvent(boolean isInstall, String savePath, String fileName, int appID)
    {
        this.isInstall = isInstall;
        this.savePath = savePath;
        this.fileName = fileName;
        this.appID = appID;
    }

    public static JSONObject appEventToJson(AppEvent mAppEvent) throws JSONException
    {
        JSONObject jsonAppEvent = new JSONObject();
        jsonAppEvent.put("isInstall", mAppEvent.isInstall);
        jsonAppEvent.put("appID", mAppEvent.appID);
        if (mAppEvent.isInstall)
        {
            jsonAppEvent.put("savePath", mAppEvent.savePath);
            jsonAppEvent.put("fileName", mAppEvent.fileName);
        }
        else
        {
            jsonAppEvent.put("packageName", mAppEvent.packageName);
        }
        return jsonAppEvent;
    }

    public static AppEvent jsonToAppEvent(JSONObject mJsonObject) throws JSONException
    {
        AppEvent mAppEvent = null;
        boolean isInstall = mJsonObject.getBoolean("isInstall");
        if (isInstall)
        {
            mAppEvent = new AppEvent(true, mJsonObject.getString("savePath"),
                    mJsonObject.getString("fileName"), mJsonObject.getInt("appID"));
        }
        else
        {
            mAppEvent = new AppEvent(false, mJsonObject.getString("packageName"), mJsonObject.getInt("appID"));
        }

        return mAppEvent;
    }


}


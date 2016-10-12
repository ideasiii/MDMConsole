package iii.ideas.mdmconsole;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import sdk.ideas.common.Logs;

public class MainApplication extends Application
{


    public MainApplication()
    {
        Global.theApplication = this;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Logs.showTrace("application on create");

    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
    }

    public String getSharedPreferencesValue(String key)
    {
        SharedPreferences prefs = getSharedPreferences();

        return prefs.getString(key, null);
    }

    public void releaseSharedPreferences()
    {
        Editor editor = getSharedPreferences().edit();
        editor.clear();
        editor.apply();
    }

    public SharedPreferences getSharedPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void saveInSharedPref(String key, String value)
    {
        Logs.showTrace("Save Pref: Key: " + key + " value: " + value);
        Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);

        if (!editor.commit())
        {
            Logs.showError("error to write SharedPref");
        }
    }

    public void saveGCMIDData(String GCMID)
    {
        if (null != GCMID)
        {
            Global.theApplication.saveInSharedPref(MDMParameterSetting.GCM_REGISTER_ID_String, GCMID);
        }
    }

    public String getGCMIDData()
    {
        return getSharedPreferencesValue(MDMParameterSetting.GCM_REGISTER_ID_String);
    }


    public void saveAccountData(String accountString, String passwordString)
    {
        if (null != accountString && null != passwordString)
        {
            Global.theApplication.saveInSharedPref(MDMParameterSetting.ACCOUNT_String, accountString);
            Global.theApplication.saveInSharedPref(MDMParameterSetting.PASSWORD_String, passwordString);
        }
    }

    public String getAccountData()
    {
        return getSharedPreferencesValue(MDMParameterSetting.ACCOUNT_String);
    }

    public String getAccountPasswordData()
    {
        return getSharedPreferencesValue(MDMParameterSetting.PASSWORD_String);
    }

    public void saveDeviceIDData(String deviceID)
    {
        if (null != deviceID)
        {
            Global.theApplication.saveInSharedPref(MDMParameterSetting.DEVICE_ID_String, deviceID);
        }
    }

    public String getDeviceIDData()
    {
        return getSharedPreferencesValue(MDMParameterSetting.DEVICE_ID_String);
    }

    public boolean isValidSharedPreferenceData()
    {
        if (MDMParameterSetting.isUnitTest)
        {
            return true;
        }

        return (null != getDeviceIDData() && null != getAccountData()
                && null != getAccountPasswordData() && null != getGCMIDData());

    }

    public void printSharedPreferenceData()
    {
        Logs.showTrace("@@Print Shared Preference Data [START]@@");
        if (null != getDeviceIDData())
        {
            Logs.showTrace("Device ID: " + getDeviceIDData());
        }
        if (null != getAccountData())
        {
            Logs.showTrace("Account: " + getAccountData());
        }
        if (null != getAccountPasswordData())
        {
            Logs.showTrace("password: " + getAccountPasswordData());
        }
        if (null != getGCMIDData())
        {
            Logs.showTrace("GCM ID: " + getGCMIDData());
        }
        Logs.showTrace("@@Print Shared Preference Data [END]@@");
    }


}

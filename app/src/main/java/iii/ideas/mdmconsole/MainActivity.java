package iii.ideas.mdmconsole;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import iii.ideas.gcm.MyResultReceiver;
import iii.ideas.gcm.RegistrationIntentService;
import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import iii.ideas.gcm.MyResultReceiver.Receiver;
import iii.ideas.service.CommunicateServer;
import iii.ideas.service.MainService;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.DeviceInfo;
import sdk.ideas.common.GenerateUUID;
import sdk.ideas.common.Logs;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.admin.DeviceAdminHandler;
import sdk.ideas.module.DeviceHandler;
import sdk.ideas.tool.premisson.RuntimePermissionHandler;

public class MainActivity extends AppCompatActivity implements Receiver
{


    private RuntimePermissionHandler mRuntimePermissionHandler = null;

    public MyResultReceiver mReceiver = null;

    EditText emailEditText = null;
    EditText passwordEditText = null;
    Button loginButton = null;
    Button logoutButton = null;
    ProgressDialog progressDialog = null;

    private String possibleAccount = "";
    private String possiblePassword = "";


    private Handler mHandler = new Handler(Looper.myLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            Logs.showTrace("what: " + String.valueOf(msg.what) + " from: " + String.valueOf(msg.arg2) + " result: " + msg.arg1 + " message: " + msg.obj);
            switch (msg.what)
            {
                case Global.MSG_RESPONSE_COMMUNICATE_SERVER:
                    switch (msg.arg2)
                    {
                        case Global.MSG_LOGIN:
                            progressDialog.dismiss();

                            if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                            {
                                onLoginSuccess();
                            }
                            else
                            {
                                // 登入失敗，看看 message 原因
                                onLoginFailed();
                            }

                            break;
                        case Global.MSG_LOGOUT:
                            progressDialog.dismiss();
                            if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                            {
                                onLogoutSuccess();
                            }
                            else
                            {
                                // 登出失敗，看看message 原因
                                onLogoutFailed();
                            }

                            break;
                    }

                    break;

                default:

                    break;
            }
        }

    };

    private Handler uiHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case CtrlType.MSG_RESPONSE_DEVICE_ADMIN_HANDLER:
                    switch (msg.arg2)
                    {
                        case ResponseCode.METHOD_ADMIN_CREATE_POLICY:

                            if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                            {
                                Log.i("DeviceAdminSample", "Administration enabled!");
                            }
                            else
                            {
                                Log.i("DeviceAdminSample", "Administration enable FAILED!");
                            }

                            OverlayPermissionCheck();

                            break;
                        case ResponseCode.METHOD_ADMIN_REMOVE_POLICY:
                            if (msg.arg1 == ResponseCode.ERR_SUCCESS)
                            {
                                Log.i("DeviceAdminSample", "Administration remove success!");
                            }
                            else
                            {
                                Log.i("DeviceAdminSample", "Administration remove FAILED!");
                            }
                            break;
                    }

                    break;

                case CtrlType.MSG_RESPONSE_PERMISSION_HANDLER:
                    Logs.showTrace("message: " + msg.obj);
                    HashMap<String, String> message = new HashMap<String, String>((HashMap<String, String>) msg.obj);

                    boolean needAskAgainRequest = false;
                    boolean neverAskAgain = false;
                    for (Map.Entry<String, String> map : message.entrySet())
                    {
                        if (map.getValue().equals("0"))
                        {
                            needAskAgainRequest = true;
                        }
                        else
                        {
                            if (map.getValue().equals("-1"))
                            {
                                neverAskAgain = true;
                            }
                        }
                    }
                    if (needAskAgainRequest)
                    {
                        showMessageOKCancel("You need to Allow Following Permissions", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                permissionGet();
                            }
                        });
                    }
                    else
                    {
                        if (neverAskAgain)
                        {
                            showMessageOKCancel("由於您勾選never ask again permission, 本程式無法再執行下去QAQ", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    MainActivity.this.finish();
                                }
                            });
                        }
                        else
                        {
                            //開始拿取裝置權限
                            devicePermissionGet();
                        }
                    }
                    break;

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Global.theApplication.printSharedPreferenceData();

        if (Global.theApplication.isValidSharedPreferenceData())
        {
            logoutPage();
        }
        else
        {
            loginPage();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            permissionGet();
        }
        else
        {
            devicePermissionGet();
        }


    }

    public void logoutPage()
    {
        setContentView(R.layout.logout_page);
        logoutButton = (Button) findViewById(R.id.btn_logout);

        logoutButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                logout();
            }
        });

        progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);


    }


    public void loginPage()
    {
        setContentView(R.layout.activity_main);

        emailEditText = (EditText) findViewById(R.id.input_email);
        passwordEditText = (EditText) findViewById(R.id.input_password);
        loginButton = (Button) findViewById(R.id.btn_login);


        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });
        progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);

    }

    public void login()
    {
        Logs.showTrace("IN Login!");

        if (!validate())
        {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);


        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        possibleAccount = emailEditText.getText().toString();
        possiblePassword = passwordEditText.getText().toString();

        if (null == Global.theApplication.getGCMIDData())
        {
            //start to register Google Cloud Message
            gcmRegister();
        }
        else
        {
            loginToServer();
        }
    }

    public void loginToServer()
    {
        //start to create login json
        JSONObject LoginData = new JSONObject();
        try
        {
            LoginData.put("account", possibleAccount);
            LoginData.put("password", possiblePassword);

            LoginData.put("gcmid", Global.theApplication.getGCMIDData());

            LoginData.put("brand", DeviceInfo.getBrand());
            LoginData.put("model", DeviceInfo.getModel());
            LoginData.put("device", 0);

            String device_id = Global.theApplication.getDeviceIDData();
            if (null == device_id)
            {
                DeviceHandler mDeviceHandler = new DeviceHandler(this);
                device_id = mDeviceHandler.getMacAddress();

                if (null == device_id || device_id.equals("02:00:00:00:00:00"))
                {
                    device_id = GenerateUUID.uuIDRandom();
                }

                Global.theApplication.saveDeviceIDData(device_id);
            }
            LoginData.put("id", device_id);
            Logs.showTrace("Login Json Data: " + LoginData.toString());
            CommunicateServer mCommunicateServer = new CommunicateServer(this.mHandler);
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


    public void onLoginSuccess()
    {
        Logs.showTrace("onLoginSuccess");

        Global.theApplication.saveAccountData(this.possibleAccount, this.possiblePassword);
        loginButton.setEnabled(true);

        //在這換頁面
        logoutPage();

        //finish();
    }

    public void onLoginFailed()
    {
        Logs.showTrace("onLoginFailed");
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
    }

    public void logout()
    {
        logoutButton.setEnabled(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logouting...");
        progressDialog.show();

        logoutToServer();
    }

    public void logoutToServer()
    {
        JSONObject LogoutData = new JSONObject();
        try
        {
            LogoutData.put("id", Global.theApplication.getDeviceIDData());

            CommunicateServer mCommunicateServer = new CommunicateServer(this.mHandler);
            mCommunicateServer.sendEvent(LogoutData.toString(), Global.MSG_LOGOUT);
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

    public void onLogoutSuccess()
    {
        //logout do something,such as device admin cancel, stop service, clean share preferences
        Logs.showTrace("onLogoutSuccess");
        logoutButton.setEnabled(true);
        Toast.makeText(getBaseContext(), "Logout Success", Toast.LENGTH_LONG).show();
        //在這換頁面
        loginPage();

        //finish();
    }

    public void onLogoutFailed()
    {
        Logs.showTrace("onLogoutFailed");
        Toast.makeText(getBaseContext(), "Logout failed", Toast.LENGTH_LONG).show();

        logoutButton.setEnabled(true);

    }


    public boolean validate()
    {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || email.length() < 4 || email.length() > 10)
        {
            emailEditText.setError("enter between 4 and 10 valid account");
            valid = false;
        }
        else
        {
            emailEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10)
        {
            passwordEditText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        }
        else
        {
            passwordEditText.setError(null);
        }

        return valid;
    }


    public void gcmRegister()
    {
        String GCM_ID = Global.theApplication.getSharedPreferencesValue(MDMParameterSetting.GCM_REGISTER_ID_String);

        if (null == GCM_ID)
        {
            if (checkPlayServices())
            {
                mReceiver = new MyResultReceiver(new Handler());
                mReceiver.setReceiver(this);

                Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
                intent.putExtra("receiverTag", mReceiver);

                startService(intent);
            }
            else
            {
                Logs.showTrace("此裝置未支援Google Play Service!");
                finish();
            }
        }
    }


    public void devicePermissionGet()
    {
        Global.mDeviceAdminHandler = new DeviceAdminHandler(MainActivity.this);
        Global.mDeviceAdminHandler.setHandler(uiHandler);
        if (!Global.mDeviceAdminHandler.isActive())
        {
            Global.mDeviceAdminHandler.createPolicy("請給予本MDM APP以下裝置權限");
        }
    }

    public void permissionGet()
    {
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.GET_ACCOUNTS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);

        mRuntimePermissionHandler = new RuntimePermissionHandler(MainActivity.this, permissions);
        mRuntimePermissionHandler.setHandler(uiHandler);
        mRuntimePermissionHandler.startRequestPermissions();

    }


    @TargetApi(Build.VERSION_CODES.M)
    public void OverlayPermissionCheck()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Logs.showTrace("check overlay permission");
            if (!Settings.canDrawOverlays(MainActivity.this))
            {
                Logs.showTrace("overlay permission GG");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Global.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
            else
            {
                Logs.showTrace("overlay permission OK");
            }
        }

    }

    @Override
    protected void onResume()
    {

        Logs.showTrace("OnResume");

        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Logs.showTrace("OnPause");
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        switch (requestCode)
        {
            case CtrlType.REQUEST_CODE_ENABLE_ADMIN:

                Global.mDeviceAdminHandler.onActivityResult(requestCode, resultCode, data);
                break;
            case Global.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (Settings.canDrawOverlays(this))
                    {
                        Logs.showTrace("OK for MANAGE_OVERLAY");
                    }
                    else
                    {
                        Logs.showTrace("GGGGG for MANAGE_OVERLAY");
                    }
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        mRuntimePermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        if (resultCode == Global.GCM_RESULT_CODE)
        {
            String GCM_ID = resultData.getString(MDMParameterSetting.GCM_REGISTER_ID_String);
            if (null != GCM_ID)
            {
                Global.theApplication.saveGCMIDData(GCM_ID);
                Logs.showTrace("GCM ID: " + GCM_ID);

                loginToServer();
            }
            else
            {
                Logs.showError("Fail to Get GCM ID");
            }
        }
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                apiAvailability.getErrorDialog(this, resultCode, Global.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Logs.showTrace("This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public void startMDMService()
    {

        Thread t = new Thread(new ServiceRunnable(true));
        t.start();
    }

    public void stopMDMService()
    {

        Thread t = new Thread(new ServiceRunnable(false));
        t.start();
    }

    class ServiceRunnable implements Runnable
    {

        private boolean startService = false;

        @Override
        public void run()
        {
            if (startService)
            {

                Intent intent = new Intent(MainActivity.this, MainService.class);
                intent.putExtra("isLogin", "true");
                Logs.showTrace("now startService.....");
                startService(intent);

            }
            else
            {

                Intent intent = new Intent(MainActivity.this, MainService.class);
                Logs.showTrace("stopService");
                stopService(intent);

            }
        }

        public ServiceRunnable(boolean startService)
        {
            this.startService = startService;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
    {
        new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("OK", okListener).create()
                .show();
    }
}

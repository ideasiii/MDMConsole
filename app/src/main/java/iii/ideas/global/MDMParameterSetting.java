package iii.ideas.global;

import sdk.ideas.tool.googleapi.gps.GPSAccuracySet;

public abstract class MDMParameterSetting
{

    public final static boolean isUnitTest = true;
    public static int unitTestCount = 0;
    public final static int maxUnitTestCount = 2;

    public static final int GET_COMMAND_UPDATE_TIMES = 15000;
    public static final int APPEVENT_WATCH_TIMES = 5000;
    public final static int DISABLE = -1;
    public final static int ENABLE = 1;

    public static final String SERVER_IP = "54.199.198.94";
    public static final int SERVER_PORT = 6607;

    public static final String GCM_SENDER_ID = "203492990438";

    public static final String GCM_REGISTER_ID_String = "GCM_REGISTER_ID";
    public static final String ACCOUNT_String = "ACCOUNT";
    public static final String PASSWORD_String = "PASSWORD";
    public static final String DEVICE_ID_String = "DEVICEID";


    public static final String JSON_ACCOUNT_String = "account";
    public static final String JSON_PASSWORD_String = "password";
    public static final String JSON_GCM_ID_String = "gcmid";
    public static final String JSON_BRAND_String = "brand";
    public static final String JSON_MODEL_String = "model";
    public static final String JSON_DEVICE_String = "device";
    public static final String JSON_DEVICE_ID_String = "id";


    public final static int TYPE_CONTROL_CAMERA = 1;
    public final static int TYPE_CONTROL_SCREEN = 2;
    public final static int TYPE_CONTROL_INSTALL = 3;
    public final static int TYPE_CONTROL_UNINSTALL = 4;
    public final static int TYPE_CONTROL_APP_INFO = 5;
    public final static int TYPE_CONTROL_MOBILE_CONTENT = 6;

    public final static int TYPE_STATE_BATTERY = 1;
    public final static int TYPE_STATE_STORAGE_SPACE = 2;
    public final static int TYPE_STATE_LOCATION = 3;
    //當使用者有新安裝或解除安裝App時，會更新App State
    public final static int TYPE_STATE_APP = 4;



    public final static String URL_MDM_APP_DOWNLOAD = "http://54.199.198.94/app/android/";
    public final static String URL_MDM_PROFILE = "http://54.199.198.94:8080/mdm/profile/";
    public final static String MDM_PROFILE_DOWNLOAD_TEMPORARY_PATH = "Download/";
    public final static String INIT_SERVER_MDM_APP_PATH = "app_init.txt";
    public final static String INIT_SERVER_MDM_SDCARD_PATH = "sdcard_file_path_record.txt";
    public final static String URL_DOCUMENT_WEB_VIEWER = "https://docs.google.com/gview?embedded=true&url=";


    /*MDM Record Variable*/
    public final static Boolean RECORD_DATA_INTERNAL_MEMORY = true;
    public final static Boolean FLAG_RECORD_APP = true;
    public final static Boolean FLAG_RECORD_SDCARD = true;
    public final static String INIT_LOCAL_MDM_APP_PATH = "MDM_APP_INIT.data";
    public final static String INIT_LOCAL_MDM_SDCARD_PATH = "MDM_SDCARD_INIT.data";
    public final static String RECORD_DATA_WRITE_EXTERNAL_PATH = "Download";

    /*MDM Restore Activity Variable*/
    public final static String RESTORE_EVENT_CLOSE_RECEIVER = "RESTORE_EVENT_CLOSE_RECEIVER";
    public static final int RESTORE_EVENT_RESULT_CODE = 5151;
    public final static Boolean FLAG_RESTORE_APP = true;
    public final static Boolean FLAG_RESTORE_SDCARD = true;

    /*MDM App Activity Variable*/
    public static final String ACTION_ADD_APP_EVENT = "iii.ideas.service.AppEventScheduler.ADD_APP_EVENT_ACTION";
    public static final String APP_EVENT_CLOSE_RECEIVER = "APP_EVENT_CLOSE_RECEIVER";
    public static final int APP_EVENT_RESULT_CODE = 5150;


    /*MDM  Storage Space Handler Parameter Setting*/
    public final static int storageSpaceCheckTime = 6000;//60000;
    public final static int diffStorageSpaceMB = 20;//50;

    /*MDM Battery Handler Parameter Setting*/
    public final static int diffBatteryLevel = 1;//5;

    /*MDM Location Handler Parameter Setting*/
    public final static int locationCheckTime = 10000;//60000;

    public final static int locationAccuracy = GPSAccuracySet.PRIORITY_BALANCED_POWER_ACCURACY;


}

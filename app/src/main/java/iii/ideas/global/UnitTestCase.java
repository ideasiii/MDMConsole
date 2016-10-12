package iii.ideas.global;

public abstract class UnitTestCase
{

    public static final String twoCommandTest2 = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 2,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 8\n" +
            "            },\n" +
            "            {\n" +
            "                \"type\": 1,\n" +
            "                \"value\": 0\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String twoCommandTest = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 2,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 1,\n" +
            "                \"value\": 0\n" +
            "            },\n" +
            "            {\n" +
            "                \"type\": 2,\n" +
            "                \"value\": 1,\n" +
            "                \"password\": \"0122\",\n" +
            "                \"lock-now\": 0\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String noCommandTest = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 0\n" +
            "    }\n" +
            "}";


    public static final String cameraUnitTest_ON = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 1,\n" +
            "                \"value\": 1\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String cameraUnitTest_OFF = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 1,\n" +
            "                \"value\": 0\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String screenLocker_ON = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 2,\n" +
            "                \"value\": 1,\n" +
            "                \"password\": \"1234\",\n" +
            "                \"lock-now\": 1\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String screenLocker_OFF = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 2,\n" +
            "                \"value\": 0\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String documentViewer_PDF = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 5,\n" +
            "                \"content-URL\": \"http://54.199.198.94/ideas/sdk/download/doc/android/MORE_Tracker_SDK_Android.pdf\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String documentViewer_DOC = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 5,\n" +
            "                \"content-URL\": \"http://www.ntpu.edu.tw/~pa/admission/960106.doc\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String installAPK_ONE = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 3,\n" +
            "                \"count\": 1,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/AppSensorTester.apk\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String installAPK_TWO = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 3,\n" +
            "                \"count\": 2,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/papago.apk\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/AliceInWonderland.apk\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String installAPK_THREE = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 3,\n" +
            "                \"count\": 3,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/AppSensorTester.apk\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/papago.apk\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/AliceInWonderland.apk\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String uninstallAPK_ONE = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 4,\n" +
            "                \"count\": 1,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-packageName\": \"org.iii.aliceinwonderland\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String uninstallAPK_TWO="{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 4,\n" +
            "                \"count\": 2,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-packageName\": \"com.soohoobook.papago\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"app-packageName\": \"org.iii.aliceinwonderland\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String installANDuninstallAPK = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 2,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 3,\n" +
            "                \"count\": 2,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/papago.apk\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"app-URL\": \"http://54.199.198.94/app/android/AliceInWonderland.apk\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"type\": 4,\n" +
            "                \"count\": 1,\n" +
            "                \"list\": [\n" +
            "                    {\n" +
            "                        \"app-packageName\": \"app.sensor.tester\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String mute_ON = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 6,\n" +
            "                \"value\": 1\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String mute_OFF = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 6,\n" +
            "                \"value\": 0\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String wifi_NOPASSWORD = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 7,\n" +
            "                \"ssid\": \"Ready_X\",\n" +
            "                \"encryption-type\": 1\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String wifi_WPA2 = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 7,\n" +
            "                \"ssid\": \"New Time Capsule\",\n" +
            "                \"password\": \"google123!\",\n" +
            "                \"encryption-type\": 3\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String record = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 8\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    public static final String restore = "{\n" +
            "    \"result\": 0,\n" +
            "    \"control\": {\n" +
            "        \"count\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"type\": 9\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";


}

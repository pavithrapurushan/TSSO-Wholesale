package in.co.tsmith.wholesale;

public class AppConfigSettings {

    static final String ClientValidator = "MOB45831-E9SO-47B1-916C-4MIS6FAFETTS";
    static final String TAG = "MSOTAG";
    static final int WSTimeOutValueVerySmall = 20000;
    static final int WSTimeOutValueSmall = 30000;
    static final int WSTimeOutValueMedium = 45000;
    //	 static final int WSTimeOutValueHigh = 120000;
    static final int WSTimeOutValueHigh = 300000;

    static final String DeviceSettingsURL ="http://vansales.tsmithindia.com/MobSODeviceSettingsService01.asmx";//url for downloading device settings

//    public static final String WSNAMESPACE = "http://tempuri.org/";
    public static final String WSNAMESPACE = "FC58402C-6084-497E-AA37-D88B55C9EAA7";


    //This Field can Take Values MOBSO or SFASO
    static final String APPFlag = "SFASO";

    //	 static final boolean IS_OFFLINE_ENABLED = false; //Commented by 1165 on 25-02-2020
    static final boolean IS_OFFLINE_ENABLED = true;
    static final boolean showAlertOutofStock = true;

//    static final String WsUrl = "http://tsmithy.in/tssowholesale/webservice.asmx"; //Old url
    static final String WsUrl = "http://tsmithy.in/shtssowholesale/webservice.asmx"; // new
}

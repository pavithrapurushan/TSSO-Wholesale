package in.co.tsmith.wholesale;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    String DeviceId = "";

    TextView tvDeviceId;
    EditText etStore;
    EditText etSubStore;
    EditText etWsUrl;
    Button btnDownloadSettings;
    Button btnSubmitSettings;
    Button btnCancel;
    String strStore = "";
    String strSubStore = "";
    String strWsUrl = "";
    String URL = "";

    String LoggedUser = "";

    private static final String NAMESPACE = "http://tempuri.org/";

    boolean IsDeviceValid = false;
    TextView tvVersionName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            String myuniqueID;
            int myversion = Integer.valueOf(android.os.Build.VERSION.SDK);
            if (myversion < 23) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                myuniqueID = info.getMacAddress();
                if (myuniqueID == null) {
                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(SettingsActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                    myuniqueID = mngr.getDeviceId();
                }
            } else if (myversion > 23 && myversion < 29) {
                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(SettingsActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
                myuniqueID = mngr.getDeviceId();
            } else {
                String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                myuniqueID = androidId;
            }


            DeviceId = myuniqueID;
            tvDeviceId = (TextView) findViewById(R.id.tvDeviceIdValue);
            tvDeviceId.setText(DeviceId);

//            etStore = (EditText) findViewById(R.id.etStore);
//            etSubStore = (EditText) findViewById(R.id.etSubStore);
            etWsUrl = (EditText) findViewById(R.id.etUrlValue);
//            btnDownloadSettings = (Button) findViewById(R.id.btnDownloadSettings);
            btnSubmitSettings = (Button) findViewById(R.id.btnSubmit);
            btnCancel = (Button) findViewById(R.id.btnCancel);
            tvVersionName = (TextView) findViewById(R.id.tvAppVersionValue);

            etWsUrl.setText(AppConfigSettings.WsUrl);


            PackageInfo pinfo = null;
            try {
                pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionNumber = pinfo.versionCode;
                String versionName = pinfo.versionName;
                tvVersionName.setText("" + versionName);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }

//        btnDownloadSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    new DownloadSettingsTask().execute();
//                } catch (Exception ex) {
//                    Toast.makeText(SettingsActivity.this, "Unexpected Error : " + ex.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        btnSubmitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    //save url and device id here


//                    strStore = etStore.getText().toString();
//                    strSubStore = etSubStore.getText().toString();
                    strWsUrl = etWsUrl.getText().toString();
                    URL = strWsUrl;
//
//
                    if (URL.length() == 0) {
                        Toast.makeText(SettingsActivity.this, "Provide Web Service Url", Toast.LENGTH_SHORT).show();
//                        tsMessage("Message", "Provide Web Service Url");
                        return;
                    }
//
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("MiniSOStoredDevId", DeviceId);
                    editor.putString("MiniSOURL", URL);
                    editor.commit();


                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    SettingsActivity.this.startActivity(intent);
                    SettingsActivity.this.finish();

//                    boolean Res0 = edtr0.commit();
//
//                    new ValidateDeviceTask().execute();





//                    strStore = etStore.getText().toString();
//                    strSubStore = etSubStore.getText().toString();
//                    strWsUrl = etWsUrl.getText().toString();
//                    URL = strWsUrl;
//
//
//                    if (URL.length() == 0) {
//                        Toast.makeText(SettingsActivity.this, "Provide Web Service Url", Toast.LENGTH_SHORT).show();
////                        tsMessage("Message", "Provide Web Service Url");
//                        return;
//                    }
//
//                    SharedPreferences sharedPrefs0 = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
//                    SharedPreferences.Editor edtr0 = sharedPrefs0.edit();
//                    edtr0.putString("MiniSOStoredDevId", DeviceId);
//                    boolean Res0 = edtr0.commit();
//
//                    new ValidateDeviceTask().execute();

                } catch (Exception ex) {
                    Toast.makeText(SettingsActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private class ValidateDeviceTask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            validateDevice();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (IsDeviceValid == false) {

               Toast.makeText(SettingsActivity.this, "Device not valid", Toast.LENGTH_SHORT).show();
//                        tsMessage("Message", "Device Not Valid");
               return;
            }

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
            SharedPreferences.Editor edtr = sharedPrefs.edit();
            edtr.putString("MiniSOStore", strStore);
            edtr.putString("MiniSOSubStore", strSubStore);
            edtr.putString("MiniSOWSUrl", strWsUrl);

            boolean Res = edtr.commit();

            if (Res == true) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                SettingsActivity.this.startActivity(intent);
                SettingsActivity.this.finish();
            } else {
                Toast.makeText(SettingsActivity.this, "Saving Preference Failed", Toast.LENGTH_SHORT).show();
//                        tsMessage("Message", "Saving Preference Failed");
            }
        }
    }

    private void validateDevice(){

        try{
            boolean Result = false;

            String MethodName = "MobSoValidateDevice";

            SoapObject request = new SoapObject(NAMESPACE, MethodName);
            request.addProperty("ClientValidator", AppConfigSettings.ClientValidator);
            request.addProperty("Username", LoggedUser);
            request.addProperty("DeviceId", DeviceId);
            request.addProperty("Identifier1", strStore);
            request.addProperty("Identifier2", strSubStore);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL,AppConfigSettings.WSTimeOutValueVerySmall);

            androidHttpTransport.call(NAMESPACE + MethodName , envelope);

            Object result = envelope.getResponse();
            String str =  result.toString();

            str = str.replace( "<RESULT>", "");
            str = str.replace( "</RESULT>", "");

            if(str.contains("0_Success")){
                Result = true;
                IsDeviceValid = true;
            }

        } catch(Exception ex) {

            IsDeviceValid = false;
        }

    }

    private class DownloadSettingsTask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            downloadSettings();
            return null;
        }
    }

    private void downloadSettings() {
        try {

            String MethodName = "MobSoGetDeviceSettings";
            String DownloadedIdentifier = "";
            String DownloadedUrl = "";

            Log.d("ACCTAG", "Download Started");

            SoapObject request = new SoapObject(NAMESPACE, MethodName);
            request.addProperty("ClientValidator", AppConfigSettings.ClientValidator);
            request.addProperty("DeviceUniqueId", DeviceId);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.DeviceSettingsURL, AppConfigSettings.WSTimeOutValueVerySmall);
            androidHttpTransport.call(NAMESPACE + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();

            if (androidHttpTransport != null) {
                androidHttpTransport.getServiceConnection().disconnect();
            }

            Log.d("ACCTAG", str);

            str = str.replace("<RESULT>", "");
            str = str.replace("</RESULT>", "");

            if (str.contains("1_Failed_")) {
                String msg = str.replace("1_Failed_", "");
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_LONG).show();
            } else {
                String[] arr = str.split("</SETTING>");
                String setting = "";
                List<DeviceSettingsPL> lstDeviceSettingsPL = new ArrayList<DeviceSettingsPL>();
                DeviceSettingsPL clsPL;
                for (int i = 0; i < arr.length; i++) {
                    setting = arr[i].replace("<SETTING>", "");
                    clsPL = new DeviceSettingsPL();

                    clsPL.WsDeviceIdentifier = setting.substring(setting.indexOf("<ID>") + 4, setting.indexOf("</ID>"));
                    clsPL.Identifier2 = setting.substring(setting.indexOf("<ID2>") + 5, setting.indexOf("</ID2>"));
                    clsPL.WsUrl = setting.substring(setting.indexOf("<URL>") + 5, setting.indexOf("</URL>"));
                    //clsPL.CanSendSMS = Integer.valueOf(setting.substring(setting.indexOf("<SENDSMS>") + 9 ,setting.indexOf("</SENDSMS>") ));

                    lstDeviceSettingsPL.add(clsPL);
                }

                if (lstDeviceSettingsPL.size() == 1) {
                    etStore.setText(lstDeviceSettingsPL.get(0).WsDeviceIdentifier); //store
                    etSubStore.setText(lstDeviceSettingsPL.get(0).WsUrl); //wsurl
                    etWsUrl.setText(lstDeviceSettingsPL.get(0).Identifier2); //Substore
                    //canThisDeviceSendSMS = lstDeviceSettingsPL.get(0).CanSendSMS;
                } else if (lstDeviceSettingsPL.size() > 1) {

                }
            }
        } catch (Exception ex) {

        }
    }

}

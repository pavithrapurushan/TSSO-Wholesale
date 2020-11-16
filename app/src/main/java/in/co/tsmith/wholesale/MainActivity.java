package in.co.tsmith.wholesale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;

    EditText etUsername;
    EditText etPassword;
    Button btnLogin;
    String strUsername = "";
    String strPassword = "";
    String Identifier1 = "";
    String Identifier2 = "";
    String URL = "";
    String DeviceId = "";
    String strCheckLogin = "";
    String strUserPLJson = "";
    String strCheckUpdate = "";

    ImageButton imgBtnSettings;
    ImageButton imgBtnPasswordVisibility;
    boolean pswdShows  = false;

    ProgressDialog pDialog;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);
    double tsMsgDialogWindowHeight;

    SharedPreferences prefs;

    public static final int progress_bar_type = 0;
    TsCommonMethods tsCommonMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tsCommonMethods =  new TsCommonMethods(this);
        tsCommonMethods.allowPermissionsDynamically();

        try {
            String myuniqueID;
            int myversion = Integer.valueOf(android.os.Build.VERSION.SDK);
            if (myversion < 23) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                myuniqueID = info.getMacAddress();
                if (myuniqueID == null) {
                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                    myuniqueID = mngr.getDeviceId();
                }
            } else if (myversion > 23 && myversion < 29) {
                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
                myuniqueID = mngr.getDeviceId();
            } else {
                String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                myuniqueID = androidId;
            }

            DeviceId = myuniqueID;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screen_height = displayMetrics.heightPixels;
            int screen_width = displayMetrics.widthPixels;



            tsMsgDialogWindowHeight = (screen_height * 38) / 100;  //  243/640

            etUsername = (EditText) findViewById(R.id.etUsername);
            etPassword = (EditText) findViewById(R.id.etPassword);
            btnLogin = (Button) findViewById(R.id.btnLogin);
            imgBtnSettings = (ImageButton)findViewById(R.id.imgBtnSettings);
            imgBtnPasswordVisibility = (ImageButton)findViewById(R.id.imgBtnPasswordVisibility);



            new CheckForUpdateAsync().execute();

            imgBtnSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intnt = new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intnt);
                }
            });

            imgBtnPasswordVisibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!pswdShows) {
                        pswdShows = true;
//                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        imgBtnPasswordVisibility.setImageResource(R.drawable.ic_visibility_off_24px);
                    } else {
                        pswdShows = false;
                        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        imgBtnPasswordVisibility.setImageResource(R.drawable.ic_visibility_24px);
                    }

                }
            });

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Identifier1 = prefs.getString("MiniSOStore", "");
            Identifier2 = prefs.getString("MiniSOSubStore", "");
            URL = prefs.getString("MiniSOURL", "");

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);
                    if (etUsername.getText().toString().equals("") && etPassword.getText().toString().equals("")) {
                        Toast.makeText(MainActivity.this, "fields can not be empty", Toast.LENGTH_SHORT).show();

                    } else {
                        strUsername = etUsername.getText().toString();
                        strPassword = etPassword.getText().toString();

                        UserPL userPLObj = new UserPL();
                        userPLObj.Username = strUsername;
                        userPLObj.Password = strPassword;

                        Gson gson = new Gson();
                        strUserPLJson = gson.toJson(userPLObj);

//                    Call API
                        new CheckLoginTask().execute();
                    }

                }
            });
        }catch (Exception ex){
            Toast.makeText(this, ""+ex, Toast.LENGTH_SHORT).show();
        }
    }

    private class CheckLoginTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Logging in..Please wait.!!");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            strCheckLogin = "";
            checkLogin();
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strCheckLogin.equals("")||strCheckLogin == null){
                Toast.makeText(MainActivity.this, "No result", Toast.LENGTH_SHORT).show();
            }else{
                Gson gson  = new Gson();
                UserPL userPLObj = gson.fromJson(strCheckLogin,UserPL.class);
                if(userPLObj.ErrorStatus == 0){
                    prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    //instead of saving each field seperately save the entire object as json string
                    editor.putString("LoginResultStr", strCheckLogin); //added by Pavithra on 12-11-2020
                    editor.putString("CustomerDetailWS", ""); //added by Pavithra on 12-11-2020

                    editor.putString("CustomerInfo", "");   //added by Pavithra on 12-11-2020

//                    ListCustomerPL listCustomerPL = new ListCustomerPL();
//                    listCustomerPL.list.get(0).


//                    editor.putInt("UserId",userPLObj.UserId);   //following commented by Pavithra on 12-11-2020
//                    editor.putInt("UserTypeDocId",userPLObj.UserDocTypeId);
//                    editor.putString("UserType", userPLObj.UserType);
//                    editor.putString("DisplayName", userPLObj.DisplayName);
//                    editor.putString("LoginResultStr", strCheckLogin);
//                    editor.putString("CustomerInfo", "");
//                    editor.putInt("CustomerId", userPLObj.AcId);
//                    editor.putInt("StoreId", userPLObj.StoreId);

                    editor.commit();

                    Intent intent = new Intent(MainActivity.this, CustomerInfoActivity.class);
                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();

                } else {
                    tsMessages(""+userPLObj.Message);
                }
            }
        }
    }

    private void checkLogin(){
        try {
//            String MethodName = "MobSoLoginCheck";


            String MethodName = "CheckLogin";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("UserJson", strUserPLJson);


//            {"AcId":0,"Active":0,"DisplayName":"","EmpId":0,"Id":0,"Password":"abcd","StoreId":7,"UserDocTypeId":0,"UserId":0,"UserType":"","Username":"admin"}
//            request.addProperty("ClientValidator", AppConfigSettings.ClientValidator);
//            request.addProperty("Username", strUsername);
//            request.addProperty("DeviceId", DeviceId);
//            request.addProperty("Identifier1", Identifier1);
//            request.addProperty("Identifier2", Identifier2);
//            request.addProperty("Password", strPassword);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueVerySmall);
            prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            URL = prefs.getString("MiniSOURL", "");
            if(URL.equals("")){  //Added by Pavithra on 10-11-2020
                URL = AppConfigSettings.WsUrl;
            }
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueVerySmall);

            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE +"/"+ MethodName, envelope);

            Object result = envelope.getResponse();

            String str = result.toString();
            strCheckLogin = str;

        }

        catch(Exception e) {
            Log.d("MA",""+e);

        }
    }


    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.tsmessage_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("Save");
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            ImageButton imgBtnCloseSaveWindow = (ImageButton) dialog.findViewById(R.id.imgBtnClosetsMsgWindow);
            TextView tvSaveStatus = (TextView) dialog.findViewById(R.id.tvTsMessageDisplay);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = (int) tsMsgDialogWindowHeight;
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);

            imgBtnCloseSaveWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
//                    if (dialogItemLookup != null)
//                        dialogItemLookup.dismiss();
                }
            });

//            tvSaveStatus.setText("" + msg);
            tvSaveStatus.setText("" + msg);
            dialog.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }


    private class CheckForUpdateAsync extends AsyncTask<String,String,String>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            checkForUpdate();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(strCheckUpdate.equals("")|| strCheckUpdate == null){
                Toast.makeText(MainActivity.this, "Api returned an empty string, try later", Toast.LENGTH_SHORT).show();
            }else{

                int Status = 0; //1 If Update Exists
                String ApkUri = "";

                String str = strCheckUpdate;

                str = str.replace( "<RESULT>", "");
                str = str.replace( "</RESULT>", "");

                if (str.contains("1_Failed_")) {
                    String msg = str.replace("1_Failed_", "");
                    Toast.makeText(MainActivity.this,msg, Toast.LENGTH_LONG).show();
                }else {
                    try {
                        Status = Integer.valueOf(str.substring(str.indexOf("<STATUS>") + 8,str.indexOf("</STATUS>") ));
                        ApkUri = str.substring(str.indexOf("<APK_URI>") + 9,str.indexOf("</APK_URI>") );
                    } catch(Exception ex){
                        Status =0 ;
                        ApkUri="";
                    }

                    if(Status == 1 ) {
                        Log.d("MISTAG", "Status Is One");
                        showUpdatePopUp(ApkUri);
                    }

                }
            }
        }
    }


    private void  checkForUpdate(){


        try {

            String versionName = "";

            PackageInfo pinfo = null;
            try {
                pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionNumber = pinfo.versionCode;
                versionName = pinfo.versionName;
//                tvVersionName.setText("" + versionName);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String MethodName = "TSSOAppCheckUpdates";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AppName", "TSSO-Wholesale");
            request.addProperty("CurrVersion", versionName);
            request.addProperty("DeviceUniqueId", DeviceId);


            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueVerySmall);
            if(URL.equals("")){  //Added by Pavithra on 10-11-2020
                URL = AppConfigSettings.WsUrl;
            }
            URL = "http://tsmithy.in/shtssoWholesale/AppUpdationService.asmx";
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueVerySmall);

            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE +"/"+ MethodName, envelope);

            Object result = envelope.getResponse();

            String str = result.toString();
            strCheckUpdate = str;


//            <RESULT><STATUS>1</STATUS><APK_URI>http://tsmithy.in/shtssowholesale/apk/TSSO_Wholesale_0_8_11.apk</APK_URI></RESULT>
        }

        catch(Exception e) {
            Log.d("MA",""+e);

        }

    }


    public void showUpdatePopUp(final String ApkUri) {
        try
        {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setTitle("Apk Update");
            b.setMessage("New Update Available for TSMIS from Techsmith Software Pvt. Ltd.");

            Log.d("MISTAG","In showUpdatePopUp");

            b.setPositiveButton("Update",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    UpdateApp(ApkUri);
                }
            });

            b.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            b.show();
        }
        catch(Exception ex){
            Toast.makeText(MainActivity.this,ex.getMessage() , Toast.LENGTH_LONG).show();
        }
    }


    private void UpdateApp(String ApkUri) {
        try{
            UpdateApp atualizaApp;
            atualizaApp = new UpdateApp();
            atualizaApp.execute(ApkUri);
        }catch(Exception ex){
            Toast.makeText(MainActivity.this,"Error : " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    public class UpdateApp  extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
            //pDialog.show();
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
            //pDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                java.net.URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                //c.setDoOutput(true);//ONLY FOR POST REQUESTS
                c.connect();

                int lenghtOfFile = c.getContentLength();

                String PATH = "/mnt/sdcard/Download/";
//                String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, "update.apk");
                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                int kkkk=0;
                while ((len1 = is.read(buffer)) != -1) {

                    total += len1;
                    // publishing the progress.... After this onProgressUpdate will be called

                    if(kkkk == 0 )
                    {
                        kkkk = kkkk +1;
                    }

                    this.publishProgress(""+(int)((total*100)/lenghtOfFile));

                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

//                Uri uri = Uri.fromFile(new File("/mnt/sdcard/Download/update.apk"));
                Uri uri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationContext().getPackageName() + ".provider",new File("/mnt/sdcard/Download/update.apk"));

                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File("/mnt/sdcard/Download/update.apk")), "application/vnd.android.package-archive");
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "app.apk")), "application/vnd.android.package-archive");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);


//                java.io.FileNotFoundException: /mnt/sdcard/Download/update.apk (Permission denied)
            } catch (Exception e) {
                String msg = e.getMessage();
                Log.d("dgf",""+msg);
                //Do Nothing

//                android.os.FileUriExposedException: file:///mnt/sdcard/Download/update.apk exposed beyond app through Intent.getData()
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

}
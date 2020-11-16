package in.co.tsmith.wholesale;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class CustomerInfoActivity extends AppCompatActivity{

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;

    AutoCompleteTextView acvCustomerName;
    ImageButton imgBtnCustSearchbyName;

    String DeviceId = "";
    String Identifier1 = "";
    String Identifier2 = "";
    String URL = "";
    String LoggedInSalesPersonName = "";
    String strLoadcustomer = "";
    String strGetCustomerList = "";
    String strGetCustomerDetails = "";
    Button btnCreateSO;

    String CustomerName = "";
    int CustomerId;

    SharedPreferences prefs;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);

    TextView tvUserName;
    LinearLayout llCustInfoToolbar;
    double llCustInfoToolbarHeight;
    Button btnClear;

    ListCustomerPL listCustomerPLObj;

    int selectedCustomerId = 0;

    EditText etCustomerId;
    EditText etCustomerAdrs;
    EditText etCustomerMobile;
    EditText etCustomerGSTNo;
    CustomerPL customerPLObj;

    ProgressDialog pDialog;

    Button btnPayables;
    Button btnSOReport;
    TextView tvUsername;

    boolean isLookupSuccess = false;

    String strGetConfig = "";

    String UserPLObjStr = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerinfo);


        llCustInfoToolbar = (LinearLayout) findViewById(R.id.llCustInfoToolbar);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;

        llCustInfoToolbarHeight = (screen_height * 8.75) / 100;  //    56/640

        LinearLayout.LayoutParams paramsllHeader = (LinearLayout.LayoutParams) llCustInfoToolbar.getLayoutParams();
        paramsllHeader.height = (int) llCustInfoToolbarHeight;
        paramsllHeader.width = LinearLayout.LayoutParams.MATCH_PARENT;
        llCustInfoToolbar.setLayoutParams(paramsllHeader);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnPayables = (Button) findViewById(R.id.btnPayables);
        btnSOReport = (Button) findViewById(R.id.btnSOReport);
        tvUsername = (TextView)findViewById(R.id.tvUsername);


        try {
            String myuniqueID;
            int myversion = Integer.valueOf(android.os.Build.VERSION.SDK);
            if (myversion < 23) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                myuniqueID = info.getMacAddress();
                if (myuniqueID == null) {
                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(CustomerInfoActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CustomerInfoActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                    myuniqueID = mngr.getDeviceId();
                }
            } else if (myversion > 23 && myversion < 29) {
                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(CustomerInfoActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CustomerInfoActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
                myuniqueID = mngr.getDeviceId();
            } else {
                String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                myuniqueID = androidId;
            }

            DeviceId = myuniqueID;

            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Identifier1 = prefs.getString("MiniSOStore", "");
            Identifier2 = prefs.getString("MiniSOSubStore", "");
//            URL = prefs.getString("MiniSOWSUrl", "");
            URL = prefs.getString("MiniSOURL", "");

            if(URL.equals("")) {  //Added by Pavithra on 10-11-2020
                URL = AppConfigSettings.WsUrl;
            }

//            LoggedInSalesPersonName = String.valueOf(prefs.getString("LoggedSalesPersonName", ""));

//            String customerInfo = prefs.getString("CustomerInfo", "");
//            String userName = prefs.getString("DisplayName", "");
//            String userType = prefs.getString("UserType", "");
//            tvUsername.setText(""+userName);

            acvCustomerName = (AutoCompleteTextView) findViewById(R.id.acvCustomerName);
            imgBtnCustSearchbyName = (ImageButton) findViewById(R.id.imgBtnCustSearchbyName);
            etCustomerId = (EditText) findViewById(R.id.etCustomerId);
            etCustomerAdrs = (EditText) findViewById(R.id.etCustomerAdrs);
            etCustomerMobile = (EditText) findViewById(R.id.etCustomerMobile);
            etCustomerGSTNo = (EditText) findViewById(R.id.etGSTIN);
            btnCreateSO = (Button) findViewById(R.id.btnCreateSO);

            customerPLObj = new CustomerPL();

            new GetConfigTask().execute();

            Gson gson = new Gson();

            String customerInfo = prefs.getString("CustomerInfo", "");
            UserPLObjStr = prefs.getString("LoginResultStr", "");
            final UserPL userPLObj = gson.fromJson(UserPLObjStr,UserPL.class); //do null checking


//            String userType = prefs.getString("UserType", "");
//            tvUsername.setText(""+userName);
            tvUsername.setText(""+userPLObj.DisplayName);


//            if(userType.equals("CR CUSTOMER")) {
            if(userPLObj.UserType.equals("CR CUSTOMER")) {

                btnPayables.setText("PAYABLES");  //Added by Pavithra on 09-11-2020

                String loginResultStr = prefs.getString("LoginResultStr", "");

//                gson = new Gson();
//                UserPL userPLObj = gson.fromJson(loginResultStr, UserPL.class);

                customerPLObj.acId = userPLObj.AcId;
                customerPLObj.GSTIN = userPLObj.GSTIN;
                customerPLObj.Phone = userPLObj.Mobile;
                customerPLObj.Address = userPLObj.Address;
                customerPLObj.Email = userPLObj.Email;
                customerPLObj.acName = userPLObj.Name;
                customerPLObj.Pincode = userPLObj.Pincode;

                acvCustomerName.setText(userPLObj.Name);
                etCustomerId.setText("" + userPLObj.AcId);
                etCustomerAdrs.setText(userPLObj.Address);
                etCustomerMobile.setText("" + userPLObj.Mobile);
                etCustomerGSTNo.setText("" + userPLObj.GSTIN);

                acvCustomerName.setEnabled(false);
                etCustomerId.setEnabled(false);
                etCustomerAdrs.setEnabled(false);
                etCustomerMobile.setEnabled(false);
                etCustomerGSTNo.setEnabled(false);

                btnPayables.setEnabled(true);
                btnSOReport.setEnabled(true);
                btnCreateSO.setEnabled(true);

                btnPayables.setAlpha(1);
                btnSOReport.setAlpha(1);
                btnCreateSO.setAlpha(1);

            }else{
                btnPayables.setText("RECEIVABLES");  //Added by Pavithra on 09-11-2020
            }

            if(userPLObj.UserType.equals("CR CUSTOMER")){

                if(!customerInfo.equals("")|| customerInfo == null ) {
                    gson = new Gson();

                    UserPL userPL = gson.fromJson(customerInfo, UserPL.class);

                    acvCustomerName.setText("" + userPL.Name);
                    etCustomerId.setText("" + userPL.AcId);
                    etCustomerAdrs.setText("" + userPL.Address);
                    etCustomerMobile.setText("" + userPL.Mobile);
                    etCustomerGSTNo.setText("" + userPL.GSTIN);

//                    customerPLObj = gson.fromJson(customerInfo, CustomerPL.class);
//                    acvCustomerName.setText("" + customerPLObj.acName);
//                    etCustomerId.setText("" + customerPLObj.acId);
//                    etCustomerAdrs.setText("" + customerPLObj.Address);
//                    etCustomerMobile.setText("" + customerPLObj.Phone);
//                    etCustomerGSTNo.setText("" + customerPLObj.GSTIN);

                    acvCustomerName.setEnabled(false);
                    etCustomerId.setEnabled(false);
                    etCustomerAdrs.setEnabled(false);
                    etCustomerMobile.setEnabled(false);
                    etCustomerGSTNo.setEnabled(false);

                    btnPayables.setEnabled(true);
                    btnSOReport.setEnabled(true);
                    btnCreateSO.setEnabled(true);

                    btnPayables.setAlpha(1);
                    btnSOReport.setAlpha(1);
                    btnCreateSO.setAlpha(1);

                }

            }else{

                String customerDetails = prefs.getString("CustomerDetailWS", "");

                if(!customerDetails.equals("")|| customerDetails == null ) {
                    gson = new Gson();
                    ListCustomerPL listCustomerPL = gson.fromJson(customerDetails, ListCustomerPL.class);
                    customerPLObj = listCustomerPL.list.get(0);
//                    customerPLObj = gson.fromJson(customerInfo, CustomerPL.class);
                    acvCustomerName.setText("" + customerPLObj.acName);
                    etCustomerId.setText("" + customerPLObj.acId);
                    etCustomerAdrs.setText("" + customerPLObj.Address);
                    etCustomerMobile.setText("" + customerPLObj.Phone);
                    etCustomerGSTNo.setText("" + customerPLObj.GSTIN);

                    acvCustomerName.setEnabled(false);
                    etCustomerId.setEnabled(false);
                    etCustomerAdrs.setEnabled(false);
                    etCustomerMobile.setEnabled(false);
                    etCustomerGSTNo.setEnabled(false);

                    btnPayables.setEnabled(true);
                    btnSOReport.setEnabled(true);
                    btnCreateSO.setEnabled(true);

                    btnPayables.setAlpha(1);
                    btnSOReport.setAlpha(1);
                    btnCreateSO.setAlpha(1);

                }

            }


//            if(!customerInfo.equals("")|| customerInfo == null ) {
//                gson = new Gson();
//                customerPLObj = gson.fromJson(customerInfo, CustomerPL.class);
//                acvCustomerName.setText("" + customerPLObj.acName);
//                etCustomerId.setText("" + customerPLObj.acId);
//                etCustomerAdrs.setText("" + customerPLObj.Address);
//                etCustomerMobile.setText("" + customerPLObj.Phone);
//                etCustomerGSTNo.setText("" + customerPLObj.GSTIN);
//
//                acvCustomerName.setEnabled(false);
//                etCustomerId.setEnabled(false);
//                etCustomerAdrs.setEnabled(false);
//                etCustomerMobile.setEnabled(false);
//                etCustomerGSTNo.setEnabled(false);
//
//                btnPayables.setEnabled(true);
//                btnSOReport.setEnabled(true);
//                btnCreateSO.setEnabled(true);
//
//                btnPayables.setAlpha(1);
//                btnSOReport.setAlpha(1);
//                btnCreateSO.setAlpha(1);
//
//            }


            acvCustomerName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    acvCustomerName.setAdapter(null); //this added to avoid old suggestions in autocomplete

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });



            acvCustomerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                        long id) {


                    String selectedCustomer = (String) parent.getItemAtPosition(pos);
                    selectedCustomerId = listCustomerPLObj.list.get(pos).acId;





                    prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("CustomerId", selectedCustomerId);  //customer id here will be zero in customer logincase
                    editor.commit();



                    new GetCustomerDetailsTask().execute();

                    isLookupSuccess = true;

                }
            });

            imgBtnCustSearchbyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (acvCustomerName.getText().toString().equalsIgnoreCase("")|| acvCustomerName.getText().toString().length() <3) {

                        Toast.makeText(CustomerInfoActivity.this, "Please input minimum 3 characters", Toast.LENGTH_SHORT).show();

                    } else {
                        new GetCustomerListTask().execute();
                    }
                }
            });

            btnPayables.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString("CustomerNameWS", acvCustomerName.getText().toString());

                    editor.commit();


                    Intent intnt = new Intent(CustomerInfoActivity.this,PayablesActivity.class);
                    startActivity(intnt);
                    CustomerInfoActivity.this.finish();

                }
            });

            btnSOReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("CustomerNameWS", acvCustomerName.getText().toString());
                    editor.commit();

                    Intent intnt = new Intent(CustomerInfoActivity.this, SOReportActivity.class);
                    startActivity(intnt);
                    CustomerInfoActivity.this.finish();
                }
            });

            btnCreateSO.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    if (acvCustomerName.getText().toString().equals("")|| acvCustomerName == null){
                        Toast.makeText(CustomerInfoActivity.this, "Select a customer", Toast.LENGTH_SHORT).show();
                    }else {
//                        if(isLookupSuccess) {


                            Gson gson = new Gson();
                            String customerInfo = gson.toJson(customerPLObj);
                            prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString("CustomerInfo", customerInfo);

                            if(userPLObj.UserType.equals("CR CUSTOMER")){

                                editor.putString("CustomerInfo", UserPLObjStr);

                            }else{

                            }


                            editor.putString("CustomerNameWS", acvCustomerName.getText().toString());
                            editor.putString("BillRemarksWSSO", "");
                            editor.putBoolean("IsSavedTssoWS", false);
                            editor.commit();

                            Intent intnt = new Intent(CustomerInfoActivity.this, SOActivity.class);
                            startActivity(intnt);
                            CustomerInfoActivity.this.finish();
//                        }else{
//                            Toast.makeText(CustomerInfoActivity.this, "Please select a valid customer", Toast.LENGTH_SHORT).show();
//                        }
                    }
                }
            });

            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    view.startAnimation(buttonClick);

                    try {
                        acvCustomerName.setText("");
                        etCustomerId.setText("");
                        etCustomerAdrs.setText("");
                        etCustomerMobile.setText("");
                        etCustomerGSTNo.setText("");

                        acvCustomerName.setEnabled(true);
                        etCustomerId.setEnabled(true);
                        etCustomerAdrs.setEnabled(true);
                        etCustomerMobile.setEnabled(true);
                        etCustomerGSTNo.setEnabled(true);

                        prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("CustomerId", 0);
//                        editor.putInt("StoreId", 0);
                        editor.putString("CustomerInfo", "");
                        editor.commit();

                        btnPayables.setEnabled(false);
                        btnSOReport.setEnabled(false);
                        btnCreateSO.setEnabled(false);

                        btnPayables.setAlpha(0.4f);
                        btnSOReport.setAlpha(0.4f);
                        btnCreateSO.setAlpha(0.4f);

                    } catch (Exception ex) {
                        Toast.makeText(CustomerInfoActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }

    }

    private class GetCustomerListTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CustomerInfoActivity.this);
            pDialog.setMessage("Loading customers...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            getCustomerList();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strGetCustomerList == null || strGetCustomerList.equals("")) {

                Toast.makeText(CustomerInfoActivity.this, "No result from web", Toast.LENGTH_SHORT).show();

            } else {

                Gson gson = new Gson();
                listCustomerPLObj = gson.fromJson(strGetCustomerList, ListCustomerPL.class);

                if(listCustomerPLObj.ErrorStatus == 0) {

                    String[] arrCust = new String[ listCustomerPLObj.list.size()];

                    for(int i = 0; i < listCustomerPLObj.list.size(); i++) {
                        arrCust[i] = listCustomerPLObj.list.get(i).acName;
                    }

                    //Added by Pavithra on 04-08-2020
                    AutocompleteCustomArrayAdapter myAdapter = new AutocompleteCustomArrayAdapter(CustomerInfoActivity.this, R.layout.autocomplete_view_row, arrCust);
                    acvCustomerName.setAdapter(myAdapter);
                    acvCustomerName.showDropDown();

                }else{
                    Toast.makeText(CustomerInfoActivity.this, ""+listCustomerPLObj.Message, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void getCustomerList() {

        try {
            String MethodName = "GetCustomersList";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("UserId", 1);
            request.addProperty("Filter", acvCustomerName.getText().toString());
            request.addProperty("Guid", "");
//            request.addProperty("UserType", "CR CUSTOMER");
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetCustomerList = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }
    }

    private class GetCustomerDetailsTask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            getCustomerDetails();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(strGetCustomerDetails == null || strGetCustomerDetails.equals("")){

            }else {

                Gson gson = new Gson();
                listCustomerPLObj = gson.fromJson(strGetCustomerDetails, ListCustomerPL.class);

                if(listCustomerPLObj.ErrorStatus == 0){
//                    gson = new Gson();
//                    String customerDetailJsonStr = gson.toJson(listCustomerPLObj);
                    prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.putString("CustomerDetailWS", customerDetailJsonStr);
                    editor.putString("CustomerDetailWS", strGetCustomerDetails);
                    editor.commit();


                    etCustomerId.setText("" + selectedCustomerId);
                    etCustomerAdrs.setText("" + listCustomerPLObj.list.get(0).Address);
                    etCustomerMobile.setText("" +listCustomerPLObj.list.get(0).Phone);
                    etCustomerGSTNo.setText("" + listCustomerPLObj.list.get(0).GSTIN);



                    acvCustomerName.setEnabled(false);
                    etCustomerId.setEnabled(false);
                    etCustomerAdrs.setEnabled(false);
                    etCustomerMobile.setEnabled(false);
                    etCustomerGSTNo.setEnabled(false);

                    btnPayables.setEnabled(true);
                    btnSOReport.setEnabled(true);
                    btnCreateSO.setEnabled(true);

                    btnPayables.setAlpha(1);
                    btnSOReport.setAlpha(1);
                    btnCreateSO.setAlpha(1);


                }else {
                    Toast.makeText(CustomerInfoActivity.this, "" + listCustomerPLObj.Message, Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    private void getCustomerDetails() {

        try {
            String MethodName = "GetCustomerDetails";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("Guid", "");
            request.addProperty("acId", selectedCustomerId);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetCustomerDetails = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomerInfoActivity.this);
        alertDialogBuilder.setMessage("Do you want to exit the application?");
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("CustomerInfo", "");
                editor.putString("LoginResultStr", ""); //added by Pavithra on 12-11-2020
                editor.putString("CustomerDetailWS", ""); //added by Pavithra on 12-11-2020
                editor.putInt("CustomerId", 0);
                editor.putInt("StoreId", 0);
                editor.commit();

                System.exit(0);
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //Added by Pavithra on 12-11-2020
    private class GetConfigTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            getConfig();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(strGetConfig.equals("")|| strGetConfig == null){
                Toast.makeText(CustomerInfoActivity.this, "Api returned an empty string, try later", Toast.LENGTH_SHORT).show();
            }else {
                String store_id = strGetConfig;
                store_id = store_id.replace("\"", "");
                prefs = PreferenceManager.getDefaultSharedPreferences(CustomerInfoActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("StoreId", Integer.valueOf(store_id));
                editor.commit();
            }
        }
    }

    private void getConfig() {
        try {
            String MethodName = "GetConfig";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
//            request.addProperty("StringID", "SOHD63CB-0739-41B7-A00C-4A4F2E9ECAD8");
            request.addProperty("StringID", "E2114C92-BD10-4139-801A-EEF145B083C6"); //stay happi Guid
            request.addProperty("SOGUID", "");

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
//            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + MethodName, envelope);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetConfig = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
//            java.net.SocketTimeoutException: failed to connect to tsmithy.in/49.50.65.147 (port 80) from /25.7.201.187 (port 44110) after 45000ms: isConnected failed: ETIMEDOUT (Connection timed out)
        }
    }

}

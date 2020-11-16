package in.co.tsmith.wholesale;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

//Modified by Pavithra on 10-10-2020
public class SOActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;

    ImageButton ic_search;
    EditText etItemSearchSOActivity;
    AutoCompleteTextView acvItemSearchSOActivity;
    String itemSearchStringLookup = "";
    String DeviceId = "";
    String Identifier1 = "";
    String Identifier2 = "";
    String URL = "";
    String LoggedInSalesPersonName = "";
    String strLoadProductList = "";

    Dialog dialogItemLookup;

    AutoCompleteTextView etItemSearchLookup;
    ImageButton imgBtnItemSearchLookup;
    ListView lvItemsPopup;

    String CustomerType = "";
    int CustomerId;

    int SRSelectedItemId = 0;
    String SelectedItemName = "";
    int SRSelectedBatchId = 0;
    String strGetItemDetailsForSO = "";
    String strSaveSO = "";

    int SelectedFormId = -1;

    Dialog qtydialog;

    List<SalesBillDetailPL> lstProducts;
    ListView lvProductlist;

    String strSalesBill = "";
    int SalesOrderModel = 1;
    int SOSummaryId = 0;

    Button btnSave;
    Button btnNew;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);

    String CustomerName = "";

    LinearLayout llSOToolbar;
    LinearLayout llBottomBar;
    double llCustInfoToolbarHeight;
    double llBottomHeight;
    double tsMsgDialogWindowHeight;

    TextView tvSODocNo;
    TextView tvBilltotal;
    ImageButton imgBtnRemarksPrescrptn;
    SharedPreferences prefs;

    String billRemarks = "";
    Dialog dialog;
    double saveDialogWindowHeight;
    double qtyDialogwindowHeight;

    ProgressDialog pDialog;
    boolean isSaved = false;

    String listOfItemsAddedStr = "";
    String strGetConfig = "";
    String strGetItemsList = "";
    String strGetItemDetails = "";
    ListItemPL listItemPLObj;
    int itemId = 0;
    SOPL soplObj;
    SODetailPL soDetailPLObj;
    List<SODetailPL> listSODetailPL;
    int itemQty = 0;
    int billedQty = 0;
    String strGetFreeStock = "";

    EditText etQty;
    TextView tvCustomerName;
    ImageButton  imgBtnBackSO;
    boolean isOutofStock = false;

    Dialog dialogOutofStock;
    String strErrorMsg =  "";

//    boolean isEntered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so);

        llSOToolbar = (LinearLayout) findViewById(R.id.llSOToolbar);

        tvCustomerName = (TextView) findViewById(R.id.tvCustomerName);
        imgBtnBackSO = (ImageButton) findViewById(R.id.imgBtnBackSO);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;

        llCustInfoToolbarHeight = (screen_height * 8.75) / 100;  //    56/640


        saveDialogWindowHeight = (screen_height * 42) / 100;

        tsMsgDialogWindowHeight = (screen_height * 38) / 100;  //  243/640
//        qtyDialogwindowHeight = (screen_height * 60) / 100;
        qtyDialogwindowHeight = (screen_height * 75) / 100;

        LinearLayout.LayoutParams paramsllHeader = (LinearLayout.LayoutParams) llSOToolbar.getLayoutParams();
        paramsllHeader.height = (int) llCustInfoToolbarHeight;
        paramsllHeader.width = LinearLayout.LayoutParams.MATCH_PARENT;
        llSOToolbar.setLayoutParams(paramsllHeader);

        listSODetailPL = new ArrayList<>();

        ic_search = (ImageButton) findViewById(R.id.imgBtnSearchItem);
//        etItemSearchSOActivity = (EditText)findViewById(R.id.etItemSearchSOActivity);
        acvItemSearchSOActivity = (AutoCompleteTextView) findViewById(R.id.acvItemSearchSOActivity);
        lvProductlist = (ListView) findViewById(R.id.lvProductlist);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnNew = (Button) findViewById(R.id.btnNew);

        tvSODocNo = (TextView) findViewById(R.id.tvSODocNo);
        tvBilltotal = (TextView) findViewById(R.id.tvBilltotal);
        imgBtnRemarksPrescrptn = (ImageButton) findViewById(R.id.imgBtnRemarksPrescrptn);

        lstProducts = new ArrayList<>();
        try {
            String myuniqueID;
            int myversion = Integer.valueOf(android.os.Build.VERSION.SDK);
            if (myversion < 23) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                myuniqueID = info.getMacAddress();
                if (myuniqueID == null) {
                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(SOActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SOActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                    myuniqueID = mngr.getDeviceId();
                }

            } else if (myversion > 23 && myversion < 29) {
                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(SOActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SOActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
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

            //following added by Pavithra on 12-11-2020
            Gson gson = new Gson();

            String UserPLObjStr = prefs.getString("LoginResultStr", "");
            UserPL userPLObj = gson.fromJson(UserPLObjStr,UserPL.class); //do null checking

            if(userPLObj.UserType.equals("CR CUSTOMER")){

                CustomerId = userPLObj.AcId;

            } else {
                CustomerId = prefs.getInt("CustomerId",0);
            }



//            CustomerId = prefs.getInt("CustomerId", 0); // Commented by Pavithra on 12-11-2020
            CustomerName = prefs.getString("CustomerNameWS", "");

            TextView tvCustomerName = (TextView) findViewById(R.id.tvCustomerName);
            tvCustomerName.setText("" + CustomerName);

            imgBtnBackSO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isSaved){
                        showAlertMessage();

                    }else{
                        btnSave.setEnabled(true);
                        Intent intent = new Intent(SOActivity.this, CustomerInfoActivity.class); //Added by Pavithra on 29-08-2020
                        startActivity(intent);
                        SOActivity.this.finish(); //Added by Pavithra on 10-09-2020
                    }


                }
            });



            ic_search.setEnabled(true); //added by Pavithra on 21-10-2020

            new GetConfigTask().execute();

            acvItemSearchSOActivity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    acvItemSearchSOActivity.setAdapter(null); //this added to avoid old suggestions in autocomplete

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            acvItemSearchSOActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
                    try {

                        String SelectedText = acvItemSearchSOActivity.getText().toString();

                        if (SelectedText.length() >= 3) {
                            itemId = listItemPLObj.list.get(pos).ItemId;
                            new GetItemDetailsTask().execute();

                        } else {
                            acvItemSearchSOActivity.setAdapter(null);
                        }
                    } catch (Exception ex) {
                        Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                    }
                }
            });


            imgBtnRemarksPrescrptn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
                        billRemarks = prefs.getString("BillRemarksWSSO", "");
                        dialog = new Dialog(SOActivity.this);
                        dialog.setContentView(R.layout.add_remarks);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setTitle("Remarks");
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


                        ImageButton imgBtnCloseRemarksWindow = (ImageButton) dialog.findViewById(R.id.imgBtnCloseRemarksWindow);
                        Button btnOkRemarks = (Button) dialog.findViewById(R.id.btnOkRemarks_Itemwise);
                        Button btnClearRemarks_Itemwise = (Button) dialog.findViewById(R.id.btnClearRemarks_Itemwise);
                        final EditText etAddRemarks = (EditText) dialog.findViewById(R.id.etAddRemarks_Itemwise);
                        etAddRemarks.setText(billRemarks);


                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = (int) saveDialogWindowHeight;
                        lp.gravity = Gravity.CENTER;
                        dialog.getWindow().setAttributes(lp);


                        imgBtnCloseRemarksWindow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        btnOkRemarks.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                billRemarks = etAddRemarks.getText().toString();

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("BillRemarksWSSO", billRemarks);
                                editor.commit();

                                if (billRemarks.equalsIgnoreCase("")) {
                                    imgBtnRemarksPrescrptn.setImageResource(R.drawable.ic_remarks_item);
                                } else {
                                    imgBtnRemarksPrescrptn.setImageResource(R.drawable.ic_remarks_colrchanged);
                                }

                                dialog.dismiss();

                            }
                        });

                        btnClearRemarks_Itemwise.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                etAddRemarks.setText("");
                            }
                        });

                        dialog.show();

                    } catch (Exception ex) {
                        Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                    }

                }
            });


            ic_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {

                        if (acvItemSearchSOActivity.getText().toString().equalsIgnoreCase("")) {
                            Toast.makeText(SOActivity.this, "Please input minimum 3 characters", Toast.LENGTH_SHORT).show();
                        } else {
                            itemSearchStringLookup = acvItemSearchSOActivity.getText().toString();
                            new GetItemsListTask().execute();
                        }

                    } catch (Exception ex) {
                        Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);


//                    listOfItemsAddedStr = prefs.getString("ListOfItemsAddedMiniSO", "");
//                    Gson gson1 = new Gson();
//                    if (listOfItemsAddedStr != null && !listOfItemsAddedStr.equals("")) {
//                        lstProducts = gson1.fromJson(listOfItemsAddedStr, new TypeToken<List<SalesBillDetailPL>>() {
//                        }.getType());
//                    }

//                    if(lstProducts.size() > 0) {

                    List<String> itemOutofStock = new ArrayList<>();

                    if (listSODetailPL.size() > 0) {
                        for(int i = 0; i < listSODetailPL.size(); i++){
                            if(listSODetailPL.get(i).SOH < listSODetailPL.get(i).Qty){
//                                String[] outofStockItems = new String[]

                                itemOutofStock.add(0,listSODetailPL.get(i).Name);
                                isOutofStock = true;

//                                if(AppConfigSettings.showAlertOutofStock) {
//                                    //show a message out of stock
////                                    tsMessages("Item is outofstock");
//                                    outOfStockAlert("Outof stock items are ..");
//                                }
                            }
                        }
                        if(isOutofStock){
                            if(AppConfigSettings.showAlertOutofStock) {
                                //show a message out of stock
//                                    tsMessages("Item is outofstock");
                                String items = "";
                                for(int i =0; i < itemOutofStock.size(); i++){
                                    items = items+"\n"+itemOutofStock.get(i);
                                }
//                                outOfStockAlert("Outof stock items are ..\n"+items);
                                outOfStockAlert("Following item(s) are out of stock ..\n"+items);
                            }else{
                                new SaveSOTask().execute();
                            }
                        }else{
                            new SaveSOTask().execute();
                        }
//                        new SaveSOTask().execute();

                    } else {
                        Toast.makeText(SOActivity.this, "Nothing to save", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btnNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    if (!isSaved) {
                        showAlertMessage();
                    } else {

                        btnSave.setEnabled(true);
                        Intent intent = new Intent(SOActivity.this, CustomerInfoActivity.class); //Added by Pavithra on 29-08-2020
                        startActivity(intent);
                        SOActivity.this.finish(); //Added by Pavithra on 10-09-2020
                    }
                }
            });


            imgBtnRemarksPrescrptn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    try {
                        prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
                        billRemarks = prefs.getString("BillRemarksWSSO", "");
                        dialog = new Dialog(SOActivity.this);
                        dialog.setContentView(R.layout.add_remarks);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setTitle("Remarks");
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        ImageButton imgBtnCloseRemarksWindow = (ImageButton) dialog.findViewById(R.id.imgBtnCloseRemarksWindow);
                        Button btnOkRemarks = (Button) dialog.findViewById(R.id.btnOkRemarks_Itemwise);
                        Button btnClearRemarks_Itemwise = (Button) dialog.findViewById(R.id.btnClearRemarks_Itemwise);
                        final EditText etAddRemarks = (EditText) dialog.findViewById(R.id.etAddRemarks_Itemwise);
                        etAddRemarks.setText(billRemarks);

                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = (int) saveDialogWindowHeight;
                        lp.gravity = Gravity.CENTER;
                        dialog.getWindow().setAttributes(lp);


                        imgBtnCloseRemarksWindow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        btnOkRemarks.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                billRemarks = etAddRemarks.getText().toString();

                                SharedPreferences.Editor editor = prefs.edit();
//                                BillRemarksAndroidSO
                                editor.putString("BillRemarksWSSO", billRemarks);
                                editor.commit();

                                if (billRemarks.equalsIgnoreCase("")) {
                                    imgBtnRemarksPrescrptn.setImageResource(R.drawable.ic_remarks_item);
                                } else {
                                    imgBtnRemarksPrescrptn.setImageResource(R.drawable.ic_remarks_colrchanged);
                                }

                                dialog.dismiss();

                            }
                        });

                        btnClearRemarks_Itemwise.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                etAddRemarks.setText("");
                            }
                        });

                        dialog.show();

                    } catch (Exception ex) {
                        Toast.makeText(SOActivity.this, "" + ex, Toast.LENGTH_SHORT).show();
                    }

                }
            });

        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }

    }

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
                Toast.makeText(SOActivity.this, "Api returned an empty string, try later", Toast.LENGTH_SHORT).show();
            }else {
                String store_id = strGetConfig;
                store_id = store_id.replace("\"", "");
                prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
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

    private class GetItemsListTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Loading products...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            getItemsList();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if (strGetItemsList.equals("") || strGetItemsList == null) {
                Toast.makeText(SOActivity.this, "No result from web", Toast.LENGTH_SHORT).show();
            } else {

                Gson gson = new Gson();
                listItemPLObj = new ListItemPL();
                listItemPLObj = gson.fromJson(strGetItemsList, ListItemPL.class);
                if (listItemPLObj.ErrorStatus == 0) {

                    String[] arrProducts = new String[listItemPLObj.list.size()];
                    for (int i = 0; i < listItemPLObj.list.size(); i++) {

                        arrProducts[i] = listItemPLObj.list.get(i).ItemName;

                    }

                    AutoCompleteProductListCustomAdapter myAdapter = new AutoCompleteProductListCustomAdapter(SOActivity.this, R.layout.autocomplete_view_row, arrProducts);
                    acvItemSearchSOActivity.setAdapter(myAdapter);
                    acvItemSearchSOActivity.showDropDown();

                } else {
                    tsMessages("" + listItemPLObj.Message);
                }
            }
        }
    }

    private void getItemsList() {

//        CustomerId StoreId

        prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
//        int cust_id = prefs.getInt("CustomerId", 0); //commented by Pavithra on 12-11-2020
        int store_id = prefs.getInt("StoreId", 0);

        try {
            String MethodName = "GetItemsList";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
//            request.addProperty("CustomerId", cust_id); //Commented by Pavithra on 12-11-2020
            request.addProperty("CustomerId", CustomerId);
            request.addProperty("Filter", acvItemSearchSOActivity.getText().toString());
            request.addProperty("IsSOHCheck", "0");
            request.addProperty("StoreId", store_id);

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
            strGetItemsList = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }
    }

    private class GetItemDetailsTask extends AsyncTask<String, String, String> {



        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Loading item details...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            getItemDetails();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (strGetItemDetails == null || strGetItemDetails.equals("")) {
                Toast.makeText(SOActivity.this, "No result from web", Toast.LENGTH_SHORT).show();
            } else {

                soplObj = new SOPL();
                Gson gson = new Gson();
                soplObj = gson.fromJson(strGetItemDetails, SOPL.class);

//                if(isEntered) {
//                    isEntered = false;
//                }else{
//                    isEntered = true;
//                }
//
//                if(!isEntered) {
//                    soplObj.ErrorStatus = 1;
//                    soplObj.Message = "Manadatory error";
//                }

                if (soplObj.ErrorStatus == 0) {

                    qtydialog = new Dialog(SOActivity.this);
                    qtydialog.setContentView(R.layout.quantity_selection_dialogwindow);
                    qtydialog.setCanceledOnTouchOutside(false);
                    qtydialog.setTitle("Quantity Selection");
                    qtydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    acvItemSearchSOActivity.setAdapter(null);

                    ImageButton cancel = (ImageButton) qtydialog.findViewById(R.id.imgBtnCloseQtySelection);
                    TextView tvSelectedItemName = (TextView) qtydialog.findViewById(R.id.tvSelectedItemName);
                    TextView tvMrp = (TextView) qtydialog.findViewById(R.id.tvMrpInQtySelection);
                    TextView tvSOH = (TextView) qtydialog.findViewById(R.id.tvSOHInQtySelection);
                    TextView tvOfferDesc = (TextView) qtydialog.findViewById(R.id.tvOfferDesc);
                    ImageButton btnPlus = (ImageButton) qtydialog.findViewById(R.id.imgBtnPlusPack);
                    ImageButton btnMinus = (ImageButton) qtydialog.findViewById(R.id.imgBtnMinusPack);
                    Button btnAdd = (Button) qtydialog.findViewById(R.id.btnAddItem_qtySelection);

//                    final EditText etQty = (EditText) qtydialog.findViewById(R.id.etQty);
                    etQty = (EditText) qtydialog.findViewById(R.id.etQty);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(qtydialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//                    lp.height = (int) qtyDialogwindowHeight;  //Commented by Pavithra on 09-11-2020 due to offer is missing in some devices.
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT; //Added by Pavithra on 09-11-2020

                    lp.gravity = Gravity.CENTER;
                    qtydialog.getWindow().setAttributes(lp);

                    tvSelectedItemName.setText("" + soplObj.items.get(0).Name);
//                    tvMrp.setText("MRP : " + soplObj.items.get(0).Mrp);
//                    tvSOH.setText("SOH : " + soplObj.items.get(0).SOH);

//                    tvMrp.setText("MRP : " + soplObj.items.get(0).Mrp);

                    tvMrp.setText("MRP : " + String.format("%.2f", soplObj.items.get(0).Mrp));
                    tvSOH.setText("SOH : " + String.format("%.2f", soplObj.items.get(0).SOH));

                    if(!soplObj.items.get(0).OfferDesc.equals("")) {
//                        tvOfferDesc.setText("OFFER!! " + soplObj.items.get(0).OfferDesc);
                        tvOfferDesc.setText("" + soplObj.items.get(0).OfferDesc);
                    }

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            qtydialog.dismiss();
                            acvItemSearchSOActivity.setText("");
                        }
                    });


                    btnPlus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                if (etQty.getText().toString().equals("") || etQty.getText().toString() == null) {
                                    etQty.setText("0");
                                }

                                itemQty = Integer.parseInt(etQty.getText().toString());
                                itemQty += 1;
                                etQty.setText("" + itemQty);

                            } catch (Exception ex){
                                Toast.makeText(SOActivity.this, ""+ex, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btnMinus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (!etQty.getText().toString().equals("") && etQty.getText().toString() != null) {

                                    itemQty = Integer.parseInt(etQty.getText().toString());
                                    if (itemQty >= 1)
                                        itemQty -= 1;

                                    etQty.setText("" +  itemQty);

                                } else {
                                    Toast.makeText(SOActivity.this, "Qty field is empty", Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception ex){
                                Toast.makeText(SOActivity.this, ""+ex, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //what we need here is product details to add to product list listview

                            try {

                                if (Integer.parseInt(etQty.getText().toString()) < 1) {
                                    Toast.makeText(SOActivity.this, "Qty cannot be less than 1", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                boolean isRepeatItem = false;
                                for(int i = 0; i < listSODetailPL.size(); i++){
                                    if(soplObj.items.get(0).ItemId == listSODetailPL.get(i).ItemId){
                                        isRepeatItem = true;
                                        billedQty = Integer.parseInt(etQty.getText().toString())+ listSODetailPL.get(i).Qty;

                                    }

                                }


                                if (soplObj.items.get(0).SchemeExists == 1) {
                                    if(!isRepeatItem) {
                                        billedQty = Integer.parseInt(etQty.getText().toString());
                                    }
                                    new GetFreeStockTask().execute();

                                    //call GetFreeStock API

                                } else {
                                    soDetailPLObj = new SODetailPL();

                                    boolean isRepeat = false;

                                    for (int i = 0; i < listSODetailPL.size(); i++) {
                                        if (soplObj.items.get(0).ItemId == listSODetailPL.get(i).ItemId) {

                                            int oldQty = listSODetailPL.get(i).Qty;
                                            int currentQty = Integer.parseInt(etQty.getText().toString());
                                            int newQty = oldQty + currentQty;
//                                        listSODetailPL.get(i).Qty = newQty;
                                            soplObj.items.get(0).Qty = newQty;

                                            Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
                                            soplObj.items.get(0).LineAmt = totAmount;

                                            //Calculations

                                            soplObj.items.get(0).Amount = soplObj.items.get(0).Qty * soplObj.items.get(0).Rate;

                                            soplObj.items.get(0).Discount = soplObj.items.get(0).Amount * soplObj.items.get(0).DiscountPer / 100;

                                            double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
                                            double taxAmount = taxableAmount * soplObj.items.get(0).TaxPer / 100;
                                            soplObj.items.get(0).LineAmt = taxableAmount + taxAmount;

//                                        listSODetailPL.get(i). = soplObj.items.get(0);
                                            listSODetailPL.set(i, soplObj.items.get(0));

                                            isRepeat = true;

                                        }
                                    }

                                    if (isRepeat) {

                                    } else {

                                        soplObj.items.get(0).Qty = Integer.parseInt(etQty.getText().toString());

                                        Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
                                        soplObj.items.get(0).LineAmt = totAmount;

                                        //Calculations

                                        soplObj.items.get(0).Amount = soplObj.items.get(0).Qty * soplObj.items.get(0).Rate;

                                        soplObj.items.get(0).Discount = soplObj.items.get(0).Amount * soplObj.items.get(0).DiscountPer / 100;

                                        double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
                                        double taxAmount = taxableAmount * soplObj.items.get(0).TaxPer / 100;
                                        soplObj.items.get(0).LineAmt = taxableAmount + taxAmount;

//                            listSODetailPL =  soplObj.items;

                                        soDetailPLObj = soplObj.items.get(0);
                                        listSODetailPL.add(soDetailPLObj);

//                                    listSODetailPL.addAll(soplObj.items); //copying the entire list


                                    }
                                    String[] arrSlNo = new String[listSODetailPL.size()];

                                    for (int i = 0; i < listSODetailPL.size(); i++) {
                                        arrSlNo[i] = listSODetailPL.get(i).Name;
                                    }

                                    double total_amount = 0;
                                    String[] arr = new String[listSODetailPL.size()];

                                    for (int j = 0; j < listSODetailPL.size(); j++) {
                                        arr[j] = listSODetailPL.get(j).Name;
                                        total_amount = total_amount + Double.valueOf(listSODetailPL.get(j).LineAmt);
                                    }

                                    SOActivityAdapter OBJArrayAdSO = new SOActivityAdapter(SOActivity.this, arrSlNo, listSODetailPL, tvBilltotal);
                                    lvProductlist.setAdapter(OBJArrayAdSO);
//                                tvBilltotal.setText("" + total_amount);
//                                tvBilltotal.setText("₹ " + String.format("%.2f", total_amount));
                                    tvBilltotal.setText("₹ " + String.format("%.2f", total_amount));
                                }

                                qtydialog.dismiss();
                                acvItemSearchSOActivity.setText(""); //Added by Pavithra on 10-10-2020
                            }catch(Exception ex){
                                Toast.makeText(SOActivity.this, ""+ex, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    //added by Pavithra on 10-10-2020
                    etQty.requestFocus();

                    etQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                qtydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                    });
                    showSoftKeyboard(etQty);
                    qtydialog.show();

                } else {
                    tsMessages("" + soplObj.Message);
                }

            }
        }
    }

    private void getItemDetails() {

        prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
//        int cust_id = prefs.getInt("CustomerId", 0); //commenetd by Pavithra on 12-11-2020
        int store_id = prefs.getInt("StoreId", 0);

        try {
            String MethodName = "GetItemDetails";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
//            request.addProperty("CustId", cust_id);  //commenetd by Pavithra on 12-11-2020
            request.addProperty("CustId", CustomerId); //edited by Pavithra on 12-11-2020
            request.addProperty("pId", itemId);
            request.addProperty("StoreId", store_id);
            request.addProperty("Guid", "d5af5098-eb14-470a-98ae-5f8c24d81470");

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetItemDetails = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }

    }

    private class GetFreeStockTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Checking free stock...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            getFreeStock();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strGetFreeStock == null || strGetFreeStock.equals("")){
                Toast.makeText(SOActivity.this, "No result from web", Toast.LENGTH_SHORT).show();
            } else {
                Gson gson = new Gson();
                SODetailPL soDetailPLObj = new SODetailPL();
                soDetailPLObj = gson.fromJson(strGetFreeStock, SODetailPL.class);

                if(soDetailPLObj.ErrorStatus ==0){

//                    soDetailPLObj = new SODetailPL();
//
//                    soplObj.items.get(0).Qty = Double.parseDouble(etQty.getText().toString());


                    int freeQty = soDetailPLObj.FreeQty;


//                    soDetailPLObj = new SODetailPL();

                    boolean isRepeat = false;

                    for(int i = 0; i < listSODetailPL.size(); i++){
                        if( soplObj.items.get(0).ItemId == listSODetailPL.get(i).ItemId){

                            int oldQty = listSODetailPL.get(i).Qty;
                            int currentQty = Integer.parseInt(etQty.getText().toString());
                            int newQty = oldQty +currentQty;
//                                        listSODetailPL.get(i).Qty = newQty;
                            soplObj.items.get(0).Qty = newQty;

                            Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
                            soplObj.items.get(0).LineAmt = totAmount;

                            //Calculations

                            soplObj.items.get(0).Amount = soplObj.items.get(0).Qty * soplObj.items.get(0).Rate;

                            soplObj.items.get(0).Discount = soplObj.items.get(0).Amount * soplObj.items.get(0).DiscountPer / 100;

                            double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
                            double taxAmount = taxableAmount * soplObj.items.get(0).TaxPer / 100;
                            soplObj.items.get(0).LineAmt = taxableAmount + taxAmount;

                            soplObj.items.get(0).FreeQty = freeQty;   //Added by Pavithra on 05-11-2020

//                                        listSODetailPL.get(i). = soplObj.items.get(0);
                            listSODetailPL.set(i, soplObj.items.get(0));

                            isRepeat = true;

                        }
                    }

                    if(isRepeat){

                    }else {

                        soplObj.items.get(0).Qty = Integer.parseInt(etQty.getText().toString());

                        Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
                        soplObj.items.get(0).LineAmt = totAmount;

                        //Calculations

                        soplObj.items.get(0).Amount = soplObj.items.get(0).Qty * soplObj.items.get(0).Rate;

                        soplObj.items.get(0).Discount = soplObj.items.get(0).Amount * soplObj.items.get(0).DiscountPer / 100;

                        double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
                        double taxAmount = taxableAmount * soplObj.items.get(0).TaxPer / 100;
                        soplObj.items.get(0).LineAmt = taxableAmount + taxAmount;

                        soplObj.items.get(0).FreeQty = freeQty;  //Added by Pavithra on 05-11-2020

//                            listSODetailPL =  soplObj.items;

                        soDetailPLObj = soplObj.items.get(0);
                        listSODetailPL.add(soDetailPLObj);
//                        listSODetailPL.addAll(soplObj.items); //copying the entire list


                    }







//                    boolean isRepeat = false;
//
//
//                    for(int i = 0; i < listSODetailPL.size(); i++) {
//                        if (soDetailPLObj.ItemId == listSODetailPL.get(i).ItemId) {
//
//                            int oldQty = listSODetailPL.get(i).Qty;
//                            int currentQty = Integer.parseInt(etQty.getText().toString());
//                            int newQty = oldQty + currentQty;
////                                        listSODetailPL.get(i).Qty = newQty;
//                            soDetailPLObj.Qty = newQty;
//
//                            Double totAmount = soDetailPLObj.Qty * Double.valueOf(soDetailPLObj.Mrp);
//                            soDetailPLObj.LineAmt = totAmount;
//                            soDetailPLObj.FreeQty = soDetailPLObj.FreeQty;
//
//
//
//                            //Calculations
//
//                            soDetailPLObj.Amount = soDetailPLObj.Qty * soDetailPLObj.Rate;
//
//                            soDetailPLObj.Discount =  soDetailPLObj.Amount* soDetailPLObj.DiscountPer/100;
//
//                            double taxableAmount = soDetailPLObj.Amount - soDetailPLObj.Discount;
//                            double taxAmount  = taxableAmount * soDetailPLObj.TaxPer/100;
//                            soDetailPLObj.LineAmt = taxableAmount+taxAmount;
//
//                            listSODetailPL.set(i, soDetailPLObj);
//
//                            isRepeat = true;
//                        }
//                    }






//                    for(int i = 0; i < listSODetailPL.size(); i++) {
//                        if (soplObj.items.get(0).ItemId == listSODetailPL.get(i).ItemId) {
//
//                            int oldQty = listSODetailPL.get(i).Qty;
//                            int currentQty = Integer.parseInt(etQty.getText().toString());
//                            int newQty = oldQty + currentQty;
////                                        listSODetailPL.get(i).Qty = newQty;
//                            soplObj.items.get(0).Qty = newQty;
//
//                            Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
//                            soplObj.items.get(0).LineAmt = totAmount;
//                            soplObj.items.get(0).FreeQty = soDetailPLObj.FreeQty;
//
//
//
//                            //Calculations
//
//                            soplObj.items.get(0).Amount = soplObj.items.get(0).Qty *soplObj.items.get(0).Rate;
//
//                            soplObj.items.get(0).Discount =  soplObj.items.get(0).Amount* soplObj.items.get(0).DiscountPer/100;
//
//                            double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
//                            double taxAmount  = taxableAmount * soplObj.items.get(0).TaxPer/100;
//                            soplObj.items.get(0).LineAmt = taxableAmount+taxAmount;
//
//                            listSODetailPL.set(i, soplObj.items.get(0));
//
//                            isRepeat = true;
//                        }
//                    }





//                    if(isRepeat){
//
//                    }else {
//
////                        soplObj.items.get(0).Qty = Integer.parseInt(etQty.getText().toString());
////
////                        Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
////                        soplObj.items.get(0).LineAmt = totAmount;
////
////                        //Calculations
////
////                        soplObj.items.get(0).Amount = soplObj.items.get(0).Qty * soplObj.items.get(0).Rate;
////
////                        soplObj.items.get(0).Discount = soplObj.items.get(0).Amount * soplObj.items.get(0).DiscountPer / 100;
////
////                        double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
////                        double taxAmount = taxableAmount * soplObj.items.get(0).TaxPer / 100;
////                        soplObj.items.get(0).LineAmt = taxableAmount + taxAmount;
////
//////                            listSODetailPL =  soplObj.items;
////                        listSODetailPL.addAll(soplObj.items); //copying the entire list
//
//
//
//
//                        soDetailPLObj.Qty = Integer.parseInt(etQty.getText().toString());
//
//                        Double totAmount = soDetailPLObj.Qty * Double.valueOf(soDetailPLObj.Mrp);
//                        soDetailPLObj.LineAmt = totAmount;
//
//                        //Calculations
//
//                        soDetailPLObj.Amount = soDetailPLObj.Qty * soDetailPLObj.Rate;
//
//                        soDetailPLObj.Discount = soDetailPLObj.Amount * soDetailPLObj.DiscountPer / 100;
//
//                        double taxableAmount = soDetailPLObj.Amount -soDetailPLObj.Discount;
//                        double taxAmount = taxableAmount * soDetailPLObj.TaxPer / 100;
//                        soDetailPLObj.LineAmt = taxableAmount + taxAmount;
//
////                            listSODetailPL =  soplObj.items;
////                        listSODetailPL.addAll(soDetailPLObj); //copying the entire list
//                        listSODetailPL.add(soDetailPLObj); //copying the entire list
//
//
//                    }


//                            soplObj.items.get(0).Qty = Integer.parseInt(etQty.getText().toString());

//                    Double totAmount = soplObj.items.get(0).Qty * Double.valueOf(soplObj.items.get(0).Mrp);
//                    soplObj.items.get(0).LineAmt = totAmount;
//                    soplObj.items.get(0).FreeQty = soDetailPLObj.FreeQty;
//
//
//
//                    //Calculations
//
//                    soplObj.items.get(0).Amount = soplObj.items.get(0).Qty *soplObj.items.get(0).Rate;
//
//                    soplObj.items.get(0).Discount =  soplObj.items.get(0).Amount* soplObj.items.get(0).DiscountPer/100;
//
//                    double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
//                    double taxAmount  = taxableAmount * soplObj.items.get(0).TaxPer/100;
//                    soplObj.items.get(0).LineAmt = taxableAmount+taxAmount;
//
//
//
////                            listSODetailPL =  soplObj.items;
//                    listSODetailPL.addAll(soplObj.items); //copying the entire list


                    String[] arrSlNo = new String[listSODetailPL.size()];

                    for (int i = 0; i < listSODetailPL.size(); i++) {
                        arrSlNo[i] = listSODetailPL.get(i).Name;
                    }

                    double total_amount = 0;
                    String[] arr = new String[listSODetailPL.size()];

                    for (int j = 0; j < listSODetailPL.size(); j++) {
                        arr[j] = listSODetailPL.get(j).Name;
                        total_amount = total_amount + Double.valueOf(listSODetailPL.get(j).LineAmt);
                    }

                    SOActivityAdapter OBJArrayAdSO = new SOActivityAdapter(SOActivity.this, arrSlNo, listSODetailPL, tvBilltotal);
                    lvProductlist.setAdapter(OBJArrayAdSO);
//                    tvBilltotal.setText("" + total_amount);
//                    tvBilltotal.setText("₹ " + String.format("%.2f", total_amount));
                    tvBilltotal.setText("₹ " + String.format("%.2f", total_amount));

                }else{
                    tsMessages(""+soDetailPLObj.Message);
                }
            }
        }
    }

    private void getFreeStock(){

        prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
//        int cust_id = prefs.getInt("CustomerId", 0);  //commented by Pavithra on 12-11-2020
        int store_id = prefs.getInt("StoreId", 0);

//        input : (String AuthKey, int ItemId, int CustomerId, int StoreId, int BilledQty)

        try {
            String MethodName = "GetFreeStock";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("ItemId",itemId );
//            request.addProperty("CustomerId", cust_id); //commented by Pavithra on 12-11-2020
            request.addProperty("CustomerId", CustomerId); //added by Pavithra on 12-11-2020
            request.addProperty("StoreId", store_id);
            request.addProperty("BilledQty", billedQty);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetFreeStock = str;


//            {"Id":0,"ItemId":0,"Name":null,"Qty":1.0,"QtyType":0.0,"UperPack":0,"Mrp":0.0,"ItemCode":null,"SOH":0.0,"UOM":null,"Rate":0.0,"FreeQty":0.0,"Tax":0.0,"Discount":0.0,"LineAmt":0.0,"TaxPer":0.0,"DiscountPer":0.0,"Amount":0.0,"OfferDesc":null,"SchemeExists":0,"BuyQtyInPacks":0.0,"FreeQtyInPacks":0.0,"ErrorStatus":0,"Message":null}
        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }
    }

    private class SaveSOTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SOActivity.this);
            pDialog.setMessage("Saving SO...");
            pDialog.setCancelable(false);
            pDialog.show();
        }


        @Override
        protected String doInBackground(String... strings) {
            saveSO();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            if (strSaveSO.equals("") || strSaveSO == null) {
//                Toast.makeText(SOActivity.this, "No result from web", Toast.LENGTH_SHORT).show();
                Toast.makeText(SOActivity.this, "Api returned an empty string, try later", Toast.LENGTH_SHORT).show();
            } else if(strSaveSO.equals("exception")){
                tsMessages(strErrorMsg);
            }
            else {
                SOPL soplObj = new SOPL();
                Gson gson = new Gson();

                soplObj = gson.fromJson(strSaveSO, SOPL.class);
                if (soplObj.ErrorStatus == 0) {

                    dialog = new Dialog(SOActivity.this);
                    dialog.setContentView(R.layout.save_dialogwindow);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setTitle("Save");
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    Button btnOkSavePopup = (Button) dialog.findViewById(R.id.btnOkSavePopup);
                    TextView tvSaveStatus = (TextView) dialog.findViewById(R.id.tvSaveStatus);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = (int) saveDialogWindowHeight;
                    lp.gravity = Gravity.CENTER;
                    dialog.getWindow().setAttributes(lp);

                    btnOkSavePopup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });

                    String str = soplObj.Message;
                    String docNo = str.substring(str.indexOf("(") + 1, str.indexOf(")"));

                    tvSaveStatus.setText("SO Saved \n\nToken No: " + docNo);
//                    tvSaveStatus.setText(""+soplObj.Message);
                    tvSaveStatus.setMovementMethod(new ScrollingMovementMethod());

                    tvSODocNo.setText("" + docNo);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("IsSavedTssoWS", true);
                    editor.commit();

                    //Added by Pavithra on 12-10-2020
                    btnSave.setEnabled(false);
                    btnSave.setAlpha(0.4f);
                    imgBtnRemarksPrescrptn.setEnabled(false);
                    imgBtnRemarksPrescrptn.setAlpha(0.4f);
                    acvItemSearchSOActivity.setEnabled(false);
                    ic_search.setEnabled(false);

                    isSaved = true;
                    dialog.show();


                    String[] arr = new String[listSODetailPL.size()];
                    for (int i = 0; i < listSODetailPL.size(); i++) {
                        arr[i] = listSODetailPL.get(i).Name;
                    }

                    SOActivityAdapter productListActivityAdapter = new SOActivityAdapter(SOActivity.this, arr, listSODetailPL, tvBilltotal);
                    lvProductlist.setAdapter(productListActivityAdapter);

                } else {
                    tsMessages("" + soplObj.Message);
                }

            }
        }
    }

    private void saveSO() {

        try {

            String customerInfo = prefs.getString("CustomerInfo", "");
            String customerDetail = prefs.getString("CustomerDetailWS", ""); // this returns null when admin login
            int storeId = prefs.getInt("StoreId", 0); //Added by Pavithra on 11-11-2020

            Gson gson = new Gson();
            CustomerPL customerPLObj = gson.fromJson(customerInfo, CustomerPL.class);
            ListCustomerPL listCustomerPLObj = gson.fromJson(customerDetail, ListCustomerPL.class);

            String userType = prefs.getString("UserType", "");

            UserPL userPLObj = new UserPL();

            String loginResultStr = prefs.getString("LoginResultStr", "");

            gson = new Gson();
            userPLObj = gson.fromJson(loginResultStr, UserPL.class);

//            if(userType.equals("CR CUSTOMER")) {
            if(userPLObj.UserType.equals("CR CUSTOMER")) {

//                String loginResultStr = prefs.getString("LoginResultStr", "");
//
//                gson = new Gson();
//                userPLObj = gson.fromJson(loginResultStr, UserPL.class);

                soplObj.UserId = userPLObj.UserId;  //Added by Pavithra on 12-11-2020
                soplObj.UserDocTypeId = userPLObj.UserDocTypeId; //Added by Pavithra on 12-11-2020

                soplObj.PartyAcId = userPLObj.AcId;
                soplObj.Address = userPLObj.Address;
                soplObj.Email = userPLObj.Email;
                soplObj.Phone = userPLObj.Mobile;
                soplObj.PartyName = userPLObj.Name;
                soplObj.Area = userPLObj.Area;
//                soplObj.StateId = userPLObj.StateId;
//                soplObj.StoreId = userPLObj.StoreId;//commented by Pavithra on 11-11-2020
                soplObj.StoreId = storeId;   //added by Pavithra on 11-11-2020
                soplObj.State = userPLObj.State;
                soplObj.Pincode = userPLObj.Pincode;
                soplObj.Remarks = billRemarks;

            }else {

//                {"Address":"C-35, ARYA SAMAJ ROAD, ADARSH NAGAR,   ","Email":"goelmedicines@gmail.com","GSTIN":"07AAKPK0385N1ZA","Phone":"9210472868","StateId":0,"StoreId":7,"acId":1787,"acName":"GOEL MEDICOSE"}


//                {
//                    "Address": "C-35, ARYA SAMAJ ROAD, ADARSH NAGAR,   ",
//                        "Email": "goelmedicines@gmail.com",
//                        "GSTIN": "07AAKPK0385N1ZA",
//                        "Phone": "9210472868",
//                        "StateId": 0,
//                        "StoreId": 7,
//                        "acId": 1787,
//                        "acName": "GOEL MEDICOSE"
//                }
//                soplObj.UserId = userPLObj.UserId;
//                soplObj.UserId = listCustomerPLObj.list.get(0).;
                soplObj.UserId = userPLObj.UserId;  //User id is same both for customer and employee //Added by Pavithra on 12-11-2020
                soplObj.UserDocTypeId = userPLObj.UserDocTypeId; //Added by Pavithra on 12-11-2020

                soplObj.PartyAcId = listCustomerPLObj.list.get(0).acId;
                soplObj.Address = listCustomerPLObj.list.get(0).Address;
                soplObj.Email = listCustomerPLObj.list.get(0).Email;
                soplObj.Phone = listCustomerPLObj.list.get(0).Phone;
                soplObj.PartyName = listCustomerPLObj.list.get(0).acName;
                soplObj.Area = listCustomerPLObj.list.get(0).Area;
                soplObj.StateId = listCustomerPLObj.list.get(0).StateId;
//                soplObj.StoreId = listCustomerPLObj.list.get(0).StoreId; //commented by Pavithra on 11-11-2020
                soplObj.StoreId = storeId;
                soplObj.State = listCustomerPLObj.list.get(0).State;
//            soplObj.Source ="";

                soplObj.Pincode = listCustomerPLObj.list.get(0).Pincode;
                soplObj.Remarks = billRemarks;
            }

//            {"ErrorStatus":1,"Message":"The parameterized query '(@Source varchar(20),@PartyName varchar(50),@PartyAcId int,@Cust' expects the parameter '@pincode', which was not supplied."}

            soplObj.items = listSODetailPL;

            gson = new Gson();
            String soString = gson.toJson(soplObj);

            String MethodName = "SaveSO";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("SoString", soString);
            request.addProperty("Guid", "d5af5098-eb14-470a-98ae-5f8c24d81470");
            request.addProperty("EnableStockValidation", 0);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strSaveSO = str;

//            {"ErrorStatus":1,"Message":"Customer State Id is not Available"}
        } catch (Exception ex) {
            Log.d("CI", "" + ex);
            strSaveSO = "exception";
            strErrorMsg = ex.getMessage();
        }

    }

    public void showAlertMessage() {

        try {
            AlertDialog.Builder b = new AlertDialog.Builder(SOActivity.this);
            b.setTitle("Confirm Discard");
            b.setMessage("Are you sure to discard this SO ?");  //item name should specify

            b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    lvProductlist.setAdapter(null);
                    isSaved = false;

                    Intent intent = new Intent(SOActivity.this, CustomerInfoActivity.class); //Added by Pavithra on 29-08-2020
                    startActivity(intent);
                    SOActivity.this.finish(); //Added by Pavithra on 10-09-2020
                }
            });

            b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            b.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(SOActivity.this);
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

                    acvItemSearchSOActivity.setText(""); //Added by Pavithra on 06-11-2020

//                    if (dialogItemLookup != null)
//                        dialogItemLookup.dismiss();
                }
            });
            tvSaveStatus.setText("" + msg);
            dialog.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }

    private void outOfStockAlert(String msg) {

        try {
//            final Dialog dialog = new Dialog(SOActivity.this);
            dialogOutofStock = new Dialog(SOActivity.this);
            dialogOutofStock.setContentView(R.layout.outofstock_dialog);
            dialogOutofStock.setCanceledOnTouchOutside(false);
            dialogOutofStock.setTitle("Save");
            dialogOutofStock.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            ImageButton imgBtnCloseSaveWindow = (ImageButton) dialogOutofStock.findViewById(R.id.imgBtnClosetsMsgWindow);
            Button btnContinue = (Button) dialogOutofStock.findViewById(R.id.btnContinue);
            TextView tvSaveStatus = (TextView) dialogOutofStock.findViewById(R.id.tvTsMessageDisplay);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialogOutofStock.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = (int) tsMsgDialogWindowHeight;
            lp.gravity = Gravity.CENTER;
            dialogOutofStock.getWindow().setAttributes(lp);

            imgBtnCloseSaveWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogOutofStock.dismiss();
                    isOutofStock =false;
//                    if (dialogItemLookup != null)
//                        dialogItemLookup.dismiss();
                }
            });

            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogOutofStock.dismiss();
                    isOutofStock =false;
                    new SaveSOTask().execute();
                }
            });
            tvSaveStatus.setText("" + msg);
            dialogOutofStock.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }


    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SOActivity.this);
        alertDialogBuilder.setMessage("Do you want to exit the application?");
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                prefs = PreferenceManager.getDefaultSharedPreferences(SOActivity.this);
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




}

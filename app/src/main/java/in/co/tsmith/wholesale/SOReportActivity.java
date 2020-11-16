package in.co.tsmith.wholesale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//Created on 09-10-2020
public class SOReportActivity extends AppCompatActivity {

    Button btnLoadReport;
    EditText etDate;
    String filterParam = "";
    LinearLayout llSOToolbar;
    double llSOToolbarHeight;
    TextView tvCustomerNameSOReport;

    SharedPreferences prefs;

    EditText etFromDate;
    EditText etToDate;
    String strFromDate = "";
    String strToDate = "";
    Button btnLoadSOSummary;

    String strGetWebSOSummary = "";

    ListView lvSoReportSummary;

    ImageButton imgBtnBackReport;
    double tsMsgDialogWindowHeight;

    ProgressDialog pDialog;
    String URL = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soreport);


        llSOToolbar = (LinearLayout) findViewById(R.id.llSOToolbar);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;

        llSOToolbarHeight = (screen_height * 8.75) / 100;
        tsMsgDialogWindowHeight = (screen_height * 38) / 100;

        LinearLayout.LayoutParams paramsllHeader = (LinearLayout.LayoutParams) llSOToolbar.getLayoutParams();
        paramsllHeader.height = (int) llSOToolbarHeight;
        paramsllHeader.width = LinearLayout.LayoutParams.MATCH_PARENT;
        llSOToolbar.setLayoutParams(paramsllHeader);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String customerName = prefs.getString("CustomerNameWS","");
        URL = prefs.getString("MiniSOURL", "");
        if(URL.equals("")) {  //Added by Pavithra on 10-11-2020
            URL = AppConfigSettings.WsUrl;
        }


        lvSoReportSummary = (ListView)findViewById(R.id.lvSoReportSummary);


        tvCustomerNameSOReport = (TextView)findViewById(R.id.tvCustomerNameSOReport);
        tvCustomerNameSOReport.setText(""+customerName);

        etFromDate = (EditText)findViewById(R.id.etFromDate);
        etToDate = (EditText)findViewById(R.id.etToDate);
        btnLoadSOSummary = (Button) findViewById(R.id.btnLoadSOSummary);
        imgBtnBackReport = (ImageButton) findViewById(R.id.imgBtnBackReport);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar myCalendar = Calendar.getInstance();
        String currentDate = myCalendar.get(Calendar.DATE) + "/"
                + (myCalendar.get(Calendar.MONTH) + 1) + "/"
                + myCalendar.get(Calendar.YEAR);

        Calendar calendar = Calendar.getInstance();
        Date currDate = calendar.getTime();
        String todaysDate = dateFormat.format(currDate);

        etToDate.setText(todaysDate);

//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date current_date = null;
        try {
            current_date = dateFormat.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current_date);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date newDate = calendar.getTime();

        String dateBeforeWeek = dateFormat.format(newDate);
        etFromDate.setText(dateBeforeWeek);

        imgBtnBackReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intnt = new Intent(SOReportActivity.this,CustomerInfoActivity.class);
                startActivity(intnt);
                SOReportActivity.this.finish();
            }
        });

        btnLoadSOSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strFromDate = etFromDate.getText().toString();
                strToDate = etToDate.getText().toString();

                if(strFromDate.equals("")||strToDate.equals("")){
                    Toast.makeText(SOReportActivity.this, "date fields cannot be empty", Toast.LENGTH_SHORT).show();
                }else {

                    //To validate date pending

//                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        Date date = format.parse(strFromDate);
                        Date dateTo = format.parse(strToDate);

                        System.out.println(date);
                        if(date.before(dateTo)) {

                            new GetWebSOSummaryTask().execute();

                        } else {

                            Toast.makeText(SOReportActivity.this, "Date not valid", Toast.LENGTH_SHORT).show();

                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

//                    new GetWebSOSummaryTask().execute();

                }
            }
        });
    }


    private class GetWebSOSummaryTask extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SOReportActivity.this);
            pDialog.setMessage("Loading SO Report...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            getWebSOSummary();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strGetWebSOSummary.equals("") || strGetWebSOSummary == null){
                Toast.makeText(SOReportActivity.this, "No result from web", Toast.LENGTH_SHORT).show();
            }else{
                Gson gson = new Gson();
                ListSOSummaryReportPL listSOSummaryReportPLObj = new ListSOSummaryReportPL();
                listSOSummaryReportPLObj = gson.fromJson(strGetWebSOSummary,ListSOSummaryReportPL.class);
                if(listSOSummaryReportPLObj.ErrorStatus == 0){

                    String[] arrslno = new String[listSOSummaryReportPLObj.lstSummary.size()];
                    for(int i = 0; i < listSOSummaryReportPLObj.lstSummary.size(); i++){
                        arrslno[i] = listSOSummaryReportPLObj.lstSummary.get(i).Customer;

                    }

                    SOReportActivityAdapter OBJArrayAdSO = new SOReportActivityAdapter(SOReportActivity.this, arrslno,  listSOSummaryReportPLObj.lstSummary);
                    lvSoReportSummary.setAdapter(OBJArrayAdSO);


                }else{
                    tsMessages(""+listSOSummaryReportPLObj.Message);
//                    Toast.makeText(SOReportActivity.this, ""+listSOSummaryReportPLObj.Message, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private void getWebSOSummary(){

//        String AuthKey,
//        int UserId,
//        String FromDate,
//        String ToDate,
//        int StoreId,
//        int UserTypeDocId,
//        String UserType

        int  customer_id = prefs.getInt("CustomerId",0);
        int store_id = prefs.getInt("StoreId",0);


        UserPL userPLObj = new UserPL();
        ListCustomerPL listCustomerPLObj =  new ListCustomerPL();


        String loginResultStr = prefs.getString("LoginResultStr", "");
        String customerDetailWS = prefs.getString("CustomerDetailWS", "");

        Gson gson = new Gson();
        userPLObj = gson.fromJson(loginResultStr, UserPL.class);
        listCustomerPLObj = gson.fromJson(customerDetailWS, ListCustomerPL.class);

        int AcId = 0;

        if(userPLObj.UserType.equals("CR CUSTOMER")) {

            AcId = userPLObj.AcId;

        } else {
            AcId = listCustomerPLObj.list.get(0).acId;
        }

        //Following commented by Pavithra on 12-11-2020

//        int userId = prefs.getInt("UserId",0);
//        int userTypeDocId = prefs.getInt("UserTypeDocId",0);
//        String userType = prefs.getString("UserType","");

//        editor.putInt("UserId",userPLObj.UserId);
//        editor.putInt("UserTypeDocId",userPLObj.UserDocTypeId);
//        editor.putString("UserType", userPLObj.UserType);

        try {
            String MethodName = "GetWebSOSummary";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
//            request.addProperty("UserId", userId); //Take from checklogin
            request.addProperty("UserId", userPLObj.UserId); //Take from checklogin

            //if customer login take acId from as follows
            request.addProperty("AcId",AcId); //Added by Pavithra on 14-11-2020
            //else as follows
//            request.addProperty("AcId", userPLObj.AcId); //Added by Pavithra on 14-11-2020

            request.addProperty("FromDate", strFromDate);    //ddmmyyyy
            request.addProperty("ToDate", strToDate);   request.addProperty("StoreId", store_id);

            request.addProperty("FromDate", "14/10/2020");    //ddmmyyyy
            request.addProperty("ToDate", "15/10/2020");      //oct 13 to now
//            request.addProperty("UserTypeDocId", userTypeDocId);   //Take from checklogin
            request.addProperty("UserTypeDocId", userPLObj.UserDocTypeId);   //Take from checklogin
//            request.addProperty("UserType", userType);        //Take from checklogin

            request.addProperty("UserType", userPLObj.UserType);        //Take from checklogin
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            String URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetWebSOSummary = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }


    }


    public void showDatePickerDialogFrom(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        DatePickerFragment.setDatePickerFragment(SOReportActivity.this, etFromDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showDatePickerDialogTo(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        DatePickerFragment.setDatePickerFragment(SOReportActivity.this, etToDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        static Context cntxt;
        static EditText editText;
        //    @SuppressLint("ValidFragment")
        public static void setDatePickerFragment(Context cxt,EditText et){
            cntxt = cxt;
            editText = et;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

            int month= monthOfYear+1;
            String fm = ""+month;
            String fd = ""+dayOfMonth;

            if(month < 10) {
                fm = "0" + month;
            }
            if (dayOfMonth < 10) {
                fd = "0" + dayOfMonth;
            }

            String date = ""+fd+"/"+fm+"/"+year;
            editText.setText(date);

        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(SOReportActivity.this);
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
            tvSaveStatus.setText("" + msg);
            dialog.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {


        Intent intnt =  new Intent(SOReportActivity.this,CustomerInfoActivity.class);
        startActivity(intnt);
        SOReportActivity.this.finish();

//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SOReportActivity.this);
//        alertDialogBuilder.setMessage("Do you want to exit the application?");
//        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//
//                prefs = PreferenceManager.getDefaultSharedPreferences(SOReportActivity.this);
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("CustomerInfo", "");
//                editor.putInt("CustomerId", 0);
//                editor.putInt("StoreId", 0);
//                editor.commit();
//
//                System.exit(0);
//
//            }
//        });
//        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//
//            }
//        });
//
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();
    }


}

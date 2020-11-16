package in.co.tsmith.wholesale;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

public class SOReportActivityAdapter extends ArrayAdapter<String> {

    Context cntext;
    String[] values;
    List<SOSummaryReportPL> listSOSummaryReportPL;

    int lineId = 0;

    String strWebSOItemDetailsReport = "";

    Dialog dialogItemDetailLookup;
    ListView lvItemDetail;
    double tsMsgDialogWindowHeight;

    ProgressDialog pDialog;

    public SOReportActivityAdapter(Context context, String[] v1, List<SOSummaryReportPL> soSummaryReportPLList) {

        super(context, R.layout.activity_soreport, v1);
        cntext = context;
        values = v1;
        listSOSummaryReportPL = soSummaryReportPLList;

//        DisplayMetrics displayMetrics = new DisplayMetrics();
        DisplayMetrics displayMetrics = cntext.getResources().getDisplayMetrics();
        int screen_height = displayMetrics.heightPixels;

        tsMsgDialogWindowHeight = (screen_height * 38) / 100;  //  243/640
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cntext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item_lv_soreport, parent, false);

        TextView tvSlNo = (TextView) rowView.findViewById(R.id.tvSlnoReport);
        TextView tvDocNumber = (TextView) rowView.findViewById(R.id.tvDocnoReport);
        TextView tvDocDate = (TextView) rowView.findViewById(R.id.tvDocDateReport);
        TextView tvNoOfItems = (TextView) rowView.findViewById(R.id.tvNoOfItemsReport);
        TextView tvApproxAmount = (TextView) rowView.findViewById(R.id.tvApproxamtReport);
        Button btnItemDetailReport = (Button) rowView.findViewById(R.id.btnItemsReport);

        btnItemDetailReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lineId = listSOSummaryReportPL.get(position).Id;
//                Toast.makeText(cntext, ""+lineId, Toast.LENGTH_SHORT).show();
                new WebSOItemDetailsReportTask().execute();

            }
        });


        SOSummaryReportPL obj = listSOSummaryReportPL.get(position);
        tvSlNo.setText(String.valueOf(position + 1));
        tvDocNumber.setText(obj.SONo);
        tvDocDate.setText(obj.SoDate);
        tvNoOfItems.setText("" + obj.NoOFItems);
//        tvApproxAmount.setText("" + obj.AppAmt);

        tvApproxAmount.setText(String.format("%.2f",+obj.AppAmt));
        return rowView;
    }


    @Override
    public int getCount() {
        return listSOSummaryReportPL.size();
    }

    private class WebSOItemDetailsReportTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(cntext);
            pDialog.setMessage("Loading Detail Report...");
            pDialog.setCancelable(false);
            pDialog.show();
        }


        @Override
        protected String doInBackground(String... strings) {
            webSOItemDetailsReport();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strWebSOItemDetailsReport == null || strWebSOItemDetailsReport.equals("")){
                Toast.makeText(cntext, "No result from web", Toast.LENGTH_SHORT).show();
            }else{

                Gson gson = new Gson();
                ListSoDetailReportPL listSoDetailReportPLObj = new ListSoDetailReportPL();
                listSoDetailReportPLObj = gson.fromJson(strWebSOItemDetailsReport, ListSoDetailReportPL.class);
                if(listSoDetailReportPLObj.ErrorStatus == 0){

                    dialogItemDetailLookup = new Dialog(cntext);
                    dialogItemDetailLookup.setContentView(R.layout.itemdetail_lookup_dialogwindow);
//                        dialogItemLookup.setCanceledOnTouchOutside(false);
                    dialogItemDetailLookup.setCanceledOnTouchOutside(true);
                    dialogItemDetailLookup.setTitle("Product Lookup");
                    dialogItemDetailLookup.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                    dialogItemDetailLookup = (AutoCompleteTextView) dialogItemDetailLookup.findViewById(R.id.etSearchItemLookup);
//                    etItemSearchLookup.setText(searchstring);
//                    etItemSearchLookup.setText(itemSearchStringLookup);
//                    imgBtnItemSearchLookup = (ImageButton) dialogItemLookup.findViewById(R.id.imgBtnItemSearchLookup);
                    lvItemDetail = (ListView) dialogItemDetailLookup.findViewById(R.id.lvItemsDetails);

                    String[] arrItems = new String[listSoDetailReportPLObj.lst.size()]; // added by 1165 on 02-11-2018
                    for (int j = 0; j < listSoDetailReportPLObj.lst.size(); j++) {
                        arrItems[j] = listSoDetailReportPLObj.lst.get(j).Name;
                    }

                    ImageButton imgBtnCloseItemLookup = (ImageButton) dialogItemDetailLookup.findViewById(R.id.imgBtnCloseItemLookup);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialogItemDetailLookup.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.gravity = Gravity.CENTER;
                    dialogItemDetailLookup.getWindow().setAttributes(lp);

                    imgBtnCloseItemLookup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogItemDetailLookup.dismiss();
//                            etItemSearchSOActivity.setText("");
                        }
                    });

//                    lvItemDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                            Toast.makeText(SOActivity.this, "" + listItemDetailsObj.lst.get(i).Name, Toast.LENGTH_SHORT).show();
//
//                        }
//                    });


                    SODetailReportActivityAdapter itemListCustomAdapter = new SODetailReportActivityAdapter(cntext, arrItems, listSoDetailReportPLObj.lst);
                    lvItemDetail.setAdapter(itemListCustomAdapter);


//                    ItemListCustomAdapter itemListCustomAdapter = new ItemListCustomAdapter(SOActivity.this, arrItems, listItemDetailsObj.lst, dialogItemLookup);
//                    lvItemsPopup.setAdapter(itemListCustomAdapter);

                    dialogItemDetailLookup.show();


                }else{
                    tsMessages(""+listSoDetailReportPLObj.Message);
//                    Toast.makeText(cntext, ""+listSoDetailReportPLObj.Message, Toast.LENGTH_SHORT).show();
                }



            }
        }
    }

    private void webSOItemDetailsReport() {

//        String AuthKey,
//        int LineId,
//        int StoreId

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cntext);
        int store_id = prefs.getInt("StoreId",0);
        String URL = prefs.getString("MiniSOURL", "");
        if(URL.equals("")) {  //Added by Pavithra on 10-11-2020
            URL = AppConfigSettings.WsUrl;
        }


        try {
            String MethodName = "WebSOItemDetailsReport";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("LineId", lineId); //Take from checklogin
            request.addProperty("StoreId", store_id);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            String URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strWebSOItemDetailsReport = str;

        } catch (Exception ex) {

            Log.d("CI", "" + ex);
        }
    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(cntext);
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
            Toast.makeText(cntext, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }
}

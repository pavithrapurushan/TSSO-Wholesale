package in.co.tsmith.wholesale;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class SODetailReportActivityAdapter extends ArrayAdapter<String> {


    Context cntext;
    String[] values;
    List<SODetailReportPL> listSODetailReportPL;

    int lineId = 0;

    String strWebSOItemDetailsReport = "";

    Dialog dialogItemDetailLookup;
    ListView lvItemDetail;

    public SODetailReportActivityAdapter(Context context, String[] v1, List<SODetailReportPL> sODetailReportPLList) {

        super(context, R.layout.activity_soreport, v1);
        cntext = context;
        values = v1;
        listSODetailReportPL = sODetailReportPLList;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cntext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item_lv_soreportdetail, parent, false);

        TextView tvSlNoReportDetail = (TextView) rowView.findViewById(R.id.tvSlNoReportDetail);
        TextView tvItemcodeReportDetail = (TextView) rowView.findViewById(R.id.tvItemcodeReportDetail);
        TextView tvPrdctNameReportDetail = (TextView) rowView.findViewById(R.id.tvPrdctNameReportDetail);
        TextView tvMrpReportDetail = (TextView) rowView.findViewById(R.id.tvMrpReportDetail);
        TextView tvQtyReportDetail = (TextView) rowView.findViewById(R.id.tvQtyReportDetail);
        TextView tvLineNetReportDetail = (TextView) rowView.findViewById(R.id.tvLineNetReportDetail);
        TextView tvBilledQtyReportDetail = (TextView) rowView.findViewById(R.id.tvBilledQtyReportDetail);

        Button btnItemDetailReport = (Button) rowView.findViewById(R.id.btnItemsReport);

//        btnItemDetailReport.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
////                lineId = listSOSummaryReportPL.get(position).Id;
////                Toast.makeText(cntext, ""+lineId, Toast.LENGTH_SHORT).show();
////                new SOReportActivityAdapter.WebSOItemDetailsReportTask().execute();
//
//            }
//        });

        if (position % 2 == 0) {
            rowView.setBackgroundColor(Color.parseColor("#5F7EBC"));
        } else {
            rowView.setBackgroundColor(Color.parseColor("#4E689B"));
        }



        SODetailReportPL  obj = listSODetailReportPL.get(position);
        tvSlNoReportDetail.setText(String.valueOf(position + 1));
        tvItemcodeReportDetail.setText(obj.Code);
        tvPrdctNameReportDetail.setText(obj.Name);
        tvMrpReportDetail.setText(String.format("%.2f",+ obj.Mrp));
//        tvMrpReportDetail.setText("" + obj.Mrp);
        tvQtyReportDetail.setText("" + obj.SOQty);
//        tvLineNetReportDetail.setText("" + obj.LineNetAmount);
        tvLineNetReportDetail.setText(String.format("%.2f",+ obj.LineNetAmount));
        tvBilledQtyReportDetail.setText("" + obj.BilledQty);

//        tvBalance.setText(String.format("%.2f",+payablesPLObj.BalanceAmt));
        return rowView;

    }


    @Override
    public int getCount() {
        return listSODetailReportPL.size();
    }

}

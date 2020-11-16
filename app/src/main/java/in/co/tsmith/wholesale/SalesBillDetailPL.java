package in.co.tsmith.wholesale;

public class SalesBillDetailPL {

    public SalesBillDetailPL()
    {
        ItemNotFromCPMFlag="0";
        ItemDetail01 ="";ItemDetail02 ="";ItemDetail03 ="";
        selectedExpiry = "";selectedBatchMrp = "";
        Remarks ="";

        TaxPer = "0";AstPer="0";TaxOnMRP="0";MRPInclusive="0";
        SODetailLineId ="0";Disc="0";Tax="0";
        UnitName = "";
        PackName = "";
    }

    String Slno;
    String Item;
    String ItemCode;
    String Batch;
    String MRPInclusive;
    String TaxOnMRP;
    String TaxFlag;
    String Mrp;
    String Rate;
    String BillingRate;
    String Qty;
    String DiscPer;
    String Soh;
    String TaxPer;
    String AstPer;
    String AstOnFlag;
    String Uperpack;
    String Amount;
    String Disc;
    String TaxableAmount;
    String Tax;
    String Ast;
    String TotalAmount;

    String SohUnits;
    String SohPacks;

    String ItemId;
    String BatchId;
    String ItemNotFromCPMFlag;
    String UnitName;
    String PackName;

    String TaxId;
    String CanEditRate;
    String CanBillNonStockkItem;
    String SchemeDetails;

    String ItemDetail01;
    String ItemDetail02;
    String ItemDetail03;

//    List<ClsNameValuePair> mrpList;
//    List<ClsNameValuePair>  expiryList;
    String Remarks;

    String selectedBatchMrp;
    String selectedExpiry;
    String SODetailLineId;
}

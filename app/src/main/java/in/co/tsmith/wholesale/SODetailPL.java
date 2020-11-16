package in.co.tsmith.wholesale;

public class SODetailPL extends CommonPL {

    public SODetailPL()
    {
        QtyType = 1;
    }

    public int Id ;
    public int ItemId ;
    public String Name ;
//    public double Qty ;
    public int Qty ;
    public double QtyType ;
    public int UperPack ;
    public double Mrp ;
    public String ItemCode ;
    public double SOH ;


    //Added By Najeela on 02092020
    public String UOM ;
    public double Rate ;

//    public double FreeQty ;
    public int FreeQty ;

    public double Tax ;

    public double Discount ;

    public Double LineAmt ;

    public Double TaxPer ;

    public Double DiscountPer;
    public Double Amount ;


    public String OfferDesc ;
    public int SchemeExists ;
//    public Double BuyQtyInPacks;
    public int BuyQtyInPacks;
//    public Double FreeQtyInPacks ;
    public int FreeQtyInPacks ;

}

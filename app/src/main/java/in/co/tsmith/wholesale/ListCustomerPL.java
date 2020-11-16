package in.co.tsmith.wholesale;

import java.util.ArrayList;
import java.util.List;

public class ListCustomerPL extends CommonPL {

    public ListCustomerPL()
    {
        list = new ArrayList<>();
    }

    public List<CustomerPL> list;
    public String Filter;
}

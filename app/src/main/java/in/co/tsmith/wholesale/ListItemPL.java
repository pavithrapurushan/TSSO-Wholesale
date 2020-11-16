package in.co.tsmith.wholesale;

import java.util.ArrayList;
import java.util.List;

public class ListItemPL extends CommonPL {

    public ListItemPL() {
        list = new ArrayList<>();
    }

    public List<ItemPL> list;
    public String Filter ;
}

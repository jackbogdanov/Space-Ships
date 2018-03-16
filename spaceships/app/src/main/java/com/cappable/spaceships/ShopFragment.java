package com.cappable.spaceships;

import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//        import static com.example.spaseship.app.R.drawable.*;

public class ShopFragment extends Fragment {

    final String ATTRIBUTE_NAME_TEXT = "text";
    final String ATTRIBUTE_NAME_IMAGE = "image";

    ListView listView;
    SimpleAdapter simpleAdapter;
    ArrayList<Map<String, Object>> data;
    Map<String, Object> m;
    ImageView iv;


    final int MENU_RED = 1;
    final int MENU_BLUE = 2;
    final int MENU_WHITE = 3;
    String[] names = {"корабль 1","корабль 2","корабль 3","корабль 4","корабль 5"};
    Object[] textures = {R.mipmap.space_ship_1, R.mipmap.space_ship_2,
            R.mipmap.space_ship_3, R.mipmap.space_ship_4};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shop_fragment, container);

        data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 4; i++) {
            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TEXT, names[i]);
            m.put(ATTRIBUTE_NAME_IMAGE, textures[i]);
            data.add(m);
        }

        String[] from = {ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE};
        int[] to = {R.id.tvText, R.id.ivImg};

        simpleAdapter = new SimpleAdapter(this.getActivity(), data, R.layout.shop_item, from, to);

        listView = (ListView) v.findViewById(R.id.listView);
        listView.setAdapter(simpleAdapter);
        registerForContextMenu(listView);
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, MENU_RED, 0, "красный");
        menu.add(0,MENU_BLUE,0,"синий");
        menu.add(0,MENU_WHITE,0,"белый");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View v = listView.getChildAt(acmi.position);
        switch (item.getItemId()){
            case MENU_RED:
                if(v != null) {
                    iv = (ImageView) v.findViewById(R.id.ivImg);
                    iv.setImageResource(R.mipmap.space_ship_4);
                }
                break;
            case MENU_BLUE:
                if(v != null){
                    iv = (ImageView) v.findViewById(R.id.ivImg);
                    iv.setImageResource(R.mipmap.space_ship_3);
                }
                break;
            case MENU_WHITE:
                if(v != null){
                    iv = (ImageView) v.findViewById(R.id.ivImg);
                    iv.setImageResource(R.mipmap.space_ship_2);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }
}

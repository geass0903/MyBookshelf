package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

public class SpinnerArrayAdapter extends ArrayAdapter<SpinnerItem> {

    @SuppressWarnings("unused")
    SpinnerArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        setDropDownViewResource(R.layout.item_spinner_drop_down);
    }

    SpinnerArrayAdapter(Context context, int textViewResourceId, List<SpinnerItem> list) {
        super(context, textViewResourceId, list);
        setDropDownViewResource(R.layout.item_spinner_drop_down);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        SpinnerItem item = getItem(position);
        if (item != null) {
            view.setText(item.mLabel);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
        SpinnerItem item = getItem(position);
        if (item != null) {
            view.setText(item.mLabel);
        }
        return view;
    }


    int getPosition(String label){
        int position = -1;
        for (int i = 0; i < this.getCount(); i++) {
            SpinnerItem item = getItem(i);
            if(item != null){
                if(item.mCode.equals(label)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }


}
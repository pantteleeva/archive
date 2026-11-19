package com.example.my_archive;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Адаптер списка архивных дел для ListView. */
public class CaseAdapter extends BaseAdapter {

    private final Context context;
    private final List<ArchiveCase> items = new ArrayList<>();

    public CaseAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ArchiveCase> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ArchiveCase getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_case, parent, false);
        }
        ArchiveCase c = items.get(position);
        ((TextView) v.findViewById(R.id.tvNumber)).setText("№ " + c.caseNumber);
        ((TextView) v.findViewById(R.id.tvTitle)).setText(c.title);
        ((TextView) v.findViewById(R.id.tvMeta)).setText(
                c.year + " • " + c.categoryName + " • " + c.docTypeName);
        return v;
    }
}

package com.budget.lefevre.easybudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HistoryListAdapter extends BaseAdapter {
    private List<HistoryElement> historyElements;
    private Context context;

    // constructor for history item adapter
    HistoryListAdapter(Context context) {
        this.historyElements = new ArrayList<>();
        this.context = context;
    }

    // add history elements to the adapter
    public void setHistoryElements(List<HistoryElement> historyElements) {
        this.historyElements = historyElements;
    }

    // necessary adapter override functions
    @Override
    public int getCount() {
        return historyElements.size();
    }

    @Override
    public HistoryElement getItem(int i) {
        if (historyElements.size() > 0)
            return historyElements.get(i);

        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.layout_history_display, null);

        TextView history = (TextView) view.findViewById(R.id.HD_historyDisplay);
        history.setText(historyElements.get(i).retrieveHistoryString());

        return view;
    }

    // useful functions for this particular adapter
    public void setIndexOfTo(int index, HistoryElement historyElement) {
        historyElements.set(index, historyElement);
    }
    public List<HistoryElement> getHistoryElements() {
        return historyElements;
    }
    public void clear() {
        historyElements.clear();
    }
}

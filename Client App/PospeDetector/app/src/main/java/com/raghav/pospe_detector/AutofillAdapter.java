package com.raghav.pospe_detector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AutofillAdapter extends RecyclerView.Adapter<AutofillAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> placename;
    private ArrayList<String> placeaddress;
    public MyCallback callback;

    public AutofillAdapter(Context context, ArrayList<String> placename, ArrayList<String> placeaddress, MyCallback callback) {
        this.context = context;
        this.placename = placename;
        this.placeaddress = placeaddress;
        this.callback = callback;
    }

    public static interface MyCallback {
        void onTouched(int position);
    }

    @Override
    public AutofillAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.autofill, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AutofillAdapter.ViewHolder viewHolder, int i) {
        viewHolder.callback = callback;
        viewHolder.placename.setText(placename.get(i));
        viewHolder.placeaddress.setText(placeaddress.get((i)));
        viewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onTouched(viewHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return placename.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected MyCallback callback;
        private LinearLayout rootLayout;
        private TextView placename;
        private TextView placeaddress;

        public ViewHolder(View view) {
            super(view);
            rootLayout = view.findViewById(R.id.rootLayout);
            placename = view.findViewById(R.id.placename);
            placeaddress = view.findViewById(R.id.placeaddress);
        }
    }
}

package com.example.maintrojan.adpater;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maintrojan.MainActivity;
import com.example.maintrojan.NoteActivity;
import com.example.maintrojan.R;

import org.json.JSONArray;
import org.json.JSONException;

public class RecyclerViewAdpater extends RecyclerView.Adapter<RecyclerViewAdpater.ViewHolder> {
    private static final String TAG = "myTag";
    private Context context;
    private JSONArray jArray;

    public RecyclerViewAdpater(Context context,JSONArray jArray){
        this.context = context;
        this.jArray = jArray;
    }

    @NonNull
    @Override
    public RecyclerViewAdpater.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.r_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdpater.ViewHolder holder, int position) {
        try {
            String[] data = jArray.getString(position).split("<=>");
            holder.textView.setText(data[0]);
            holder.date.setText(data[1]);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return jArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textView;
        public TextView date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textView = itemView.findViewById(R.id.text1);
            date = itemView.findViewById(R.id.dateView);
        }

        @Override
        public void onClick(View view) {
            try {
                String name = jArray.getString(getAdapterPosition()).split("<=>")[0];
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("type","view");
                intent.putExtra("pos",getAdapterPosition());
                intent.putExtra("fileName",name);
                context.startActivity(intent);
                MainActivity.recyclerPos = getAdapterPosition();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

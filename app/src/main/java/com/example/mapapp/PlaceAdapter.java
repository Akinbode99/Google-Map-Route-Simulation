package com.example.mapapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlacesViewHolder>{
    private ArrayList<ActorPlace> datalist;
    private View.OnClickListener mClickListener;
    public boolean isClickable = true;


    public  PlaceAdapter(ArrayList<ActorPlace> datalist){
        this.datalist = datalist;
    }

    @NonNull
    @Override
    public PlaceAdapter.PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.actorplace,viewGroup, false);
        RecyclerView.ViewHolder holder = new PlaceAdapter.PlacesViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick(view);
            }
        });
        return (PlaceAdapter.PlacesViewHolder)holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceAdapter.PlacesViewHolder placesViewHolder, int i) {

                placesViewHolder.place_primarylocation.setText(datalist.get(i).getPrimary_place());
                placesViewHolder.place_secondarylocation.setText(datalist.get(i).getSecondary_place());
                placesViewHolder.placemarker.setImageResource(R.drawable.ic_placemarker);



    }

    @Override
    public int getItemCount() {
        return (datalist != null) ? datalist.size() : 0;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        private TextView place_primarylocation,place_secondarylocation;
        private ImageView placemarker;
        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);
            placemarker = (ImageView) itemView.findViewById(R.id.locationimage);
            place_primarylocation = (TextView) itemView.findViewById(R.id.location_Primary);
            place_secondarylocation = (TextView) itemView.findViewById(R.id.location_secondary);
        }
    }
    public void setClickListener(View.OnClickListener callback) {
        if(!isClickable){
            return;
        }
        else{
            mClickListener = callback;
        }

    }

    public void UpdateList(ArrayList<ActorPlace> list){
        datalist = list;
        notifyDataSetChanged();
    }
}


package co.za.gmapssolutions.beatraffic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import co.za.gmapssolutions.beatraffic.R;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final String TAG = ItemAdapter.class.getSimpleName();
    private final List<RouteDetail> mData;
//    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    public ItemAdapter( List<RouteDetail> data) {
        this.mData = data;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(mInflater.inflate(R.layout.bottom_sheet_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        String animal = mData.get(position).getRouteDetails();
        holder.myTextView.setText(animal);
        holder.btnAltRoute.setText(mData.get(position).getBtnRouteStateValue());
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView myTextView;
        Button btnAltRoute;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.bottomSheetTextView);
            btnAltRoute = itemView.findViewById(R.id.btn_start_navigation_route);
            btnAltRoute.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    public RouteDetail getItem(int id) {
        return mData.get(id);
    }
    public void setBtnAltStateValue(int id,String value){
        mData.get(id).setBtnRouteStateValue(value);
    }
    public String getBtnAltStateValue(int id){
        return mData.get(id).getBtnRouteStateValue();
    }
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public List<RouteDetail> sortRoutes(int id){
        List<RouteDetail> sortedList = new ArrayList<>();
        RouteDetail first = mData.get(id);
        sortedList.add(first);
        int i =1;
        for(RouteDetail routeDetail : mData){
            if(routeDetail.getId() != id){
                routeDetail.setId(i);
                sortedList.add(routeDetail);
                i++;
            }
        }
        sortedList.get(0).setId(0);
        return sortedList;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}


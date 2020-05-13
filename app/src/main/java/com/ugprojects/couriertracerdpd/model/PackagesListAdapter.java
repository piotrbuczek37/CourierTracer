package com.ugprojects.couriertracerdpd.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ugprojects.couriertracerdpd.R;

import java.util.List;

public class PackagesListAdapter extends RecyclerView.Adapter<PackagesListAdapter.ViewHolder> {

    private List<Package> packageList;

    public PackagesListAdapter(List<Package> packageList) {
        this.packageList = packageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.courier_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Package pack = packageList.get(position);

        holder.packageNumber.setText(pack.getPackageNumber());
        String packageAddressText = pack.getAddress() + ", " + pack.getPostCode();
        holder.packageAddress.setText(packageAddressText);

        holder.deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView packageNumber;
        public TextView packageAddress;
        public LinearLayout linearLayout;
        public ImageView deleteItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            packageNumber = itemView.findViewById(R.id.packageNumberTitle);
            packageAddress = itemView.findViewById(R.id.packageAddress);
            linearLayout = itemView.findViewById(R.id.packageListLinearLayout);
            deleteItem = itemView.findViewById(R.id.deleteItem);
        }

    }
}

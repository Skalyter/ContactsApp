package com.tiberiugaspar.tpjadcontactsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;
import com.tiberiugaspar.tpjadcontactsapp.utils.ContactUtils;

import java.util.List;

public class SimplePhoneNumberAdapter extends RecyclerView.Adapter<SimplePhoneNumberAdapter.SimplePhoneNumberViewHolder> {

    private final List<PhoneNumber> phoneNumberList;
    private final Context context;

    public SimplePhoneNumberAdapter(List<PhoneNumber> phoneNumberList, Context context) {
        this.phoneNumberList = phoneNumberList;
        this.context = context;
    }

    @NonNull
    @Override
    public SimplePhoneNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_contact_details_number, parent, false);
        return new SimplePhoneNumberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SimplePhoneNumberViewHolder holder, int position) {

        PhoneNumber p = phoneNumberList.get(position);

        String category = ContactUtils.getCategoryForPosition(position);

        holder.category.setText(category);
        holder.phoneNumber.setText(p.getPhoneNumber());

        holder.imageCall.setOnClickListener(view -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse(String.format("tel:%s", p.getPhoneNumber())));
            context.startActivity(callIntent);
        });

        holder.imageSms.setOnClickListener(view -> {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse(String.format("sms:%s", p.getPhoneNumber())));
            context.startActivity(smsIntent);
        });

    }

    @Override
    public int getItemCount() {
        return phoneNumberList.size();
    }

    static class SimplePhoneNumberViewHolder extends RecyclerView.ViewHolder {
        final TextView category, phoneNumber;
        final ImageView imageCall, imageSms;

        public SimplePhoneNumberViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.item_category);
            phoneNumber = itemView.findViewById(R.id.item_phone_number);
            imageCall = itemView.findViewById(R.id.item_call);
            imageSms = itemView.findViewById(R.id.item_sms);
        }
    }
}

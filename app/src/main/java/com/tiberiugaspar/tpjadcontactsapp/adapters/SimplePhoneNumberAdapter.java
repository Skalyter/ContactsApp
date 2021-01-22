package com.tiberiugaspar.tpjadcontactsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;

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
        String category = "";
        switch (p.getCategory()){
            case 0:
                category = "No label";
                break;
            case 1:
                category = "Mobile";
                break;
            case 2:
                category="Home";
                break;
            case 3:
                category="Work";
                break;
            case 4:
                category="Main";
                break;
            case 5:
                category="Work fax";
                break;
            case 6:
                category="Home fax";
                break;
            default:
                category="Others";
                break;
        }
        holder.category.setText(category);
        holder.phoneNumber.setText(p.getPhoneNumber());

    }

    @Override
    public int getItemCount() {
        return phoneNumberList.size();
    }

    static class SimplePhoneNumberViewHolder extends RecyclerView.ViewHolder{
        final TextView category, phoneNumber;

        public SimplePhoneNumberViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.item_category);
            phoneNumber = itemView.findViewById(R.id.item_phone_number);
        }
    }
}

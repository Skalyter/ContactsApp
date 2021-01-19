package com.tiberiugaspar.tpjadcontactsapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;

import java.util.List;

public class PhoneNumberAdapter extends RecyclerView.Adapter<PhoneNumberAdapter.PhoneNumberViewHolder> {

    final private List<PhoneNumber> phoneNumberList;
    final private Context context;

    public PhoneNumberAdapter(List<PhoneNumber> phoneNumberList, Context context) {
        this.phoneNumberList = phoneNumberList;
        this.context = context;
    }

    @NonNull
    @Override
    public PhoneNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_phone_number,
                parent,
                false);
        return new PhoneNumberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneNumberViewHolder holder, int position) {
        final PhoneNumber phoneNumber = phoneNumberList.get(position);
        if (phoneNumber.getPhoneNumber()!=null) {
            holder.phoneNumber.setText(phoneNumber.getPhoneNumber());
            holder.phoneNumberCategory.setSelection(phoneNumber.getCategory());
        }
        holder.imageDeleteNumber.setOnClickListener(view -> {
            if (phoneNumberList.size() > 1){
                phoneNumberList.remove(position);
                this.notifyItemRangeRemoved(position, phoneNumberList.size());
            } else {
                Toast.makeText(context, R.string.remove_last_phone_number_warn, Toast.LENGTH_SHORT).show();
            }
        });
        holder.phoneNumberCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                phoneNumberList.get(position).setCategory(adapterView.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        holder.phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
            phoneNumberList.get(position).setPhoneNumber(editable.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return phoneNumberList.size();
    }

    static class PhoneNumberViewHolder extends RecyclerView.ViewHolder {
        final TextInputEditText phoneNumber;
        final ImageView imageDeleteNumber;
        final Spinner phoneNumberCategory;

        public PhoneNumberViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneNumber = itemView.findViewById(R.id.phone_number);
            imageDeleteNumber = itemView.findViewById(R.id.image_remove);
            phoneNumberCategory = itemView.findViewById(R.id.spinner_category);
        }
    }
}

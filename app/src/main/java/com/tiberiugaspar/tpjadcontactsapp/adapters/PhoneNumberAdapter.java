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

/**
 * Custom implementation of the base {@link RecyclerView.Adapter} class
 *
 * <p>This implementation is used to display editable information about a list of
 * {@link PhoneNumber} objects</p>
 *
 * @see androidx.recyclerview.widget.RecyclerView.Adapter
 */
public class PhoneNumberAdapter extends RecyclerView.Adapter<PhoneNumberAdapter.PhoneNumberViewHolder> {

    private final List<PhoneNumber> phoneNumberList;
    private final Context context;

    /**
     * The only constructor of {@link PhoneNumberAdapter}
     *
     * @param phoneNumberList - The list of {@link PhoneNumber} objects to be displayed
     * @param context         - The context from which the {@link RecyclerView} object is instantiated
     */
    public PhoneNumberAdapter(List<PhoneNumber> phoneNumberList, Context context) {
        this.phoneNumberList = phoneNumberList;
        this.context = context;
    }

    /**
     * This method is called when the {@link PhoneNumberViewHolder} is created, for each of the
     * {@link PhoneNumberAdapter}'s list items
     *
     * @param parent   - The viewGroup in which the {@link View} object will be inflated
     * @param viewType - Used in advanced custom {@link androidx.recyclerview.widget.RecyclerView.Adapter}
     *                 *                 with multiple view types
     * @return - A new {@link PhoneNumberViewHolder} to hold the values.
     */
    @NonNull
    @Override
    public PhoneNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_phone_number,
                parent,
                false);
        return new PhoneNumberViewHolder(itemView);
    }

    /**
     * This method is called to bind data from list to the {@link PhoneNumberViewHolder}
     *
     * <p>The main functions of this method are setting the category and the phone number
     * for each item from the phoneNumberList and setting a View.OnClickListener to make it possible
     * for the user to remove the current holder and its corresponding item in the list.</p>
     *
     * @param holder   - The {@link PhoneNumberViewHolder} object which holds the views
     * @param position - The position of {@link PhoneNumberViewHolder} corresponding
     *                 to an item from the list
     */
    @Override
    public void onBindViewHolder(@NonNull PhoneNumberViewHolder holder, int position) {
        final PhoneNumber phoneNumber = phoneNumberList.get(position);
        if (phoneNumber.getPhoneNumber() != null) {
            holder.phoneNumber.setText(phoneNumber.getPhoneNumber());
            holder.phoneNumberCategory.setSelection(phoneNumber.getCategory());
        }

        //remove the current holder and its corresponding item in the list only if
        //the list size is greater than 1, following the hypothesis that a contact
        //must have at least one phone number
        holder.imageDeleteNumber.setOnClickListener(view -> {
            if (phoneNumberList.size() > 1) {
                phoneNumberList.remove(position);
                this.notifyItemRangeRemoved(position, phoneNumberList.size());
            } else {
                Toast.makeText(context, R.string.remove_last_phone_number_warn, Toast.LENGTH_SHORT).show();
            }
        });

        //update the category of the corresponding phone number from the list at every change of
        //the selected item from the Spinner
        holder.phoneNumberCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                phoneNumberList.get(position).setCategory(adapterView.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //save any change from user's input in the EditText in the corresponding
        //PhoneNumber object from the list
        holder.phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                phoneNumberList.get(position).setPhoneNumber(editable.toString());
            }
        });
    }

    /**
     * @return - The size of the {@link PhoneNumberAdapter}
     */
    @Override
    public int getItemCount() {
        return phoneNumberList.size();
    }

    /**
     * Custom implementation for {@link RecyclerView.ViewHolder}
     *
     * <p>This implementation is used to display a list of editable {@link PhoneNumber}
     * objects, allowing the user to update any of the phone numbers a contact
     * might have</p>
     *
     * @see androidx.recyclerview.widget.RecyclerView.ViewHolder
     */
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

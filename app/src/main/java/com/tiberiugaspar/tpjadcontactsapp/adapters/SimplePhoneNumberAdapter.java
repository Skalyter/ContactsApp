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

/**
 * Custom Adapter for {@link RecyclerView}
 * <p>This implementation is used to display information about a list of
 * {@link PhoneNumber} objects</p>
 *
 * @see androidx.recyclerview.widget.RecyclerView.Adapter
 */
public class SimplePhoneNumberAdapter extends RecyclerView.Adapter<SimplePhoneNumberAdapter.SimplePhoneNumberViewHolder> {

    private final List<PhoneNumber> phoneNumberList;
    private final Context context;

    /**
     * The only constructor of {@link SimplePhoneNumberAdapter}
     *
     * @param phoneNumberList - The list of {@link PhoneNumber} objects
     * @param context         - The context from which the {@link RecyclerView} object is instantiated
     */
    public SimplePhoneNumberAdapter(List<PhoneNumber> phoneNumberList, Context context) {
        this.phoneNumberList = phoneNumberList;
        this.context = context;
    }

    /**
     * @param parent   The viewGroup in which the {@link View} object will be inflated
     * @param viewType - Used in advanced custom {@link androidx.recyclerview.widget.RecyclerView.Adapter}
     *                 with multiple view types
     * @return - A new {@link SimplePhoneNumberViewHolder} to hold the values.
     */
    @NonNull
    @Override
    public SimplePhoneNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_contact_details_number, parent, false);
        return new SimplePhoneNumberViewHolder(itemView);
    }

    /**
     * This method is called to bind data from list to the {@link SimplePhoneNumberViewHolder}
     *
     * <p>The main functions of this method are setting the category and the phone number
     * for each item from the phoneNumberList and setting a View.OnClickListener to make it possible
     * for the user to start the calling or sms activity from the current holder and its
     * corresponding phoneNumber in the list.</p>
     *
     * @param holder   - The {@link SimplePhoneNumberViewHolder} object which holds the views
     * @param position - The position of {@link SimplePhoneNumberViewHolder} corresponding
     *                 to an item from the list
     */
    @Override
    public void onBindViewHolder(@NonNull SimplePhoneNumberViewHolder holder, int position) {

        PhoneNumber p = phoneNumberList.get(position);

        String category = ContactUtils.getCategoryForPosition(position);

        holder.category.setText(category);
        holder.phoneNumber.setText(p.getPhoneNumber());

        //start calling activity for given phone number
        holder.imageCall.setOnClickListener(view -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse(String.format("tel:%s", p.getPhoneNumber())));
            context.startActivity(callIntent);
        });

        //start sms activity for give phone number
        holder.imageSms.setOnClickListener(view -> {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse(String.format("sms:%s", p.getPhoneNumber())));
            context.startActivity(smsIntent);
        });

    }

    /**
     * @return - The size of {@link SimplePhoneNumberAdapter}
     */
    @Override
    public int getItemCount() {
        return phoneNumberList.size();
    }

    /**
     * Custom implementation for {@link RecyclerView.ViewHolder}
     *
     * <p>This implementation is used to display a list of {@link PhoneNumber}
     * objects, allowing the user to use the phone numbers accordingly, in order
     * to initiate calls or sms conversations</p>
     *
     * @see androidx.recyclerview.widget.RecyclerView.ViewHolder
     */
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

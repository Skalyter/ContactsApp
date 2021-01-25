package com.tiberiugaspar.tpjadcontactsapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.tiberiugaspar.tpjadcontactsapp.ContactDetailsActivity;
import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.utils.ContactUtils;
import com.tiberiugaspar.tpjadcontactsapp.utils.TAGS;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private final Context context;
    private final List<Contact> contactList;

    private final ColorGenerator generator;

    private TextDrawable.IBuilder builder;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
        generator = ColorGenerator.MATERIAL;
        builder = TextDrawable.builder()
                .beginConfig()
                    .width(60)
                    .height(60)
                .endConfig()
                .round();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.contactName.setText(String.format("%s %s", contact.getFirstName(), contact.getLastName()));
        if (contact.getUriToImage() == null
                || contact.getUriToImage().equals("null")
                || contact.getUriToImage().equals("")) {

            String initials = ContactUtils.getContactInitials(contact);

            int color = generator.getRandomColor();

            TextDrawable drawable = builder.build(initials, color);

            holder.contactPicture.setImageDrawable(drawable);

        } else {
            Glide.with(holder.contactPicture.getContext()).load(contact.getUriToImage()).circleCrop()
                    .into(holder.contactPicture);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ContactDetailsActivity.class);
                intent.putExtra(TAGS.EXTRA_CONTACT_ID, contact.getContactId());
                ((Activity)context).startActivityForResult(intent, TAGS.REQ_CODE_EDIT_CONTACT);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder{

        final ImageView contactPicture;
        final TextView contactName;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.item_contact_name);
            contactPicture = itemView.findViewById(R.id.item_profile_pic);
        }
    }
}

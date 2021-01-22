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

import com.bumptech.glide.Glide;
import com.tiberiugaspar.tpjadcontactsapp.ContactDetailsActivity;
import com.tiberiugaspar.tpjadcontactsapp.R;
import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.utils.TAGS;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private final Context context;
    public final List<Contact> contactList;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
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
        if (contact.getUriToImage()==null || contact.getUriToImage().equals("null")){
            //todo: add gradle dependency from github project for custom icons
        } else {
            Glide.with(holder.contactPicture.getContext()).load(contact.getUriToImage()).into(holder.contactPicture);
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

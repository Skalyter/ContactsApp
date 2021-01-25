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

/**
 * Custom Adapter for {@link RecyclerView}
 * <p>This custom adapter instantiates the list of {@link Contact} items, displaying the
 * profile picture or a {@link TextDrawable} object, with contact's initials
 * and the first name and last name for each item.</p>
 *
 * @see androidx.recyclerview.widget.RecyclerView.Adapter for more details
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private final Context context;
    private final List<Contact> contactList;

    private final ColorGenerator generator;

    private TextDrawable.IBuilder builder;

    /***
     *
     * The only constructor of {@link ContactAdapter}
     *
     * @param context - The context from which the RecyclerView is instantiated
     * @param contactList - The list of {@link Contact} objects to be displayed
     */
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

    /**
     * This method is called when the {@link ContactViewHolder} is created, for each item in the
     * adapter's list.
     *
     * @param parent   - The viewGroup in which the {@link View} object will be inflated
     * @param viewType - Used in advanced custom {@link androidx.recyclerview.widget.RecyclerView.Adapter}
     *                 with multiple view types
     * @return - A new {@link ContactViewHolder} to hold the values
     */
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(itemView);
    }

    /**
     * This method is called to bind data from list to the {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}
     *
     * <p>If the corresponding contact has a profile picture, it is set using {@link Glide};
     * otherwise we use {@link TextDrawable} to draw users' initials instead,
     * applying a random background material color using {@link ContactUtils}'s getRandomColor() method
     * </p>
     *
     * @param holder   - The {@link ContactViewHolder} object which holds the views
     * @param position - The position of {@link ContactViewHolder},
     *                 corresponding to an item from the {@link ContactAdapter}'s list
     */
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.contactName.setText(String.format("%s %s", contact.getFirstName(), contact.getLastName()));

        //if contact has no profile picture available, we draw its initials instead
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

        //attach a View.OnClickListener() to the itemView to start the ContactDetailsActivity
        // when user taps on a item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ContactDetailsActivity.class);
                intent.putExtra(TAGS.EXTRA_CONTACT_ID, contact.getContactId());
                ((Activity) context).startActivityForResult(intent, TAGS.REQ_CODE_EDIT_CONTACT);
            }
        });
    }

    /**
     * @return - The size of the {@link ContactAdapter}'s list
     */
    @Override
    public int getItemCount() {
        return contactList.size();
    }

    /**
     * Custom implementation for {@link RecyclerView.ViewHolder}
     *
     * <p>This implementation is used to display information about a contact, such as:
     * name and profile picture</p>
     *
     * @see androidx.recyclerview.widget.RecyclerView.ViewHolder
     */
    static class ContactViewHolder extends RecyclerView.ViewHolder {

        final ImageView contactPicture;
        final TextView contactName;

        public ContactViewHolder(@NonNull View itemView) {

            super(itemView);
            contactName = itemView.findViewById(R.id.item_contact_name);
            contactPicture = itemView.findViewById(R.id.item_profile_pic);
        }
    }
}

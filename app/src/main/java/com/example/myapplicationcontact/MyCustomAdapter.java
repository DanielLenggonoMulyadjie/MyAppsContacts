package com.example.myapplicationcontact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyCustomAdapter extends ArrayAdapter<ContactsInfo> {

    private List<ContactsInfo> contactsInfoList;

    public MyCustomAdapter(Context context, int resource, List<ContactsInfo> objects) {
        super(context, resource, objects);
        this.contactsInfoList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_info, parent, false);
        }

        ContactsInfo contact = contactsInfoList.get(position);

        TextView txtDisplayName = convertView.findViewById(R.id.txtDisplayName);
        TextView txtPhoneNumber = convertView.findViewById(R.id.txtPhoneNumber);

        txtDisplayName.setText(contact.getDisplayName());
        txtPhoneNumber.setText(contact.getPhoneNumber());

        return convertView;
    }
}

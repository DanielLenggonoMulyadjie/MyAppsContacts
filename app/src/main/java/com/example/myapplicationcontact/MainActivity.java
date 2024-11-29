package com.example.myapplicationcontact;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    MyCustomAdapter dataAdapter;
    ListView listView;
    Button btnGetContacts;
    List<ContactsInfo> contactsInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetContacts = findViewById(R.id.btnGetContacts);
        listView = findViewById(R.id.lstContacts);

        btnGetContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestContactPermission();
            }
        });
    }

    private void getContacts() {
        contactsInfoList = new ArrayList<>();
        Cursor cursor = getContactsCursor();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ContactsInfo contactsInfo = extractContactInfo(cursor);
                if (contactsInfo != null) {
                    contactsInfoList.add(contactsInfo);
                }
            }
            cursor.close();
        }
        updateListView();
    }

    private Cursor getContactsCursor() {
        return getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER},
                null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );
    }

    private ContactsInfo extractContactInfo(Cursor cursor) {
        int hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

        // Pastikan kolom ditemukan
        if (hasPhoneNumberIndex == -1 || idIndex == -1 || nameIndex == -1) {
            return null; // Abaikan jika ada kolom yang tidak ditemukan
        }

        int hasPhoneNumber = Integer.parseInt(cursor.getString(hasPhoneNumberIndex));
        if (hasPhoneNumber > 0) {
            ContactsInfo contactsInfo = new ContactsInfo();
            String contactId = cursor.getString(idIndex);
            String displayName = cursor.getString(nameIndex);
            contactsInfo.setContactId(contactId);
            contactsInfo.setDisplayName(displayName);

            Cursor phoneCursor = getPhoneCursor(contactId);
            if (phoneCursor != null) {
                int phoneNumberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                while (phoneCursor.moveToNext() && phoneNumberIndex != -1) {
                    String phoneNumber = phoneCursor.getString(phoneNumberIndex);
                    contactsInfo.setPhoneNumber(phoneNumber); // Simpan nomor terakhir
                }
                phoneCursor.close();
            }
            return contactsInfo;
        }
        return null;
    }

    private Cursor getPhoneCursor(String contactId) {
        return getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId}, null
        );
    }

    private void updateListView() {
        dataAdapter = new MyCustomAdapter(MainActivity.this, R.layout.contact_info, contactsInfoList);
        listView.setAdapter(dataAdapter);
    }

    public void requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_CONTACTS)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]{android.Manifest.permission.READ_CONTACTS},
                                    PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
                getContacts();
            }
        } else {
            getContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts();
            } else {
                Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}

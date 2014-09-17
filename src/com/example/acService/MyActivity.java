package com.example.acService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class MyActivity extends Activity {


    private static int REQUEST_SELECT_APP = 12;
    private static int REQUEST_SELECT_LOGIN_ACTIVITY = 13;
    private static int REQUEST_SELECT_SUCCESS_ACTIVITY = 14;
    private static int REQUEST_SELECT_FAILURE_ACTIVITY = 15;
    private static int REQUEST_RECON_ACTIVITY = 16;
    private static int REQUEST_SELECT_INPUT1 = 17;

    private String appName, loginActName, successActname, failureActName;
    private ArrayList<String> editFields;
    private ArrayList<String> buttons;
    private ArrayList<String> fieldIds;
    private ArrayList<String> fields;

    private TextView appNameText, loginActText, successActText, failureActText;
    private Button btnApp, btnLoginAct, btnSuccessAct, btnFailureAct, btnRun;
    private Button btnInput1, btnGo;

    private boolean proceed = false;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    TinyDB prefsDb;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = getSharedPreferences("com.example.acService.prefs", Context.MODE_PRIVATE);
        editor = prefs.edit();

        prefsDb = new TinyDB(getApplicationContext());

        appNameText = (TextView) findViewById(R.id.text_app);
        loginActText = (TextView) findViewById(R.id.text_login);
        successActText = (TextView) findViewById(R.id.text_success);
        failureActText = (TextView) findViewById(R.id.text_failure);


        btnApp = (Button) findViewById(R.id.btn_app);
        btnLoginAct = (Button) findViewById(R.id.btn_login);
        btnSuccessAct = (Button) findViewById(R.id.btn_success);
        btnFailureAct = (Button) findViewById(R.id.btn_failure);
        btnRun = (Button) findViewById(R.id.btn_next);

        btnInput1 = (Button) findViewById(R.id.btn_input1);
        btnGo = (Button) findViewById(R.id.btn_go);

        fieldIds = new ArrayList<String>();
        editFields = new ArrayList<String>();
        buttons = new ArrayList<String>();

        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AppChooserActivity.class);
                startActivityForResult(i, REQUEST_SELECT_APP);
            }
        });

        btnLoginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!appName.isEmpty()) {
                    Intent i = new Intent(getApplicationContext(), ActivityChooserActivity.class);
                    i.putExtra("appname", appName);
                    startActivityForResult(i, REQUEST_SELECT_LOGIN_ACTIVITY);
                }else{
                    Toast.makeText(getApplicationContext(), "Please choose target application", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSuccessAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!appName.isEmpty()) {
                    Intent i = new Intent(getApplicationContext(), ActivityChooserActivity.class);
                    i.putExtra("appname", appName);
                    startActivityForResult(i, REQUEST_SELECT_SUCCESS_ACTIVITY);
                }else{
                    Toast.makeText(getApplicationContext(), "Please choose target application", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnFailureAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!appName.isEmpty()) {
                    Intent i = new Intent(getApplicationContext(), ActivityChooserActivity.class);
                    i.putExtra("appname", appName);
                    startActivityForResult(i, REQUEST_SELECT_FAILURE_ACTIVITY);
                }else{
                    Toast.makeText(getApplicationContext(), "Please choose target application", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateReconData()){
                    prefsDb.putString("appName", appName);
                    prefsDb.putString("loginActivity", loginActName);
                    prefsDb.putString("successActivity", successActname);
                    prefsDb.putString("failureActivity", failureActName);
                    prefsDb.putInt("runMode", 1);

                    Toast.makeText(getApplicationContext(),
                            "Please open the login screen of your target application.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        btnInput1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readFile();
                if(!editFields.isEmpty() && !buttons.isEmpty()){
                    Intent i = new Intent(getApplicationContext(), FieldChooserActivity.class);
                    i.putExtra("input_fields", editFields);
                    i.putExtra("buttons", buttons);
                    startActivityForResult(i, REQUEST_SELECT_INPUT1);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Sorry, we didn't find required fields during recon.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateAll()){
                    prefsDb.putString("appName", appName);
                    prefsDb.putString("loginActivity", loginActName);
                    prefsDb.putString("successActivity", successActname);
                    prefsDb.putString("failureActivity", failureActName);
                    prefsDb.putList("fields", fields);
                    prefsDb.putInt("runMode", 2);

                    Toast.makeText(getApplicationContext(),
                            "Please open your target application.",
                            Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_APP) {
            if (data.hasExtra("appname")) {
                appName = data.getStringExtra("appname");
                appNameText.setText(appName);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_SELECT_LOGIN_ACTIVITY){
            if(data.hasExtra("activity")){
                loginActName = data.getStringExtra("activity");
                loginActText.setText(loginActName);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_SELECT_SUCCESS_ACTIVITY){
            if(data.hasExtra("activity")){
                successActname = data.getStringExtra("activity");
                successActText.setText(successActname);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_SELECT_FAILURE_ACTIVITY){
            if(data.hasExtra("activity")){
                failureActName = data.getStringExtra("activity");
                failureActText.setText(failureActName);
            }
        }
        else if(requestCode == REQUEST_RECON_ACTIVITY){
            FileInputStream fis;
            try {
                fis = openFileInput("FIELD_DATA");
                ObjectInputStream ois = new ObjectInputStream(fis);
                ArrayList<FieldData> returnlist = (ArrayList<FieldData>) ois.readObject();
                ois.close();

                for(FieldData fd : returnlist){
                    fieldIds.add(fd.id);
                    if(fd.type.equals("android.widget.EditText")){
                        editFields.add(fd.id);
                    }else if(fd.type.equals("android.widget.Button")){
                        buttons.add(fd.id);
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_SELECT_INPUT1){
            fields = new ArrayList<String>();
            if(data.hasExtra("input_fields")){
                fields.addAll(data.getStringArrayListExtra("input_fields"));
            }
            if(data.hasExtra("button")){
                fields.addAll(data.getStringArrayListExtra("button"));
            }
        }
    }



    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        appName = prefsDb.getString("appName");
        loginActName = prefsDb.getString("loginActivity");
        successActname = prefsDb.getString("successActivity");
        failureActName = prefsDb.getString("failureActivity");
        fields = prefsDb.getList("fields");
    }

    void readFile(){
        FileInputStream fis;
        fieldIds.clear();
        editFields.clear();
        buttons.clear();
        try {
            fis = openFileInput("FIELD_DATA");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<FieldData> returnlist = (ArrayList<FieldData>) ois.readObject();
            ois.close();

            for(FieldData fd : returnlist){
                fieldIds.add(fd.id);
                if(fd.type.equals("android.widget.EditText")){
                    editFields.add(fd.id);
                }else if(fd.type.equals("android.widget.Button")){
                    buttons.add(fd.id);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    boolean validateAll(){

        if( appName.isEmpty() || loginActName.isEmpty() || successActname.isEmpty() ||
                failureActName.isEmpty() || fields.isEmpty())
            return false;

        return true;
    }


    boolean validateReconData(){
        if( appName.isEmpty() || loginActName.isEmpty() || successActname.isEmpty() ||
                failureActName.isEmpty())
            return false;
        return true;
    }
}

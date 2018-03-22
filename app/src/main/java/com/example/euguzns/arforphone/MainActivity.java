package com.example.euguzns.arforphone;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.ref.WeakReference;

import dataobject.MessageType;
import dataobject.SensorObject;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Sensor setup
    private SensorManager mSensorManager = null;
    private Sensor mLinearACCSensor = null;
    private Sensor mACCSensor = null;

    // Class instance
    Classification mClassificationForPhone;
    Classification mClassificationForWatch;
    Timer timerSensor;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_START = 2;
    public static final int MESSAGE_STOP = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_OBJECT = 6;
    public static final int MESSAGE_STAND = 7;
    public static final int MESSAGE_SIT = 8;
    public static final int MESSAGE_LIE = 9;
    public static final int MESSAGE_WALK = 10;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Temporary values related to ACC data
    private int accCount = 0;
    private boolean bluetoothConnected = false;
    private String selectedActivity = null;
    private int activityResult = 100;
    private int previousActivity = 100;
    private int previousRealActivity = 100;
    private boolean firstActivity = true;
    private boolean startOnPhone = false;
    private boolean doNotStart = true;

    //    private static SensorObject mSensorObject = null;
    private static MessageType msg = null;

    // Array to store ACC data
    private double[] accXArray;
    private double[] accYArray;
    private double[] accZArray;

    // Array to store ACC data
    private double accX;
    private double accY;
    private double accZ;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private final MyHandler mHandler = new MyHandler(this);

    private int sensorSize = 50;
    public static String mSdCard;

    String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());

    SensorObject objPhone;
    SensorObject objWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        accXArray = new double[sensorSize];
        accYArray = new double[sensorSize];
        accZArray = new double[sensorSize];

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Ask for permissions
        makeFolderPermission();
        objPhone = new SensorObject();
        objWatch = new SensorObject();
        timerSensor = new Timer();

        // Setup to listen the sensor
        // You can change the sensor type anytime on the second line "Sensor.TYPE_LINEAR_ACCELERATION"
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mACCSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final Spinner spi = (Spinner) findViewById(R.id.activitySpinner);
        spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedActivity = spi.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // This is the part you can change the classifier
        // Simply declare the classifier what you want to use and the change the last variable of mClassification
        mSdCard = Environment.getExternalStorageDirectory().getPath();
        String[] activities = {"Stand", "Sit", "Lie", "Walk"};
        SMO svm = new SMO();
        J48 j48 = new J48();
        mClassificationForPhone = new Classification(mSdCard + "/ARPhone_NEW.txt/", 24, activities, svm);
        mClassificationForWatch = new Classification(mSdCard + "/ARWatch_NEW.txt/", 24, activities, j48);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // Initialize the BluetoothChatService to perform bluetooth connections
            if (mChatService == null)
                mChatService = new BluetoothChatService(this, mHandler);
        }
    }

    @Override
    protected void onResume() {
        // unregister listener
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // unregister listener
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        if (mChatService != null) mChatService.stop();
        bluetoothConnected = false;

        // Close the app completely when you terminate from the phone
        // If this is not included, the sensor reading will run till battery depletion
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // Initialize the BluetoothChatService to perform bluetooth connections
                    mChatService = new BluetoothChatService(this, mHandler);
                    Toast.makeText(this, "Bluetooth is available", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
        Button buttonClick = (Button) findViewById(R.id.connect);
        Button buttonClick2 = (Button) findViewById(R.id.start);
        TextView textview = (TextView) findViewById(R.id.activityTextView);
        timeStamp = getTime();
        //String patientname = "Bob";  // Bob Phone, Alice Smart Watch
        switch (msg.what) {
            case MESSAGE_START:
                sensorStart();
                break;
            case MESSAGE_STOP:
                sensorStop();
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                buttonClick.setText("Connected");
                buttonClick2.setText("Do not start");
                doNotStart = false;
                bluetoothConnected = true;
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                if (msg.arg1 == 2) {
                    buttonClick.setText("Connect");
                    buttonClick2.setText("Start");
                    doNotStart = true;
                    bluetoothConnected = false;
                }
                break;
            case MESSAGE_OBJECT:
                this.objWatch = (SensorObject) msg.obj;
                selectedActivity = objWatch.getActType();
                break;
            case MESSAGE_STAND:
                textview.append("\n"+ResponseOfCurrentActivityandActiveSensor()+"\n");
                break;
            case MESSAGE_SIT:
                textview.append("\n"+ResponseOfCurrentActivityandActiveSensor()+"\n");
                break;
            case MESSAGE_LIE:
                textview.append("\n"+ResponseOfCurrentActivityandActiveSensor()+"\n");
                break;
            case MESSAGE_WALK:
                textview.append("\n"+ResponseOfCurrentActivityandActiveSensor()+"\n");
                break;
        }
    }

    public String ResponseOfCurrentActivityandActiveSensor(){
        String url = "http://192.168.226.126:8080/demo/CurrentActivityandActiveSensor?name=Bob";
        String response = sendGetRequest(url);
        return response;

    }

    public void afterWorksPhone() {

        double[] features = null;
        FileWrite fwr = new FileWrite();
        FeatureExtraction fe = new FeatureExtraction();

        fwr.fileSaveACC(objPhone, "Phone");
        features = fe.getFeaturesSingle(objPhone);
        fwr.fileSaveFeatures(features, selectedActivity, "Phone");

        // This is the part you need to annotate or not for respective phone or watch based recognition.

        activityResult = mClassificationForPhone.classify(features);

        activityResult = mClassificationForPhone.classify(features);

        if (firstActivity) {
            previousRealActivity =  previousActivity = activityResult;
            switch (activityResult) {
                case 0:
                    mHandler.sendEmptyMessage(MainActivity.MESSAGE_STAND);
                    break;
                case 1:
                    mHandler.sendEmptyMessage(MainActivity.MESSAGE_SIT);
                    break;
                case 2:
                    mHandler.sendEmptyMessage(MainActivity.MESSAGE_LIE);
                    break;
                case 3:
                    mHandler.sendEmptyMessage(MainActivity.MESSAGE_WALK);
                    break;
            }
            firstActivity = false;
        } else {
            if ((previousRealActivity == previousActivity && previousActivity != activityResult) || (previousRealActivity != previousActivity && previousActivity != activityResult)) {
                previousActivity = activityResult;
            } else if (previousRealActivity != previousActivity && previousActivity == activityResult) {
                previousRealActivity = previousActivity = activityResult;
                switch (activityResult) {
                    case 0:
                        mHandler.sendEmptyMessage(MainActivity.MESSAGE_STAND);
                        break;
                    case 1:
                        mHandler.sendEmptyMessage(MainActivity.MESSAGE_SIT);
                        break;
                    case 2:
                        mHandler.sendEmptyMessage(MainActivity.MESSAGE_LIE);
                        break;
                    case 3:
                        mHandler.sendEmptyMessage(MainActivity.MESSAGE_WALK);
                        break;
                }
            }
        }

        SendPhoneRequesttoDatabase(activityResult);
    }

    public void afterWorksWatch() {
        double[] features = null;
        FileWrite fwr = new FileWrite();
        FeatureExtraction fe = new FeatureExtraction();

        fwr.fileSaveACC(objWatch, "Watch");
        features = fe.getFeaturesSingle(objWatch);
        fwr.fileSaveFeatures(features, objWatch.getActType(), "Watch");

        // This is the part you need to annotate or not for respective phone or watch based recognition.


        activityResult = mClassificationForWatch.classify(features);                            // Classify the activity based on the extracted features
        switch (activityResult) {
            case 0:
                sendMessage("Stand");
                break;
            case 1:
                sendMessage("Sit");
                break;
            case 2:
                sendMessage("Lie");
                break;
            case 3:
                sendMessage("Walk");
                break;

        }
        SendWatchRequesttoDatabase(activityResult);
    }

    public void SendWatchRequesttoDatabase(int activityflag) {

        String currentactivity = null;
        if (activityflag == 0) {
            currentactivity = "Stand";

        } else if (activityflag == 1) {
            currentactivity = "Sit";

        } else if (activityflag == 2) {
            currentactivity = "Lie";

        } else if (activityflag == 3) {
            currentactivity = "Walk";

        }
        String url = "http://192.168.226.126:8080/demo/addActivity";

        restclient sendinfo = new restclient();


        sendinfo.setActivity(currentactivity);
        sendinfo.setName("Alice");

        Gson gson = new Gson();
        String json = gson.toJson(sendinfo);

        sendPostRequest(json, url);


    }

    public void SendPhoneRequesttoDatabase(int activityflag) {

        String currentactivity = null;

        if (activityflag == 0) {
            currentactivity = "Stand";

        } else if (activityflag == 1) {
            currentactivity = "Sit";

        } else if (activityflag == 2) {
            currentactivity = "Lie";

        } else if (activityflag == 3) {
            currentactivity = "Walk";

        }

        String url = "http://192.168.226.126:8080/demo/addActivity";

        restclient sendinfo = new restclient();

        sendinfo.setActivity(currentactivity);
        sendinfo.setName("Bob");

        Gson gson = new Gson();
        String json = gson.toJson(sendinfo);

        sendPostRequest(json, url);


    }

    public static void sendPostRequest(String post, String url) {

        OutputStream outputStream = null;
        byte[] outputBytes = null;
        HttpURLConnection httpConnection = null;
        try {
            System.out.println(post);
            httpConnection = (HttpURLConnection) ((new URL(url).openConnection()));
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestMethod("POST");
            httpConnection.connect();
            outputBytes = post.getBytes("UTF-8");
            outputStream = httpConnection.getOutputStream();
            outputStream.write(outputBytes);
            outputStream.close();
            InputStream inputStream = httpConnection.getInputStream();
        } catch (MalformedURLException ex) {
            System.err.println("MalformedURLException");
            System.err.println(ex.getMessage());
        } catch (ProtocolException ex) {
            System.err.println("ProtocolException");
            System.err.println(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            System.err.println("UnsupportedEncodingException");
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println("IOException");
            System.err.println(ex.getMessage());
        }

        System.out.println("Post Request Sent");
    }


    public static String sendGetRequest(String url) {
//Some url endpoint that you may have

//String to place our result in
        String result=null;
//Instantiate new instance of our class
        HttpRequestClient getRequest = new HttpRequestClient(); //Perform the doInBackground method, passing in our url
        try {
            result = getRequest.execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }
    // Nothing to do here but necessary function
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Ask user for permission to write a file to the phone storage
    public void makeFolderPermission() {
        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("myAppName", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void sensorStart() {
        mSensorManager.registerListener(this, mACCSensor, SensorManager.SENSOR_DELAY_FASTEST);
        timerSensor = new Timer();
        timerSensor.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                accXArray[accCount] = accX;
                accYArray[accCount] = accY;
                accZArray[accCount] = accZ;

                accCount++;

                if (accCount == 50) {
                    readyToStart();
                    accCount = 0;
                }
            }
        }, 1000, 20);
    }

    public void readyToStart() {
        objPhone.setData(accXArray, accYArray, accZArray);
        if (startOnPhone)
            afterWorksPhone();
        else
            afterWorksWatch();
    }

    public void sensorStop() {
        mSensorManager.unregisterListener(this);
        timerSensor.cancel();
    }

    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();

            /////////////////////to send to watch
           String url = "http://192.168.226.126:8080/demo/CurrentActivityandActiveSensor?name=Alice";
            String responseofget = sendGetRequest(url);///////////////////////

            String merge = message+','+responseofget;
            byte[] sendtowatch = merge.getBytes();

            mChatService.write(sendtowatch);
        }
    }

    public void connect(View v) {
        if (bluetoothConnected) {
            Toast.makeText(getApplicationContext(), "Already Connected!", Toast.LENGTH_SHORT).show();
        } else {
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
    }

    public void start(View v) {
        Button buttonClick = (Button) findViewById(R.id.start);
        if (doNotStart) {
            if (startOnPhone) {
                sensorStop();
                startOnPhone = false;
                firstActivity = true;
                previousRealActivity = previousActivity = activityResult = 100;
                buttonClick.setText("Start");
            } else {
                sensorStart();
                startOnPhone = true;
                buttonClick.setText("Stop");
            }
        }
    }

    public String getTime() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String result = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");//yyyy_MM_dd HH:mm:ss"yyyy:MM:dd_HH:mm:ss.SSS"
        result = format.format(new Date());

        return result;
    }
}

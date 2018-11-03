package com.socket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.socket.Constants.AUTH_KEY;
import static com.socket.Constants.AUTH_VALUE;
import static com.socket.Constants.CONTENT_TYPE_KEY;
import static com.socket.Constants.CONTENT_TYPE_VALUE;
import static com.socket.Constants.TIME_OUT_MIN;


public class ImageShareActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ImageShareActivity.class.getSimpleName();
    private Socket mSocket;
    private Button mBtnConnect;
    private Button mBtnDisConnect;
    private Button mBtnCheckSocketStatus;
    private TextView mTvInputMessage;
    private EditText mEtUniqueID;
    private EditText mEtServerUrl;
    private TextView mTvBrowserUrl;
    private String mUnqiueId, mServerUrl, mBrowserUrl;
    private ChatApplication chatApplication;

    /*
    image share
     */
    private static final int REQUEST_CODE = 100;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private String encodeImage;
    private OrientationChangeCallback mOrientationChangeCallback;

    //image optimize
    private Bitmap mReusableBitmap;
    private Bitmap mCleanBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagesshare);

        initView();
        /*
        image capture / video
         */
        // call for the projection manager
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // start projection
        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjection();
            }
        });
        // stop projection
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopProjection();
            }
        });
        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    private void initView() {
        mUnqiueId = "1";
        chatApplication = (ChatApplication) getApplication();
        mSocket = chatApplication.getSocket();
        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnConnect.setOnClickListener(this);
        mBtnDisConnect = findViewById(R.id.btnDisConnect);
        mBtnDisConnect.setOnClickListener(this);
        mBtnCheckSocketStatus = findViewById(R.id.btnCheckSocketStatus);
        mBtnCheckSocketStatus.setOnClickListener(this);
        mTvInputMessage = findViewById(R.id.tvInputMessage);
        mEtUniqueID = findViewById(R.id.etUniqueID);
        mEtUniqueID.setText("1");
        mEtServerUrl = findViewById(R.id.etServerUrl);
        mEtServerUrl.setText("http://192.168.0.123:4000/");
        mTvBrowserUrl = findViewById(R.id.tvBrowserUrl);
        mTvBrowserUrl.setText("http://192.168.0.123:4000/device/1");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
//                mUnqiueId = mEtUniqueID.getText().toString().trim();
//                mServerUrl = mEtServerUrl.getText().toString().trim();
//                if (mUnqiueId.length() > 0) {
//                    mUnqiueId = mEtUniqueID.getText().toString().replaceAll(" ", "");
//                } else if (mServerUrl.length() > 0) {
//                    Log.e(TAG, "mServerUrl: >>"+mServerUrl);
//                    mSocket = chatApplication.getMySocket(mServerUrl);
                    mSocket.connect();
                    mSocket.on("chat message", mMessageReceiver);
                    mSocket.emit("create", mUnqiueId);
//                    mTvBrowserUrl.setText(mEtServerUrl + "/device/" + mUnqiueId);
//                } else {
//                    Toast.makeText(this, "Please enter Id and server Url.", Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.btnDisConnect:
//                if (mSocket != null && mSocket.connected()){
                    mSocket.disconnect();
                    mSocket.on("join", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                        }
                    });
//                }
                break;
            case R.id.btnCheckSocketStatus:
                if (mSocket != null && mSocket.connected()){
                    mTvInputMessage.setText("Socket Connected!");
                    mTvInputMessage.setTextColor(Color.GREEN);
                }else {
                    mTvInputMessage.setText("Socket Not Connected!");
                    mTvInputMessage.setTextColor(Color.RED);
                }
                break;
        }
    }

    private Emitter.Listener mMessageReceiver = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value1 = args[0].toString();
//                    String value2 = args[1].toString();
                    Log.e(TAG, "mMessageReceiver: >>" + value1 + ">>");
//                    mTvInputMessage.setText("All Input Messages\n\n" + data.toString());
                }
            });
        }
    };

    /*
    image capture as video
     */
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    /*Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;
                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    // write bitmap to array
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    //compress the image to jpg format
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);

                    *//** encode image to base64 so that it can be picked by saveImage.php file
                     * *//*
                    encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                    // write bitmap to a file
                    fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
//                    sendToServer();
                    Log.e(TAG, "onImageAvailable: >>" + encodeImage);
//                    mSocket.emit("chat message", encodeImage);
                    IMAGES_PRODUCED++;
                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);*/

                    Image.Plane[] planes = image.getPlanes();
                    Image.Plane mPlane = planes[0];
                    mWidth = mPlane.getRowStride() / mPlane.getPixelStride();

                    if (mWidth > image.getWidth()) {
                        if (mReusableBitmap == null) {
                            mReusableBitmap = Bitmap.createBitmap(mWidth, image.getHeight(), Bitmap.Config.ARGB_8888);
                        }
                        mReusableBitmap.copyPixelsFromBuffer(mPlane.getBuffer());
                        mCleanBitmap = Bitmap.createBitmap(mReusableBitmap, 0, 0, image.getWidth(), image.getHeight());
                    } else {
                        mCleanBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                        mCleanBitmap.copyPixelsFromBuffer(mPlane.getBuffer());
                    }

                    Bitmap resizedBitmap;
                    if (3 != Constants.DEFAULT_RESIZE_FACTOR) {
                        float scale = 3 / 10f;
                        final Matrix matrix = new Matrix();
                        matrix.postScale(scale, scale);
                        resizedBitmap = Bitmap.createBitmap(mCleanBitmap, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
                        mCleanBitmap.recycle();
                    } else {
                        resizedBitmap = mCleanBitmap;
                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    resizedBitmap.recycle();

                    encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("device_id", "1");
                                jsonObject.put("message", "androiddeveloper");
                                jsonObject.put("message", "androiddeveloper");
//                                jsonObject.put("message", encodeImage);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("chat message", jsonObject.toString());*/
//                            mSocket.emit("chat message", "1", "developer");
                            mSocket.emit("chat message", mUnqiueId, encodeImage);

                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (sMediaProjection != null) {
                File externalFilesDir = getExternalFilesDir(null);
                if (externalFilesDir != null) {
                    STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
                    File storeDirectory = new File(STORE_DIRECTORY);
                    if (!storeDirectory.exists()) {
                        boolean success = storeDirectory.mkdirs();
                        if (!success) {
                            Log.e(TAG, "failed to create file storage directory.");
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
                    return;
                }

                // display metrics
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDensity = metrics.densityDpi;
                mDisplay = getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    /****************************************** Factoring Virtual Display creation ****************/
    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 1);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }

    /*
    api calling
     */
    public void hitRegApi() {
        try {
            JSONObject jsonObject = new JSONObject();
           /* jsonObject.put("user_id", "24");
            jsonObject.put("device_id", "132321");
            jsonObject.put("device_type", "android");
            jsonObject.put("device_token", "978978879");*/

            jsonObject.put("user_name", "test111");
            jsonObject.put("email", "test112221@gmail.com");
            jsonObject.put("password", "test1");
            jsonObject.put("user_country", "101");
            jsonObject.put("user_latitude", "test1101");
            jsonObject.put("user_longitude", "test1102");
            jsonObject.put("user_contact", "9926012345643546545");
            jsonObject.put("device_id", "abc");
            jsonObject.put("device_type", "xyz");
            jsonObject.put("device_token", "pqr");
            jsonObject.put("username", "votivetestweq1kkj");
            jsonObject.put("bike_name", "1");
//            jsonObject.put("confirm_password", "test1");

            final String requestBody = jsonObject.toString();

            Log.e(TAG, "hitApi: >>" + "http://votivelaravel.in/grupapp/api/register");
            Log.e(TAG, "hitApi: >>" + jsonObject.toString());

            StringRequest sr = new StringRequest(Request.Method.POST, "http://votivelaravel.in/grupapp/api/register",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "onResponse: >>" + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse: >>" + error);
                        }
                    }) {
                /* @Override
                 protected Map<String, String> getParams() {
                     Map<String, String> params = new HashMap<String, String>();
                     params.put("device_token", "qwertyui");
                     params.put("deviceid", AppUtils.getDeviceId(SplashActivity.this));
                     params.put("devicetype", "android");
                     return params;
                 }*/
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put(AUTH_KEY, AUTH_VALUE);
                    headers.put(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return String.format("application/json; charset=utf-8");
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                                requestBody, "utf-8");
                        return null;
                    }
                }
            };
            ChatApplication.getInstance().addToRequestQueue(sr);
            sr.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_MIN,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hitApi() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", "24");
            jsonObject.put("device_id", "132321");
            jsonObject.put("device_type", "android");
            jsonObject.put("device_token", "978978879");
            final String requestBody = jsonObject.toString();

            Log.e(TAG, "hitApi: >>" + "http://votivelaravel.in/grupapp/api/getprofile");
            Log.e(TAG, "hitApi: >>" + jsonObject.toString());

            StringRequest sr = new StringRequest(Request.Method.POST, "http://votivelaravel.in/grupapp/api/getprofile",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "onResponse: >>" + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse: >>" + error);
                        }
                    }) {
                /* @Override
                 protected Map<String, String> getParams() {
                     Map<String, String> params = new HashMap<String, String>();
                     params.put("device_token", "qwertyui");
                     params.put("deviceid", AppUtils.getDeviceId(SplashActivity.this));
                     params.put("devicetype", "android");
                     return params;
                 }*/
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put(AUTH_KEY, AUTH_VALUE);
                    headers.put(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return String.format("application/json; charset=utf-8");
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",requestBody, "utf-8");
                        return null;
                    }
                }
            };
            ChatApplication.getInstance().addToRequestQueue(sr);
            sr.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_MIN,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
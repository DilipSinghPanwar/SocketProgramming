package com.socket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class TextMessagesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TextMessagesActivity.class.getSimpleName();
    private Socket mSocket;
    private EditText mEtInputMessage;
    private Button mBtnConnect;
    private Button mBtnDisConnect;
    private Button mBtnCheckSocketStatus;
    private Button mBtnSendMsg;
    private TextView mTvInputMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textmessage);
        initView();
    }

    private void initView() {
        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        mEtInputMessage = findViewById(R.id.etInputMessage);
        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnConnect.setOnClickListener(this);
        mBtnDisConnect = findViewById(R.id.btnDisConnect);
        mBtnDisConnect.setOnClickListener(this);
        mBtnCheckSocketStatus = findViewById(R.id.btnCheckSocketStatus);
        mBtnCheckSocketStatus.setOnClickListener(this);
        mBtnSendMsg = findViewById(R.id.btnSendMsg);
        mBtnSendMsg.setOnClickListener(this);
        mTvInputMessage = findViewById(R.id.tvInputMessage);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                mSocket.connect();
                mSocket.on("chat message", mMessageReceiver);
                break;
            case R.id.btnDisConnect:
                mSocket.disconnect();
                break;
            case R.id.btnCheckSocketStatus:
                Log.e(TAG, "connection status: >>" + mSocket.connected());
                mTvInputMessage.setText("Socket Connection Status : " + mSocket.connected());
                break;
            case R.id.btnSendMsg:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSocket.emit("chat message", mEtInputMessage.getText().toString().trim());
                    }
                });
                break;
        }
    }

    private Emitter.Listener mMessageReceiver = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = args[0].toString();
                    Log.e(TAG, "NmMessageReceiver: >>" + data);
                    mTvInputMessage.setText("All Input Messages\n\n" + data.toString());
                }
            });
        }
    };
}
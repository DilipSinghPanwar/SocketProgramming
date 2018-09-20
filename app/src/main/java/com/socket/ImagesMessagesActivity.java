package com.socket;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ImagesMessagesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ImagesMessagesActivity.class.getSimpleName();
    private Socket mSocket;
    private EditText mEtInputMessage;
    private Button mBtnConnect;
    private Button mBtnDisConnect;
    private Button mBtnCheckSocketStatus;
    private Button mBtnSendMsg;
    private TextView mTvInputMessage;
    private Button mBtnTakeScreenShot;
    private ImageView mIvImageMessage;
    private Button mBtnSendImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagesmessage);
        initView();
    }

    private void initView() {
        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        mEtInputMessage = findViewById(R.id.etInputMessage);
        mEtInputMessage.setText("Hi, How are you ?");
        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnConnect.setOnClickListener(this);
        mBtnDisConnect = findViewById(R.id.btnDisConnect);
        mBtnDisConnect.setOnClickListener(this);
        mBtnCheckSocketStatus = findViewById(R.id.btnCheckSocketStatus);
        mBtnCheckSocketStatus.setOnClickListener(this);
        mBtnSendMsg = findViewById(R.id.btnSendMsg);
        mBtnSendMsg.setOnClickListener(this);
        mTvInputMessage = findViewById(R.id.tvInputMessage);
        mBtnTakeScreenShot = findViewById(R.id.btnTakeScreenShot);
        mBtnTakeScreenShot.setOnClickListener(this);
        mIvImageMessage = findViewById(R.id.ivImageMessage);
        mBtnSendImage = findViewById(R.id.btnSendImage);
        mBtnSendImage.setOnClickListener(this);
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
            case R.id.btnTakeScreenShot:
                shareScreen();
                break;
            case R.id.btnSendImage:
                mSocket.emit("chat message", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NzM2QjVGQjlBRjQ0MTFFODlEMzBFRkVERTE2MUY1RUYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NzM2QjVGQkFBRjQ0MTFFODlEMzBFRkVERTE2MUY1RUYiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo3MzZCNUZCN0FGNDQxMUU4OUQzMEVGRURFMTYxRjVFRiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo3MzZCNUZCOEFGNDQxMUU4OUQzMEVGRURFMTYxRjVFRiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pn6HA1oAABx4SURBVHja7J0JbBvZecffHJzhLZGUqFuybNmW1td6bWfXXZ97JHs43iMOkqBIgAYNiiJNigZIESANUCAtUDQ90LTYokBbtF1ssosku9jLayfew/J9yWtblg/JuqiTFA/xvtnvG4mORFEWKVHWwe+fcGWRM6OZ4fvNd7zvvcelUilGIpGyi6dbQCIRICQSAUIiESAkEgFCIhEgJBIBQiIRICQSAUIiESAkEokAIZEIEBKJACGRCBASiQAhkQgQEokAIZEIEBKJACGRCBASiUSAkEj5S1zNF3fy5MlFOW5vby8zmUzs8uXLzOFwsPr6elZRUcFu3brFBgcHWTKZZFVVVczn87FNmzaxGzdusFgsxhKJBBMEgeFMMkajkWk0Gma324VQKCT7/X4Z3hMOHjxovHnz5pq2traaxx9/XGu1Witg/6rx8XFTMBjU8zyfikQiktfr1U2ezoKmpYFzFURRFDZs2PDe1q1b/0Or1Y794Ac/IDKKAZDlJo7jFEBA+ng8Xg4ANXg8ns0AR2MgEFgXDofNr732msnpdJbB7wiKCFCpotEoh1DhC48x2bALdl54LIB7F8BRvmXLlr+Gtzz0bREgD8eH5fm01dAjBGAFHgUQHgPLs7erq2szWA8VWpelnp8MITx69Oj3DAbDCfj1A/rmCJBFF4IBjb8GwNg3Ojq612az4c9N4B4ty/MdGxvjwbVrIUAIkEW3GuCuVAEMh99///3D3d3dBwYGBrTL/bxVKpXicdE3SIAsGhhqtVoGC/Hc+fPnv3Pnzp2nIWhX5x2rwP94gceghcmShO5XCixRCoJpjEE4jueToiBCQJJ3gA5H5DCaSYaCQXTt+CyQECAESGGF8QM+fSHwXtvZ2fnnPT09RwCM6lybrNlkBiAED/j/vtraOtfo8FAAfo9V1dQ6LJay0Z7ebk93V2esurqGl1QSJ2s08bLy8jjP8alkKpnPiXKwT1RSq7mee12HPr96ZR9m1jKvhUSAFBQOsBoM4oynWltbf3Lt2rUDGHQ/SBJYBZPJ7DeZzd2SrG5fU19/jRNE24aNG4YT8eTIxfNnImAeEmvXNYXKyqzhca8napNUSaPRyMmymtPq9Kmq6pqkAHAl8slmJeGogpDU6/VyOBgw32yXAJAQfYkEyOIJ+zIg+P7GiRMnftrR0bHuQelX3La6puZiTU1Nq95Q0rZuXdPNgQFbv06r8/OiGAdrwfp6+9ASKZYlGo3CK6L0nyCI+HPiFVf6VZL5AoKMwH6RUDAVDoX5dMqYRIAszs0TRd7lcn0L4Pi7oaGhitm2MxiNbO/e/UfNlrJ33G7Xab1e1xeLxUNarU7p10AghMmfi2rt4BWNhPG8hRR99wTIYkoAeTyeb3744Yc/w469rEE7x7Ot27ZdWr9x43/V1a35IBwOD/t83iRag2QiqXTQPSyfH/9ONBKZDHvIchAgiyie5zm3233o7bff/tvZ4DCbTZEXDr302pYt235+48bnfdhG067SUigSDtMXR4AsrtJPXq/X2/Lmm2/+qK+vrzoLPMxaUTny5S+/9OPKmtpfgqEJLWVmaKrlIBEg0wPSZLLgxwMADOBW/fD06dNPZNtm0+Ytd188dPj7ao3meDyeWFKYwZcjy0GAzK5wARsHNji/38/19PR8/dy5c3+YbZv16zf2PvvF5/5YkuVTmGXiOH7p4ACYowQHAZKLS1QINwXrqqDRNb3xxhvfg+BclblNZWWV+1t/9O0flZdZT417PSwYDLJEovCd0ulUL7pyPF4fvMLhkJIB43msFMZzFVkCfldcOwrICZAHBNOFdK/4S5cufaOjo2NL5mcajTb60qtH/q3lkUd+HQ1HmMlUyjq7OqHRhgsCOVqjYDCgWMTaOivD9LDP61VSwyF4b/OWrazcamUBv49JKpndutXB3PBvWa1BuqmVEyCLCwgeZ3R0dO2xY8e+nq2X/A/27Dlz4Kmn/zUSCifSmapCBOZ4jFAwxBrq1zBTaSmTJZkZS01MAEvR230Pzotjbo+HvXrka2zXrl3M1t8HgEisoqKKHT9+VLEskwWIJAIky8WJC7889OXLy8vZmTNnDnV1da3L/Ly01OTY/eSefykpKXE4RkYUmNIxQBoW7BHP92+GIhEFgDJrOautrQXHKamMUHQ6nSyeiGPtlvI5XiMePxCYsDDhUJhVV1exF154kX344QdgfSKKy0UiQGYIa6QK4eJAwyv95JNPXoWf0tTP8On85N69J9Y0NH40PDTIErGJEhF0+wEcZjQm4YmuYlu2PspGAZ7MwsBsisWi6LKxZ770IhxfZBotXEOKY16vj8UAhLksE54vxj/l5RXsxRe/DJC8yzCbVkh3kwBZJTp37lxBrJDL5dpz+/bt5szPdHqd87HHdr2l1xujoVCQCbI4UX+eQkA0CijYng3GUlZVVQuQDLOhIRtsIM/SuBlrXNsEcUY9KyuzslAowCKRcN4WACHB/SwWCzt06CX23nvvwnkkqQedAJkuj2fhQ6tlWcZJF/aOj4+bM+OS9eubL5dXVJyZmIxBnLXRowwGIxt3u7L2zaRjFlGUFauEQTgWKS60HwfjJbRkhw+/DJC8Ta2dAJkunDmkAEF+WW9v765QKCRMd9804Wefe/63m7dsHcslxsBBUNaKciZpJHb8o6NMFFT3wVi3fgMzm8smLQ6+lyjY0x7hhfiIvfTSq+zdd9+h8R4ESOGyWOhegeXYMjY2tjbzMwjcx5rWr78QiUSV8vO5mjM2SxwliJYGx4OoREkJtp/cu1/JSi1mw0VLZDQiJK+w999/b8IyKQASLEUNSL7Zo2y+PMCxORAIVGS+39i4rs0+MtIx0N+f+wFx2h6AdsfOL4DrpgYAJWisD6eAEaEwmy3gbr3C3nzrjVQ4HMZeRCKAAJk/HOCecN3d3Rv8fv+0dJhGo0mUW62f+7zj7nx7y9NzW/G8CIAsvI8Cj4VxklarZWqNhvHozM3inqH1slor2I7HdgZ/e/yjKPFR5IDs2LFjQe4VPGU1x48fX5NZ06XX60OVlVV34/FkXj3VKSwDAXeqBDv91Jp5BeFKiQkcA4L5MkEUKlKppOX27Q45GgnpxhwONU7KAISkZtmXSyVT4b6eHrAgoa3JJSymJECWga5cubKg+AUaYYPP56vK/Kyk1GSvrWvoS8RjeXgpHEtiDZUgMFmjVf6dr6UAK6HyjI/vHhy07R/ot20CGFqcTsfa137+T3IimRCSiRQ/p4uH58vxLBKNKP0qpCIG5J133lnQ/gDIuoGBgdrM99VqtdPhGLXnM6NICqyFRqvDokY216QOmS6ZSiVhZXDL1bbLf3b3zp2XAJCaYCBY8PuFfysej1OPYrEAcvDgwYXAwYaGhkru3btnyPwM4o9+WS2P5hdcp5gkSzn1pk9tsFgN4Ha79vz2o6M/a2u79EQisXhuEbp8giBQZFIsgDQ3N897X5yBJBAIWDAOyXR1wO/vbX7kkfFUjjGEUnQYCrNIBN2aWM59HAipx+N+9NjR9//5yuVLOxf7fmG/0caNG7sIiyIBpK2tbd77Yl9FV1eXBdwhLrPR+ry+xFu/eCOnAB03UWvUbM++gyyOPr8C2Nz7TfThcPq2y5e+e7Xtys654pOpP/NVekmG559//vWKioqThEWRAIK1SPMVBMRcb2+vLjNeQHAa1zYFU6ncrYcOYo/amtq8pvXBv9Pbc++J9vYbr2TLdsmynCovLx+En4O1tbW2uro6J1i98P2MQI7nBvvjBBSi3++/A8f5JbztJiyKBBAcIzFfGQwGwWazyZlPe3jaxqqrqxz5ltK3nvw013arFBYC3LzDbt/b39c7g3IAw719+/b/bWlp+cXt27e7wHJESktLkzqdLpVu+LkCgq4kAIgz/sbg9wSVohQRIFismHY78v3isZMQYgYuS1wQHx/3hicXwsk5QE+l8nB/cBTh8IjlZvv1RzLPGy3H4cOH/xPio5/AvyPoimHgjlYmbWnyASRzX1IRAYI96aFQSPGx8YmfTwYI51AHQGZ0dUuynHCOjcXzqfPCdHA4HMljeDjHBFGwuMddM2rAGhsbh5qamt4DFzCSXu6NGjcBMi9VVlayY8eOKW4EVrRilmZithEuJ0AAMDGLZUnE4rGcAZmIQfTsq1/9BovlGIPgIKt+W1/Jv165XJ8loLY7nU4HjiDEcwC3SrmmxZ62lABZhcIn60Sq1MPa29vZM888o/Qr5FKjNQnADF8lEU8o48RzzRih1dKoteyZZ7+U8zREarUM53tDUImikCV5EIB4w4dBPGrv3r3K9djt9vS8XdSqCZD8pKRmfT528eJFtnPnTqylmhOSiZonPpXFsnBY05RPShWn4Pn88zZlOG1O56tksLplCNZntHaz2exrbm4OolVMxxqTnZpKWrsQw4xJRQbIZHDL3G43u3DhAtu9e7dS/fogdwvhyPZZkiXTWZ+crVgcXjhOPNcSEwmtVCikylb2gbHHvXv3UtyU/hSE2e/3K9fC5djPQiJAZsQC+HRFdwsh2bx5s9LXMFvgDo0T1y+XZsQAnJAEFyjO5ejKICAacJkwSZBrw8VtIdbA5aCyxUZcZ2fnDNjSZe9oTaSJZdtyuie4/eTSbvfn4CIVISBTIRkfH2dXr15lGzZsUBoIQpJpEaBhi5FIZMbsCoIoJmS1OsbnAYislgt5DRye82wWbGxsTIl1crFwaUDQ+uBPdDsXUp5DgKwSSLAxYCO6e/cu1h7dL7XI8rSe7TDLrqAPgcWUtsPhyAr8bPCi9cAaMZPJpPy+kOqD1aiiTXkgFNgw7ty5s+L7ERAOBB7hmOKi5fyaag0Xs1qYAFmBDQvditu3b6/YwHZyYjslzTuH1SMRIPNrYFMhWUkNbGKCuIgCRzrIJhEgiwrJSulsy4SDRIA8lAaHMYmy6uxEMeKya3npyebSATnBQYA81JgEGx5mtyBYjUqStOxmNcBqYiy57+npITgeglZ9mhcbEaZ1oVHpRVEEDyqZelDDmpzuJ2mz2dT5lbQ/HGG5u8Fg0AG8SVynPbWA7EI6zQvXycM9CsF9obLgYgJkMnCtHBgY+Lrb7W6Chh+ZC5BJxbu6ujSw367ldk1XrlzZDtfyVz6fDzzCiLAQQNJLy0H8JYPlHKiurv4V3JtuwqJIAIEvv6Kjo+Pvz50797VYLCahC5XreHCMQwqxCGiaxXxWnfr9tjNBvnXr1trOzs7vY1lJIVLT6doteGEJ/4Hdu3f/KbzdS2gUASC3b98+3Nra+k2MKwoYJnNY7pFr48SlmJOpyWlwcw0ZJrbjE/F4Vh8P4V2E8R/iqVOnntu3b99Xjhw58o+ERhEAcv369S2FhUMZpcgHAj4x11RwQplrildWjRLF3MvdwULEBFF8qFE49qIPDg7WEhZFAgiWpRf6mOXWCvf+/U/fyjWAT00G/iMjwyzXmVCUIcIqVefG5pbzw0NDBx/mPdPr9TSrSRHFIIlZYoykVqeL8ZwSk+QEUSwWlWpqawdffuXI3zSsabyZh5evLMCJS6LlIzjHgYNPf/Gnfp/fcv361a2ySopxPL/gsENZYDSVTIWCQRVYjGlmEMvkbTabi7AoEkCySZbVyVe+cuT/tmzZ+rrL6RSgsUjcLLOh3w8IYINxj0fQanU2s9lyE1ppHoMmUsr/8u2zwGyb0Wj8bN+Bp17e0Nyy0WIpk9SyKo6L9cy3/xIfBpIkh9Tg7338u+N/cfpU61NTP8ehAPX19TbCoqgBkWNr1jZdM1vKPvX7fBO5ogc0Xvwch7fiTOj5DHoqSDoABPFLj15v6MHJGUSBZwsBJD1GHw5r5XnRi67c1Opdk8k0DtuMERZFDAg0O37c7ZLi1TU8BMMJLC+ZK4ZIp12XquJ36t+f7zngfmA9WCAYZP39fSUer6cus7QdHgR+7DAkLKa4ukV3xdh5yHOYrGUlJpOyKlNqlc8rhSBgfGEyWVjjmnWsaV2T1mEfLZ0ZZ8V8DofDR1gUtQWZ7nIYS0oUhwXTwauxtgmvqaa2TllFF69XklS4/rpaI6tnfPdWq9VVXV3tJSwIkGmQGAAStCzBycnYVosw3kAXUqfDaY4msmhoPSPRcFUsFtVlbr9mzRrXcizQJECWASR6w8Q6OasFEkwo2Gz9ylRHN25cuz8FHi/wgt/rbfB6vTMWkQ+Hwx673U5jbgmQ7EFsGpKA38+WYyVvPgIAlNlbMP6YupqugBfG8fpYPD5tSiN8e2hoaCQQCNC8PwTI7EJI8GEbXKmQAOglpSZmMptZdU3thDWckvmS1Wru5s129alT09fJwWt97LHHeiorK8nFIkBmtyIY1CIkAjSsCFbz4ngSSbpf9TrfQD7f9CxuP9XVUyZ4E/jJ6uDZ10F3Oh1seGQYO0TTR5q2jVqt1jhGRxsyV9md/HvO2tpacrEIkAc3TCwlHx4cZBcvnGNq+DeOV29Ys5ZVV9ewYDAADbEy7766fBfcwbiov6+XiSqJqUSBfXziuDKT+4OmFsXy/B07vsAOHHiGhcIzuzNU4GrZHaPyhx+8b838TKvVJj0ej/fatWvUCAiQ2YVPalt/Hztz+qTSGP2xmJICTk/GpsyhxS3u9DppCHAaUJUkK1bD6x2/P6HEbIAgvLgvuldyMJjFwghYXSyWlJTMmFLVbDZ7wHqMIIQkAmQWFwUCVbAcn336yf35almGq/MwNRXCydnmHwiIXm9gpwHsWDzGmtZvnDGbPF6fz+u1xKIxSxZAXCMjIwFaPoEAmdUFGh4aYp98/DuWXntjJQrP/dLF86ynu0sBRhlYNTlgC6uXw5GI1eV2mTP3g+3Gh4eH4zQzPAGS1a0aHBxgn358YkXDMRV2p9OpxE4VFVXTgv4US2nhFznLPmPgXkUIEAJkxhN3wGYDt2p1wDHVncJ+kJaWzay+vkGJX+A9zu12yVfb2mZ87w0NDaPbt28PESAEyP0nKo5/6O/rYyc/+2RaZ9pqgv/s2VY2Ntas1GKBBRFdLlcNuFgzetHr6uocGzduDNOCoMUOSGpi4JDBYGS3OtrZqdbPFNdjNRYqTqSs1ayrq5NZLG6MScSxMXtNwOeb1gOK1z8wMIBjQWKryYoSIPMUwBHraL+RaD11Uqn3X+2ZG4yx7PZRZdUqY0mpKtNKYL8PfDYKiuO2pCIGRKPRpBLxeOLq1TYWi0aYXqd/cC+30nvOc8qSaEtgZRBeYSLlzONY+lzn9soGCeOYKplIGOBQ8M7vO8yNRmO4trZ2yO12p2gJtiIHJBDwiydPfno4kYib44kE83l9YpbVnqf5ZJFIWIIncKC+Yc1HvCBcZA/eoXBfDsRFfp9/x8jQ0AuhULAEGnaEU7rw5/HnORbr7+8zOu32fRPDdqfflps3b/oxJqMgvcgB8fv9/NUrl/cLovAkp8w4MnfVCC7H3Nvbl7h769a3X371yI/5pqbX87Em8xkqq6yAFQ5/5Te/+uXPXC5nVTQSEcCGJBmb/5TzOA1SPB6TMk9Fp9O5E4lEACsGCJAiAiQ+y8yE0BgEfOV1sGhM1dPbXffpJyf+0mQxX4EG3JErHOjeNK5dm/NSbxP9GK4Nra2f/tDW39e42PepsbFxaNOmTZ5FmK2RAFnOwpnQC33MwSFb7dkzpzdDLJATIMpgLL2BrcfSj2Ru/r3Aiyzg89f1dndvexj3CWIQ3/bt24OzLWZKgKxSwVPxklarjQWDwYKlZgRBlRRVYiqfZaBVkqiUoieTqRz/hrIkAa5KsOil5wjFunXrrjc1NbmWcuYWAmQJVF9f/+G+ffv+/dy5c98E90GD80zlGDsom+I66ZmzD+IkczzH4yvX4JhNzuCY3+zu8F+On7leB2ayJEmKYFZtvuHI5LlwYGEje/bsad2/f///wHFD5GIVGSDQCFwtLS0/Wb9+/bn+/v7maDSKHWGpuWIBgCAGcYP2zp07r7a3t29dZlbxLrze9Pl8IewZn2ewzgP4YkNDQ8/u3buPGQyGUVr+uTgBwSeut6am5k3M0OALe4rnepJjY6msrFTDdmuXGyDbtm1rf+KJJ/6htbU1AO7jvI6B14cuIqZ1wf0kt6pYAUlDgq4DNor060ENIl31CiCJYHGW3f0ByIWRkREeO/Tm+9RP3wOqu5pbNDomAw6MUcrKyrDHXYBGuOxmbYBGLdjtdp5G/hEgSwJHeXm5Mll1eojtchRmnhCQyYmo6csjQB6OEA70y1dCwDo5viOnmIpEgCy4sVmtVgWOleaXIyRkSShIX1TXqrKyUnkSr9SgNV2ijoH7ahzXQhZkCeFAy7HS3ZR0vRe5WwRIwRpUGo5CrTe+HK4JixzJ3SJAFtyQUOmYYzU1prQlIUgoBpl3MJ7u51iJAXk+kKCotooAyRuOlZTKJUjIxXqo/nnarSqGwrw0JPlOmk0qIguS9sWxtqq0tLRo4MiEBC0nzrJIKWCyINPU3d19f50+TIEWY0k3ZbcIkFk1MjKiWA8EpJgbRxoS6ichQKb7j5P+N7kWE6KyFIpBSDnEZZjRo+wWAUKaxd1CVwuta7ofCC0sWVkChDTVt4bYzO/3Ky/McM010pIAIRWd0JLcvXtXeU3V008/TTeHACGl3SvSAywt3QISiQAhkQgQEokAIZEIEBKJACGRCBASiQAhkQgQEokAIZFIBAiJRICQSAQIiUSAkEgECIlEgJBIBAiJRICQSAQIiUSAkEgkAoREIkBIJAKERCJASCQChEQiQEgkAoREIkBIJAKERCIRICQSAUIiESAkEgFCIhEgq1S49h8uwUwiQEhZ4BBFFXO5nEylkuiGECCktHCZZYPBwPR6Pbt44SzzuF1MkmRaTZYAIeGyynq9gZnNFuX3eDzOLl+6AJZkjKnVGoKEACluOAwGI8BhVqwIShBEFolG2JXLF5nT6WBqDUFCgBSh0FIgHCaTaQYAKpWKRSIAyRWAZAwtiZogIUCKCw6jsUSBIx2gZwohiSIkly8wJwTusizTjSNAisOtmguOtDCrFY1G2eWL55nb7VbcLxIBsmo1ka2aiDnmguP3kIgAVZxdvHiW+XxexvP0VRAgqxQOTOOmA/J8YgqeF1gqmWIXzp9mAb+fcQQJAbKahDDodHpmsZTdz1blK47j8L/s/PkzCiRkSQiQlY4FwsBhzKHR6Fh5ebkSfxRCFxCSgJ9JELiDdeEow0WArExCGItrtVpWUlpSMDjSFunSZOAOinEcT4QssSh9kqdkSU5u2LAxpAXXasxhV9K2hXzSI3C/+fVbrLq6OmAwGFLDdMsJkJWkQDCoc7lcLzjGHKUBf6AEAEkW2BVK+bzjoWujI4/6fQEN3XECZEXJOWbXwRP+O5IkfyuVTKogyk6xAgKCQTv8PxmJRqWAz0flvwTI4ikWi83/xogiBuIzYrRkMsV8Xq8a/qleimvCBAH22Bcy9iEVKSAlJSXz3len08XBfVp2rVAAwXlxlA4mQBasxx9/fN77QoActNvtd48fP76sntYWi2WgsrIyilaERIAsSNCQ5r2vWq1OPvnkk6+fOnVq99mzZ59dDtfz6KOPtm/atOm/wTKGyMUiQJZU2DMuSVLXgQMH/sRqtX63s7NzDzy1k+DhJB7yeXDRaFTd3Nx899ChQz83Go0X4TxSWMtFWnxRby2J9ABRpEciESAkEgFCIhEgJBIBQiIRICQSAUIiESAkEgFCIhEgJBKJACGRCBASiQAhkQgQEokAIZEIEBKJACGRCBASqRj0/wIMALHL5HKzWldzAAAAAElFTkSuQmCC");
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
                    Log.e(TAG, "mMessageReceiver: >>" + data);
                    mTvInputMessage.setText("All Input Messages\n\n" + data.toString());
                }
            });
        }
    };

    private void shareScreen() {
        try {
            File cacheDir = new File(Environment.getExternalStorageDirectory(), "socket");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            String path = new File(Environment.getExternalStorageDirectory(), "socket") + "/screenshot.jpg";
            Utils.savePic(Utils.takeScreenShot(this), path);
            Toast.makeText(getApplicationContext(), "Screenshot Saved", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException ignored) {
            ignored.printStackTrace();
        }
    }
}
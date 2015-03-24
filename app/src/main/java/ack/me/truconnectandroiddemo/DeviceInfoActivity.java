/* TruConnect Android Library & Example Applications
*
* Copyright (C) 2015, Sensors.com,  Inc. All Rights Reserved.
*
* The TruConnect Android Library and TruConnect example applications are provided free of charge by
* Sensors.com. The combined source code, and all derivatives, are licensed by Sensors.com SOLELY
* for use with devices manufactured by ACKme Networks, or devices approved by Sensors.com.
*
* Use of this software on any other devices or hardware platforms is strictly prohibited.
*
* THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
* BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
* INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
* LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ack.me.truconnectandroiddemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import ack.me.truconnectandroid.truconnect.TruconnectCommand;
import ack.me.truconnectandroid.truconnect.TruconnectErrorCode;
import ack.me.truconnectandroid.truconnect.TruconnectGPIODirection;
import ack.me.truconnectandroid.truconnect.TruconnectGPIOFunction;
import ack.me.truconnectandroid.truconnect.TruconnectManager;
import ack.me.truconnectandroid.truconnect.TruconnectResult;

public class DeviceInfoActivity extends Activity
{
    public static final String TAG = "DeviceInfo";
    private static final long UPDATE_PERIOD_MS = 1000;
    private static final int ADC_GPIO = 10;
    private static final int TEST_GPIO = 9;//button2 on wahoo
    private static final int LED_GPIO = 14;

    private TextView adcTextView;
    private TextView gpioTextView;
    private ToggleButton ledButton;
    private int ledState = 0;

    private ServiceConnection mConnection;
    private TruconnectService mService;
    private boolean mBound = false;

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mReceiverIntentFilter;

    private TruconnectManager mTruconnectManager;

    private Handler mHandler;
    private Runnable updateValuesTask;

    private ProgressDialog mDisconnectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        adcTextView = (TextView)findViewById(R.id.adc_value);
        gpioTextView = (TextView)findViewById(R.id.gpio_value);
        ledButton = (ToggleButton)findViewById(R.id.led_button);

        ledButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ledState == 0)
                {
                    ledButton.setChecked(true);
                    ledState = 1;
                    writeLedState();
                }
                else
                {
                    ledButton.setChecked(false);
                    ledState = 0;
                    writeLedState();
                }
            }
        });

        initServiceConnection();
        initBroadcastManager();
        initBroadcastReceiver();
        initReceiverIntentFilter();

        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mReceiverIntentFilter);

        mHandler = new Handler();
        updateValuesTask = new Runnable()
        {
            @Override
            public void run()
            {
                if (mTruconnectManager != null)
                {
                    Log.d(TAG, "Updating values");
                    mTruconnectManager.adc(ADC_GPIO);
                    mTruconnectManager.GPIOGet(TEST_GPIO);
                }
            }
        };
    }

    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();

        mHandler.removeCallbacks(updateValuesTask);

        showDisconnectDialog();
        mTruconnectManager.disconnect();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent serviceIntent = new Intent(getApplicationContext(), TruconnectService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
//        mTruconnectManager.disconnect();
        unbindService(mConnection);
    }

    private void initServiceConnection()
    {
        mConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service)
            {
                TruconnectService.LocalBinder binder = (TruconnectService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;

                mTruconnectManager = mService.getManager();
                initGPIOs();
                updateValues();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0)
            {
                mBound = false;
            }
        };
    }

    private void initBroadcastReceiver()
    {
        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // Get extra data included in the Intent
                String action = intent.getAction();

                switch (action)
                {
                    case TruconnectService.ACTION_COMMAND_SENT:
                        String command = TruconnectService.getCommand(intent).toString();
                        Log.d(TAG, "Command " + command + " sent");
                        break;

                    case TruconnectService.ACTION_COMMAND_RESULT:
                        handleCommandResponse(intent);
                        break;

                    case TruconnectService.ACTION_ERROR:
                        TruconnectErrorCode errorCode = TruconnectService.getErrorCode(intent);
                        //handle errors
                        break;

                    case TruconnectService.ACTION_DISCONNECTED:
                        mDisconnectDialog.dismiss();
                        finish();
                        break;
                }
            }
        };
    }

    public void initBroadcastManager()
    {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

    }

    public void initReceiverIntentFilter()
    {
        mReceiverIntentFilter = new IntentFilter();
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_DISCONNECTED);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_COMMAND_RESULT);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_ERROR);
    }

    private void initGPIOs()
    {
        mTruconnectManager.GPIOFunctionSet(ADC_GPIO, TruconnectGPIOFunction.NONE);
        mTruconnectManager.GPIOFunctionSet(TEST_GPIO, TruconnectGPIOFunction.NONE);
        mTruconnectManager.GPIOFunctionSet(LED_GPIO, TruconnectGPIOFunction.NONE);

        mTruconnectManager.GPIOFunctionSet(TEST_GPIO, TruconnectGPIOFunction.STDIO);
        mTruconnectManager.GPIOFunctionSet(LED_GPIO, TruconnectGPIOFunction.STDIO);

        mTruconnectManager.GPIODirectionSet(TEST_GPIO, TruconnectGPIODirection.INPUT);
        mTruconnectManager.GPIODirectionSet(LED_GPIO, TruconnectGPIODirection.OUTPUT_LOW);
    }

    private void updateValues()
    {
        mHandler.postDelayed(updateValuesTask, UPDATE_PERIOD_MS);
    }

    private void handleCommandResponse(Intent intent)
    {
        TruconnectCommand command = TruconnectService.getCommand(intent);
        int code = TruconnectService.getResponseCode(intent);
        String result = TruconnectService.getData(intent);
        String message = "";

        Log.d(TAG, "Command " + command + " result");

        if (code == TruconnectResult.SUCCESS)
        {
            switch (command)
            {
                case ADC:
                    message = String.format("ADC: %s", result);
                    adcTextView.setText(message);
                    break;

                case GPIO_GET:
                    message = String.format("GPIO: %s", result);
                    gpioTextView.setText(message);
                    updateValues();
                    break;

                case GPIO_SET:
                    break;
            }
        }
        else
        {
            message = String.format("ERROR %d - %s", code, result);
            showToast(message, Toast.LENGTH_SHORT);
        }
    }

    private void showDisconnectDialog()
    {
        final ProgressDialog dialog = new ProgressDialog(DeviceInfoActivity.this);
        String title = getString(R.string.disconnect_dialog_title);
        String msg = getString(R.string.disconnect_dialog_message);
        dialog.setIndeterminate(true);//Dont know how long disconnect could take.....
        dialog.setCancelable(false);

        mDisconnectDialog = dialog.show(DeviceInfoActivity.this, title, msg);
        mDisconnectDialog.setCancelable(false);
    }

    private void showToast(final String msg, final int duration)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, duration).show();
            }
        });
    }

    private void writeLedState()
    {
        mTruconnectManager.GPIOSet(LED_GPIO, ledState);
    }
}

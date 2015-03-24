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
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallback;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import ack.me.truconnectandroid.truconnect.TruconnectCommandMode;
import ack.me.truconnectandroid.truconnect.TruconnectErrorCode;
import ack.me.truconnectandroid.truconnect.TruconnectManager;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class MainActivity extends Activity
{
    private static final long SCAN_PERIOD = 5000;
    private static final String TAG = "TruconnectAndroid";
    private final int BLE_ENABLE_REQ_CODE = 1;
    private SmoothProgressBar mScanProgressBar;
    private ProgressDialog mConnectProgressDialog;
    private DeviceList mDeviceList;
    private Button mScanButton;

    private Handler mHandler;
    private Runnable mStopScanTask;

    private TruconnectManager mTruconnectManager;
    private boolean mConnecting = false;
    private boolean mConnected = false;

    private String mCurrentDeviceName;

    private ServiceConnection mConnection;
    private TruconnectService mService;
    private boolean mBound = false;

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mReceiverIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initProgressBar();
        initScanButton();
        initDeviceList();
        initBroadcastManager();
        initServiceConnection();
        initBroadcastReceiver();
        initReceiverIntentFilter();

        startService(new Intent(this, TruconnectService.class));

        mHandler = new Handler();

        mStopScanTask = new Runnable()
        {
            @Override
            public void run()
            {
                stopScan();
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mDeviceList.clear();
        mConnected = false;
        mConnecting = false;

        Intent intent = new Intent(this, TruconnectService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mReceiverIntentFilter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        mHandler.removeCallbacks(mStopScanTask);

        if (mBound)
        {
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
            unbindService(mConnection);
            mBound = false;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        stopService(new Intent(this, TruconnectService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == BLE_ENABLE_REQ_CODE)
        {
            mService.initTruconnectManager();//try again
            if (mTruconnectManager.isInitialised())
            {
                startScan();
            }
            else
            {
                showUnrecoverableErrorDialog(R.string.init_fail_title, R.string.init_fail_msg);
            }
        }
    }

    private void initProgressBar()
    {
        mScanProgressBar = (SmoothProgressBar) findViewById(R.id.progressBar);
        mScanProgressBar.setVisibility(View.VISIBLE);
    }

    private void initScanButton()
    {
        mScanButton = (Button) findViewById(R.id.scanButton);
        mScanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mDeviceList.clear();
                startScan();
            }
        });
    }

    private void initDeviceList()
    {
        ListView deviceListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, R.id.textView);

        initialiseListviewListener(deviceListView);
        mDeviceList = new DeviceList(adapter, deviceListView);
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
                if(!mTruconnectManager.isInitialised())
                {
                    startBLEEnableIntent();
                }
                else
                {
                    startScan();
                }
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
                    case TruconnectService.ACTION_SCAN_RESULT:
                        addDeviceToList(TruconnectService.getData(intent));
                        break;

                    case TruconnectService.ACTION_CONNECTED:
                        String deviceName = TruconnectService.getData(intent);

                        mConnected = true;
                        dismissConnectDialog();
                        showToast("Connected to " + deviceName, Toast.LENGTH_SHORT);
                        Log.d(TAG, "Connected to " + deviceName);

                        mTruconnectManager.setMode(TruconnectManager.MODE_COMMAND_REMOTE);
                        mTruconnectManager.setSystemCommandMode(TruconnectCommandMode.MACHINE);
                        startDeviceInfoActivity();
                        break;

                    case TruconnectService.ACTION_DISCONNECTED:
                        break;

                    case TruconnectService.ACTION_MODE_WRITE:
                        break;

                    case TruconnectService.ACTION_MODE_READ:
                        break;

                    case TruconnectService.ACTION_DATA_WRITE:
                        break;

                    case TruconnectService.ACTION_DATA_READ:
                        break;

                    case TruconnectService.ACTION_COMMAND_SENT:
                        String command = TruconnectService.getCommand(intent).toString();
                        Log.d(TAG, "Command " + command + " sent");
                        break;

                    case TruconnectService.ACTION_COMMAND_RESULT:
                        break;

                    case TruconnectService.ACTION_ERROR:
                        TruconnectErrorCode errorCode = TruconnectService.getErrorCode(intent);
                        //handle errors
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
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_SCAN_RESULT);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_CONNECTED);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_DISCONNECTED);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_MODE_READ);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_MODE_WRITE);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_DATA_READ);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_DATA_WRITE);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_COMMAND_SENT);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_COMMAND_RESULT);
        mReceiverIntentFilter.addAction(TruconnectService.ACTION_ERROR);
    }

    private void startBLEEnableIntent()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLE_ENABLE_REQ_CODE);
    }

    private void initialiseListviewListener(ListView listView)
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                mCurrentDeviceName = mDeviceList.get(position);

                if (!mConnecting)
                {
                    mConnecting = true;

                    stopScan();
                    Log.d(TAG, "Connecting to BLE device " + mCurrentDeviceName);
                    mTruconnectManager.connect(mCurrentDeviceName);

                    final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                    String title = getString(R.string.progress_title);
                    String msg = getString(R.string.progress_message);
                    dialog.setIndeterminate(true);//Dont know how long connection could take.....
                    dialog.setCancelable(true);

                    mConnectProgressDialog = dialog.show(view.getContext(), title, msg);
                    mConnectProgressDialog.setCancelable(true);
                    mConnectProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            dialogInterface.dismiss();
                        }
                    });

                }
            }
        });
    }

    private void startScan()
    {
        if (mTruconnectManager != null)
        {
            runOnUiThread(new Runnable()
              {
                  @Override
                  public void run()
                  {
                      mTruconnectManager.startScan();
                  }
              });
            startProgressBar();
            disableScanButton();
            mHandler.postDelayed(mStopScanTask, SCAN_PERIOD);
        }
    }

    private void stopScan()
    {
        if (mTruconnectManager != null && mTruconnectManager.stopScan())
        {
            stopProgressBar();
            enableScanButton();
        }
    }

    private void startDeviceInfoActivity()
    {
        startActivity(new Intent(getApplicationContext(), DeviceInfoActivity.class));
    }

    private void startProgressBar()
    {
        updateProgressBar(true);
    }

    private void stopProgressBar()
    {
        updateProgressBar(false);
    }

    private void enableScanButton()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mScanButton.setEnabled(true);
            }
        });
    }

    private void disableScanButton()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mScanButton.setEnabled(false);
            }
        });
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

    private void showErrorDialog(final int titleID, final int msgID)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(titleID)
                .setMessage(msgID)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void showUnrecoverableErrorDialog(final int titleID, final int msgID)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(titleID)
                        .setMessage(msgID)
                        .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    private void dismissConnectDialog()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mConnectProgressDialog.dismiss();
            }
        });
    }

    //Only adds to the list if not already in it
    private void addDeviceToList(final String name)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mDeviceList.add(name);
            }
        });
    }

    private void updateProgressBar(final boolean start)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (start)
                {
                    mScanProgressBar.progressiveStart();
                }
                else
                {
                    mScanProgressBar.progressiveStop();
                }
            }
        });
    }
}
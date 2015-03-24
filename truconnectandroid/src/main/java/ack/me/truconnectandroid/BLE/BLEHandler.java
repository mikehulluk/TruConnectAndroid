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

package ack.me.truconnectandroid.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.NoSuchElementException;
import java.util.UUID;

public class BLEHandler
{
    private static final String TAG = "BLEHandler";

    private static final boolean GATT_AUTOCONNECT_FALSE = false;

    private BluetoothAdapter.LeScanCallback mScanCallback;//for android SDK
    private BluetoothCallbackHandler mBluetoothCallbacks;//for android SDK
    private BLEHandlerCallbacks mCallbacks;//callbacks for user

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    private BLEDeviceList mScannedList;
    private SearchableList<BLEConnection> mConnectionList;

    private boolean mScanning = false;

    private BLECommandQueue mQueue;
    private boolean mCommandInProgress = false;

    public BLEHandler (BLEDeviceList scanList, SearchableList<BLEConnection> connectedList, BLECommandQueue queue)
    {
        mScannedList = scanList;
        mConnectionList = connectedList;
        mQueue = queue;
    }

    public boolean init(Context context, BLEHandlerCallbacks callbacks)
    {
        boolean result = false;

        mScanning = false;
        mContext = context;
        mCallbacks = callbacks;
        mBluetoothCallbacks = new BluetoothCallbackHandler(this, callbacks);

        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (mBluetoothManager != null)
        {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter != null)
                result = true;
        }

        return result;
    }

    public boolean isInitialised()
    {
        return (mBluetoothAdapter != null);
    }

    public boolean isBLEEnabled()
    {
        return (isInitialised() && mBluetoothAdapter.isEnabled());
    }

    public boolean isScanning()
    {
        return mScanning;
    }

    public boolean startBLEScan()
    {
        boolean result = false;
        final UUID TC_UUIDS[] = {BluetoothCallbackHandler.TRUCONNECT_SERVICE_UUID};

        if (isBLEEnabled() && !mScanning)
        {
            mScannedList.clear();
            result = mBluetoothAdapter.startLeScan(mBluetoothCallbacks.getScanCallback());
//            result = mBluetoothAdapter.startLeScan(TC_UUIDS, mBluetoothCallbacks.getScanCallback());
            mScanning = true;
        }
        return result;
    }

    public boolean stopBLEScan()
    {
        boolean result = false;

        if (isInitialised() && mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.stopLeScan(mBluetoothCallbacks.getScanCallback());
            mScanning = false;
            result = true;
        }

        return result;
    }

    /* Returns true if connection request was successful. The connection is not established until
     * the onConnect callback is called. */
    public boolean connect(String deviceName)
    {
        boolean result = false;
        BLEDevice device = mScannedList.get(deviceName);

        if (device != null)
        {
            BLEGatt gatt = device.connectGatt(mContext,
                                              GATT_AUTOCONNECT_FALSE,
                                              mBluetoothCallbacks.getGattCallbacks());
            if (gatt != null)
            {
                BLEConnection newConnection = new BLEConnection();
                newConnection.setDevice(device.getDevice());
                newConnection.setMode(BLEConnection.Mode.CONNECTING);
                if (mConnectionList.add(newConnection))
                {
                    result = true;
                }
            }
        }

        return result;
    }

    public boolean disconnect(String deviceName)
    {
        boolean successful = false;

        if (deviceName != null)
        {
            BLEConnection connection = mConnectionList.get(deviceName);
            if (connection != null)
            {
                mQueue.clear();
                mCommandInProgress = false;
                connection.setMode(BLEConnection.Mode.DISCONNECTING);
                connection.disconnect();
                successful = true;
            }
        }

        return successful;
    }

    public void deinit()
    {
        for (int i=0; i < mConnectionList.size(); i++)
        {
            mConnectionList.get(i).close();
        }

        if (mScanning)
        {
            mBluetoothAdapter.stopLeScan(mScanCallback);
        }

        mBluetoothAdapter = null;
    }

    public boolean readData(String deviceName)
    {
        boolean result = false;

        BLEConnection connection = getConnection(deviceName);

        if (connection != null)
        {
            result = mQueue.add(connection, BLECommandType.READ_DATA);
            triggerNextCommand();
        }

        return result;
    }

    public boolean writeData(String deviceName, String data)
    {
        boolean result = false;

        BLEConnection connection = getConnection(deviceName);

        if (connection != null && data != null)
        {
            result = mQueue.add(connection, BLECommandType.WRITE_DATA, data);
            triggerNextCommand();
        }

        return result;
    }

    public boolean writeMode(String deviceName, int mode)
    {
        boolean result = false;

        BLEConnection connection = getConnection(deviceName);

        if (connection != null && BluetoothCallbackHandler.isModeValid(mode))
        {
            result = mQueue.add(connection, BLECommandType.WRITE_MODE, mode);
            triggerNextCommand();
        }

        return result;
    }

    public boolean readMode(String deviceName)
    {
        boolean result = false;

        BLEConnection connection = getConnection(deviceName);

        if (connection != null)
        {
            result = mQueue.add(connection, BLECommandType.READ_MODE);
            triggerNextCommand();
        }

        return result;
    }

    //Starts processing next command if there is none being processed
    private void triggerNextCommand()
    {
        if (!mCommandInProgress)
        {
//            Log.d(TAG, "Starting next command");
            mCommandInProgress = true;
            mBluetoothCallbacks.processNextCommand();
        }
    }

    protected BLEDeviceList getScanned()
    {
        return mScannedList;
    }

    protected BLEConnection getConnection(String deviceName)
    {
        BLEConnection connection = null;

        if (deviceName != null && mConnectionList != null)
        {
            connection = mConnectionList.get(deviceName);
        }

        return connection;
    }

    protected boolean removeConnection(String deviceName)
    {
        boolean result = false;

        if (deviceName != null && mConnectionList != null)
        {
            result = mConnectionList.remove(deviceName);
        }

        return result;
    }

    protected BLEConnection.Mode getConnectionMode(String deviceName)
    {
        BLEConnection connection = mConnectionList.get(deviceName);
        BLEConnection.Mode mode = null;

        if(connection != null)
        {
            mode = connection.getMode();
        }

        return mode;
    }

    protected BLECommand getNextCommand()
    {
        try
        {
            return mQueue.next();
        }
        catch (NoSuchElementException e)
        {
            return null;
        }
    }

    protected boolean addCommand(BLECommand command)
    {
        return mQueue.add(command);
    }

    protected boolean addCommand(BLEConnection connection, BLECommandType type)
    {
        return mQueue.add(new BLECommand(connection, type));
    }

    protected void setCommandInProgress(boolean inProgress)
    {
//        Log.d(TAG, "Setting command in progress to " + inProgress);
        mCommandInProgress = inProgress;
    }
}

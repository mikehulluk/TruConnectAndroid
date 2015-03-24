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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.UUID;

public class BluetoothCallbackHandler
{
    public static final String TAG = "BluetoothCallbkHandler";

    public static final UUID TRUCONNECT_SERVICE_UUID =
                                            UUID.fromString("175f8f23-a570-49bd-9627-815a6a27de2a");
    public static final UUID TX_UUID = UUID.fromString("cacc07ff-ffff-4c48-8fae-a9ef71b75e26");
    public static final UUID RX_UUID = UUID.fromString("1cce1ea8-bd34-4813-a00a-c76e028fadcb");
    public static final UUID MODE_UUID = UUID.fromString("20b9794f-da1a-4d14-8014-a0fb9cefb2f7");

    public static final UUID CLIENT_CHAR_CONFIG_UUID =
                                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final BLEHandlerCallbacks.Result ERROR_DISCOVERY =
                                                      BLEHandlerCallbacks.Result.SERVICE_DISC_ERROR;

    private final BLEHandlerCallbacks.Result ERROR_CONNECTION =
                                                         BLEHandlerCallbacks.Result.CONNECT_FAILURE;

    private final BLEHandlerCallbacks.Error ERROR_CON_NO_REQ =
                                                  BLEHandlerCallbacks.Error.CONNECT_WITHOUT_REQUEST;

    private final BLEHandlerCallbacks.Error ERROR_DISCON_NO_REQ =
                                               BLEHandlerCallbacks.Error.DISCONNECT_WITHOUT_REQUEST;

    private final BLEHandlerCallbacks.Error ERROR_INVALID_MODE =
                                                             BLEHandlerCallbacks.Error.INVALID_MODE;

    private final BLEHandlerCallbacks.Error ERROR_NO_TX_CHAR =
                                                     BLEHandlerCallbacks.Error.NO_TX_CHARACTERISTIC;

    private final BLEHandlerCallbacks.Error ERROR_NO_RX_CHAR =
                                                     BLEHandlerCallbacks.Error.NO_RX_CHARACTERISTIC;

    private final BLEHandlerCallbacks.Error ERROR_NO_MODE_CHAR =
                                                   BLEHandlerCallbacks.Error.NO_MODE_CHARACTERISTIC;

    private final BLEHandlerCallbacks.Error ERROR_NO_CONNECTION =
                                                      BLEHandlerCallbacks.Error.NO_CONNECTION_FOUND;

    private final BLEHandlerCallbacks.Error ERROR_NULL_GATT =
                                                    BLEHandlerCallbacks.Error.NULL_GATT_ON_CALLBACK;

    private final BLEHandlerCallbacks.Error ERROR_NULL_CHAR =
                                                    BLEHandlerCallbacks.Error.NULL_CHAR_ON_CALLBACK;
    private BluetoothAdapter.LeScanCallback mScanCallback;
    private BluetoothGattCallback mGattCallbacks;
    private BLEHandlerCallbacks mBLECallbacks;

    private BLEHandler mHandler;

    private static final int MODES_EQUAL = 0;

    public static final int MODE_STREAM = 1;
    public static final int MODE_LOCAL_COMMAND = 2;
    public static final int MODE_REMOTE_COMMAND = 3;

    private final int FORMAT_MODE = BluetoothGattCharacteristic.FORMAT_UINT8;
    private final int OFFSET_MODE = 0;
    private final int OFFSET_RX = 0;
    private final int OFFSET_TX = 0;

    BluetoothCallbackHandler(BLEHandler handler, final BLEHandlerCallbacks BLECallbacks)
    {
        mHandler = handler;
        mBLECallbacks = BLECallbacks;

        mScanCallback = new BluetoothAdapter.LeScanCallback()
        {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
            {
                BLEDeviceList list = mHandler.getScanned();

                list.add(new BLEDevice(device));
                mBLECallbacks.onScanResult(device.getName());
            }
        };

        mGattCallbacks = new BluetoothGattCallback()
        {

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status)
            {
                String deviceName = gatt.getDevice().getName();

                if (status != BluetoothGatt.GATT_SUCCESS)
                {
                    mBLECallbacks.onConnectFailed(deviceName, ERROR_DISCOVERY);
                }
                else
                {
                    BluetoothGattService service = gatt.getService(TRUCONNECT_SERVICE_UUID);
                    if (service == null)
                    {
                        mBLECallbacks.onConnectFailed(deviceName, ERROR_DISCOVERY);
                    }
                    else
                    {
                        BLEConnection connection = mHandler.getConnection(deviceName);

                        if (connection == null)
                        {
                            mBLECallbacks.onError(deviceName, ERROR_NO_CONNECTION);
                        }
                        else
                        {
                            setCharacteristics(deviceName, connection, service);
                            connection.setService(service);
                            mBLECallbacks.onConnect(deviceName);
                        }
                    }
                }
                processNextCommand();
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
            {
                String deviceName = gatt.getDevice().getName();
                BLEConnection.Mode currentMode = mHandler.getConnectionMode(deviceName);

                if (currentMode == null)
                {
                    mBLECallbacks.onError(deviceName, ERROR_INVALID_MODE);
                    return;
                }

                switch(currentMode)
                {
                    case CONNECTED:
                        if (newState == BluetoothGatt.STATE_DISCONNECTED)
                            mBLECallbacks.onError(deviceName, ERROR_DISCON_NO_REQ);
                        break;

                    case DISCONNECTED:
                        if (newState == BluetoothGatt.STATE_CONNECTED)
                            mBLECallbacks.onError(deviceName, ERROR_CON_NO_REQ);
                        break;

                    case CONNECTING:
                        if(newState == BluetoothGatt.STATE_CONNECTED)
                        {
                            if(status == BluetoothGatt.GATT_SUCCESS)
                            {
                                BLEConnection connection = mHandler.getConnection(deviceName);
                                setConnectionGatt(connection, gatt);
                                connection.setMode(BLEConnection.Mode.CONNECTED);

                                BLECommand serv_disc_cmd = new BLECommand(connection,
                                                                  BLECommandType.DISCOVER_SERVICES);
                                if(!mHandler.addCommand(serv_disc_cmd))
                                {
                                    mBLECallbacks.onConnectFailed(deviceName, ERROR_DISCOVERY);
                                }
                            }
                            else
                                mBLECallbacks.onConnectFailed(deviceName, ERROR_CONNECTION);
                        }
                        else
                        {
                            if (status == BluetoothGatt.GATT_SUCCESS)
                                mBLECallbacks.onError(deviceName, ERROR_CON_NO_REQ);
                            else
                                mBLECallbacks.onConnectFailed(deviceName, ERROR_CONNECTION);
                        }
                        break;

                    case DISCONNECTING:
                        if (newState == BluetoothGatt.STATE_DISCONNECTED)
                        {
                            if (status == BluetoothGatt.GATT_SUCCESS)
                            {
                                mHandler.removeConnection(deviceName);
                                mBLECallbacks.onDisconnect(deviceName);
                            }
                            else
                                mBLECallbacks.onDisconnectFailed(deviceName);
                        }
                        else
                        {
                            if (status == BluetoothGatt.GATT_SUCCESS)
                                mBLECallbacks.onError(deviceName, ERROR_CON_NO_REQ);
                            else
                                mBLECallbacks.onDisconnectFailed(deviceName);
                        }
                        break;

                    default:
                        mBLECallbacks.onError(deviceName, ERROR_INVALID_MODE);
                        break;
                }

                processNextCommand();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                if (verifyGatt(gatt) && verifyCharacteristic(characteristic))
                {
                    String deviceName = gatt.getDevice().getName();
                    UUID uuid = characteristic.getUuid();

                    if (uuid.compareTo(MODE_UUID) == MODES_EQUAL)
                    {
                        if (status == BluetoothGatt.GATT_SUCCESS)
                        {
                            int mode = characteristic.getIntValue(FORMAT_MODE, OFFSET_MODE);
                            mBLECallbacks.onModeRead(deviceName, mode);
                        }
                        else
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.MODE_READ_FAILED);
                        }
                    }
                    else if (uuid.compareTo(TX_UUID) == MODES_EQUAL)
                    {
                        if (status == BluetoothGatt.GATT_SUCCESS)
                        {
                            String data = characteristic.getStringValue(OFFSET_RX);
                            mBLECallbacks.onDataRead(deviceName, data);
                        } else
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.DATA_READ_FAILED);
                        }
                    }
                }

                processNextCommand();
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                if (verifyGatt(gatt) && verifyCharacteristic(characteristic))
                {
                    String deviceName = gatt.getDevice().getName();
                    UUID uuid = characteristic.getUuid();

                    if(uuid.compareTo(MODE_UUID) == MODES_EQUAL)
                    {
                        if (status == BluetoothGatt.GATT_SUCCESS)
                        {
                            int mode = characteristic.getIntValue(FORMAT_MODE, OFFSET_MODE);
                            mBLECallbacks.onModeChanged(deviceName, mode);
                        }
                        else
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.MODE_WRITE_FAILED);
                        }
                    }
                    else if (uuid.compareTo(RX_UUID) == MODES_EQUAL)
                    {
                        if (status == BluetoothGatt.GATT_SUCCESS)
                        {
                            String data = characteristic.getStringValue(OFFSET_TX);
                            mBLECallbacks.onDataWrite(deviceName, data);
                        }
                        else
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.DATA_WRITE_FAILED);
                        }
                    }
                }

                processNextCommand();
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
            {
//                Log.d(TAG, "Called characteristic changed");

                UUID uuid = characteristic.getUuid();
                if (uuid.compareTo(TX_UUID) == MODES_EQUAL)
                {
                    String data = characteristic.getStringValue(OFFSET_TX);
                    mBLECallbacks.onDataRead(gatt.getDevice().getName(), data);
                }

                processNextCommand();
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
            {
//                Log.d(TAG, "In descriptor read");
                processNextCommand();
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
            {
//                Log.d(TAG, "In descriptor write");
                processNextCommand();
            }
        };
    }

    public BluetoothAdapter.LeScanCallback getScanCallback()
    {
        return mScanCallback;
    }

    public BluetoothGattCallback getGattCallbacks()
    {
        return mGattCallbacks;
    }

    public static boolean isModeValid(int mode)
    {
        return (mode >= MODE_STREAM && mode <= MODE_REMOTE_COMMAND);
    }

    private boolean setConnectionGatt(BLEConnection connection, BluetoothGatt gatt)
    {
        boolean result = false;

        if (connection != null)
        {
            connection.setGatt(gatt);
            result = true;
        }

        return result;
    }

    private void setCharacteristics(String deviceName, BLEConnection connection,
                                    BluetoothGattService service)
    {
        BluetoothGattCharacteristic txChar = service.getCharacteristic(TX_UUID);
        BluetoothGattCharacteristic rxChar = service.getCharacteristic(RX_UUID);
        BluetoothGattCharacteristic modeChar = service.getCharacteristic(MODE_UUID);

        if (txChar == null)
        {
            mBLECallbacks.onError(deviceName, ERROR_NO_TX_CHAR);
        }
        else
        {
            connection.setTxCharacteristic(txChar);
            mHandler.addCommand(connection, BLECommandType.SET_RX_NOTIFY);
        }

        if (rxChar == null)
        {
            mBLECallbacks.onError(deviceName, ERROR_NO_RX_CHAR);
        }
        else
        {
            connection.setRxCharacteristic(rxChar);
        }

        if (modeChar == null)
        {
            mBLECallbacks.onError(deviceName, ERROR_NO_MODE_CHAR);
        }
        else
        {
            connection.setModeCharacteristic(modeChar);
        }
    }

    //calls BLEHandlerCallbacks.onError() and returns false if Gatt invalid
    private boolean verifyGatt(BluetoothGatt gatt)
    {
        boolean valid = true;

        if (gatt == null)
        {
            mBLECallbacks.onError("", ERROR_NULL_GATT);
            valid = false;
        }

        return valid;
    }

    //calls BLEHandlerCallbacks.onError() and returns false if characteristic invalid
    private boolean verifyCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        boolean valid = true;

        if (characteristic == null)
        {
            mBLECallbacks.onError("", ERROR_NULL_CHAR);
            valid = false;
        }

        return valid;
    }

    protected void processNextCommand()
    {
        BLECommand nextCommand = mHandler.getNextCommand();

        if (nextCommand != null)
        {
            BLEConnection connection = nextCommand.getConnection();

            if (connection == null)
            {
                mBLECallbacks.onError("", ERROR_NO_CONNECTION);
            }
            else
            {
                String deviceName = connection.getDevice().getName();
                switch (nextCommand.getType())
                {
                    case READ_MODE:
                        if (!connection.readModeCharacteristic())
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.MODE_READ_FAILED);
                        }
                        break;

                    case WRITE_MODE:
                        if (!connection.writeModeCharacteristic(nextCommand.getMode()))
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.MODE_WRITE_FAILED);
                        }
                        break;

                    case READ_DATA:
                        if (!connection.readTxCharacteristic())
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.DATA_READ_FAILED);
                        }
                        break;

                    case WRITE_DATA:
                        if (!connection.writeRxCharacteristic(nextCommand.getData()))
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.DATA_WRITE_FAILED);
                        }
                        break;

                    case SET_RX_NOTIFY:
                        if (!connection.setNotifyOnDataReady(true))
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.SET_RX_NOTIFY_FAILED);
                        }
                        break;

                    case DISCOVER_SERVICES:
                        if (!connection.discoverServices())
                        {
                            mBLECallbacks.onError(deviceName, BLEHandlerCallbacks.Error.SERVICE_DISCOVERY_FAILED);
                        }
                        break;
                }
            }
        }
        else
        {
            mHandler.setCommandInProgress(false);
        }
    }
}

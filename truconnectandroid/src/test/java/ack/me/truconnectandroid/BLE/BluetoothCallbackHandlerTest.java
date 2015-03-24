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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BluetoothCallbackHandlerTest
{
    private final int GATT_STATUS_SUCCESS = BluetoothGatt.GATT_SUCCESS;
    private final int GATT_STATUS_FAILURE = BluetoothGatt.GATT_FAILURE;
    private final int GATT_STATE_CONNECTED = BluetoothGatt.STATE_CONNECTED;
    private final String TEST_NAME = "TEST";
    private final String TEST_DATA = "DATA";
    private final int TEST_RSSI = 0;
    private final byte[] TEST_RECORDS = {};

    private BluetoothGatt mMockGatt;
    private BluetoothGattService mMockService;
    private BluetoothDevice mMockDevice;
    private BLEHandlerCallbacks mMockCallbacks;
    private BLEHandler mMockBLEHandler;
    private BLEConnection mMockConnection;
    private BLECommandQueue mMockQueue;

    private BluetoothGattCharacteristic mMockTxChar;
    private BluetoothGattCharacteristic mMockRxChar;
    private BluetoothGattCharacteristic mMockModeChar;

    private BluetoothCallbackHandler mHandler;
    private BluetoothAdapter.LeScanCallback mMockScanCallback;

    private BLEDeviceList mMockScanList;
    private SearchableList<BLEConnection> mMockConnectedList;

    @Before
    public void setUp() throws Exception
    {
        mMockGatt = mock(BluetoothGatt.class);
        mMockDevice = mock(BluetoothDevice.class);
        mMockCallbacks = mock(BLEHandlerCallbacks.class);
        mMockScanCallback = mock(BluetoothAdapter.LeScanCallback.class);
        mMockConnection = mock(BLEConnection.class);
        mMockTxChar = mock(BluetoothGattCharacteristic.class);
        mMockRxChar = mock(BluetoothGattCharacteristic.class);
        mMockModeChar = mock(BluetoothGattCharacteristic.class);

        mMockBLEHandler = mock(BLEHandler.class);
        mHandler = new BluetoothCallbackHandler(mMockBLEHandler, mMockCallbacks);

        mMockScanList = mock(BLEDeviceList.class);
        mMockConnectedList = mock(SearchableList.class);
        mMockService = mock(BluetoothGattService.class);
        mMockQueue = mock(BLECommandQueue.class);

        when(mMockBLEHandler.getScanned()).thenReturn(mMockScanList);

        when(mMockDevice.getName()).thenReturn(TEST_NAME);

        when(mMockGatt.getDevice()).thenReturn(mMockDevice);
        when(mMockGatt.getService(BluetoothCallbackHandler.TRUCONNECT_SERVICE_UUID)).thenReturn(mMockService);

        when(mMockConnection.setNotifyOnDataReady(any(boolean.class))).thenReturn(true);

        when(mMockConnectedList.get(TEST_NAME)).thenReturn(mMockConnection);

        when(mMockBLEHandler.getConnection(TEST_NAME)).thenReturn(mMockConnection);

        when(mMockService.getCharacteristic(BluetoothCallbackHandler.TX_UUID)).thenReturn(mMockTxChar);
        when(mMockService.getCharacteristic(BluetoothCallbackHandler.RX_UUID)).thenReturn(mMockRxChar);
        when(mMockService.getCharacteristic(BluetoothCallbackHandler.MODE_UUID)).thenReturn(mMockModeChar);

        when(mMockModeChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)).thenReturn(BluetoothCallbackHandler.MODE_STREAM);
        when(mMockModeChar.getUuid()).thenReturn(BluetoothCallbackHandler.MODE_UUID);

        when(mMockRxChar.getStringValue(0)).thenReturn(TEST_DATA);
        when(mMockRxChar.getUuid()).thenReturn(BluetoothCallbackHandler.RX_UUID);

        when(mMockTxChar.getStringValue(0)).thenReturn(TEST_DATA);
        when(mMockTxChar.getUuid()).thenReturn(BluetoothCallbackHandler.TX_UUID);
    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void testOnLeScan_callsOnScanResultCallback() throws Exception
    {
        when(mMockDevice.getName()).thenReturn(TEST_NAME);
        mHandler.getScanCallback().onLeScan(mMockDevice, TEST_RSSI, TEST_RECORDS);
        verify(mMockCallbacks, times(1)).onScanResult(TEST_NAME);
    }

    @Test
    public void testOnLeScan_addsResultToList()
    {
        mHandler.getScanCallback().onLeScan(mMockDevice, TEST_RSSI, TEST_RECORDS);
        verify(mMockScanList).add(any(BLEDevice.class));
    }

    @Test
    public void testOnConnectionStateChanged_callsAddCommandOnSuccessfulConnected() throws Exception
    {
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_CONNECTED);
        verify(mMockBLEHandler, times(1)).addCommand(any(BLECommand.class));
    }

    @Test
    public void testOnConnectionStateChanged_doesntCallDiscoverServicesOnNotConnected() throws Exception
    {
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_DISCONNECTED);
        verify(mMockGatt, never()).discoverServices();
    }

    @Test
    public void testOnConnectionStateChanged_doesntCallDiscoverServicesOnFailure() throws Exception
    {
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_CONNECTED);
        verify(mMockGatt, never()).discoverServices();
    }

    @Test
    public void testOnConnectionStateChanged_callsOnConnectFailedOnQueueAddFail() throws Exception
    {
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        when(mMockBLEHandler.addCommand(any(BLECommand.class))).thenReturn(false);

        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_CONNECTED);

        verify(mMockCallbacks, times(1)).onConnectFailed(TEST_NAME,
                                                         BLEHandlerCallbacks.Result.SERVICE_DISC_ERROR);
    }

    @Test
    public void testOnConnectionStateChanged_setsGattOnConnectedSuccess() throws Exception
    {
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_CONNECTED);
        verify(mMockConnection).setGatt(mMockGatt);
    }

    @Test
    public void testOnConnectionChanged_callsOnErrorIfConnectedWithoutRequest() throws Exception
    {
        //Mode is disconnected, but changed to connected without a request
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.DISCONNECTED);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_CONNECTED);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_CONNECTED);

        verify(mMockCallbacks, times(2)).onError(TEST_NAME, BLEHandlerCallbacks.Error.CONNECT_WITHOUT_REQUEST);
    }

    @Test
    public void testOnConnectionChanged_callsOnErrorIfDisconnectedWithoutRequest() throws Exception
    {
        //Mode is connected, but changed to disconnected without a request
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTED);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_DISCONNECTED);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_DISCONNECTED);

        verify(mMockCallbacks, times(2)).onError(TEST_NAME, BLEHandlerCallbacks.Error.DISCONNECT_WITHOUT_REQUEST);
    }

    @Test
    public void testOnConnectionChanged_callsOnErrorIfConnectedSuccessOnDisconnectRequest() throws Exception
    {
        //Mode is disconnecting, but changed to connected (success)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.DISCONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_CONNECTED);

        verify(mMockCallbacks, times(1)).onError(TEST_NAME, BLEHandlerCallbacks.Error.CONNECT_WITHOUT_REQUEST);
    }

    @Test
    public void testOnConnectionChanged_callsOnDisconnectFailIfConnectedFailureOnDisconnectRequest() throws Exception
    {
        //Mode is disconnecting, but changed to connected (failure)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.DISCONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_CONNECTED);

        verify(mMockCallbacks, times(1)).onDisconnectFailed(TEST_NAME);
    }

    @Test
    public void testOnConnectionChanged_callsOnDisconnectIfDisconnectedSuccessOnDisconnectRequest() throws Exception
    {
        //Mode is disconnecting, changed to disconnected (success)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.DISCONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_DISCONNECTED);

        verify(mMockCallbacks, times(1)).onDisconnect(TEST_NAME);
    }

    @Test
    public void testOnConnectionChanged_callsOnDisconnectFailedIfDisconnectedFailedOnDisconnectRequest() throws Exception
    {
        //Mode is disconnecting, changed to disconnected (failure)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.DISCONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_DISCONNECTED);

        verify(mMockCallbacks, times(1)).onDisconnectFailed(TEST_NAME);
    }

    @Test
    public void testOnConnectionChanged_callsOnConnectFailedIfConnectedFailureOnConnectRequest() throws Exception
    {
        //Mode is connecting, changed to connected (failure)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_CONNECTED);

        verify(mMockCallbacks, times(1)).onConnectFailed(TEST_NAME,
                BLEHandlerCallbacks.Result.CONNECT_FAILURE);
    }

    @Test
    public void testOnConnectionChanged_callsOnErrorIfDisconnectedSuccessOnConnectRequest() throws Exception
    {
        //Mode is connecting, but changed to disconnected (success)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_DISCONNECTED);

        verify(mMockCallbacks, times(1)).onError(TEST_NAME, BLEHandlerCallbacks.Error.CONNECT_WITHOUT_REQUEST);
    }

    @Test
    public void testOnConnectionChanged_callsonConnectFailedIfDisconnectedFailureOnConnectRequest() throws Exception
    {
        //Mode is connecting, but changed to disconnected (failure)
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(BLEConnection.Mode.CONNECTING);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_FAILURE,
                                                            BluetoothGatt.STATE_DISCONNECTED);

        verify(mMockCallbacks, times(1)).onConnectFailed(TEST_NAME, BLEHandlerCallbacks.Result.CONNECT_FAILURE);
    }

    @Test
    public void testOnConnectionStateChanged_callsOnErrorOnNullMode() throws Exception
    {
        when(mMockBLEHandler.getConnectionMode(TEST_NAME)).thenReturn(null);
        mHandler.getGattCallbacks().onConnectionStateChange(mMockGatt,
                                                            GATT_STATUS_SUCCESS,
                                                            BluetoothGatt.STATE_CONNECTED);
        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.INVALID_MODE);
    }

    @Test
    public void testOnServicesDiscovered_callsOnConnectedOnSuccess() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks, times(1)).onConnect(TEST_NAME);
    }

    @Test
    public void testOnServicesDiscovered_callsOnConnectFailOnFailure() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_FAILURE);

        verify(mMockCallbacks, times(1)).onConnectFailed(TEST_NAME,
                BLEHandlerCallbacks.Result.SERVICE_DISC_ERROR);
    }

    @Test
    public void testOnServicesDiscovered_doesntCallOnConnectOnFailure() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_FAILURE);

        verify(mMockCallbacks, never()).onConnect(any(String.class));
    }

    @Test
    public void testOnServicesDiscovered_callsGetServiceOnSuccess() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockGatt).getService(BluetoothCallbackHandler.TRUCONNECT_SERVICE_UUID);
    }

    @Test
    public void testOnServicesDiscovered_doesntCallGetServiceOnFailure() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_FAILURE);

        verify(mMockGatt, never()).getService(BluetoothCallbackHandler.TRUCONNECT_SERVICE_UUID);
    }

    @Test
    public void testOnServicesDiscovered_setsHandlerService() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockConnection).setService(mMockService);
    }

    @Test
    public void testOnServicesDiscovered_callsOnConnectFailOnNoServiceFound() throws Exception
    {
        when(mMockGatt.getService(BluetoothCallbackHandler.TRUCONNECT_SERVICE_UUID)).thenReturn(null);
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks, times(1)).onConnectFailed(TEST_NAME,
                                                         BLEHandlerCallbacks.Result.SERVICE_DISC_ERROR);
    }

    @Test
    public void testOnServicesDiscovered_doesntCallOnConnectOnNoServiceFound() throws Exception
    {
        when(mMockGatt.getService(BluetoothCallbackHandler.TRUCONNECT_SERVICE_UUID)).thenReturn(null);
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks, never()).onConnect(any(String.class));
    }

    @Test
    public void testOnServicesDiscovered_setsConnectionTxCharacteristic() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockConnection).setTxCharacteristic(mMockTxChar);
    }

    @Test
    public void testOnServicesDiscovered_callsOnErrorOnNullTxChar() throws Exception
    {
        when(mMockService.getCharacteristic(BluetoothCallbackHandler.TX_UUID)).thenReturn(null);
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.NO_TX_CHARACTERISTIC);
    }

    @Test
    public void testOnServicesDiscovered_setsConnectionRxCharacteristic() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockConnection).setRxCharacteristic(mMockRxChar);
    }

    @Test
    public void testOnServicesDiscovered_callsOnErrorOnNullRxChar() throws Exception
    {
        when(mMockService.getCharacteristic(BluetoothCallbackHandler.RX_UUID)).thenReturn(null);
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.NO_RX_CHARACTERISTIC);
    }

    @Test
    public void testOnServicesDiscovered_setsConnectionModeCharacteristic() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockConnection).setModeCharacteristic(mMockModeChar);
    }

    @Test
    public void testOnServicesDiscovered_callsOnErrorOnNullModeChar() throws Exception
    {
        when(mMockService.getCharacteristic(BluetoothCallbackHandler.MODE_UUID)).thenReturn(null);
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.NO_MODE_CHARACTERISTIC);
    }

    @Test
    public void testOnServicesDiscovered_callsOnErrorOnNullConnection() throws Exception
    {
        when(mMockBLEHandler.getConnection(TEST_NAME)).thenReturn(null);
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.NO_CONNECTION_FOUND);
    }

    @Test
    public void testOnServicesDiscovered_enablesNotifyOnDataReady() throws Exception
    {
        mHandler.getGattCallbacks().onServicesDiscovered(mMockGatt, GATT_STATUS_SUCCESS);

        verify(mMockBLEHandler, times(1)).addCommand(any(BLEConnection.class), eq(BLECommandType.SET_RX_NOTIFY));
    }

    @Test
    public void testOnCharacteristicRead_callOnErrorOnNullGatt() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicRead(null, mMockTxChar, GATT_STATUS_SUCCESS);
        verify(mMockCallbacks).onError(any(String.class), eq(BLEHandlerCallbacks.Error.NULL_GATT_ON_CALLBACK));
    }

    @Test
    public void testOnCharacteristicRead_callOnErrorOnNullCharacteristic() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicRead(mMockGatt, null, GATT_STATUS_SUCCESS);
        verify(mMockCallbacks).onError(any(String.class), eq(BLEHandlerCallbacks.Error.NULL_CHAR_ON_CALLBACK));
    }

    @Test
    public void testOnCharacteristicRead_callOnModeReadOnModeReadSuccess() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicRead(mMockGatt, mMockModeChar, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onModeRead(TEST_NAME, BluetoothCallbackHandler.MODE_STREAM);
    }

    @Test
    public void testOnCharacteristicRead_callOnErrorOnModeReadFailure() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicRead(mMockGatt, mMockModeChar, GATT_STATUS_FAILURE);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.MODE_READ_FAILED);
    }

    @Test
    public void testOnCharacteristicRead_callOnDataReadOnDataReadSuccess() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicRead(mMockGatt, mMockTxChar, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onDataRead(TEST_NAME, TEST_DATA);
    }

    @Test
    public void testOnCharacteristicRead_callOnErrorOnDataReadFailure() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicRead(mMockGatt, mMockTxChar, GATT_STATUS_FAILURE);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.DATA_READ_FAILED);
    }

    @Test
    public void testOnCharacteristicWrite_callOnErrorOnNullGatt() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicWrite(null, mMockRxChar, GATT_STATUS_SUCCESS);
        verify(mMockCallbacks).onError(any(String.class), eq(BLEHandlerCallbacks.Error.NULL_GATT_ON_CALLBACK));
    }

    @Test
    public void testOnCharacteristicWrite_callOnErrorOnNullCharacteristic() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicWrite(mMockGatt, null, GATT_STATUS_SUCCESS);
        verify(mMockCallbacks).onError(any(String.class), eq(BLEHandlerCallbacks.Error.NULL_CHAR_ON_CALLBACK));
    }

    @Test
    public void testOnCharacteristicWrite_callOnDataWriteOnDataWriteSuccess() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicWrite(mMockGatt, mMockRxChar, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onDataWrite(TEST_NAME, TEST_DATA);
    }

    @Test
    public void testOnCharacteristicWrite_callOnErrorOnDataWriteFailure() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicWrite(mMockGatt, mMockRxChar, GATT_STATUS_FAILURE);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.DATA_WRITE_FAILED);
    }

    @Test
    public void testOnCharacteristicWrite_callOnModeWriteOnModeWriteSuccess() throws Exception
    {
        when(mMockModeChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)).thenReturn(BluetoothCallbackHandler.MODE_REMOTE_COMMAND);
        mHandler.getGattCallbacks().onCharacteristicWrite(mMockGatt, mMockModeChar, GATT_STATUS_SUCCESS);

        verify(mMockCallbacks).onModeChanged(TEST_NAME, BluetoothCallbackHandler.MODE_REMOTE_COMMAND);
    }

    @Test
    public void testOnCharacteristicWrite_callOnErrorOnModeWriteFailure() throws Exception
    {
        mHandler.getGattCallbacks().onCharacteristicWrite(mMockGatt, mMockModeChar, GATT_STATUS_FAILURE);

        verify(mMockCallbacks).onError(TEST_NAME, BLEHandlerCallbacks.Error.MODE_WRITE_FAILED);
    }

    @Test
    public void testOnCharacteristicChanged() throws Exception
    {

    }
}
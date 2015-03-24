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
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class BLEHandlerTest
{
    Context mMockContext;
    BluetoothManager mMockManager;
    BluetoothAdapter mMockAdapter;
    BluetoothAdapter.LeScanCallback mMockScanCallback;
    Handler mMockHandler;
    BLEHandlerCallbacks mCallbacks;
    BLEHandlerCallbacks mMockCallbacks;
    BLEHandler mHandler;
    BluetoothDevice mMockDevice;
    BLEDevice mMockBLEDevice;
    BLEGatt mMockBLEGatt;
    BLEConnection mMockBLEConnection;
    BluetoothGatt mMockGatt;

    private BLEDeviceList mMockScanList;
    private SearchableList mMockConnectedList;
    private BLECommandQueue mMockQueue;

    final UUID DEVICE_UUIDS[] = {UUID.fromString("175f8f23-a570-49bd-9627-815a6a27de2a")};
    private final String DEVICE_NAME = "test";
    private final String DEVICE_DATA = "data";
    private final int INVALID_MODE = BluetoothCallbackHandler.MODE_REMOTE_COMMAND + 1;
    private final int VALID_MODE = BluetoothCallbackHandler.MODE_STREAM;

    @Before
    public void setUp() throws Exception
    {
        mMockContext = mock(Context.class);
        mMockManager = mock(BluetoothManager.class);
        mMockAdapter = mock(BluetoothAdapter.class);
        mMockHandler = mock(Handler.class);
        mMockScanCallback = mock(BluetoothAdapter.LeScanCallback.class);
        mMockHandler = mock(Handler.class);
        mMockDevice = mock(BluetoothDevice.class);
        mMockBLEDevice = mock(BLEDevice.class);
        mMockBLEGatt = mock(BLEGatt.class);
        mMockBLEConnection = mock(BLEConnection.class);
        mMockGatt = mock(BluetoothGatt.class);

        mMockScanList = mock(BLEDeviceList.class);
        mMockConnectedList = mock(SearchableList.class);
        mMockQueue = mock(BLECommandQueue.class);

        mHandler = new BLEHandler(mMockScanList, mMockConnectedList, mMockQueue);
        mCallbacks = new BLEHandlerCallbacks(mHandler);
        mMockCallbacks = mock(BLEHandlerCallbacks.class);

        when(mMockConnectedList.add(any(BLEConnection.class))).thenReturn(true);
        when(mMockManager.getAdapter()).thenReturn(mMockAdapter);
        when(mMockContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(mMockManager);
        when(mMockAdapter.isEnabled()).thenReturn(true);
        when(mMockAdapter.startLeScan(any(BluetoothAdapter.LeScanCallback.class))).thenReturn(true);
        when(mMockHandler.postDelayed(any(Runnable.class), any(long.class))).thenReturn(true);
        when(mMockBLEDevice.getName()).thenReturn(DEVICE_NAME);
        when(mMockScanList.get(DEVICE_NAME)).thenReturn(mMockBLEDevice);
        when(mMockConnectedList.get(DEVICE_NAME)).thenReturn(mMockBLEConnection);
        when(mMockBLEDevice.connectGatt(any(Context.class), any(boolean.class), any(BluetoothGattCallback.class))).thenReturn(mMockBLEGatt);
        when(mMockBLEConnection.getGatt()).thenReturn(mMockGatt);
        when(mMockBLEConnection.readModeCharacteristic()).thenReturn(true);
        when(mMockBLEConnection.writeModeCharacteristic(VALID_MODE)).thenReturn(true);
        when(mMockBLEConnection.readTxCharacteristic()).thenReturn(true);
        when(mMockBLEConnection.writeRxCharacteristic(any(String.class))).thenReturn(true);

        when(mMockQueue.add(any(BLEConnection.class), any(BLECommandType.class))).thenReturn(true);
        when(mMockQueue.add(any(BLEConnection.class), any(BLECommandType.class), anyString())).thenReturn(true);
        when(mMockQueue.add(any(BLEConnection.class), any(BLECommandType.class), anyInt())).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void testInit_returnsFalseOnNullManager() throws Exception
    {
        when(mMockContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(null);


        assertFalse(mHandler.init(mMockContext, mCallbacks));
    }

    @Test
    public void testInit_returnsTrueOnValidManagerAndAdapter() throws Exception
    {
        assertTrue(mHandler.init(mMockContext, mCallbacks));
    }

    @Test
    public void testInit_returnsFalseOnNullAdapter() throws Exception
    {
        when(mMockManager.getAdapter()).thenReturn(null);


        assertFalse(mHandler.init(mMockContext, mCallbacks));
    }

    @Test
    public void testInit_resetsScanningState() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.isScanning());
    }

    @Test
    public void testIsInitialised_returnsFalseOnNullAdapter() throws Exception
    {
        when(mMockManager.getAdapter()).thenReturn(null);

        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.isInitialised());
    }

    @Test
    public void testIsInitialised_returnsTrueOnValidAdapter() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);

        assertTrue(mHandler.isInitialised());
    }

    @Test
    public void testIsBLEEnabled_returnsFalseOnNullAdapter() throws Exception
    {
        when(mMockManager.getAdapter()).thenReturn(null);

        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.isBLEEnabled());
    }

    @Test
    public void testIsBLEEnabled_returnsFalseIfNotEnabled() throws Exception
    {
        when(mMockAdapter.isEnabled()).thenReturn(false);

        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.isBLEEnabled());
    }

    @Test
    public void testIsBLEEnabled_returnsTrueIfEnabled() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);

        assertTrue(mHandler.isBLEEnabled());
    }

    @Test
    public void testStartBLEScan_returnsFalseIfBLEDisabled() throws Exception
    {
        when(mMockAdapter.isEnabled()).thenReturn(false);

        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.startBLEScan());
    }

    @Test
    public void testStartBLEScan_callsStartLeScan() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        verify(mMockAdapter).startLeScan(any(BluetoothAdapter.LeScanCallback.class));
    }

    @Test
    public void testStartBLEScan_doesntCallStartLeScanIfDisabled() throws Exception
    {
        when(mMockAdapter.isEnabled()).thenReturn(false);

        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        verify(mMockAdapter, never()).startLeScan(any(BluetoothAdapter.LeScanCallback.class));
    }

    @Test
    public void testStartBLEScan_returnsFalseIfStartLeScanFails()
    {

        when(mMockAdapter.startLeScan(any(BluetoothAdapter.LeScanCallback.class))).thenReturn(false);
        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.startBLEScan());
    }
    @Test
    public void testStartBLEScan_returnsFalseIfStarted() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        assertFalse(mHandler.startBLEScan());
    }

    @Test
    public void testStartBLEScan_returnsTrueOnSuccess() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);

        assertTrue(mHandler.startBLEScan());
    }

    @Test
    public void testStartBLEScan_returnsFalseOnNullAdapter() throws Exception
    {
        when(mMockManager.getAdapter()).thenReturn(null);

        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.startBLEScan());
    }

    @Test
    public void testStartBLEScan_clearsScanList() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        verify(mMockScanList).clear();
    }

    @Test
    public void testIsScanning_returnsTrueWhenScanning() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        assertTrue(mHandler.isScanning());
    }

    @Test
    public void testInScanning_returnsFalseWhenNotScanning() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.isScanning());
    }

    @Test
    public void testStopBLEScan_returnsFalseIfNotEnabled() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);
        when(mMockAdapter.isEnabled()).thenReturn(false);

        assertFalse(mHandler.stopBLEScan());
    }

    @Test
    public void testStopBLEScan_returnsTrueOnSuccess() throws Exception
    {

        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();

        assertTrue(mHandler.stopBLEScan());
    }

    @Test
    public void testStopBLEScan_returnsFalseIfNotInitialised() throws Exception
    {

        when(mMockContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(null);
        mHandler.init(mMockContext, mCallbacks);

        assertFalse(mHandler.stopBLEScan());
    }

    @Test
    public void testStopBLEScan_callsStopLeScan() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.stopBLEScan();
        verify(mMockAdapter).stopLeScan(any(BluetoothAdapter.LeScanCallback.class));
    }

    @Test
    public void testStopBLEScan_resetsScanStatus() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();
        mHandler.stopBLEScan();
        assertFalse(mHandler.isScanning());
    }

    @Test
    public void testDeinit_stopsScanIfStarted() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.startBLEScan();
        mHandler.deinit();

        verify(mMockAdapter).stopLeScan(any(BluetoothAdapter.LeScanCallback.class));
    }

    @Test
    public void testDeinit_doesntStopScanIfNotStarted() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.deinit();

        verify(mMockAdapter, never()).stopLeScan(any(BluetoothAdapter.LeScanCallback.class));
    }

    @Test
    public void testDeinit_closesConnections() throws Exception
    {
        when(mMockConnectedList.size()).thenReturn(2);
        when(mMockConnectedList.get(any(int.class))).thenReturn(mMockBLEConnection);

        mHandler.init(mMockContext, mCallbacks);
        mHandler.deinit();

        verify(mMockBLEConnection, times(2)).close();
    }

    @Test
    public void testDeinit_deInitialises() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.deinit();

        assertFalse(mHandler.isInitialised());
    }

    @Test
    public void testConnect_returnsFalseIfDeviceNotFound() throws Exception
    {
        when(mMockScanList.get(DEVICE_NAME)).thenReturn(null);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.connect(DEVICE_NAME));
    }

    @Test
    public void testConnect_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertTrue(mHandler.connect(DEVICE_NAME));
    }

    @Test
    public void testConnect_callsConnectGatt() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.connect(DEVICE_NAME);
        verify(mMockBLEDevice).connectGatt(eq(mMockContext), eq(false), any(BluetoothGattCallback.class));
    }

    @Test
    public void testConnect_returnsFalseIfConnectGattReturnsNull() throws Exception
    {
        when(mMockBLEDevice.connectGatt(any(Context.class), any(boolean.class), any(BluetoothGattCallback.class))).thenReturn(null);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.connect(DEVICE_NAME));
    }

    @Test
    public void testConnect_addsNewConnectionOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.connect(DEVICE_NAME);
        verify(mMockConnectedList).add(any(BLEConnection.class));
    }

    @Test
    public void testConnect_doesntAddNewConnectionOnNullGatt() throws Exception
    {
        when(mMockBLEDevice.connectGatt(any(Context.class),
                                        any(boolean.class),
                                        any(BluetoothGattCallback.class))).thenReturn(null);
        mHandler.init(mMockContext, mCallbacks);
        mHandler.connect(DEVICE_NAME);
        verify(mMockConnectedList, never()).add(any(BLEConnection.class));
    }

    @Test
    public void testConnect_returnsFalseOnAddConnectionFail() throws Exception
    {
        when(mMockConnectedList.add(any(BLEConnection.class))).thenReturn(false);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.connect(DEVICE_NAME));
    }

    @Test
    public void testDisconnect_returnsFalseOnDeviceNotFound() throws Exception
    {
        when(mMockConnectedList.get(DEVICE_NAME)).thenReturn(null);
        assertFalse(mHandler.disconnect(DEVICE_NAME));
    }

    @Test
    public void testDisconnect_returnsFalseOnNullName() throws Exception
    {
        assertFalse(mHandler.disconnect(null));
    }

    @Test
    public void testDisconnect_returnsTrueOnSuccess() throws Exception
    {
        assertTrue(mHandler.disconnect(DEVICE_NAME));
    }

    @Test
    public void testDisconnect_callsGattDisconnect() throws Exception
    {
        mHandler.disconnect(DEVICE_NAME);
        verify(mMockBLEConnection, times(1)).disconnect();
    }

    @Test
    public void testReadMode_returnsFalseOnNullName() throws Exception
    {
        assertFalse(mHandler.readMode(null));
    }

    @Test
    public void testReadMode_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertTrue(mHandler.readMode(DEVICE_NAME));
    }

    @Test
    public void testReadMode_callsQueueAddOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.readMode(DEVICE_NAME);
        verify(mMockQueue, times(1)).add(any(BLEConnection.class), eq(BLECommandType.READ_MODE));
    }

    @Test
    public void testReadMode_returnsFalseOnQueueAddFail() throws Exception
    {
        when(mMockQueue.add(any(BLEConnection.class), eq(BLECommandType.READ_MODE))).thenReturn(false);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.readMode(DEVICE_NAME));
    }

    @Test
    public void testReadMode_returnsFalseOnDeviceNotFound() throws Exception
    {
        when(mMockConnectedList.get(DEVICE_NAME)).thenReturn(null);
        assertFalse(mHandler.readMode(DEVICE_NAME));
    }

    @Test
    public void testWriteMode_returnsFalseOnNullName() throws Exception
    {
        assertFalse(mHandler.writeMode(null, VALID_MODE));
    }

    @Test
    public void testWriteMode_returnsFalseOnInvalidMode() throws Exception
    {
        assertFalse(mHandler.writeMode(DEVICE_NAME, INVALID_MODE));
    }

    @Test
    public void testWriteMode_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertTrue(mHandler.writeMode(DEVICE_NAME, VALID_MODE));
    }

    @Test
    public void testWriteMode_callsQueueAddOnSuccess() throws Exception
    {
        int mode = BluetoothCallbackHandler.MODE_REMOTE_COMMAND;
        mHandler.init(mMockContext, mCallbacks);
        mHandler.writeMode(DEVICE_NAME, mode);
        verify(mMockQueue, times(1)).add(any(BLEConnection.class), eq(BLECommandType.WRITE_MODE), eq(mode));
    }

    @Test
    public void testWriteMode_returnsFalseOnQueueAddFail() throws Exception
    {
        when(mMockQueue.add(any(BLEConnection.class), any(BLECommandType.class), anyInt())).thenReturn(false);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.writeMode(DEVICE_NAME, VALID_MODE));
    }

    @Test
    public void testWriteMode_returnsFalseOnDeviceNotFound() throws Exception
    {
        when(mMockConnectedList.get(DEVICE_NAME)).thenReturn(null);
        assertFalse(mHandler.writeMode(DEVICE_NAME, VALID_MODE));
    }

    @Test
    public void testReadData_returnsFalseOnNullName() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.readData(null));
    }

    @Test
    public void testReadData_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertTrue(mHandler.readData(DEVICE_NAME));
    }

    @Test
    public void testReadData_callsQueueAddOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.readData(DEVICE_NAME);
        verify(mMockQueue, times(1)).add(any(BLEConnection.class), eq(BLECommandType.READ_DATA));
    }

    @Test
    public void testReadData_returnsFalseOnQueueAddFail() throws Exception
    {
        when(mMockQueue.add(any(BLEConnection.class), eq(BLECommandType.READ_DATA))).thenReturn(false);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.readData(DEVICE_NAME));
    }

    @Test
    public void testReadData_returnsFalseOnDeviceNotFound() throws Exception
    {
        when(mMockConnectedList.get(DEVICE_NAME)).thenReturn(null);
        assertFalse(mHandler.readData(DEVICE_NAME));
    }

    @Test
    public void testWriteData_returnsFalseOnNullName() throws Exception
    {
        assertFalse(mHandler.writeData(null, DEVICE_DATA));
    }

    @Test
    public void testWriteData_returnsFalseOnNullData() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.writeData(DEVICE_NAME, null));
    }

    @Test
    public void testWriteData_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        assertTrue(mHandler.writeData(DEVICE_NAME, DEVICE_DATA));
    }

    @Test
    public void testWriteData_callsQueueAddOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mCallbacks);
        mHandler.writeData(DEVICE_NAME, DEVICE_DATA);
        verify(mMockQueue, times(1)).add(any(BLEConnection.class), eq(BLECommandType.WRITE_DATA), eq(DEVICE_DATA));
    }

    @Test
    public void testWriteData_returnsFalseOnQueueAddFail() throws Exception
    {
        when(mMockQueue.add(any(BLEConnection.class), any(BLECommandType.class), anyString())).thenReturn(false);
        mHandler.init(mMockContext, mCallbacks);
        assertFalse(mHandler.writeData(DEVICE_NAME, DEVICE_DATA));
    }

    @Test
    public void testWriteData_returnsFalseOnDeviceNotFound() throws Exception
    {
        when(mMockConnectedList.get(DEVICE_NAME)).thenReturn(null);
        assertFalse(mHandler.writeData(DEVICE_NAME, DEVICE_NAME));
    }
}
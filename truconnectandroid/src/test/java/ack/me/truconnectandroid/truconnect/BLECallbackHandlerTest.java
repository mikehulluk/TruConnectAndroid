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

package ack.me.truconnectandroid.truconnect;

import org.junit.Before;
import org.junit.Test;

import ack.me.truconnectandroid.BLE.BLEHandler;
import ack.me.truconnectandroid.BLE.BLEHandlerCallbacks;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BLECallbackHandlerTest
{
    private final String VALID_NAME = "test_name";
    private final String TEST_DATA_SHORT = "test";// < MAX_PACKET_LENGTH
    private final String TEST_DATA_LONG = "set sy d n testing_device_name";// > MAX_PACKET_LENGTH

    private final int MODE_UNKNOWN = TruconnectHandler.MODE_STREAM;
    private final int MODE_STREAM = TruconnectHandler.MODE_STREAM;
    private final int MODE_COMMAND_LOCAL = TruconnectHandler.MODE_STREAM;
    private final int MODE_COMMAND_REMOTE = TruconnectHandler.MODE_STREAM;

    private final int PACKET_SIZE_MAX = TruconnectHandler.PACKET_SIZE_MAX;

    private final BLEHandlerCallbacks.Result RESULT_CON_ERR = BLEHandlerCallbacks.Result.CONNECT_FAILURE;
    private final BLEHandlerCallbacks.Result RESULT_DISCOVRY_ERR = BLEHandlerCallbacks.Result.SERVICE_DISC_ERROR;

    private BLEHandler mMockBLEHandler;
    private TruconnectHandler mMockTruconnectHandler;
    private TruconnectCallbacks mMockTruconnectCallbacks;
    private BLECallbackHandler mHandler;

    @Before
    public void setUp() throws Exception
    {
        mMockBLEHandler = mock(BLEHandler.class);
        mMockTruconnectHandler = mock(TruconnectHandler.class);
        mMockTruconnectCallbacks = mock(TruconnectCallbacks.class);

        when(mMockTruconnectHandler.getCallbacks()).thenReturn(mMockTruconnectCallbacks);
        when(mMockTruconnectHandler.getNextWriteDataPacket()).thenReturn("");
        when(mMockTruconnectHandler.getLastWritePacket()).thenReturn(TEST_DATA_SHORT);

        mHandler = new BLECallbackHandler(mMockBLEHandler, mMockTruconnectHandler);
    }

    @Test
    public void testOnScanResult_callsOnScanResult() throws Exception
    {
        mHandler.getBLECallbacks().onScanResult(VALID_NAME);

        verify(mMockTruconnectCallbacks).onScanResult(VALID_NAME);
    }

    @Test
    public void testOnConnect_callsOnConnect() throws Exception
    {
        mHandler.getBLECallbacks().onConnect(VALID_NAME);

        verify(mMockTruconnectCallbacks).onConnected(VALID_NAME);
    }

    @Test
    public void testOnConnect_setsConnectedStatus() throws Exception
    {
        mHandler.getBLECallbacks().onConnect(VALID_NAME);

        verify(mMockTruconnectHandler).setConnectedStatus(true);
    }

    @Test
    public void testOnConnectFailed_callsOnConnectFailedOnConnectFailure() throws Exception
    {
        mHandler.getBLECallbacks().onConnectFailed(VALID_NAME, RESULT_CON_ERR);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.CONNECT_FAILED);
    }

    @Test
    public void testOnConnectFailed_callsOnConnectFailedOnDiscoveryFailure() throws Exception
    {
        mHandler.getBLECallbacks().onConnectFailed(VALID_NAME, RESULT_DISCOVRY_ERR);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.SERVICE_DISCOVERY_ERROR);
    }

    @Test
    public void testOnDisconnect_callsOnDisconnect() throws Exception
    {
        mHandler.getBLECallbacks().onDisconnect(VALID_NAME);

        verify(mMockTruconnectCallbacks).onDisconnected();
    }

    @Test
    public void testOnDisconnect_resetsConnectedStatus() throws Exception
    {
        mHandler.getBLECallbacks().onDisconnect(VALID_NAME);

        verify(mMockTruconnectHandler).setConnectedStatus(false);
    }

    @Test
    public void testOnDisconnectFailed_callsOnDisconnectFailedOnDisconnectFailure() throws Exception
    {
        mHandler.getBLECallbacks().onDisconnectFailed(VALID_NAME);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.DISCONNECT_FAILED);
    }

    @Test
    public void testOnDataWrite_writesPacketIfDataAvailable() throws Exception
    {
        when(mMockTruconnectHandler.getNextWriteDataPacket()).thenReturn(TEST_DATA_SHORT);
        mHandler.getBLECallbacks().onDataWrite(VALID_NAME, TEST_DATA_SHORT);

        verify(mMockBLEHandler).writeData(VALID_NAME, TEST_DATA_SHORT);
    }

    @Test
    public void testOnDataWrite_doesntWriteDataIfPacketEmpty() throws Exception
    {
        mHandler.getBLECallbacks().onDataWrite(VALID_NAME, TEST_DATA_SHORT);

        verify(mMockBLEHandler, never()).writeData(anyString(), anyString());
    }

    @Test
    public void testOnDataWrite_callsOnDataWriteOnSuccess() throws Exception
    {
        mHandler.getBLECallbacks().onDataWrite(VALID_NAME, TEST_DATA_SHORT);

        verify(mMockTruconnectCallbacks).onDataWritten(TEST_DATA_SHORT);
    }

    @Test
    public void testOnDataWrite_callsOnDataWriteFailedOnWriteFail() throws Exception
    {
        mHandler.getBLECallbacks().onDataWrite(VALID_NAME, TEST_DATA_SHORT.substring(1));

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.WRITE_FAILED);
    }

    @Test
    public void testOnDataWrite_doesntCallDataWriteOnWriteFailAndDataToWrite() throws Exception
    {
        when(mMockTruconnectHandler.getNextWriteDataPacket()).thenReturn(TEST_DATA_SHORT);
        mHandler.getBLECallbacks().onDataWrite(VALID_NAME, TEST_DATA_SHORT.substring(1));

        verify(mMockBLEHandler, never()).writeData(anyString(), anyString());
    }

    @Test
    public void testOnDataRead_callsOnDataReadOnDataRead() throws Exception
    {
        mHandler.getBLECallbacks().onDataRead(VALID_NAME, TEST_DATA_SHORT);

        verify(mMockTruconnectCallbacks).onDataRead(TEST_DATA_SHORT);
    }

    @Test
    public void testOnDataRead_addsDataToBuffer() throws Exception
    {
        mHandler.getBLECallbacks().onDataRead(VALID_NAME, TEST_DATA_SHORT);

        verify(mMockTruconnectHandler).onRead(TEST_DATA_SHORT);
    }

    @Test
    public void testOnModeRead_updatesMode() throws Exception
    {
        mHandler.getBLECallbacks().onModeRead(VALID_NAME, MODE_STREAM);

        verify(mMockTruconnectHandler).setCurrentMode(MODE_STREAM);
    }

    @Test
    public void testOnModeRead_callsOnModeRead() throws Exception
    {
        mHandler.getBLECallbacks().onModeRead(VALID_NAME, MODE_STREAM);

        verify(mMockTruconnectCallbacks).onModeRead(MODE_STREAM);
    }

    @Test
    public void testOnModeChanged_updatesMode() throws Exception
    {
        mHandler.getBLECallbacks().onModeChanged(VALID_NAME, MODE_STREAM);

        verify(mMockTruconnectHandler).setCurrentMode(MODE_STREAM);
    }

    @Test
    public void testOnModeChanged_callsOnModeWritten() throws Exception
    {
        final int NEW_MODE = MODE_COMMAND_REMOTE;
        mHandler.getBLECallbacks().onModeChanged(VALID_NAME, NEW_MODE);

        verify(mMockTruconnectCallbacks).onModeWritten(NEW_MODE);
    }

    @Test
    public void testOnError_callsOnErrorOnConWithoutReq() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.CONNECT_WITHOUT_REQUEST);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.DEVICE_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnDisconWithoutReq() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.DISCONNECT_WITHOUT_REQUEST);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.DEVICE_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnInvalidMode() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.INVALID_MODE);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.INTERNAL_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnNoTxCharacteristic() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.NO_TX_CHARACTERISTIC);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.DEVICE_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnNoRxCharacteristic() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.NO_RX_CHARACTERISTIC);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.DEVICE_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnNoModeCharacteristic() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.NO_MODE_CHARACTERISTIC);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.DEVICE_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnNoConnection() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.NO_CONNECTION_FOUND);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.NO_CONNECTION_FOUND);
    }

    @Test
    public void testOnError_callsOnErrorOnNullGatt() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.NULL_GATT_ON_CALLBACK);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.SYSTEM_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnNullCharacteristic() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.NULL_CHAR_ON_CALLBACK);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.SYSTEM_ERROR);
    }

    @Test
    public void testOnError_callsOnErrorOnWriteError() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.DATA_WRITE_FAILED);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.WRITE_FAILED);
    }

    @Test
    public void testOnError_callsOnErrorOnReadError() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.DATA_READ_FAILED);

        verify(mMockTruconnectCallbacks).onError(TruconnectErrorCode.READ_FAILED);
    }

    @Test
    public void testOnError_clearsWriteBufferOnWriteError() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.DATA_WRITE_FAILED);

        verify(mMockTruconnectHandler).clearWriteBuffer();
    }

    @Test
    public void testOnError_clearsReadBufferOnReadError() throws Exception
    {
        mHandler.getBLECallbacks().onError(VALID_NAME, BLEHandlerCallbacks.Error.DATA_READ_FAILED);

        verify(mMockTruconnectHandler).clearReadBuffer();
    }


}
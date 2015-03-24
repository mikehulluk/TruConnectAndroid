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

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import ack.me.truconnectandroid.BLE.BLEHandler;
import ack.me.truconnectandroid.BLE.BLEHandlerCallbacks;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TruconnectHandlerTest
{
    private Context mMockContext;
    private BLEHandler mMockBLEHandler;
    private TruconnectCallbacks mMockCallbacks;

    private final String VALID_NAME = "test_name";
    private final String VALID_ARGS = "args";
    private final TruconnectCommand VALID_COMMAND = TruconnectCommand.ADC;

    private TruconnectHandler mHandler;

    @Before
    public void setUp() throws Exception
    {
        mMockContext = mock(Context.class);
        mMockBLEHandler = mock(BLEHandler.class);
        mMockCallbacks = mock(TruconnectCallbacks.class);

        mHandler = new TruconnectHandler();

        when(mMockBLEHandler.init(any(Context.class), any(BLEHandlerCallbacks.class))).thenReturn(true);
        when(mMockBLEHandler.isBLEEnabled()).thenReturn(true);
        when(mMockBLEHandler.startBLEScan()).thenReturn(true);
        when(mMockBLEHandler.stopBLEScan()).thenReturn(true);
        when(mMockBLEHandler.connect(anyString())).thenReturn(true);
        when(mMockBLEHandler.disconnect(anyString())).thenReturn(true);
        when(mMockBLEHandler.writeData(anyString(), anyString())).thenReturn(true);
        when(mMockBLEHandler.writeMode(anyString(), anyInt())).thenReturn(true);
        when(mMockBLEHandler.readMode(anyString())).thenReturn(true);
    }

    @Test
    public void testInit_initsBLEHelper() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        verify(mMockBLEHandler).init(eq(mMockContext), any(BLEHandlerCallbacks.class));
    }

    @Test
    public void testInit_returnsFalseOnInitFail() throws Exception
    {
        when(mMockBLEHandler.init(any(Context.class), any(BLEHandlerCallbacks.class))).thenReturn(false);
        assertFalse(mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks));
    }

    @Test
    public void testInit_returnsFalseOnBLENotEnabled() throws Exception
    {
        when(mMockBLEHandler.isBLEEnabled()).thenReturn(false);
        assertFalse(mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks));
    }

    @Test
    public void testInit_returnsTrueOnSuccess() throws Exception
    {
        assertTrue(mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks));
    }

    @Test
    public void testInit_returnsFalseOnNullHandler() throws Exception
    {
        assertFalse(mHandler.init(mMockContext, null, mMockCallbacks));
    }

    @Test
    public void testInit_returnsFalseOnNullCallbacks() throws Exception
    {
        assertFalse(mHandler.init(mMockContext, mMockBLEHandler, null));
    }

    @Test
    public void testStartScan_returnsFalseOnScanStartFail() throws Exception
    {
        when(mMockBLEHandler.startBLEScan()).thenReturn(false);

        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.startScan());
    }

    @Test
    public void testStartScan_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertTrue(mHandler.startScan());
    }

    @Test
    public void testStartScan_returnsFalseIfNotInitialised() throws Exception
    {
        assertFalse(mHandler.startScan());
    }

    @Test
    public void testStopScan_returnsFalseOnStopFail() throws Exception
    {
        when(mMockBLEHandler.stopBLEScan()).thenReturn(false);

        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.stopScan());
    }

    @Test
    public void testStopScan_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertTrue(mHandler.stopScan());
    }

    @Test
    public void testStopScan_returnsFalseIfNotInitialised() throws Exception
    {
        assertFalse(mHandler.stopScan());
    }

    @Test
    public void testConnect_returnsFalseOnConnectFail() throws Exception
    {
        when(mMockBLEHandler.connect(anyString())).thenReturn(false);

        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.connect(VALID_NAME));
    }

    @Test
    public void testConnect_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertTrue(mHandler.connect(VALID_NAME));
    }

    @Test
    public void testConnect_callsConnect() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        verify(mMockBLEHandler).connect(VALID_NAME);
    }

    @Test
    public void testConnect_returnsFalseIfNotInitialised() throws Exception
    {
        assertFalse(mHandler.connect(VALID_NAME));
    }

    @Test
    public void testConnect_returnsFalseIfAlreadyConnected() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        assertFalse(mHandler.connect(VALID_NAME));
    }

    @Test
    public void testDisconnect_returnsFalseOnDisconnectFail() throws Exception
    {
        when(mMockBLEHandler.disconnect(anyString())).thenReturn(false);

        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.disconnect());
    }

    @Test
    public void testConnect_callsDisconnect() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        mHandler.disconnect();

        verify(mMockBLEHandler).disconnect(VALID_NAME);
    }

    @Test
    public void testDisconnect_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        assertTrue(mHandler.disconnect());
    }

    @Test
    public void testDisconnect_returnsFalseIfNotConnected() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);

        assertFalse(mHandler.disconnect());
    }

    @Test
    public void testDisconnect_returnsFalseIfNotInitialised() throws Exception
    {
        assertFalse(mHandler.disconnect());
    }

    @Test
    public void testIsConnected_returnsFalseIfNotConnected() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.isConnected());
    }

    @Test
    public void testIsConnected_returnsTrueIfConnected() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        assertTrue(mHandler.isConnected());
    }

    @Test
    public void testIsConnected_returnsFalseIfNotInitialised() throws Exception
    {
        assertFalse(mHandler.isConnected());
    }

    @Test
    public void testInitResetsConnectedStatus() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.isConnected());
    }

    @Test
    public void testSendCommand_returnsFalseOnNullCommand() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.sendCommand(null, VALID_ARGS));
    }

    @Test
    public void testSendCommand_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertTrue(mHandler.sendCommand(VALID_COMMAND, VALID_ARGS));
    }

    @Test
    public void testSendCommand_returnsFalseOnNullArgs() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        assertFalse(mHandler.sendCommand(VALID_COMMAND, null));
    }

    @Test
    public void testSendCommand_callsWriteData() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        mHandler.sendCommand(VALID_COMMAND, VALID_ARGS);

        final String commandString = String.format("%s %s\r\n", VALID_COMMAND, VALID_ARGS);

        verify(mMockBLEHandler).writeData(VALID_NAME, commandString);
    }

    @Test
    public void testSendCommand_returnsFalseOnStringTooLong() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);

        //122 characters (128 - line_ending - command - space)
        final String INVALID_ARGS = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        assertFalse(mHandler.sendCommand(VALID_COMMAND, INVALID_ARGS));
    }

    @Test
    public void testWriteMode_writesModeCharacteristic() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        mHandler.setMode(TruconnectHandler.MODE_STREAM);

        verify(mMockBLEHandler).writeMode(VALID_NAME, TruconnectHandler.MODE_STREAM);
    }

    @Test
    public void testWriteMode_returnsFalseOnWriteFail() throws Exception
    {
        when(mMockBLEHandler.writeMode(anyString(), anyInt())).thenReturn(false);

        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);

        assertFalse(mHandler.setMode(TruconnectHandler.MODE_STREAM));
    }

    @Test
    public void testSetMode_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);

        assertTrue(mHandler.setMode(TruconnectHandler.MODE_STREAM));
    }

    @Test
    public void testGetMode_callsReadMode() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);
        mHandler.getMode();

        verify(mMockBLEHandler).readMode(VALID_NAME);
    }

    @Test
    public void testReadMode_returnsFalseOnReadFail() throws Exception
    {
        when(mMockBLEHandler.readMode(anyString())).thenReturn(false);

        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);

        assertFalse(mHandler.getMode());
    }

    @Test
    public void testGetMode_returnsTrueOnSuccess() throws Exception
    {
        mHandler.init(mMockContext, mMockBLEHandler, mMockCallbacks);
        mHandler.connect(VALID_NAME);

        assertTrue(mHandler.getMode());
    }

    @Test
    public void testGetVersion() throws Exception
    {

    }
}
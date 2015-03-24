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

import ack.me.truconnectandroid.BLE.BLEHandler;
import ack.me.truconnectandroid.BLE.BLEHandlerCallbacks;

public class BLECallbackHandler
{
    private BLEHandler mBLEHandler;
    private BLEHandlerCallbacks mBLECallbacks;
    private TruconnectHandler mTruconnectHandler;
    private TruconnectCallbacks mTruconnectCallbacks;

    BLECallbackHandler(BLEHandler BLEHandler, TruconnectHandler truconnectHandler)
    {
        mBLEHandler = BLEHandler;
        mTruconnectHandler = truconnectHandler;
        mTruconnectCallbacks = truconnectHandler.getCallbacks();

        mBLECallbacks = new BLEHandlerCallbacks(BLEHandler)
        {
            @Override
            public void onScanResult(String deviceName)
            {
                mTruconnectCallbacks.onScanResult(deviceName);
            }

            @Override
            public void onConnect(String deviceName)
            {
                mTruconnectHandler.setConnectedStatus(true);
                mTruconnectCallbacks.onConnected(deviceName);
            }

            @Override
            public void onConnectFailed(String deviceName, BLEHandlerCallbacks.Result result)
            {
                TruconnectErrorCode errorCode;

                if (result == BLEHandlerCallbacks.Result.SERVICE_DISC_ERROR)
                {
                    errorCode = TruconnectErrorCode.SERVICE_DISCOVERY_ERROR;
                }
                else
                {
                    errorCode = TruconnectErrorCode.CONNECT_FAILED;
                }

                mTruconnectCallbacks.onError(errorCode);
                mTruconnectHandler.onError();
            }

            @Override
            public void onDisconnect(String deviceName)
            {
                mTruconnectHandler.setConnectedStatus(false);
                mTruconnectCallbacks.onDisconnected();
            }

            @Override
            public void onDisconnectFailed(String deviceName)
            {
                mTruconnectCallbacks.onError(TruconnectErrorCode.DISCONNECT_FAILED);
                mTruconnectHandler.onError();
            }

            @Override
            public void onDataRead(String deviceName, String data)
            {
                //append data to read buffer
                mTruconnectHandler.onRead(data);
                mTruconnectCallbacks.onDataRead(data);
            }

            @Override
            public void onDataWrite(String deviceName, String data)
            {
                //write data in chunks
                if(mTruconnectHandler.getLastWritePacket().contentEquals(data))
                {
                    mTruconnectCallbacks.onDataWritten(data);

                    String nextPacket = mTruconnectHandler.getNextWriteDataPacket();
                    if(!nextPacket.contentEquals(""))
                    {
                        mBLEHandler.writeData(deviceName, nextPacket);
                    }
                }
                else
                {
                    mTruconnectCallbacks.onError(TruconnectErrorCode.WRITE_FAILED);
                    mTruconnectHandler.onError();
                }
            }

            @Override
            public void onModeChanged(String deviceName, int mode)
            {
                mTruconnectHandler.setCurrentMode(mode);
                mTruconnectCallbacks.onModeWritten(mode);
            }

            @Override
            public void onModeRead(String deviceName, int mode)
            {
                mTruconnectHandler.setCurrentMode(mode);
                mTruconnectCallbacks.onModeRead(mode);
            }

            @Override
            public void onError(String deviceName, BLEHandlerCallbacks.Error error)
            {
                TruconnectErrorCode truconnectErrorCode = TruconnectErrorCode.INTERNAL_ERROR;

                switch (error)
                {
                    case DISCONNECT_WITHOUT_REQUEST:
                    case CONNECT_WITHOUT_REQUEST:
                        truconnectErrorCode = TruconnectErrorCode.DEVICE_ERROR;
                        break;

                    case INVALID_MODE:
                        truconnectErrorCode = TruconnectErrorCode.INTERNAL_ERROR;
                        break;

                    case NO_TX_CHARACTERISTIC:
                    case NO_RX_CHARACTERISTIC:
                    case NO_MODE_CHARACTERISTIC:
                        truconnectErrorCode = TruconnectErrorCode.DEVICE_ERROR;
                        break;

                    case NO_CONNECTION_FOUND:
                        truconnectErrorCode = TruconnectErrorCode.NO_CONNECTION_FOUND;
                        break;

                    case NULL_GATT_ON_CALLBACK:
                    case NULL_CHAR_ON_CALLBACK:
                        truconnectErrorCode = TruconnectErrorCode.SYSTEM_ERROR;
                        break;

                    case DATA_WRITE_FAILED:
                        truconnectErrorCode = TruconnectErrorCode.WRITE_FAILED;
                        mTruconnectHandler.clearWriteBuffer();
                        break;

                    case DATA_READ_FAILED:
                        truconnectErrorCode = TruconnectErrorCode.READ_FAILED;
                        mTruconnectHandler.clearReadBuffer();
                        break;
                }

                mTruconnectCallbacks.onError(truconnectErrorCode);
                mTruconnectHandler.onError();
            }
        };
    }

    public BLEHandlerCallbacks getBLECallbacks()
    {
        return mBLECallbacks;
    }
}

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

public class BLEHandlerCallbacks
{
    private BLEHandler mBLEHandler;

    public static enum Result
    {
        SUCCESS,
        CONNECT_FAILURE,
        SERVICE_DISC_ERROR
    }

    public static enum Error
    {
        CONNECT_WITHOUT_REQUEST,
        DISCONNECT_WITHOUT_REQUEST,
        INVALID_MODE,
        NO_TX_CHARACTERISTIC,
        NO_RX_CHARACTERISTIC,
        NO_MODE_CHARACTERISTIC,
        NO_CONNECTION_FOUND,
        NULL_GATT_ON_CALLBACK,
        NULL_CHAR_ON_CALLBACK,
        DATA_READ_FAILED,
        DATA_WRITE_FAILED,
        MODE_READ_FAILED,
        MODE_WRITE_FAILED,
        SET_RX_NOTIFY_FAILED,
        SERVICE_DISCOVERY_FAILED
    }

    public BLEHandlerCallbacks(BLEHandler handler)
    {
        mBLEHandler = handler;
    }

    public void setHandler(BLEHandler handler)
    {
        mBLEHandler = handler;
    }

    public void onScanResult(String deviceName)
    {

    }

    public void onConnect(String deviceName)
    {

    }

    public void onConnectFailed(String deviceName, Result result)
    {

    }

    public void onDisconnect(String deviceName)
    {

    }

    public void onDisconnectFailed(String deviceName)
    {

    }

    public void onDataRead(String deviceName, String data)
    {

    }

    public void onDataWrite(String deviceName, String data)
    {

    }

    public void onModeChanged(String deviceName, int mode)
    {

    }

    public void onModeRead(String deviceName, int mode)
    {

    }

    public void onError(String deviceName, Error error)
    {

    }
}

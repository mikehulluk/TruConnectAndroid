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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public class BLEConnection
{
    private BluetoothGatt mGatt;
    private BluetoothGattService mService;
    private BluetoothDevice mDevice;

    private BluetoothGattCharacteristic mTxCharacteristic;
    private BluetoothGattCharacteristic mRxCharacteristic;
    private BluetoothGattCharacteristic mModeCharacteristic;

    private final int MODE_OFFSET = 0;

    public enum Mode
    {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    };

    private Mode mMode;

    public BLEConnection()
    {
        mMode = Mode.DISCONNECTED;
    }

    public String toString()
    {
        return mDevice.getName();
    }

    public void close()
    {
        if(mGatt != null)
        {
            mGatt.close();
        }
    }

    public void disconnect()
    {
        if(mGatt != null)
        {
            mGatt.disconnect();
        }
    }

    public BluetoothGatt getGatt()
    {
        return mGatt;
    }

    public void setGatt(BluetoothGatt mGatt)
    {
        this.mGatt = mGatt;
    }

    public BluetoothGattService getService()
    {
        return mService;
    }

    public void setService(BluetoothGattService mService)
    {
        this.mService = mService;
    }

    public BluetoothDevice getDevice()
    {
        return mDevice;
    }

    public void setDevice(BluetoothDevice mDevice)
    {
        this.mDevice = mDevice;
    }

    public Mode getMode()
    {
        return mMode;
    }

    public void setMode(Mode mode)
    {
        mMode = mode;
    }

    protected void setTxCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        mTxCharacteristic = characteristic;
    }

    protected void setRxCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        mRxCharacteristic = characteristic;
    }

    protected void setModeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        mModeCharacteristic = characteristic;
    }

    public boolean readModeCharacteristic()
    {
        return mGatt.readCharacteristic(mModeCharacteristic);
    }

    public boolean writeModeCharacteristic(int mode)
    {
        mModeCharacteristic.setValue(mode, BluetoothGattCharacteristic.FORMAT_UINT8, MODE_OFFSET);
        return mGatt.writeCharacteristic(mModeCharacteristic);
    }

    public boolean readTxCharacteristic()
    {
        return mGatt.readCharacteristic(mTxCharacteristic);
    }

    public boolean writeRxCharacteristic(String data)
    {
        mRxCharacteristic.setValue(data);
        return mGatt.writeCharacteristic(mRxCharacteristic);
    }

    public boolean setNotifyOnDataReady(boolean enable)
    {
        mGatt.setCharacteristicNotification(mTxCharacteristic, enable);

        BluetoothGattDescriptor charNotifyDescriptor =
                new BluetoothGattDescriptor(BluetoothCallbackHandler.CLIENT_CHAR_CONFIG_UUID,
                                            BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED);
        mTxCharacteristic.addDescriptor(charNotifyDescriptor);

        if(enable)
        {
            charNotifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        else
        {
            charNotifyDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mGatt.writeDescriptor(charNotifyDescriptor);
    }

    public boolean discoverServices()
    {
        return mGatt.discoverServices();
    }
}

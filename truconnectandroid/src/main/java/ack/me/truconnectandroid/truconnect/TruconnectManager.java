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

import java.util.UUID;

import ack.me.truconnectandroid.BLE.BLECommandQueue;
import ack.me.truconnectandroid.BLE.BLEConnection;
import ack.me.truconnectandroid.BLE.BLEDevice;
import ack.me.truconnectandroid.BLE.BLEDeviceList;
import ack.me.truconnectandroid.BLE.BLEHandler;
import ack.me.truconnectandroid.BLE.SearchableList;

public class TruconnectManager
{
    public static final int GPIO_MIN = 0;
    public static final int GPIO_MAX = 14;
    public static final int GPIO_VAL_MIN = 0;
    public static final int GPIO_VAL_MAX = 1;

    public static final int MODE_STREAM = TruconnectHandler.MODE_STREAM;
    public static final int MODE_COMMAND_LOCAL = TruconnectHandler.MODE_COMMAND_LOCAL;
    public static final int MODE_COMMAND_REMOTE = TruconnectHandler.MODE_COMMAND_REMOTE;

    private TruconnectHandler mTruconnectHandler;

    public TruconnectManager()
    {
        mTruconnectHandler = new TruconnectHandler();
    }

    public boolean init(Context context, TruconnectCallbacks callbacks)
    {
        SearchableList<BLEConnection> connectionList = new SearchableList<BLEConnection>();
        BLEDeviceList deviceList = new BLEDeviceList(new SearchableList<BLEDevice>());
        BLECommandQueue queue = new BLECommandQueue();
        BLEHandler bleHandler = new BLEHandler(deviceList, connectionList, queue);

        return mTruconnectHandler.init(context, bleHandler, callbacks);
    }

    public void deinit()
    {
        if (mTruconnectHandler != null)
        {
            mTruconnectHandler.deinit();
            mTruconnectHandler = null;
        }
    }

    public boolean startScan()
    {
        return mTruconnectHandler.startScan();
    }

    public boolean stopScan()
    {
        return mTruconnectHandler.stopScan();
    }

    public boolean connect(String deviceName)
    {
        return mTruconnectHandler.connect(deviceName);
    }

    public boolean disconnect()
    {
        return mTruconnectHandler.disconnect();
    }

    public boolean isConnected()
    {
        return mTruconnectHandler.isConnected();
    }

    public boolean isInitialised()
    {
        return mTruconnectHandler.isInitialised();
    }

    public boolean setMode(int mode)
    {
        return mTruconnectHandler.setMode(mode);
    }

    public boolean getMode()
    {
        return mTruconnectHandler.getMode();
    }

    public boolean adc(int gpio)
    {
        if (isGPIOValid(gpio))
        {
            mTruconnectHandler.sendCommand(TruconnectCommand.ADC, gpioToString(gpio));
        }
        return true;
    }

    //Cant uses these when connected to phone, no point implementing them
    //adv
    //con
    //dct
    //rbmode
    //scan (not very useful)

    public boolean beep(int duration)
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.BEEP, String.valueOf(duration));
    }

    public boolean factoryReset(String BLEAddress)
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.FACTORY_RESET, BLEAddress);
    }

    public boolean GPIOFunctionSet(int gpio, TruconnectGPIOFunction func)
    {
        boolean result = false;

        if (isGPIOValid(gpio))
        {
            String args = makeGPIOArgString(gpio, func.toString());
            result = mTruconnectHandler.sendCommand(TruconnectCommand.GPIO_FUNCTION, args);
        }

        return result;
    }

    public boolean GPIODirectionSet(int gpio, TruconnectGPIODirection dir)
    {
        boolean result = false;

        if (isGPIOValid(gpio))
        {
            String args = makeGPIOArgString(gpio, dir.toString());
            result = mTruconnectHandler.sendCommand(TruconnectCommand.GPIO_DIRECTION, args);
        }

        return result;
    }

    public boolean GPIOGet(int gpio)
    {
        boolean result = false;

        if (isGPIOValid(gpio))
        {
            String args = gpioToString(gpio);
            result = mTruconnectHandler.sendCommand(TruconnectCommand.GPIO_GET, args);
        }

        return result;
    }

    public boolean GPIOSet(int gpio, int value)
    {
        boolean result = false;

        if (isGPIOValid(gpio) && isGPIOValueValid(value))
        {
            String args = makeGPIOArgString(gpio, String.valueOf(value));
            result = mTruconnectHandler.sendCommand(TruconnectCommand.GPIO_SET, args);
        }

        return result;
    }

    public boolean getBluetoothAddress()
    {
        String variable = TruconnectVariable.BLUETOOTH_ADDRESS.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothConnectionCount()
    {
        String variable = TruconnectVariable.BLUETOOTH_CON_COUNT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothServiceUUID()
    {
        String variable = TruconnectVariable.BLUETOOTH_SERVICE_UUID.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothTxPowerAdv()
    {
        String variable = TruconnectVariable.BLUETOOTH_TX_POWER_ADV.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothTxPowerCon()
    {
        String variable = TruconnectVariable.BLUETOOTH_TX_POWER_CON.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothAdvMode()
    {
        String variable = TruconnectVariable.BLUETOOTH_ADV_MODE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothAdvHighDur()
    {
        String variable = TruconnectVariable.BLUETOOTH_ADV_HIGH_DUR.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothAdvHighInt()
    {
        String variable = TruconnectVariable.BLUETOOTH_ADV_HIGH_INT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothAdvLowDur()
    {
        String variable = TruconnectVariable.BLUETOOTH_ADV_LOW_DUR.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBluetoothAdvLowInt()
    {
        String variable = TruconnectVariable.BLUETOOTH_ADV_LOW_INT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBusInitMode()
    {
        String variable = TruconnectVariable.BUS_INIT_MODE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getBusSerialControl()
    {
        String variable = TruconnectVariable.BUS_SERIAL_CONTROL.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralConCount()
    {
        String variable = TruconnectVariable.CENTRAL_CON_COUNT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralConMode()
    {
        String variable = TruconnectVariable.CENTRAL_CON_MODE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralScanHighDur()
    {
        String variable = TruconnectVariable.CENTRAL_SCAN_HIGH_DUR.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralScanHighInt()
    {
        String variable = TruconnectVariable.CENTRAL_SCAN_HIGH_INT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralScanLowDur()
    {
        String variable = TruconnectVariable.CENTRAL_SCAN_LOW_DUR.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralScanLowInt()
    {
        String variable = TruconnectVariable.CENTRAL_SCAN_LOW_INT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getCentralScanMode()
    {
        String variable = TruconnectVariable.CENTRAL_SCAN_MODE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemActivityTimeout()
    {
        String variable = TruconnectVariable.SYSTEM_ACTIVITY_TIMEOUT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemBoardName()
    {
        String variable = TruconnectVariable.SYSTEM_BOARD_NAME.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemCommandEcho()
    {
        String variable = TruconnectVariable.SYSTEM_COMMAND_ECHO.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemCommandHeader()
    {
        String variable = TruconnectVariable.SYSTEM_COMMAND_HEADER.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemCommandPrompt()
    {
        String variable = TruconnectVariable.SYSTEM_COMMAND_PROMPT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemDeviceName()
    {
        String variable = TruconnectVariable.SYSTEM_DEVICE_NAME.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemIndicatorStatus()
    {
        String variable = TruconnectVariable.SYSTEM_INDICATOR_STATUS.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemOTAEnable()
    {
        String variable = TruconnectVariable.SYSTEM_OTA_ENABLE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemPrintLevel()
    {
        String variable = TruconnectVariable.SYSTEM_PRINT_LEVEL.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemRemoteEnable()
    {
        String variable = TruconnectVariable.SYSTEM_REMOTE_COMMAND_ENABLE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemGoToSleepTimeout()
    {
        String variable = TruconnectVariable.SYSTEM_GO_TO_SLEEP_TIMEOUT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getSystemUUID()
    {
        String variable = TruconnectVariable.SYSTEM_UUID.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getVersion()
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.VERSION, "");
    }

    public boolean getSystemWakeUpTimeout()
    {
        String variable = TruconnectVariable.SYSTEM_WAKE_UP_TIMEOUT.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getUARTBaudRate()
    {
        String variable = TruconnectVariable.UART_BAUD_RATE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getUARTFlowControl()
    {
        String variable = TruconnectVariable.UART_FLOW_CONTROL.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean getUserVariable()
    {
        String variable = TruconnectVariable.USER_VARIABLE.toString();
        return mTruconnectHandler.sendCommand(TruconnectCommand.GET, variable);
    }

    public boolean pwmStart(int gpio, float dutyCycle, int frequency)
    {
        boolean result = false;

        int highCount = calcPWMHighCount(dutyCycle, frequency);
        int lowCount = calcPWMCount(dutyCycle, frequency);

        return startPWM(gpio, highCount, lowCount);
    }

    public boolean pwmStop(int gpio)
    {
        boolean result = false;

        if (isGPIOValid(gpio))
        {
            String args = String.format("%d stop", gpio);
            result = mTruconnectHandler.sendCommand(TruconnectCommand.PWM, args);
        }

        return result;
    }

    public boolean reboot()
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.REBOOT, "");
    }

    public boolean save()
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.SAVE, "");
    }

    public boolean setBluetoothServiceUUID(UUID uuid)
    {
        return setBluetoothServiceUUID(uuid.toString());
    }

    public boolean setBluetoothServiceUUID(String uuid)
    {
        String argString = String.format("%s %s", TruconnectVariable.BLUETOOTH_SERVICE_UUID, uuid);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBluetoothTxPowerAdv(int power)
    {
        String argString = String.format("%s %d", TruconnectVariable.BLUETOOTH_TX_POWER_ADV, power);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBluetoothTxPowerCon(int power)
    {
        String argString = String.format("%s %d", TruconnectVariable.BLUETOOTH_TX_POWER_CON, power);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBluetoothAdvHighDur(int dur)
    {
        String argString = String.format("%s %d", TruconnectVariable.BLUETOOTH_ADV_HIGH_DUR, dur);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBluetoothAdvHighInt(int interval)
    {
        String argString = String.format("%s %d",
                                         TruconnectVariable.BLUETOOTH_ADV_HIGH_INT, interval);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBluetoothAdvLowDur(int dur)
    {
        String argString = String.format("%s %d", TruconnectVariable.BLUETOOTH_ADV_LOW_DUR, dur);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBluetoothAdvLowInt(int interval)
    {
        String argString = String.format("%s %d",
                                         TruconnectVariable.BLUETOOTH_ADV_LOW_INT, interval);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBusInitMode(TruconnectBusInitMode mode)
    {
        String argString = String.format("%s %s", TruconnectVariable.BUS_INIT_MODE, mode);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setBusSerialControl(TruconnectSerialControl control)
    {
        String argString = String.format("%s %s", TruconnectVariable.BUS_INIT_MODE, control);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setCentralScanHighDur(int dur)
    {
        String argString = String.format("%s %d", TruconnectVariable.CENTRAL_SCAN_HIGH_DUR, dur);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setCentralScanHighInt(int interval)
    {
        String argString = String.format("%s %d",
                                         TruconnectVariable.CENTRAL_SCAN_HIGH_INT, interval);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setCentralScanLowDur(int dur)
    {
        String argString = String.format("%s %d", TruconnectVariable.CENTRAL_SCAN_LOW_DUR, dur);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setCentralScanLowInt(int interval)
    {
        String argString = String.format("%s %d", TruconnectVariable.CENTRAL_SCAN_LOW_INT, interval);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setActivityTimeout(int timeout)
    {
        String argString = String.format("%s %d",
                                         TruconnectVariable.SYSTEM_ACTIVITY_TIMEOUT, timeout);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemBoardName(String name)
    {
        String argString = String.format("%s %s", TruconnectVariable.SYSTEM_BOARD_NAME, name);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemCommandEcho(boolean enabled)
    {
        String enabled_arg = boolToStrArg(enabled);
        String argString = String.format("%s %s",
                                         TruconnectVariable.SYSTEM_COMMAND_ECHO, enabled_arg);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemCommandHeader(boolean enabled)
    {
        String enabled_arg = boolToStrArg(enabled);
        String argString = String.format("%s %s",
                                         TruconnectVariable.SYSTEM_COMMAND_HEADER, enabled_arg);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemCommandMode(TruconnectCommandMode mode)
    {
        String argString = String.format("%s %s", TruconnectVariable.SYSTEM_COMMAND_MODE, mode);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemCommandPrompt(boolean enabled)
    {
        String enabled_arg = boolToStrArg(enabled);
        String argString = String.format("%s %s",
                                         TruconnectVariable.SYSTEM_COMMAND_PROMPT, enabled_arg);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemDeviceName(String name)
    {
        String argString = String.format("%s %s", TruconnectVariable.SYSTEM_DEVICE_NAME, name);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    /* Set the blink rate and duty cycle for the status LED when disconnected and connected. */
    public boolean setSystemIndicatorBlinkRate(float dutyDiscon, float periodDiscon, float dutyCon,
                                               float periodCon)
    {
        int conHighCount = calcStatusLedHighCount(dutyCon, periodCon);
        int conLowCount = calcStatusLedLowCount(dutyCon, periodCon);
        int disconHighCount = calcStatusLedHighCount(dutyDiscon, periodDiscon);
        int disconLowCount = calcStatusLedLowCount(dutyDiscon, periodDiscon);

        String argString = String.format("%s %02X%02X%02X%02X",
                                         TruconnectVariable.SYSTEM_INDICATOR_STATUS,
                                         disconLowCount, disconHighCount,
                                         conLowCount, conHighCount);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemOTAEnable(boolean enabled)
    {
        String enabled_arg = boolToStrArg(enabled);
        String argString = String.format("%s %s", TruconnectVariable.SYSTEM_OTA_ENABLE, enabled_arg);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemPrintLevel(TruconnectPrintLevel level)
    {
        String argString = String.format("%s %s", TruconnectVariable.SYSTEM_PRINT_LEVEL, level);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemRemoteCommandEnable(boolean enabled)
    {
        String enabled_arg = boolToStrArg(enabled);
        String argString = String.format("%s %s",
                                         TruconnectVariable.SYSTEM_REMOTE_COMMAND_ENABLE,
                                         enabled_arg);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemGoToSleepTimeout(int timeout)
    {
        String argString = String.format("%s %d",
                                         TruconnectVariable.SYSTEM_GO_TO_SLEEP_TIMEOUT, timeout);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setSystemGoWakeUpTimeout(int timeout)
    {
        String argString = String.format("%s %d",
                                         TruconnectVariable.SYSTEM_WAKE_UP_TIMEOUT, timeout);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setUARTBaudRate(TruconnectBaudRate baud)
    {
        String argString = String.format("%s %s", TruconnectVariable.SYSTEM_PRINT_LEVEL, baud);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setUARTFlowControl(boolean enabled)
    {
        String enabled_arg = boolToStrArg(enabled);
        String argString = String.format("%s %s", TruconnectVariable.UART_FLOW_CONTROL, enabled_arg);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean setUserVariable(String value)
    {
        String argString = String.format("%s %s", TruconnectVariable.USER_VARIABLE, value);
        return mTruconnectHandler.sendCommand(TruconnectCommand.SET, argString);
    }

    public boolean sleep()
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.SLEEP, "");
    }

    public boolean streamMode()
    {
        return mTruconnectHandler.sendCommand(TruconnectCommand.STREAM_MODE, "");
    }

    private String gpioToString(int gpio)
    {
        return String.valueOf(gpio);
    }

    private boolean isGPIOValueValid(int value)
    {
        return (value >= GPIO_VAL_MIN && value <= GPIO_VAL_MAX);
    }

    private boolean isGPIOValid(int gpio)
    {
        return (gpio >= GPIO_MIN && gpio <= GPIO_MAX);
    }

    private String makeGPIOArgString(int gpio, String arg)
    {
        return String.format("%d %s", gpio, arg);
    }

    private String boolToStrArg(boolean arg)
    {
        String strArg;

        if (arg)
        {
            strArg = "1";
        }
        else
        {
            strArg = "0";
        }

        return strArg;
    }

    private boolean startPWM(int gpio, int highCount, int lowCount)
    {
        boolean result = false;

        if (isGPIOValid(gpio))
        {
            String args = String.format("%d %d %d", gpio, highCount, lowCount);
            result = mTruconnectHandler.sendCommand(TruconnectCommand.PWM, args);
        }

        return result;
    }

    private int calcPWMHighCount(float dutyCycle, int frequency)
    {
        return calcPWMCount(dutyCycle, frequency);
    }

    private int calcPWMLowCount(float dutyCycle, int frequency)
    {
        float inverseDuty = 1.0f - dutyCycle;
        return calcPWMCount(inverseDuty, frequency);
    }

    private int calcPWMCount(float dutyCycle, int frequency)
    {
        final float PWM_CONSTANT = 131072.0f;

        return (int)((PWM_CONSTANT * dutyCycle) / (float)frequency);
    }

    private int calcStatusLedLowCount(float dutyCycle, float period)
    {
        float inverseDuty = 1.0f - dutyCycle;
        return calcStatusLedCount(inverseDuty, period);
    }

    private int calcStatusLedHighCount(float dutyCycle, float period)
    {
        return calcStatusLedCount(dutyCycle, period);
    }

    private int calcStatusLedCount(float dutyCycle, float period)
    {
        final float PWM_CONSTANT = 8.0f;

        return (int)(PWM_CONSTANT * dutyCycle * period);
    }
}

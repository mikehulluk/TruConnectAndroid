package ack.me.truconnectandroid.truconnect;

import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ack.me.truconnectandroid.BLE.BLEHandler;
import ack.me.truconnectandroid.BLE.BLEHandlerCallbacks;


/**
 * Created by mitchell on 16/03/15.
 */
public class TruconnectHandler
{
    public static final int COMMAND_STRING_LEN_MAX = 127;
    public static final int PACKET_SIZE_MAX = 20;
    public static final int MODE_UNKNOWN = 0;
    public static final int MODE_STREAM = 1;
    public static final int MODE_COMMAND_LOCAL = 2;
    public static final int MODE_COMMAND_REMOTE = 3;

    private static final String TAG = "TruconnectHandler";

    private BLECallbackHandler mBLECallbacks;
    private BLEHandler mBLEHandler;

    private boolean mConnected = false;
    private String mDeviceName;

    private TruconnectCallbacks mCallbacks;

    private String mWriteBuffer = "";
    private String mLastWritePacket = "";

    private int mCurrentMode = MODE_UNKNOWN;

    private TruconnectCommandQueue mCommandQueue;
    private boolean mCommandInProgress = false;
    private TruconnectCommand mCurrentCommand;

    private boolean mIsInitialised = false;

    private ResponseParser mResponseParser;

    public TruconnectHandler()
    {
        mResponseParser = new ResponseParser();
    }

    public boolean init(Context context, BLEHandler handler, TruconnectCallbacks callbacks)
    {
        boolean result = false;

        if (handler != null && callbacks != null)
        {
            mBLEHandler = handler;
            mCallbacks = callbacks;
            mCommandQueue = new TruconnectCommandQueue();

            initCallbacks();
            if (handler.init(context, mBLECallbacks.getBLECallbacks()) && handler.isBLEEnabled())
            {
                result = true;
                mConnected = false;
                mIsInitialised = true;
            }
        }

        return result;
    }

    public void deinit()
    {
        if (mBLEHandler != null)
        {
            mBLEHandler.deinit();
            mIsInitialised = false;
        }
    }

    public boolean startScan()
    {
        boolean result = false;

        if (mBLEHandler != null)
        {
            result = mBLEHandler.startBLEScan();
        }

        return result;
    }

    public boolean stopScan()
    {
        boolean result = false;

        if (mBLEHandler != null)
        {
            result = mBLEHandler.stopBLEScan();
        }

        return result;
    }

    public boolean connect(String deviceName)
    {
        boolean result = false;

        if (mBLEHandler != null && !mConnected)
        {
//            Log.d(TAG, "Connecting to " + deviceName);
            result = mBLEHandler.connect(deviceName);
            mConnected = result;
            mDeviceName = deviceName;
        }

        return result;
    }

    public boolean disconnect()
    {
        boolean result = false;

        if(mBLEHandler != null && mConnected)
        {
//            Log.d(TAG, "Disconnecting, clearing command queue");
            clearCommandQueue();
            result = mBLEHandler.disconnect(mDeviceName);
        }

        return result;
    }

    public boolean isConnected()
    {
        return mConnected;
    }

    public boolean isInitialised()
    {
        return mIsInitialised;
    }

    public boolean sendCommand(TruconnectCommand command, String args)
    {
        boolean result = false;

        if (command != null && args != null)
        {
            final String commandString = String.format("%s %s\r\n", command, args);
            if (commandString.length() <= COMMAND_STRING_LEN_MAX)
            {
                TruconnectCommandRequest req = new TruconnectCommandRequest(command, commandString);
                result = mCommandQueue.add(req);
                runNextCommand();//run next command if not currently running one
            }
        }

        return result;
    }

    public boolean setMode(int mode)
    {
        return mBLEHandler.writeMode(mDeviceName, mode);
    }

    public boolean getMode()
    {
        return mBLEHandler.readMode(mDeviceName);
    }

    private void initCallbacks()
    {
        mBLECallbacks = new BLECallbackHandler(mBLEHandler, this);
    }

    private synchronized void runNextCommand()
    {
        if (!mCommandInProgress)
        {
            TruconnectCommandRequest req = mCommandQueue.getNext();
            if (req != null)
            {
//                Log.d(TAG, "Running next command");
                mCommandInProgress = true;

                mCurrentCommand = req.getCommand();
                mLastWritePacket = req.getCommandString();
                mBLEHandler.writeData(mDeviceName, req.getCommandString());
            }
        }
    }

    protected TruconnectCallbacks getCallbacks()
    {
        return mCallbacks;
    }

    protected void setConnectedStatus(boolean status)
    {
        mConnected = status;
    }

    protected void onRead(String data)
    {
        mResponseParser.addToBuffer(data);
        parseResponse();
    }

    protected void onError()
    {
        Log.d(TAG, "onError, aborting current command");
        mCommandInProgress = false;
        mCurrentCommand = null;//throw away response, if we get one
    }

    protected String getNextWriteDataPacket()
    {
        int length = mWriteBuffer.length();
        String packet;

        if (length > PACKET_SIZE_MAX)
        {
            packet = mWriteBuffer.substring(0, PACKET_SIZE_MAX-1);
            mWriteBuffer = mWriteBuffer.substring(PACKET_SIZE_MAX);
        }
        else
        {
            packet = mWriteBuffer;
            clearWriteBuffer();
        }

        return packet;
    }

    protected String getLastWritePacket()
    {
        return mLastWritePacket;
    }

    protected void setCurrentMode(int mode)
    {
        mCurrentMode = mode;
    }

    protected void clearWriteBuffer()
    {
        mWriteBuffer = "";
    }

    protected void clearCommandQueue()
    {
        mCommandQueue.clear();
    }

    protected void clearReadBuffer()
    {
        mResponseParser.clearBuffer();
    }

    private void parseResponse()
    {
        TruconnectResult result = mResponseParser.parseResponse();

        if (result != null)
        {
            if (result.getResponseCode() == TruconnectResult.INCOMPLETE_RESPONSE)
            {
                mCallbacks.onError(TruconnectErrorCode.INCOMPLETE_RESPONSE);
                Log.d(TAG, "Error, incomplete response");
            }
            else if (mCurrentCommand != null)
            {
                mCallbacks.onCommandResult(mCurrentCommand, result);
                Log.d(TAG, "Command complete");
            }
            else
            {
                Log.d(TAG, "Command aborted");
            }
            mCommandInProgress = false;
            runNextCommand();
        }
    }
}

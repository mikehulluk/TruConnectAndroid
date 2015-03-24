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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BLEDeviceListTest
{
    private static final int VALID_INDEX = 0;
    private static final String VALID_NAME = "test";

    private SearchableList<BLEDevice> mMockList;
    private BLEDeviceList mList;
    private BLEDevice mDevice;
    private BluetoothDevice mMockBluetoothDevice;

    @Before
    public void setUp() throws Exception
    {
        mMockList = mock(SearchableList.class);
        mList = new BLEDeviceList(mMockList);
        mMockBluetoothDevice = mock(BluetoothDevice.class);
        mDevice = new BLEDevice(mMockBluetoothDevice);

        when(mMockList.add(any(BLEDevice.class))).thenReturn(true);
        when(mMockList.get(any(int.class))).thenReturn(mDevice);
        when(mMockList.size()).thenReturn(1);

        when(mMockBluetoothDevice.getName()).thenReturn(VALID_NAME);
        when(mMockList.get(VALID_NAME)).thenReturn(null);
    }

    @Test
    public void testAdd_returnsFalseOnNullObject() throws Exception
    {
        assertFalse(mList.add(null));
    }

    @Test
    public void testAdd_returnsTrueOnSuccess() throws Exception
    {
        assertTrue(mList.add(mDevice));
    }

    @Test
    public void testAdd_callsAdd()
    {
        mList.add(mDevice);
        verify(mMockList, times(1)).add(mDevice);
    }

    @Test
    public void testAdd_doesntAddDuplicates()
    {
        when(mMockList.get(VALID_NAME)).thenReturn(mDevice);
        mList.add(mDevice);
        verify(mMockList, never()).add(mDevice);
    }

    @Test
    public void testAdd_returnsFalseOnAddError()
    {
        when(mMockList.add(any(BLEDevice.class))).thenReturn(false);
        assertFalse(mList.add(mDevice));
    }

    @Test
    public void testGet_returnsNullOnInvalidIndex() throws Exception
    {
        assertNull(mList.get((int)-1));
    }

    @Test
    public void testGet_callsGetOnList() throws Exception
    {
        mList.add(mDevice);
        mList.get(VALID_INDEX);
        verify(mMockList).get(VALID_INDEX);
    }

    @Test
    public void testGet_returnsCorrectObject()
    {
        mList.add(mDevice);
        assertEquals(mDevice, mList.get(VALID_INDEX));
    }

    @Test
    public void testGet_returnsNullOnEmptyList()
    {
        when(mMockList.size()).thenReturn(0);

        assertNull(mList.get(VALID_INDEX));
    }

    @Test
    public void testGetStr_returnsNullOnEmptyList()
    {
        when(mMockList.size()).thenReturn(0);

        assertNull(mList.get(VALID_NAME));
    }

    @Test
    public void testGetStr_returnsNullOnEmptyString() throws Exception
    {
        assertNull(mList.get(""));
    }

    @Test
    public void testGetStr_searchesAndReturnsCorrectDevice() throws Exception
    {
        final String DEV_NAME1 = "device1";
        final String DEV_NAME2 = "device2";
        BLEDevice mockDevice1 = mock(BLEDevice.class);
        BLEDevice mockDevice2 = mock(BLEDevice.class);

        when(mMockList.size()).thenReturn(2);

        when(mockDevice1.getName()).thenReturn(DEV_NAME1);
        when(mockDevice2.getName()).thenReturn(DEV_NAME2);

        when(mMockList.get(DEV_NAME1)).thenReturn(mockDevice1);
        when(mMockList.get(DEV_NAME2)).thenReturn(mockDevice2);

        assertEquals(mockDevice2, mList.get(DEV_NAME2));
    }

    @Test
    public void testClear_callsClear() throws Exception
    {
        mList.clear();

        verify(mMockList).clear();
    }
}
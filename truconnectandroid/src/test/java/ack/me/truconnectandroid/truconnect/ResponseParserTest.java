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

import static org.junit.Assert.*;

public class ResponseParserTest
{
    ResponseParser mParser;

    @Before
    public void setUp() throws Exception
    {
        mParser = new ResponseParser();
    }

    @Test
    public void test_returnsCorrectResponseWithFullResponse() throws Exception
    {
        final String CORRECT_RESPONSE = "1173";
        final String FULL_RESPONSE = "R000006\r\n" + CORRECT_RESPONSE + "\r\n";

        mParser.addToBuffer(FULL_RESPONSE);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(TruconnectResult.SUCCESS, result.getResponseCode());
        assertEquals(CORRECT_RESPONSE, result.getData());
    }

    @Test
    public void test_stripsControlCharacters() throws Exception
    {
        final String CORRECT_RESPONSE = "1173";
        final String FULL_RESPONSE = "\u0000R000006\r\n\u0000" + CORRECT_RESPONSE + "\r\n";

        mParser.addToBuffer(FULL_RESPONSE);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(TruconnectResult.SUCCESS, result.getResponseCode());
        assertEquals(CORRECT_RESPONSE, result.getData());
    }

    @Test
    public void test_returnsCorrectResponseWithChunkedData() throws Exception
    {
        final String CORRECT_RESPONSE = "1173";
        final String CHUNK_1 = "R000";
        final String CHUNK_2 = "006\r\n";
        final String CHUNK_3 = CORRECT_RESPONSE.substring(0, 2);
        final String CHUNK_4 = CORRECT_RESPONSE.substring(2) + "\r\n";


        mParser.addToBuffer(CHUNK_1);
        mParser.parseResponse();
        mParser.addToBuffer(CHUNK_2);
        mParser.parseResponse();
        mParser.addToBuffer(CHUNK_3);
        mParser.parseResponse();
        mParser.addToBuffer(CHUNK_4);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(TruconnectResult.SUCCESS, result.getResponseCode());
        assertEquals(CORRECT_RESPONSE, result.getData());
    }

    @Test
    public void test_handlesExcessData() throws Exception
    {
        final String CORRECT_RESPONSE = "1173";
        final String EXCESS_DATA = "ntahaoeuhn\r\noeu";
        final String FULL_RESPONSE = "R000006\r\n" + CORRECT_RESPONSE + "\r\n" + EXCESS_DATA;

        mParser.addToBuffer(FULL_RESPONSE);
        mParser.parseResponse();
        mParser.addToBuffer(FULL_RESPONSE);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(TruconnectResult.SUCCESS, result.getResponseCode());
        assertEquals(CORRECT_RESPONSE, result.getData());
    }

    @Test
    public void test_handlesDataBeforeResponse() throws Exception
    {
        final String CORRECT_RESPONSE = "1173";
        final String EXCESS_DATA = "ntahaoeuhn\r\noeu";
        final String FULL_RESPONSE = EXCESS_DATA + "R000006\r\n" + CORRECT_RESPONSE + "\r\n";

        mParser.addToBuffer(FULL_RESPONSE);
        mParser.parseResponse();
        mParser.addToBuffer(FULL_RESPONSE);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(TruconnectResult.SUCCESS, result.getResponseCode());
        assertEquals(CORRECT_RESPONSE, result.getData());
    }

    @Test
    public void test_callsOnErrorOnTooFewBytes() throws Exception
    {
        final String INCOMPLETE_RESPONSE = "1173";
        final String FULL_RESPONSE = "R000010\r\n" + INCOMPLETE_RESPONSE + "\r\n";

        mParser.addToBuffer(FULL_RESPONSE);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(TruconnectResult.INCOMPLETE_RESPONSE, result.getResponseCode());
        assertEquals(INCOMPLETE_RESPONSE, result.getData());
    }

    @Test
    public void test_returnsCorrectResultCode() throws Exception
    {
        final int CORRECT_RESPONSE_CODE = TruconnectResult.FAILED;
        final String CORRECT_RESPONSE = "Command failed";
        final String FULL_RESPONSE = "R" + String.valueOf(CORRECT_RESPONSE_CODE) + "00016\r\n" + CORRECT_RESPONSE + "\r\n";

        mParser.addToBuffer(FULL_RESPONSE);
        TruconnectResult result = mParser.parseResponse();

        assertEquals(CORRECT_RESPONSE_CODE, result.getResponseCode());
        assertEquals(CORRECT_RESPONSE, result.getData());
    }
}
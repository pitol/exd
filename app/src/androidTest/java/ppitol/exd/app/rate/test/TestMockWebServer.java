package ppitol.exd.app.rate.test;

import android.test.AndroidTestCase;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestMockWebServer extends AndroidTestCase {

    public void testSimpleGet() throws Exception {
        MockWebServer server = new MockWebServer();
        server.play();

        server.enqueue(new MockResponse().setResponseCode(200).setBody("this is a mock response"));

        URL url = new URL(server.getUrl("/"), "/somepath");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line = reader.readLine();
        assertEquals("this is a mock response", line);

        conn.disconnect();

        RecordedRequest rr = server.takeRequest();
        assertEquals("/somepath", rr.getPath());
        assertEquals("GET", rr.getMethod());

        server.shutdown();
    }
}

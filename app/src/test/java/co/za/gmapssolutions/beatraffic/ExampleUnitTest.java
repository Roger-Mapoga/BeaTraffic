package co.za.gmapssolutions.beatraffic;

import co.za.gmapssolutions.beatraffic.restClient.KafkaProducerRestClient;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
//@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
//    @Mock Future future;
//    @Mock ThreadPoolExecutor threadPoolExecutor;
//    @Mock KafkaProducerRestClient kafkaProducerRestClient;
//    @Mock HttpURLConnection urlConnection;
//    @Mock OutputStream os;
//
//    @Test
//    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
//    }
//
//    @Test
//    public void testHttpPost() throws IOException, ExecutionException, InterruptedException {
//        //beatTrafficLocation jsonInputString = new beatTrafficLocation();//"{id : 1, type : 'car',streetName : 'kutlwano', longitude : 21.0, latitude : 21.0}";
////
//        doReturn(future).when(threadPoolExecutor).submit(kafkaProducerRestClient);
//        threadPoolExecutor.submit(kafkaProducerRestClient);
//
//        assertNull(future.get());
////
//        threadPoolExecutor.shutdown();
//
//    }
//    @Test
//    public void testRestClientPost() throws IOException {
//        RestClient restClient = new RestClient(urlConnection);
//        byte[] input = "".getBytes(StandardCharsets.UTF_8);
//        int response = 200;
//        doReturn(os).when(urlConnection).getOutputStream();
//        doNothing().when(os).write(input,0,input.length);
//        doReturn(response).when(urlConnection).getResponseCode();
//        restClient.post("[{\"test\": \"testing\"}]");
//        assertEquals(200,response);
//    }
    @Test
    public void testRestClientGet(){
        try {
            URL url = new URL("http://192.168.8.102:8080/location");
            RestClient restClient = new RestClient(url);
            restClient.get();
        } catch (IOException e) {
            e.printStackTrace();
//            System.exit(1);
        }
    }
    @Test
    public void testRestClientPost(){
        try {
            URL url = new URL("http://192.168.8.102:8080/location");
            RestClient restClient = new RestClient(url);
            System.out.println(restClient.post("test"));
        } catch (IOException e) {
            e.printStackTrace();
//            System.exit(1);
        }
    }
}
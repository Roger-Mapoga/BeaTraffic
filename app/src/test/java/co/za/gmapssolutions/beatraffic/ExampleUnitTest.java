package co.za.gmapssolutions.beatraffic;

import co.za.gmapssolutions.beatraffic.restClient.KafkaProducerRestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
    @Mock Future future;
    @Mock ThreadPoolExecutor threadPoolExecutor;
    @Mock KafkaProducerRestClient kafkaProducerRestClient;

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testHttpPost() throws IOException, ExecutionException, InterruptedException {
        //beatTrafficLocation jsonInputString = new beatTrafficLocation();//"{id : 1, type : 'car',streetName : 'kutlwano', longitude : 21.0, latitude : 21.0}";
//
        doReturn(future).when(threadPoolExecutor).submit(kafkaProducerRestClient);
        threadPoolExecutor.submit(kafkaProducerRestClient);

        assertNull(future.get());
//
        threadPoolExecutor.shutdown();

    }
}
package util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TimeoutTest extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

    ExampleRunner.runJavaExample("vertx-core/src/test/java/", TimeoutTest.class, false);
  }

  @Override
  public void start() throws Exception {

    final HttpClientOptions httpClientOpts = new HttpClientOptions().setKeepAlive(true).setMaxPoolSize(10).setPipelining(true);//.setIdleTimeout(1);
    HttpClient client = vertx.createHttpClient(httpClientOpts);

    vertx.createHttpServer().requestHandler(request -> {
      String toAttr = request.getParam("t");
      if (toAttr != null) {

        // request that should timeout
        HttpClientRequest req = client.request(HttpMethod.GET, 8083, "localhost", "/", resp -> {


        });
        req.exceptionHandler(t -> {
          System.err.println("" + t.getMessage());

        });
        req.setTimeout(110);
        req.end();

        request.response().end("Timeout request");
      } else {

        // request that should not timeout
        HttpClientRequest req = client.request(HttpMethod.GET, 8083, "localhost", "/", resp -> {

        });
        req.exceptionHandler(t -> {
          System.err.println("" + t.getMessage());

        });
        req.setTimeout(3300);
        req.end();

        request.response().end("request");
      }

    }).listen(8080);



  }
}

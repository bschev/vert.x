package util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

import java.util.concurrent.TimeUnit;

public class FaultTolerantReqExample extends AbstractVerticle {

  private HttpClient client = null;

  private static int connClosedCount = 0;

  private final int CONN_CLOSED_LIMIT = 10;

  @Override
  public void start() throws Exception {

    initHttpClient();

    vertx.createHttpServer().requestHandler(this::handleRequest).listen(8080);

    vertx.setPeriodic(TimeUnit.MINUTES.toMillis(1), this::checkConnectionClose);
  }

  private void handleRequest(HttpServerRequest serverRequest) {

    if (connClosedCount <= CONN_CLOSED_LIMIT) {

      // request that may not response
      final HttpClientRequest req = client.request(HttpMethod.GET, 8083, "localhost", "/", resp -> {
        // successful response...
      });
      req.exceptionHandler(t -> {
        // ugly string comparison, not sure what other VertxExceptions can pop up here
        if ("Connection was closed".equals(t.getMessage()) && t instanceof VertxException) {
          connClosedCount++;
          System.out.println("connClosedCount: " + connClosedCount);
        }
      });
      req.setTimeout(200);
      req.end();

      serverRequest.response().end("successful response...");
    } else {
      System.out.println("no request created because of recent connection problems. connClosedCount: " + connClosedCount);
      // no request creation, because of recent connection problems.
      serverRequest.response().end("failed response...");
    }
  }

  private void checkConnectionClose(Long timerId) {

    System.out.println("checkConnectionClose connClosedCount: " + connClosedCount);
    if (connClosedCount > CONN_CLOSED_LIMIT) {
      System.out.println("re-initialise HttpClient connClosedCount: " + connClosedCount);
      // to many failed requests, re-initialise the HttpClient
      // (first tests shod that it makes no difference if the client is re-initialised or not)
//      closeHttpClient();
//      initHttpClient();
      connClosedCount = 0;
    }
  }

  private void initHttpClient() {
    final HttpClientOptions httpClientOpts = new HttpClientOptions().setKeepAlive(true).setMaxPoolSize(10).setPipelining(true).setIdleTimeout(1);
    client = vertx.createHttpClient(httpClientOpts);
  }

  private void closeHttpClient() {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public void stop() throws Exception {
    closeHttpClient();
  }

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

    ExampleRunner.runJavaExample("vertx-core/src/test/java/", FaultTolerantReqExample.class, false);
  }

}

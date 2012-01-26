package vertx.tests.busmods.persistor;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.JsonMessage;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.newtests.TestClientBase;

/**
 *
 * Most of the testing is done in JS since it's so much easier to play with JSON in JS rather than Java
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestClient extends TestClientBase {

  private EventBus eb = EventBus.instance;

  @Override
  public void start() {
    super.start();
    tu.appReady();
  }

  @Override
  public void stop() {
    super.stop();
  }

  public void testPersistor() throws Exception {

    //First delete everything
    JsonObject json = new JsonObject();
    json.putString("collection", "testcoll");
    json.putString("action", "delete");
    json.putObject("matcher", new JsonObject());

    eb.sendJson("test.persistor", json, new Handler<JsonMessage>() {
      public void handle(JsonMessage reply) {
        tu.azzert("ok".equals(reply.jsonObject.getString("status")));
      }
    });

    final int numDocs = 1;
    for (int i = 0; i < numDocs; i++) {
      JsonObject doc = new JsonObject();
      doc.putString("name", "joe bloggs");
      doc.putNumber("age", 40);
      doc.putString("cat-name", "watt");

      json = new JsonObject();
      json.putString("collection", "testcoll");
      json.putString("action", "save");
      json.putObject("document", doc);

      eb.sendJson("test.persistor", json, new Handler<JsonMessage>() {
        public void handle(JsonMessage reply) {
          tu.azzert("ok".equals(reply.jsonObject.getString("status")));
        }
      });
    }

    JsonObject matcher = new JsonObject();
    matcher.putString("name", "joe bloggs");

    json = new JsonObject();
    json.putString("collection", "testcoll");
    json.putString("action", "find");
    json.putObject("matcher", matcher);

    eb.sendJson("test.persistor", json, new Handler<JsonMessage>() {
      public void handle(JsonMessage reply) {
        tu.azzert("ok".equals(reply.jsonObject.getString("status")));
        JsonArray results = reply.jsonObject.getArray("results");
        tu.azzert(results.size() == numDocs);
        tu.testComplete();
      }
    });


  }

}

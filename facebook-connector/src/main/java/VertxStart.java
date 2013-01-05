import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;


public class VertxStart extends Verticle {

	@Override
	public void start() throws Exception {
		this.container.deployVerticle("de.orolle.vertx.facebook.FacebookConnector", new JsonObject().putString("address", "fb"), 1, new Handler<String>() {
			@Override
			public void handle(String event) {
				System.out.println(event);
				vertx.eventBus().send("fb", new JsonObject()
						.putString("action", "fql")
						.putString("access_token", "AAACEdEose0cBAHw3XGybMJ9RVXNE1cmGqMkW5P39ZADnKhpwzzawphCuHhqLjrlTLmp4VhvZCJZCQ0GetwGmMoUSZCaRvEarGa3eGeajc6viHMRimkgj")
						.putBoolean("SSL", false)
						.putObject("query", new JsonObject().putString("a", "SELECT name, uid FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())")), 
						new Handler<Message<JsonObject>>() {
							@Override
							public void handle(Message<JsonObject> event) {
								System.out.println(event.body.toString());
							}
						});
			}
		});
	
	}

}

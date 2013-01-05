package de.orolle.vertx.facebook;


import java.util.HashMap;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * facebook-connector BusMod allows to query facebooks api through facebook query language (fql).
 * 
 * @author orolle
 *
 */
public class FacebookConnector extends BusModBase {
	private static final String FACEBOOK_HOST = "graph.facebook.com";
	
	private String address;
	private boolean useSSL;

	protected void getFQL(final JsonObject query, final String accessToken, final Handler<JsonObject> handler) {		
		final String url = "/fql?q="+URIHelper.uri(query.toString())+"&method=GET&format=json&access_token="+accessToken;
		logger.debug("Sending FQL Request: " + url);
		
		HttpClientRequest fqlRequest = vertx.createHttpClient().setConnectTimeout(10000).setSSL(useSSL).setPort(useSSL? 443 : 80).setHost(FACEBOOK_HOST)
				.get(url, new Handler<HttpClientResponse>() {
					@Override
					public void handle(HttpClientResponse event) {
						event.bodyHandler(new Handler<Buffer>() {
							@Override
							public void handle(Buffer event) {
								String str = event.toString();
								JsonObject response = null;

								try{
									response = new JsonObject(str);
								}
								catch (Exception e) {
									try{
										response = new JsonObject("{\""+str+"\"}");
									}catch (Exception e1) {
									}
								}

								handler.handle(simplifyFQLResult(response));
							}
						});

						event.exceptionHandler(new Handler<Exception>() {
							@Override
							public void handle(Exception event) {
								event.printStackTrace();

								handler.handle(new JsonObject().putString("error", "query execution failed because of exception"));
							}
						});
					}
				});

		fqlRequest.exceptionHandler(new Handler<Exception>() {
			@Override
			public void handle(Exception event) {
				event.printStackTrace();
				handler.handle(new JsonObject().putString("error", "query execution failed because of exception"));
			}
		});

		fqlRequest.end();
	}
	
	@SuppressWarnings("unchecked")
	private JsonObject simplifyFQLResult(JsonObject fqlResultSet) {
		if(fqlResultSet.getObject("error") != null){
			return fqlResultSet;
		}
		
		Object[] objs = fqlResultSet.getArray("data").toArray();
		JsonObject result = new JsonObject();

		for (Object container : objs) {
			JsonObject jContainer = new JsonObject((HashMap<String, Object>) container);
			final String queryName = jContainer.getString("name");

			if(queryName!=null){
				Object obj = jContainer.getField("fql_result_set");
				JsonArray subResult = null;

				if (obj instanceof Object[]) {
					Object[] os = (Object[]) obj;
					subResult = new JsonArray(os);
				}else if (obj instanceof JsonArray) {
					JsonArray as = (JsonArray) obj;
					subResult = as;
				}

				if(subResult != null){
					result.putArray(queryName, subResult);
				}
			}
		}

		return result;
	}
	
	@Override
	public void start() {
		super.start();
		
		address = getOptionalStringConfig("address", "vertx.facebook-connector");
		useSSL = getOptionalBooleanConfig("SSL", true);
		
		eb.registerHandler(address, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				receiveMessage(event);
			}
		});
	}
	
	protected void receiveMessage(final Message<JsonObject> msg){
		final String action = msg.body.getString("action");
		final String accessToken = msg.body.getString("access_token");
		
		System.out.println(msg.body.toString());
		
		switch (action) {
		case "fql":
			this.getFQL(msg.body.getObject("query"), accessToken, new Handler<JsonObject>() {
				@Override
				public void handle(JsonObject event) {
					msg.reply(event);
				}
			});
			break;

		default:
			break;
		}
	}

}

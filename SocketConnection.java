package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

public class SocketConnection {

	public static void main(String[] args) {
		String url = "http://localhost:3001/api/bb/v1/login";
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", "abc@cf.co.uk"));
		pairs.add(new BasicNameValuePair("password", "abc123"));
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		
		post.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
		
		try {
			post.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpResponse response;
		
		try {
			response = client.execute(post);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		String rl, output = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		} catch (UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			while ((rl = br.readLine()) != null) {
				output += rl;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		JSONObject json;
		try {
			json = new JSONObject(output);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Boolean accessed;
		try {
			accessed = (boolean) json.get("accessed");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if (accessed) {
			Header[] header = response.getHeaders("set-cookie");
			String cookie_val = header[0].getValue();
			
			Socket socket;
			try {
				socket = IO.socket("http://localhost:3001");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					Transport transport = (Transport)args[0];
					
					transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
					    @Override
					    public void call(Object... args) {
					        @SuppressWarnings("unchecked")
					        Map<String, String> headers = (Map<String, String>)args[0];
					        // send cookies to server.
					        headers.put("cookie", cookie_val);
					    }
					}).on(Transport.EVENT_RESPONSE_HEADERS, new Emitter.Listener() {
					    @Override
					    public void call(Object... args) {
					        @SuppressWarnings("unchecked")
					        Map<String, String> headers = (Map<String, String>)args[0];
					        // get cookies from server.
					        // TODO Update cookie when convenient!!!
					        String cookie = headers.get("set-cookie");
					    }
				    });
				}
			});
			
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				
				@Override
				public void call(Object... arg0) {
					
					System.out.println("Connected to socket.io");
					socket.emit("operator login", "Send whatever you want for the example");
				}
			}).on("redirect", new Emitter.Listener() {
				
				@Override
				public void call(Object... args) {
					
					for (Object arg : args) {
						System.out.println(arg);
					}
					socket.disconnect();
				}

			}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					System.out.println("Disconnected to socket.io");
				}

			});
			
			socket.connect();
		}
	}
}

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

import io.undertow.Undertow;

public class TestUndertow {

	public static void main(String[] args) throws InterruptedException {
		UndertowJaxrsServer server = new UndertowJaxrsServer();

		server.start(Undertow.builder().addHttpListener(8001, "localhost"));
		
		System.out.println("http://localhost:8001/base/test");
		server.deploy(MyApp.class);
		
		System.out.println("http://localhost:8001/root/test");
		server.deploy(MyApp.class, "root");
		
		System.out.println("http://localhost:8001/test");
		server.deploy(MyApp.class, "/");
		
		
		//server pobezi minutu, pak to ukoncm
		Thread.sleep(60000);
		
		server.stop();


	}

	@Path("/test")
	public static class Resource {
		@GET
		@Produces("text/plain")
		public String get() {
			System.out.println("test");
			return "hello world";
		}
	}

	@ApplicationPath("/base")
	public static class MyApp extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			HashSet<Class<?>> classes = new HashSet<Class<?>>();
			classes.add(Resource.class);
			return classes;
		}
	}

}

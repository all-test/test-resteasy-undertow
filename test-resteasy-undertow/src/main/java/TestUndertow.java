import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
		
		//System.out.println("http://localhost:8001/test");
		server.deploy(IndexApp.class, "/");
		
		
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
	
	
	@Path("/")
	public static class IndexResource {
		@GET
		@Path("")
		public Response getIndex(@Context UriInfo uriInfo) {
			System.out.println("index");
			URI uri = uriInfo.getAbsolutePathBuilder().path("index.html").build();
			return Response.temporaryRedirect(uri).build();
		}
		
		@GET
		@Produces("text/plain")
		@Path("/{name: [a-zA-Z_0-9]*\\.html}")
		public String getHtmlFile(@PathParam("name") String name) {
			System.out.println("file: "+name);
			return "file: "+name;
		}		
	}

	@ApplicationPath("/")
	public static class IndexApp extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			HashSet<Class<?>> classes = new HashSet<Class<?>>();
			classes.add(IndexResource.class);
			return classes;
		}
	}	

}

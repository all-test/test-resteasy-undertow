import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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
		//Thread.sleep(60000);
		
		//server.stop();


	}

	@Path("/test")
	public static class Resource {
		int num=0;
		@GET
		@Produces("text/plain")
		public String get() {
			num++;
			System.out.println("test "+num);
			return "hello world "+num;
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
			URI uri = uriInfo.getAbsolutePathBuilder().path("/static/index.html").build();
			
			return Response.temporaryRedirect(uri).build();
		}
		
		@GET
		@Path("/static/{name: [a-zA-Z_0-9/]+\\.[a-zA-Z_0-9]+}")
		public Response staticFile(@PathParam("name") String name){
			URL resource=getClass().getClassLoader().getResource("static/"+name);
			if(resource==null){
				throw new NotFoundException();
			}
			MediaType mt;
			File f=new File(resource.getFile());
			String contentType=null;
			try {contentType = Files.probeContentType(f.toPath());} catch (IOException e) {}

			if(contentType==null){
				if(name.endsWith(".json"))mt=MediaType.valueOf("application/json;charset=UTF-8");
				else if(name.endsWith(".js"))mt=MediaType.valueOf("application/javascript;charset=UTF-8");
				else if(name.endsWith(".css"))mt=MediaType.valueOf("text/css;charset=UTF-8");
				else mt=MediaType.APPLICATION_OCTET_STREAM_TYPE;
			}else{
				mt=MediaType.valueOf(contentType);
			}
			
			if(mt.getType().equals("text")){
				//vsechny textaky budou UTF-8
				mt=mt.withCharset("UTF-8");
			}
			ResponseBuilder response = Response.ok(f,mt);
	        //response.header("Content-Disposition", "attachment; filename=\""+name+"\"");	
			response.lastModified(new Date(f.lastModified()));
	        return response.build();				
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

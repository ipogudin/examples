package examples.jetty;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.MetricsServlet;

public class TestServer {

  public static void main(String[] args) throws Exception
  {
      MetricRegistry registry = new MetricRegistry();
      
      final Graphite graphite = new Graphite(new InetSocketAddress("graphite", 2003));
      /*final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                                                        .prefixedWith("test")
                                                        .convertRatesTo(TimeUnit.SECONDS)
                                                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                        .filter(MetricFilter.ALL)
                                                        .build(graphite);*/
      //reporter.start(1, TimeUnit.SECONDS);
      
      InstrumentedQueuedThreadPool pool = new InstrumentedQueuedThreadPool(registry);
      pool.setMinThreads(1024);
      pool.setMaxThreads(1024);
      Server server = new Server();
      ServerConnector connector=new ServerConnector(server);
      connector.setPort(8080);
      server.setConnectors(new Connector[]{connector});

      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
      context.setContextPath("/");
      context.setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry", registry);
      
      server.setHandler(context);
      

      context.addServlet(new ServletHolder(new TestMapGeneratorServlet(registry)), "/*");
      context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");

      server.start();
      server.join();
  }
  
}

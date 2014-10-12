package examples.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;

import examples.generators.MapGenerator;

@SuppressWarnings("serial")
public class TestMapGeneratorServlet extends HttpServlet
{
    private static final int DEFAULT_EXCEPTIONS = -1;
    private static final int DEFAULT_ELEMENTS = 1;
    private static final int DEFAULT_LEVELS = 1;
    private MapGenerator mapGenerator = new MapGenerator();
    private ObjectMapper objectMapper = new ObjectMapper();
    private MetricRegistry registry;
    private Timer timer;
    
    public TestMapGeneratorServlet(MetricRegistry registry) {
      super();
      this.registry = registry;
      timer = registry.timer("test.requests");
    }
    
    @Override
    public void service(ServletRequest arg0, ServletResponse arg1)
        throws ServletException, IOException {
      Timer.Context t = timer.time();
      try {
        super.service(arg0, arg1);
      }
      finally {
        t.stop();
      }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        ServiceResponse serviceResponse = null;
        try {
          
          if (Math.abs(ThreadLocalRandom.current().nextInt(100)) <= 
              parseIntParameter(request, "exceptions", DEFAULT_EXCEPTIONS)) {
            throw new RuntimeException("Random error");
          }
          
          serviceResponse = new ServiceResponse(
              "ok", null, mapGenerator.generate(
                  parseIntParameter(request, "elements", DEFAULT_ELEMENTS), 
                  parseIntParameter(request, "levels", DEFAULT_LEVELS)));
        
        }
        catch (Throwable e) {
          serviceResponse =  new ServiceResponse(
              "error", e.getClass().getCanonicalName() + " " + String.valueOf(e.getMessage()), null); 
        }
        
        objectMapper.writeValue(response.getWriter(), serviceResponse);
    }
    
    protected int parseIntParameter(HttpServletRequest request, String name, int defaultValue) {
      int result = defaultValue;
      String valueString = request.getParameter(name);
      try {
        result = Integer.valueOf(valueString);
      }
      catch (NumberFormatException e) {
        
      }
      return result;
    }
}


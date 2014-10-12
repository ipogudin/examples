package examples.jetty;

import java.util.Map;

public class ServiceResponse {
  
  private final String status;
  private final String error;
  private final Map<Long, Object> map;

  public ServiceResponse(String status, String error, Map<Long, Object> map) {
    super();
    this.status = status;
    this.error = error;
    this.map = map;
  }

  public String getStatus() {
    return status;
  }

  public String getError() {
    return error;
  }


  public Map<Long, Object> getMap() {
    return map;
  }
  
}

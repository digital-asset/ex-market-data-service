package jsonapi.http;

public class HttpResponse {

  private final int status;
  private final Object result;
  private final Object errors;
  private final Object warnings;

  public HttpResponse(int status, Object result, Object errors, Object warnings) {
    this.status = status;
    this.result = result;
    this.errors = errors;
    this.warnings = warnings;
  }

  public int getStatus() {
    return status;
  }

  public Object getResult() {
    return result;
  }

  public Object getErrors() {
    return errors;
  }

  public Object getWarnings() {
    return warnings;
  }
}

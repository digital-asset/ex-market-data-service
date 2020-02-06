package jsonapi.http;

import java.net.URI;

public interface HttpClient {

  HttpResponse get(URI resource);

  HttpResponse post(URI resource, Object body);
}

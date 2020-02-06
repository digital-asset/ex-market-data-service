package jsonapi.json;

import java.io.InputStream;

public interface JsonDeserializer<T> {

  T apply(InputStream s);
}

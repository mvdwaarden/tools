package comm;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamHandler {
	void handle(InputStream is, OutputStream os);
}

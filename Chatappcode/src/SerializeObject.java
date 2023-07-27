import java.io.*;
import java.util.*;


public class SerializeObject implements Serializable {
    /** Read the object from Base64 string. */
	public static Object deserializeObject( String s ) throws IOException ,ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s.trim());
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;               
	}

	/** Write the object to a Base64 string. */
	public static String serializeObject( Serializable o ) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(65508);
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( o );
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}

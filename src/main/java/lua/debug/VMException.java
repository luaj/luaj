package lua.debug;

public class VMException extends RuntimeException {
	private static final long serialVersionUID = 7876955153693775429L;

	public VMException(Exception e) {
		super(e.getMessage());
	}
}

package org.fao.geonet.exceptions;

public class LabelNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public LabelNotFoundException(String message) {
		super(message);
	}

	public LabelNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}

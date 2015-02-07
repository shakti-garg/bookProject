package org.poc.book.exception;

public class BookReaderException extends Exception {

	private static final long serialVersionUID = -3087767002774323632L;

	public BookReaderException(String exceptionMsg) {
		super(exceptionMsg);
	}
	
	public BookReaderException(Exception exception) {
		super(exception);
	}
	
	public BookReaderException(Throwable t){
		super(t);
	}
	
	public BookReaderException(String exceptionMsg, Exception exception) {
		super(exceptionMsg, exception);
	}
}

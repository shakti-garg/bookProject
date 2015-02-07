package org.poc.book.reader;

import java.util.List;

public abstract class BookReader implements AutoCloseable {

	public abstract String getBookTitle();
	
	public abstract List<String> getAuthor();
	
	public abstract Iterable<Chapter> getChapters();
	
	public abstract void print();
}

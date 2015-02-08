package org.poc.book.reader;

public class Chapter {

	private int index;
	private String title;
	private String text;
	
	public Chapter(int index, String title, String text) {
		this.index = index;
		this.title = title;
		this.text = text;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}

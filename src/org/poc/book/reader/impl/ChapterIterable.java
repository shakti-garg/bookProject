package org.poc.book.reader.impl;

import java.util.Iterator;

import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TableOfContents;

import org.poc.book.reader.Chapter;

public class ChapterIterable implements Iterable<Chapter>{

	private Spine bookSpine;
	private TableOfContents tocs;
	
	public ChapterIterable(Spine bookSpine, TableOfContents tocs) {
		this.bookSpine = bookSpine;
		this.tocs = tocs;
	}
	
	@Override
	public Iterator<Chapter> iterator() {
		return new ChapterIterator(bookSpine, tocs);
	}

}

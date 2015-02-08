package org.poc.book.reader.impl;

import java.util.Iterator;
import java.util.List;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.epub.EpubContentParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.poc.book.reader.Chapter;
import org.xml.sax.ContentHandler;

public class ChapterIterator implements Iterator<Chapter> {

	private Spine bookSpine;
	private List<TOCReference> tocRefs;
	private int tocRefIndex;
	private int chapterIndex;
	
	public ChapterIterator(Spine bookSpine, TableOfContents tocs) {
		this.bookSpine = bookSpine;
		this.tocRefs = tocs.getTocReferences();
		
		this.tocRefIndex = 0;
		this.chapterIndex = -1;
	}

	@Override
	public boolean hasNext() {
		boolean flag = false;
		
		while(this.tocRefIndex < this.tocRefs.size()){
			if(this.tocRefs.get(this.tocRefIndex).getTitle().toLowerCase().contains("chapter")){
				flag = true;
				chapterIndex++;
				
				break;
			}
			this.tocRefIndex++;
		}

		return flag;
	}

	@Override
	public Chapter next() {
		Resource chapterResource = this.bookSpine.getResource(bookSpine.findFirstResourceById(tocRefs.get(tocRefIndex).getResourceId()));
		
		String chapterTitle = tocRefs.get(tocRefIndex).getTitle();
		String chapterText = null;
		try{
			org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
			ParseContext context = new ParseContext();

			BodyContentHandler handler = new BodyContentHandler(10*1024*1024);
			XHTMLContentHandler xhtmlHandler = new XHTMLContentHandler(handler, metadata);
			xhtmlHandler.startDocument();
			ContentHandler contentHandler = new EmbeddedContentHandler(new BodyContentHandler(xhtmlHandler));
			Parser epubContentParser = new EpubContentParser();
			epubContentParser.parse(chapterResource.getInputStream(), contentHandler, metadata, context);
			xhtmlHandler.endDocument();

			chapterText = contentHandler.toString().toLowerCase();
		}
		catch(Exception e){
			System.err.println(e);
		}

		this.tocRefIndex++;
		return new Chapter(this.chapterIndex, chapterTitle, chapterText);
	}

	@Override
	public void remove() {}

}

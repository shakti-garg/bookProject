package org.poc.book.reader.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Guide;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.epub.EpubReader;

import org.poc.book.exception.BookReaderException;
import org.poc.book.reader.BookReader;
import org.poc.book.reader.Chapter;

public class KindleBookReader extends BookReader{

	private InputStream is;

	private Book book;
	private Metadata bookMetadata;

	public KindleBookReader(String kindleFilePath) throws BookReaderException {
		this(new File(kindleFilePath));
	}

	public KindleBookReader(File kindleFile) throws BookReaderException {
		try {
			is = new BufferedInputStream(new FileInputStream(kindleFile));
		} catch (FileNotFoundException e) {
			throw new BookReaderException("Kindle Book at path: "+kindleFile.getAbsolutePath()+" doesn't exist.", e);
		}

		EpubReader epubReader = new EpubReader();
		try {
			book = epubReader.readEpub(is);
		} catch (IOException e) {
			throw new BookReaderException("Error in Reading Kindle Book at path: "+kindleFile.getAbsolutePath(), e);
		}

		bookMetadata = book.getMetadata();
	}

	@Override
	public List<String> getAuthor() {
		List<String> authorList = new ArrayList<String>();

		for(Author bookAuthor : bookMetadata.getAuthors()){
			authorList.add(bookAuthor.toString());
		}

		return authorList;
	}

	@Override
	public String getBookTitle() {
		return book.getTitle();
	}

	@Override
	public Iterable<Chapter> getChapters() {		
		return new ChapterIterable(this.book.getSpine(), this.book.getTableOfContents());
	}

	@Override
	public void close() throws BookReaderException {
		if(is != null){
			try {
				is.close();
			} catch (IOException e) {
				throw new BookReaderException("Exception in closing file",e);
			}
		}
	}

	@Override
	public void print() {
		System.out.println("=============================Book Metadata===============================\n");
		for(Author bookAuthor : bookMetadata.getAuthors()){
			System.out.println("Author: "+bookAuthor);
		}		
		for(Author bookCont : bookMetadata.getContributors()){
			System.out.println("Contributor: "+bookCont);
		}
		for(Date bookDate : bookMetadata.getDates()){
			System.out.println("Date: "+bookDate);
		}
		for(String bookDesc : bookMetadata.getDescriptions()){
			System.out.println("Description: "+bookDesc);
		}
		System.out.println("First Title: "+bookMetadata.getFirstTitle());
		System.out.println("Format: "+bookMetadata.getFormat());
		for(Identifier bookId : bookMetadata.getIdentifiers()){
			System.out.println("Identifier: "+bookId);
		}
		System.out.println("Language: "+bookMetadata.getLanguage());
		Map<QName, String> otherProps = bookMetadata.getOtherProperties();
		System.out.println("Other Properties: ");
		for(QName key : otherProps.keySet()){
			System.out.println("Key: "+key+" , "+"value: "+otherProps.get(key));
		}
		for(String bookPublisher : bookMetadata.getPublishers()){
			System.out.println("Publisher: "+bookPublisher);
		}
		for(String bookRight : bookMetadata.getRights()){
			System.out.println("Right: "+bookRight);
		}
		for(String bookSubject : bookMetadata.getSubjects()){
			System.out.println("Subject: "+bookSubject);
		}
		for(String bookTitle : bookMetadata.getTitles()){
			System.out.println("Title: "+bookTitle);
		}
		for(String bookType : bookMetadata.getTypes()){
			System.out.println("Type: "+bookType);
		}

		System.out.println("Book Title: "+book.getTitle());
		System.out.println("======================================================================================\n");

		System.out.println("=========================Table Of Contents=============================================\n");
		TableOfContents tocs = book.getTableOfContents();
		for(TOCReference toc : tocs.getTocReferences()){
			System.out.println(toc.getTitle()+"-"+toc.getCompleteHref()+"-"+toc.getFragmentId()+"-"+toc.getResourceId());
		}

		System.out.println("======================================================================================\n");
		System.out.println("===================================Resources===========================================\n");
		List<Resource> contents = book.getContents();
		System.out.println("Resources size: "+contents.size());
		for(Resource content : contents){
			System.out.println(content.toString());
		}

		System.out.println("======================================================================================\n");
		System.out.println("================================Guide References==========================================\n");
		Guide guide = book.getGuide();
		List<GuideReference> guideRefs = guide.getReferences();
		System.out.println("Guide Refs Size: "+guideRefs.size());
		for(GuideReference guideRef : guideRefs){
			System.out.println(guideRef.toString());
		}

		System.out.println("======================================================================================\n");
		System.out.println("================================Spine References=========================================\n");
		Spine bookSpine = book.getSpine();
		List<SpineReference> spineRefList = bookSpine.getSpineReferences();
		System.out.println(spineRefList.size());
		System.out.println("======================================================================================\n");
	}
}

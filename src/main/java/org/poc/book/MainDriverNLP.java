package org.poc.book;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class MainDriverNLP {
	public static void main(String[] args) throws IOException, SAXException, TikaException {
		InputStream is = new BufferedInputStream(new FileInputStream("in/Meluha1.epub"));

		ContentHandler contentHandler = new BodyContentHandler(10*1024*1024);
		Metadata metadata = new Metadata();
		ParseContext parseContext = new ParseContext();

		//Parser parser = new AutoDetectParser();
		EpubParser epubParser = new EpubParser();
		epubParser.parse(is, contentHandler, metadata, parseContext);

		is.close();

		/*Tika tika = new Tika();
	    tika.setMaxStringLength(10*1024*1024);

	    InputStream input = new ByteArrayInputStream(data);
	    Metadata metadata = new Metadata();
	    String content = tika.parseToString(input, metadata);*/

		String inText = contentHandler.toString().substring(10000,20000);
		//System.out.println("content: /n"+inText);

		/*for(String paramName : metadata.names()){
			System.out.println("param: "+paramName+", value: "+metadata.get(paramName));
		}*/

		SentenceModel sentenceModel = new SentenceModel(new FileInputStream("model/en-sent.bin"));
		SentenceDetector sentenceDetector = new SentenceDetectorME(sentenceModel);
		String[] sentences = sentenceDetector.sentDetect(inText);

		/*System.out.println("sentences: /n");
		for(String sentence : sentences){
			System.out.println(sentence);			
		}*/

		TokenizerModel tokenModel = new TokenizerModel(new FileInputStream("model/en-token.bin"));
		Tokenizer tokenizer = new TokenizerME(tokenModel);

		TokenNameFinderModel nameModel = new TokenNameFinderModel(new FileInputStream("model/en-ner-person.bin"));
		NameFinderME nameFinder = new NameFinderME(nameModel);

		TokenNameFinderModel locModel = new TokenNameFinderModel(new FileInputStream("model/en-ner-location.bin"));
		NameFinderME locFinder = new NameFinderME(locModel);

		for(String sentence : sentences){
			String[] tokens = tokenizer.tokenize(sentence);
			/*System.out.println("tokens: /n");
			for(String token : tokenizedSentence){
				System.out.println(token);
			}*/

			Span nameSpans[] = nameFinder.find(tokens);
			if(nameSpans.length != 0){
				System.out.println("Found entity: NAME: " + Arrays.toString(Span.spansToStrings(nameSpans, tokens)));
			}
			
			Span locSpans[] = locFinder.find(tokens);
			if(locSpans.length != 0){
				System.out.println("Found entity: LOCATION: " + Arrays.toString(Span.spansToStrings(locSpans, tokens)));
			}
		}

		nameFinder.clearAdaptiveData();

	}
}

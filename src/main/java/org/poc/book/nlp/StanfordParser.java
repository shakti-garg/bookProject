package org.poc.book.nlp;

import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordParser {
	
	private StanfordCoreNLP pipeline;
	
	public StanfordParser() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, regexner, pos, lemma, parse, sentiment");//, ner, dcoref
		props.setProperty("regexner.mapping", "in/characters.txt");
		
		pipeline = new StanfordCoreNLP(props);		
	}
	
	public Annotation parseText(String chapterText){
		Annotation annotatedChapter = new Annotation(chapterText);
		pipeline.annotate(annotatedChapter);
		
		return annotatedChapter;

	}

}

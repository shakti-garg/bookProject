package org.poc.book;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.poc.book.exception.BookReaderException;
import org.poc.book.nlp.StanfordParser;
import org.poc.book.reader.BookReader;
import org.poc.book.reader.Chapter;
import org.poc.book.reader.impl.KindleBookReader;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class MainDriverStanford {
	public static void main(String[] args){
		String[] kindleBookSources = {"in/Meluha1.epub", "in/Meluha2.epub", "in/Meluha3.epub"};
		String outputFolderPath = "out1";

		try {
			execute(kindleBookSources, outputFolderPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void execute(String[] kindleBookSources, String outputFolderPath) throws Exception{
		File outputFolder = new File(outputFolderPath);
		if(!outputFolder.exists()){
			outputFolder.mkdir();
		}

		Writer fileWriter1 = new BufferedWriter(new FileWriter(outputFolder.getAbsolutePath()+"/1-occurence.csv"));
		fileWriter1.append("BookName").append(",").append("ChapterName").append(",").append("CharacterName");
		fileWriter1.append(",").append("Percentile").append("\n");

		Writer fileWriter2 = new BufferedWriter(new FileWriter(outputFolder.getAbsolutePath()+"/2-sentiment.csv"));
		fileWriter2.append("BookName").append(",").append("ChapterName").append(",").append("CharacterName");
		fileWriter2.append(",").append("Percentile")/*.append(",").append("Sentence")*/.append(",").append("Sentiment");
		fileWriter2/*.append(",").append("Sentiment Score")*/.append("\n");

		StanfordParser nlpParser = new StanfordParser();
		
		for(String kindleFilePath : kindleBookSources){
			try(BookReader bookReader = new KindleBookReader(kindleFilePath)){
				String bookTitleName = bookReader.getBookTitle();
				
				System.out.println("Book Name: "+bookTitleName);
				System.out.println("Author Name: "+bookReader.getAuthor());

				for(Chapter chapter : bookReader.getChapters()){
					System.out.println(chapter.getIndex()+" : "+chapter.getTitle());
					
					Annotation annotatedChapter = nlpParser.parseText(chapter.getText());
					
					List<CoreMap> sentences = annotatedChapter.get(SentencesAnnotation.class);
					int numSentences = sentences.size();

					Map<String, List<Double>> wordPercentiles = new HashMap<String, List<Double>>();
					Map<String, List<String>> characterSentiments = new HashMap<String, List<String>>();
					for(int i=0; i<numSentences; i++){
						CoreMap sentence = sentences.get(i);

						String sentiment = sentence.get(SentimentCoreAnnotations.ClassName.class);
						//Tree sentimentTree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);

						List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
						int numTokens = tokens.size();

						StringBuilder sentText = new StringBuilder();
						for(CoreLabel token : tokens){
							sentText.append(token.get(TextAnnotation.class)).append(" ");
						}

						SemanticGraph semanticGraph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
						List<String> subjects = new ArrayList<String>();
						for(SemanticGraphEdge semanticGraphEdge : semanticGraph.getEdgeSet()){
							if(semanticGraphEdge.getRelation().toString().equals("nsubj") || semanticGraphEdge.getRelation().toString().equals("nsubjpass")){
								subjects.add(semanticGraphEdge.getDependent().get(TextAnnotation.class));
							}
						}

						for(int j=0; j<numTokens; j++){
							String nerTag = tokens.get(j).get(NamedEntityTagAnnotation.class);
							if(nerTag != null && nerTag.equals("CHARACTER")){
								String characterName = tokens.get(j).get(TextAnnotation.class);

								if(!wordPercentiles.containsKey(characterName)){
									wordPercentiles.put(characterName, new ArrayList<Double>());
								}
								wordPercentiles.get(characterName).add(chapter.getIndex()+(((double)i/numSentences)+(((double)j/numTokens)/numSentences)));

								/*if(!characterSentiments.containsKey(characterName)){
									characterSentiments.put(characterName, new ArrayList<String>());
								}
								boolean isCharacterSubject = false;
								for(String subjectName : subjects){
									if(subjectName.trim().equals(characterName.trim())){
										characterSentiments.get(characterName).add(new Double(chapter.getIndex()+(((double)i/numSentences)+(((double)j/numTokens)/numSentences))).toString()+",\""+sentText.toString()+"\","+sentiment);

										isCharacterSubject = true;
										break;
									}
								}
								if(!isCharacterSubject){
									characterSentiments.get(characterName).add(new Double(chapter.getIndex()+(((double)i/numSentences)+(((double)j/numTokens)/numSentences))).toString()+",\""+sentText.toString()+"\","+"-----");
								}*/

								for(String subjectName : subjects){
									if(subjectName.trim().equals(characterName.trim())){
										if(!characterSentiments.containsKey(characterName)){
											characterSentiments.put(characterName, new ArrayList<String>());
										}

										characterSentiments.get(characterName).add(new Double(chapter.getIndex()+(((double)i/numSentences)+(((double)j/numTokens)/numSentences))).toString()+","+sentiment);

										break;
									}
								}

							}
						}
					}

					System.out.println(bookTitleName+","+chapter.getTitle()+","+wordPercentiles);

					for(String characterName: wordPercentiles.keySet()){
						for(Double percentile : wordPercentiles.get(characterName)){
							fileWriter1.append("\"").append(bookTitleName)
							.append("\",\"").append(chapter.getTitle())
							.append("\",\"").append(characterName)
							.append("\",").append(percentile.toString())
							.append("\n");
						}
					}

					for(String characterName: characterSentiments.keySet()){
						for(String value : characterSentiments.get(characterName)){
							fileWriter2.append("\"").append(bookTitleName)
							.append("\",\"").append(chapter.getTitle())
							.append("\",\"").append(characterName)
							.append("\",").append(value)
							.append("\n");
						}
					}
				}
			} catch (BookReaderException e) {
				throw new Exception("Exception in reading kindle book.",e);
			} 
		}

		fileWriter1.flush();
		fileWriter1.close();

		fileWriter2.flush();
		fileWriter2.close();
	}
}

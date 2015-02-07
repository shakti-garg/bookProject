package org.poc.book;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.epub.EpubContentParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class MainDriverStanford {
	public static void main(String[] args) throws IOException, SAXException, TikaException {
		String[] bookSources = {"in/Meluha1.epub", "in/Meluha2.epub", "in/Meluha3.epub"};

		EpubReader epubReader = new EpubReader();

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, regexner, pos, lemma, parse, sentiment");//, ner, dcoref
		props.setProperty("regexner.mapping", "in/meluha-characters.txt");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Writer fileWriter1 = new BufferedWriter(new FileWriter("out/1-occurence.csv"));
		fileWriter1.append("BookName").append(",").append("ChapterName").append(",").append("CharacterName");
		fileWriter1.append(",").append("Percentile").append("\n");

		Writer fileWriter2 = new BufferedWriter(new FileWriter("out/2-sentiment.csv"));
		fileWriter2.append("BookName").append(",").append("ChapterName").append(",").append("CharacterName");
		fileWriter2.append(",").append("Percentile")/*.append(",").append("Sentence")*/.append(",").append("Sentiment");
		fileWriter2.append(",").append("Sentiment Score").append("\n");

		for(String bookSource : bookSources){
			InputStream is = new BufferedInputStream(new FileInputStream(bookSource));
			Book book = epubReader.readEpub(is);

			Metadata bookMd = book.getMetadata();

			for(Author bookAuthor : bookMd.getAuthors()){
				System.out.println("Author: "+bookAuthor);
			}		
			for(Author bookCont : bookMd.getContributors()){
				System.out.println("Contributor: "+bookCont);
			}
			for(Date bookDate : bookMd.getDates()){
				System.out.println("Date: "+bookDate);
			}
			for(String bookDesc : bookMd.getDescriptions()){
				System.out.println("Description: "+bookDesc);
			}
			System.out.println("First Title: "+bookMd.getFirstTitle());
			System.out.println("Format: "+bookMd.getFormat());
			for(Identifier bookId : bookMd.getIdentifiers()){
				System.out.println("Identifier: "+bookId);
			}
			System.out.println("Language: "+bookMd.getLanguage());
			Map<QName, String> otherProps = bookMd.getOtherProperties();
			System.out.println("Other Properties: ");
			for(QName key : otherProps.keySet()){
				System.out.println("Key: "+key+" , "+"value: "+otherProps.get(key));
			}
			for(String bookPublisher : bookMd.getPublishers()){
				System.out.println("Publisher: "+bookPublisher);
			}
			for(String bookRight : bookMd.getRights()){
				System.out.println("Right: "+bookRight);
			}
			for(String bookSubject : bookMd.getSubjects()){
				System.out.println("Subject: "+bookSubject);
			}
			for(String bookTitle : bookMd.getTitles()){
				System.out.println("Title: "+bookTitle);
			}
			for(String bookType : bookMd.getTypes()){
				System.out.println("Type: "+bookType);
			}

			String bookTitle = book.getTitle();
			System.out.println("Book Title: "+bookTitle);

			System.out.println("/n=================================================================/n");
			System.out.println("Table of Contents");
			TableOfContents tocs = book.getTableOfContents();
			for(TOCReference toc : tocs.getTocReferences()){
				System.out.println(toc.getTitle()+"-"+toc.getCompleteHref()+"-"+toc.getFragmentId()+"-"+toc.getResourceId());
			}

			System.out.println("/n=================================================================/n");
			List<Resource> contents = book.getContents();
			System.out.println("Resources: "+contents.size());
			for(Resource content : contents){
				System.out.println(content.toString());
			}

			System.out.println("/n=================================================================/n");
			Guide guide = book.getGuide();
			List<GuideReference> guideRefs = guide.getReferences();
			System.out.println("Guide Refs: "+guideRefs.size());
			for(GuideReference guideRef : guideRefs){
				System.out.println(guideRef.toString());
			}

			System.out.println("/n=================================================================/n");
			Spine bookSpine = book.getSpine();
			List<SpineReference> spineRefList = bookSpine.getSpineReferences();
			System.out.println(spineRefList.size());

			System.out.println("/n=================================================================/n");
			System.out.println("Chapters");

			int chapterIndex= 0;
			for(TOCReference toc : tocs.getTocReferences()){
				if(toc.getTitle().toLowerCase().contains("chapter")){
					Resource chapterResource = bookSpine.getResource(bookSpine.findFirstResourceById(toc.getResourceId()));

					org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
					ParseContext context = new ParseContext();

					BodyContentHandler handler = new BodyContentHandler(10*1024*1024);
					XHTMLContentHandler xhtmlHandler = new XHTMLContentHandler(handler, metadata);
					xhtmlHandler.startDocument();
					ContentHandler contentHandler = new EmbeddedContentHandler(new BodyContentHandler(xhtmlHandler));
					Parser epubContentParser = new EpubContentParser();
					epubContentParser.parse(chapterResource.getInputStream(), contentHandler, metadata, context);
					xhtmlHandler.endDocument();

					String chapterText = contentHandler.toString().toLowerCase();

					Annotation annotatedChapter = new Annotation(chapterText);
					pipeline.annotate(annotatedChapter);

					/*Map<Integer, CorefChain> corefs = annotatedChapter.get(CorefChainAnnotation.class);
					for(Integer key : corefs.keySet()){
						CorefChain coref = corefs.get(key);
						
						System.out.println("coref key: "+key);
						System.out.println("coref value: "+coref.toString());
					}*/
					
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

						/*StringBuilder sentText = new StringBuilder();
						for(CoreLabel token : tokens){
							sentText.append(token.get(TextAnnotation.class)).append(" ");
						}*/

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
								wordPercentiles.get(characterName).add(chapterIndex+(((double)i/numSentences)+(((double)j/numTokens)/numSentences)));

								/*if(!characterSentiments.containsKey(characterName)){
									characterSentiments.put(characterName, new ArrayList<String>());
								}
								boolean isCharacterSubject = false;
								for(String subjectName : subjects){
									if(subjectName.trim().equals(characterName.trim())){
										characterSentiments.get(characterName).add(new Double(chapterIndex+(((double)i/numSentences)+(((double)j/numTokens)/numSentences))).toString()+",\""+sentText.toString()+"\","+sentiment);

										isCharacterSubject = true;
										break;
									}
								}
								if(!isCharacterSubject){
									characterSentiments.get(characterName).add(new Double(chapterIndex+(((double)i/numSentences)+(((double)j/numTokens)/numSentences))).toString()+",\""+sentText.toString()+"\","+"-----");
								}*/

								for(String subjectName : subjects){
									if(subjectName.trim().equals(characterName.trim())){
										if(!characterSentiments.containsKey(characterName)){
											characterSentiments.put(characterName, new ArrayList<String>());
										}

										characterSentiments.get(characterName).add(new Double(chapterIndex+(((double)i/numSentences)+(((double)j/numTokens)/numSentences))).toString()+","+sentiment);

										break;
									}
								}

							}
						}
					}

					System.out.println(bookTitle+","+toc.getTitle()+","+wordPercentiles);

					for(String characterName: wordPercentiles.keySet()){
						for(Double percentile : wordPercentiles.get(characterName)){
							fileWriter1.append("\"").append(bookTitle)
							.append("\",\"").append(toc.getTitle())
							.append("\",\"").append(characterName)
							.append("\",").append(percentile.toString())
							.append("\n");
						}
					}

					for(String characterName: characterSentiments.keySet()){
						for(String value : characterSentiments.get(characterName)){
							fileWriter2.append("\"").append(bookTitle)
							.append("\",\"").append(toc.getTitle())
							.append("\",\"").append(characterName)
							.append("\",").append(value)
							.append("\n");
						}
					}

					chapterIndex++;

					//TODO remove break
					//break;
				}
			}
		}
		
		fileWriter1.flush();
		fileWriter1.close();
		
		fileWriter2.flush();
		fileWriter2.close();
	}
}

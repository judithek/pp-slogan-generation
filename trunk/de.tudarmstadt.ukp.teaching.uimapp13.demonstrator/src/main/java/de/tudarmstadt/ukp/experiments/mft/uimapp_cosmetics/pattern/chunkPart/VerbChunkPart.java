package de.tudarmstadt.ukp.experiments.mft.uimapp_cosmetics.pattern.chunkPart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.experiments.mft.uimapp_cosmetics.emotions.EmotionModel;
import de.tudarmstadt.ukp.experiments.mft.uimapp_cosmetics.pattern.PatternGenerator.Resources;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.Sense;
import de.tudarmstadt.ukp.lmf.model.enums.EPartOfSpeech;
import de.tudarmstadt.ukp.lmf.model.meta.SemanticLabel;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;

public class VerbChunkPart
    extends ChunkPart
{

   public VerbChunkPart()
   {
       super();
       _chunkPartType = ChunkPartType.V;
       _isValueDerivable = false;
   }


   @Override
   public ArrayList<String> generate(Resources resources)
   {

       ArrayList<String> output = new ArrayList<String>();

       output.add(_lemma);

       if(!_isValueDerivable || !resources.isUbyGernationAllowed())
       {
           return output;
       }

       String ubySemantic = "verb."+_semanticValue;

//
//       System.out.println("[");
//       System.out.println(ubySemantic);
//       System.out.println(_lemma);
//       System.out.println("]");
//

       List<LexicalEntry> lexicalEntries = resources.getUby().getLexicalEntries(_lemma, EPartOfSpeech.verb, null);

       for (LexicalEntry lexEntry : lexicalEntries) {


           for (Sense sense:lexEntry.getSenses())
           {

               /*

               if(output.size()>5)
               {
                   return output;
               }
               */

               Synset synset = sense.getSynset();

               if(synset == null)
               {
                   continue;
               }

               for(Sense sense2 : synset.getSenses())
               {
//                   System.out.println("\t"+sense2.getLexicalEntry().getLemmaForm());

                   for(SemanticLabel sem : sense2.getSemanticLabels())
                   {
//                       System.out.println("\t\t"+sem.getLabel());

                       if(sem.getLabel().equals(ubySemantic))
                       {
                           String word = sense2.getLexicalEntry().getLemmaForm();
                           boolean isNegativeConnotation = false;
                           EmotionModel emotion = resources.getEmotionAnalizer().getEmotion(word);
                           if(emotion!=null)
                           {
                               isNegativeConnotation= emotion.isNegative();
                           }

                           boolean isFreqToSmall = false;

                           String[] subwords = word.split(" ");
                           List<String> subwordWfreqs = new ArrayList<String>();


                           try
                           {
                               for(String subword : subwords)
                               {
                                   if(subword.length()==0)
                                   {
                                       continue;
                                   }
                                   long freq = resources.getWeb1tSearcher().getFrequency(subword);
                                   if(freq<1000)
                                   {
                                       isFreqToSmall = true;
                                       break;
                                   }
                                   subwordWfreqs.add("("+subword+")["+freq+"] ");
                               }
                           }
                           catch (IOException e) {
                           }

                           if(isFreqToSmall)
                           {
                               System.out.println("Too smal frequence : "+word);
                               continue;
                           }



                           String wordfreq = "";

                           for(String val : subwordWfreqs)
                           {
                               wordfreq = wordfreq + val;
                           }

                            if(!output.contains(wordfreq) && !isNegativeConnotation)
                               {
                                   output.add(wordfreq);
//                               System.out.println("\t\tYES");
                               }

                       }
                   }

               }

           }
       }


       return output;
   }



}

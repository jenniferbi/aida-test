import mpi.aida.*;
import mpi.aida.access.DataAccess;
import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.JsonSettings.JSONTYPE;
import mpi.aida.config.settings.PreparationSettings;
import mpi.aida.config.settings.PreparationSettings.DOCUMENT_INPUT_FORMAT;
import mpi.aida.config.settings.disambiguation.*;
import mpi.aida.config.settings.preparation.ManualPreparationSettings;
import mpi.aida.config.settings.preparation.StanfordHybridPreparationSettings;
import mpi.aida.data.*;
import mpi.aida.preparator.Preparator;
import mpi.aida.util.Counter;
import mpi.aida.util.htmloutput.HtmlGenerator;
import mpi.aida.util.splitter.DelimBasedTextSplitter;
import mpi.aida.util.timing.RunningTimer;
import mpi.tools.javatools.util.FileUtils;
import org.apache.commons.cli.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import java.nio.file.Files;
import java.nio.file.Paths;

class AidaTestFast
{
    public static void main(String args[]) throws Exception
    {
      // Define the input.
      String inputText = "[[Terry]] played for [[Chelsea]].";
      //String inputText = new String(Files.readAllBytes(Paths.get("long_input.txt"))); 
      //System.out.println("Read file: " + inputText);

      // Prepare the input for disambiguation. The Stanford NER will be run
      // to identify names. Strings marked with [[ ]] will also be treated as names.
      PreparationSettings prepSettings = new StanfordHybridPreparationSettings();
      Preparator p = new Preparator();
      PreparedInput input = p.prepare(inputText, prepSettings);

      // Disambiguate the input with the graph coherence algorithm.
      DisambiguationSettings disSettings = new FastCocktailPartyDisambiguationSettings();    
      //DisambiguationSettings disSettings = new FastLocalKeyphraseBasedDisambiguationWithNullSettings();    
      //DisambiguationSettings disSettings = new FastLocalKeyphraseBasedDisambiguationSettings();    
      Disambiguator d = new Disambiguator(input, disSettings);
      DisambiguationResults results = d.disambiguate();

      // Print the disambiguation results.
      for (ResultMention rm : results.getResultMentions()) {
	ResultEntity re = results.getBestEntity(rm);
	System.out.println(rm.getMention() + " -> " + re);
      }

    }
}


package uk.kcl.info;

import be.vibes.fexpression.DimacsModel;
import be.vibes.solver.Sat4JSolverFacade;
import be.vibes.solver.exception.SolverInitializationException;
import be.vibes.ts.Execution;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.Transition;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import be.vibes.ts.io.xml.XmlLoaders;
import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.archives.Feature;
import uk.kcl.info.archives.FeatureModel;
import uk.kcl.info.archives.ModelXMLReader;
import uk.kcl.info.bfm.TraceExplorer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import static be.vibes.fexpression.DimacsModel.createFromDimacsFile;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final Options options;
    private static final String NAME = "target/BehavioralFeatureModel-1.0-SNAPSHOT-jar-with-dependencies.jar";
    private static final String HELP = "help";
    private static final String FTS = "fts";
    private static final String BFM = "bfm";
    private static final String OUTPUT = "o";
    private static final String DEFAULT = "d";
    private static String outputDirName = "target/output/";
    private static String sulName;
    private String fmFile;
    private String ftsFile;
    private String bfmFile;

    public Main() {
        options = new Options();
        options.addOption(Option.builder(HELP)
                .desc("Prints this help message.")
                .build());
        options.addOption(Option.builder(DEFAULT)
                .desc("Specify we want to use all predefined parameters.")
                .build());
        options.addOption(Option.builder(FTS)
                .desc("Specify the name of the System Under Learning. This name should correspond to two files: \n" +
                        "- sulName.fts: an XML storing the FTS to convert and which is placed under src/main/resources/fts/;\n" +
                        "- sulName.dimacs: a DIMACS file storing the FM of the system and which is placed under src/main/resources/fm/.")
                .argName("sulName")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder(BFM)
                .desc("Specify the name of the System Under Learning. This name should correspond to two files: \n" +
                        "- sulName.bfm: an XML storing the BFM to convert and which is placed under src/main/resources/bfm/;\n" +
                        "- sulName.dimacs: a DIMACS file storing the FM of the system and which is placed under src/main/resources/fm/.")
                .argName("sulName")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder(OUTPUT)
                .desc("Specify the output directory."
                        + " If this option is not provided, target/output/ is used as default.")
                .hasArg()
                .argName("outputDir")
                .numberOfArgs(1)
                .build());
    }

    public void initialise(CommandLine line) throws Exception {
        logger.info("Initilisation");

        if (line.hasOption(FTS)) {
            String[] name = line.getOptionValues(FTS);
            sulName = name[0];
            ftsFile = "src/main/resources/fts/" + sulName + ".fts";
            fmFile = "src/main/resources/fm/" + sulName + ".dimacs";
            Sat4JSolverFacade fm = new Sat4JSolverFacade(createFromDimacsFile(fmFile));
            Preconditions.checkState(fm.isSatisfiable(), "Given FM is not SAT!");
        } else {
            logger.error("Error while parsing command line: FTS and FM input models are mandatory!");
            printHelpMessage();
            System.exit(1);
        }

        if (line.hasOption(BFM)) {
            String[] name = line.getOptionValues(BFM);
            sulName = name[0];
            ftsFile = "src/main/resources/bfm/" + sulName + ".bfm";
            fmFile = "src/main/resources/fm/" + sulName + ".dimacs";
            Sat4JSolverFacade fm = new Sat4JSolverFacade(createFromDimacsFile(fmFile));
            Preconditions.checkState(fm.isSatisfiable(), "Given FM is not SAT!");
        } else {
            logger.error("Error while parsing command line: BFM and FM input models are mandatory!");
            printHelpMessage();
            System.exit(1);
        }

        if (line.hasOption(OUTPUT)) {
            String[] files = line.getOptionValues(OUTPUT);
            outputDirName = files[0];
        }

        Files.createDirectories(Paths.get(outputDirName + sulName + "/fts/"));
        Files.createDirectories(Paths.get(outputDirName + sulName + "/bfm/"));
        logger.info("Initilisation: done");

    }

    public void initialise(String fileName) throws Exception {
        logger.info("Initilisation");

        sulName = fileName;
        ftsFile = "src/main/resources/fts/" + fileName + ".fts";
        fmFile = "src/main/resources/fm/" + fileName + ".dimacs";

        DimacsModel dimacs = createFromDimacsFile(fmFile);
        Sat4JSolverFacade fm = new Sat4JSolverFacade(dimacs);
        Preconditions.checkState(fm.isSatisfiable(), "Given FM is not SAT!");

        Files.createDirectories(Paths.get(outputDirName + sulName + "/fts/"));
        Files.createDirectories(Paths.get(outputDirName + sulName + "/bfm/"));
        logger.info("Initilisation: done");

    }

    public static void main(String[] args) throws Exception {

        Main main = new Main();
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;

        try {
            line = parser.parse(main.options, args);
        } catch (ParseException ex) {
            logger.error("Error while parsing command line!", ex);
            main.printHelpMessage();
            System.exit(1);
        }

        if (line.hasOption(HELP)) {
            main.printHelpMessage();
            System.exit(0);
        }

        if (line.hasOption(DEFAULT)) {

            logger.info("default");
            explore();
            //readFIDE();
            /*
            List<String> files = new LinkedList<>();

            files.add("toy"); // VM1: 358 (<1'')
            files.add("svm"); // VM1: 7834 (<8'')
            files.add("minepump"); // VM1: 528127 (<9')
            files.add("cpterminal"); // VM1: 1001398 (+/- 17')
            files.add("aerouc5"); // VM1: 6815125 (< 2h)

            //files.add("claroline"); // VM2: ()
            //files.add("wp-ageRRN");
            //files.add("wp-ageRR");
            //files.add("wp-elsaRRN");
            //files.add("wp-elsaRR");

            for(String fileName: files){
                int l = (73 - fileName.length())/2;
                String str = "*".repeat(l) + " Learning " + fileName + " " + "*".repeat(l);
                logger.info("************************************************************************************");
                logger.info(str);
                logger.info("************************************************************************************");
                main.initialise(fileName);
                //main.simplifyOT(fileName);
            }*/
        } else {
            logger.info("else");
            main.initialise(line);
            //main.learn();
        }
    }

    /*
    private void FTStoBFM(String system) throws IOException, SolverInitializationException {
        sulName = system;
        String file = "src/main/resources/ot/" + system + "_ot.csv";
        fmFile = "src/main/resources/fm/" + system + ".dimacs";
        ftsFile = "src/main/resources/fts/" + system + ".fts";
        DimacsModel dimacs = createFromDimacsFile(fmFile);
        Sat4JSolverFacade fm = new Sat4JSolverFacade(dimacs);

        logger.info("Table Loaded");
        Learner learner = new Learner(sul, outputDirName);

        logger.info("Simplifying learned Featured Transition System");
        FeaturedTransitionSystem fts = Learner.makeHypothesisWithSimplification(table);
        logger.info("Saving learned Featured Transition System in XML file");
        XmlSavers.save(fts,outputDirName + sulName + "/"+ "simplified.fts");
    }

    private void BFMtoFTS(String system) throws IOException, SolverInitializationException {
            sulName = system;
            String file = "src/main/resources/ot/" + system + "_ot.csv";
            fmFile = "src/main/resources/fm/" + system + ".dimacs";
            ftsFile = "src/main/resources/fts/" + system + ".fts";
            DimacsModel dimacs = createFromDimacsFile(fmFile);
            Sat4JSolverFacade fm = new Sat4JSolverFacade(dimacs);

            logger.info("Table Loaded");
            Learner learner = new Learner(sul, outputDirName);

            logger.info("Simplifying learned Featured Transition System");
            FeaturedTransitionSystem fts = Learner.makeHypothesisWithSimplification(table);
            logger.info("Saving learned Featured Transition System in XML file");
            XmlSavers.save(fts,outputDirName + sulName + "/"+ "simplified.fts");
    }*/

    public static void explore() throws TransitionSystenExecutionException, IOException, SolverInitializationException, TransitionSystemDefinitionException {
        String fmFile = "src/main/resources/fm/svm.dimacs";
        Sat4JSolverFacade fm = new Sat4JSolverFacade(createFromDimacsFile(fmFile));
        String ftsFile = "src/main/resources/fts/svm.fts";
        FeaturedTransitionSystem fts = XmlLoaders.loadFeaturedTransitionSystem(ftsFile);
        TraceExplorer explorer = new TraceExplorer(fm,fts);
        List<Execution> traces = explorer.exploreAllTraces();

        for(Execution trace: traces){

            String str = "";
            for (Iterator<Transition> it = trace.iterator(); it.hasNext(); ) {
                Transition t = it.next();
                t.
            }

            System.out.println(str);
        }
    }

    public static void readFIDE() {
        String filePath = "src/main/resources/FeatureIDE/model.xml";
        FeatureModel featureModel = ModelXMLReader.readFeatureModel(filePath);

        logger.info("Model");

        // Print the parsed features
        for (Feature feature : featureModel.getFeatures()) {
            logger.info("Feature Name: " + feature.getName());
            logger.info("Mandatory: " + feature.isMandatory());
            logger.info("Abstract: " + feature.isAbstractFeature());
            logger.info("--------------------------");
        }
        logger.info("Out");
    }

    private void printHelpMessage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(128);
        formatter.printHelp(String.format("java -jar %s.jar -%s | -%s | -%s | -%s (-%s)?",
                NAME, HELP, DEFAULT, FTS, BFM, OUTPUT), options);
    }
}
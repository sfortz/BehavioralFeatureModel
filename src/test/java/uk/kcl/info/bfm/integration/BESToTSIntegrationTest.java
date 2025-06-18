package uk.kcl.info.bfm.integration;

import be.vibes.ts.TransitionSystem;
import be.vibes.ts.io.dot.TransitionSystemDotPrinter;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.utils.translators.BesToTsConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

public class BESToTSIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BES_IN_PATH = BASE_PATH + "bes/";
    private static final String XML_OUT_PATH = BASE_PATH + "ts/xml/";
    private static final String DOT_OUT_PATH = BASE_PATH + "ts/dot/";

    @Test
    public void testRobotLinearBESConversion() throws Exception {
        // Load BES
        BundleEventStructure bes = XmlLoaderUtility.loadBundleEventStructure(new File(BES_IN_PATH + "robot-linear.bes"));
        // Convert to TS
        BesToTsConverter converter = new BesToTsConverter(bes);
        TransitionSystem ts = converter.convert();

        saveTs(ts, "robot-linear");

        // Optionally compare with an expected TransitionSystem if available
        // TransitionSystem expected = XmlLoaderUtility.loadTransitionSystem(new File("expected/robot-linear.ts"));
        // assertTrue(areTraceEquivalent(ts, expected));
    }

    @Test
    public void testRobotBESConversion() throws Exception {
        // Load BES
        BundleEventStructure bes = XmlLoaderUtility.loadBundleEventStructure(new File(BES_IN_PATH + "robot.bes"));
        // Convert to TS
        BesToTsConverter converter = new BesToTsConverter(bes);
        TransitionSystem ts = converter.convert();

        saveTs(ts, "robot");

        // Optionally compare with an expected TransitionSystem if available
        // TransitionSystem expected = XmlLoaderUtility.loadTransitionSystem(new File("expected/robot.ts"));
        // assertTrue(areTraceEquivalent(ts, expected));
    }

    private void saveTs(TransitionSystem ts, String filenamePrefix) throws Exception {

        // Save DOT
        File dotFile = new File(DOT_OUT_PATH + filenamePrefix + "-from-bes.dot");
        dotFile.getParentFile().mkdirs();
        try (PrintStream out = new PrintStream(new FileOutputStream(dotFile))) {
            TransitionSystemDotPrinter printer = new TransitionSystemDotPrinter(ts, out);
            printer.printDot();
            printer.flush();
        }

        // Save XML
        XmlSaverUtility.save(ts, XML_OUT_PATH + filenamePrefix + "-from-bes.ts");
    }

    // Stub function: to be implemented if you provide a trace-equivalence checker
    private boolean areTraceEquivalent(TransitionSystem ts1, TransitionSystem ts2) {
        Set<String> traces1 = null; //ts1.getTraces();
        Set<String> traces2 = null; //ts2.getTraces();
        return traces1.equals(traces2);
    }
}

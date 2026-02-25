// package catan; // what is this problem

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigParsingTest {

    private static int callReadConfigFile(String filename) throws Exception {
        Method m = Demonstrator.class.getDeclaredMethod("readConfigFile", String.class);
        m.setAccessible(true);
        return (int) m.invoke(null, filename);
    }

    private static Path tempConfig(String content) throws IOException {
        Path p = Files.createTempFile("config", ".txt");
        Files.write(p, content.getBytes());
        return p;
    }

    /**
     * Tests that a valid configuration line like "turns: 200"
     * is correctly parsed and returned as an integer.
     */
    @Test(timeout = 2000)
    public void test_config_validTypical() throws Exception {
        Path p = tempConfig("turns: 200\n");
        assertEquals(200, callReadConfigFile(p.toString()));
    }

    /**
     * Tests that the minimum valid value (1) is accepted.
     */
    @Test(timeout = 2000)
    public void test_config_boundaryMin() throws Exception {
        Path p = tempConfig("turns: 1\n");
        assertEquals(1, callReadConfigFile(p.toString()));
    }

    /**
     * Tests that the maximum valid value (8192) is accepted.
     */
    @Test(timeout = 2000)
    public void test_config_boundaryMax() throws Exception {
        Path p = tempConfig("turns: 8192\n");
        assertEquals(8192, callReadConfigFile(p.toString()));
    }

    /**
     * Tests that a config file without a valid "turns:" line
     * throws an IOException.
     */
    @Test(timeout = 2000, expected = IOException.class)
    public void test_config_missingTurns_throws() throws Exception {
        Path p = tempConfig("# comment only\n\n");
        callReadConfigFile(p.toString());
    }

    /**
     * Tests that a non-numeric value after "turns:"
     * throws a NumberFormatException.
     */
    @Test(timeout = 2000, expected = NumberFormatException.class)
    public void test_config_malformedNumber_throws() throws Exception {
        Path p = tempConfig("turns: abc\n");
        callReadConfigFile(p.toString());
    }
}
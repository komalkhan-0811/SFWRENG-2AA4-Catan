package catan;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigParsingTest {

    private static int callReadConfigFile(String filename) throws Exception {
        Method m = Demonstrator.class.getDeclaredMethod("readConfigFile", String.class);
        m.setAccessible(true);
        try {
            return (int) m.invoke(null, filename);
        } catch (InvocationTargetException e) {
            
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw e;
        }
    }

    private static Path tempConfig(String content) throws IOException {
        Path p = Files.createTempFile("config", ".txt");
        Files.write(p, content.getBytes());
        return p;
    }

    @Test
    public void test_config_validTypical() throws Exception {
        Path p = tempConfig("turns: 200\n");
        assertEquals(200, callReadConfigFile(p.toString()));
    }

    @Test
    public void test_config_boundaryMin() throws Exception {
        Path p = tempConfig("turns: 1\n");
        assertEquals(1, callReadConfigFile(p.toString()));
    }

    @Test
    public void test_config_boundaryMax() throws Exception {
        Path p = tempConfig("turns: 8192\n");
        assertEquals(8192, callReadConfigFile(p.toString()));
    }

    @Test
    public void test_config_missingTurns_throws() throws Exception {
        Path p = tempConfig("# comment only\n\n");
        assertThrows(IOException.class, () -> callReadConfigFile(p.toString()));
    }

    @Test
    public void test_config_malformedNumber_throws() throws Exception {
        Path p = tempConfig("turns: abc\n");
        assertThrows(NumberFormatException.class, () -> callReadConfigFile(p.toString()));
    }
}


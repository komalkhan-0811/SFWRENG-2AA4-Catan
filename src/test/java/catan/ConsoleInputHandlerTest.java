package catan;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ConsoleInputHandler
 * 
 */
class ConsoleInputHandlerTest {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalSystemOut;
    
    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalSystemOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
    }
    
  
    //For null branch
    @Test
    void test_constructor_nullScanner_throwsException() {
        
        Exception exception = assertThrows(IllegalArgumentException.class,() -> new ConsoleInputHandler(null));
        
        assertEquals("Scanner must not be null.", exception.getMessage());
    }
    
   
    
    @Test
    void test_constructor_validScanner_assigns() {
  
        Scanner testScanner = new Scanner(new StringReader("test\n"));
        
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        // Verify scanner was assigned by using it
        String result = handler.readLine("");
        assertEquals("test", result);
    }

    
    @Test
    void test_displayMessage_executesPrintln() {
  
        Scanner scanner = new Scanner(new StringReader(""));
        ConsoleInputHandler handler = new ConsoleInputHandler(scanner);
        
        handler.displayMessage("Test");
        
        assertEquals("Test\n", outputStream.toString());
    }
    
 
    
    @Test
    void test_readLine_executesPrint() {

        Scanner scanner = new Scanner(new StringReader("data\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(scanner);
        
        handler.readLine("Prompt: ");
        
        String output = outputStream.toString();
        assertTrue(output.startsWith("Prompt: "));
    }

    
    @Test
    void test_readLine_hasNextLineTrue_returnsTrimmed() {
    
        Scanner scanner = new Scanner(new StringReader("  input  \n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(scanner);
        
        String result = handler.readLine(">");
        
        assertEquals("input", result);
    }
    
 
    
    @Test
    void test_readLine_hasNextLineFalse_returnsEmpty() {
        
        Scanner scanner = new Scanner(new StringReader("")); // Empty = no next line
        ConsoleInputHandler handler = new ConsoleInputHandler(scanner);
        
        String result = handler.readLine(">");
        
        assertEquals("", result);
    }
   
   
}
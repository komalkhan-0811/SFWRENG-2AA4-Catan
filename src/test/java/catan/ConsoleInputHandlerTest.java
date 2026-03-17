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
 * Test suite for ConsoleInputHandler.
 * Tests console I/O functionality with input/output streams.
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
    
   
    
    @Test
    void test_defaultConstructor_createsValidHandler() {
        ConsoleInputHandler handler = new ConsoleInputHandler();
        
        assertNotNull(handler, "Handler should not be null");
    }
    
    @Test
    void test_customScannerConstructor_createsValidHandler() {
        Scanner testScanner = new Scanner(new StringReader("test input"));
        
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        assertNotNull(handler, "Handler should not be null");
    }
    
    @Test
    void test_nullScannerConstructor_throwsException() {
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ConsoleInputHandler(null),
            "Should throw IllegalArgumentException for null scanner"
        );
        
        assertEquals("Scanner must not be null.", exception.getMessage());
    }
    
    
    @Test
    void test_displayMessage_printsToSystemOut() {
        Scanner testScanner = new Scanner(new StringReader(""));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.displayMessage("Hello World");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Hello World"), "Output should contain message");
        assertTrue(output.endsWith("\n"), "Output should end with newline");
    }
    
    @Test
    void test_displayMessage_emptyString() {
        Scanner testScanner = new Scanner(new StringReader(""));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.displayMessage("");
        
        String output = outputStream.toString();
        assertEquals("\n", output, "Empty message should still print newline");
    }
    
    @Test
    void test_displayMessage_multipleMessages() {
        Scanner testScanner = new Scanner(new StringReader(""));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.displayMessage("First");
        handler.displayMessage("Second");
        handler.displayMessage("Third");
        
        String output = outputStream.toString();
        assertTrue(output.contains("First"), "Should contain first message");
        assertTrue(output.contains("Second"), "Should contain second message");
        assertTrue(output.contains("Third"), "Should contain third message");
    }
    
    @Test
    void test_displayMessage_specialCharacters() {
        Scanner testScanner = new Scanner(new StringReader(""));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.displayMessage("Message with symbols: @#$%^&*()");
        
        String output = outputStream.toString();
        assertTrue(output.contains("@#$%^&*()"), "Should handle special characters");
    }
    
    
    @Test
    void test_readLine_printsPrompt() {
        Scanner testScanner = new Scanner(new StringReader("user input\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.readLine("Enter command: ");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Enter command: "), "Should print prompt");
    }
    
    @Test
    void test_readLine_returnsUserInput() {
        Scanner testScanner = new Scanner(new StringReader("user input\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Prompt: ");
        
        assertEquals("user input", result, "Should return user input");
    }
    
    @Test
    void test_readLine_trimsWhitespace() {
        Scanner testScanner = new Scanner(new StringReader("  spaced input  \n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Prompt: ");
        
        assertEquals("spaced input", result, "Should trim leading and trailing whitespace");
    }
    
    @Test
    void test_readLine_multipleInputs() {
        Scanner testScanner = new Scanner(new StringReader("first\nsecond\nthird\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String first = handler.readLine("1: ");
        String second = handler.readLine("2: ");
        String third = handler.readLine("3: ");
        
        assertEquals("first", first, "First input should match");
        assertEquals("second", second, "Second input should match");
        assertEquals("third", third, "Third input should match");
    }
    
    @Test
    void test_readLine_emptyInput() {
        Scanner testScanner = new Scanner(new StringReader("\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Prompt: ");
        
        assertEquals("", result, "Empty input should return empty string");
    }
    
    @Test
    void test_readLine_noMoreInput_returnsEmptyString() {
        Scanner testScanner = new Scanner(new StringReader("")); // No input at all
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Prompt: ");
        
        assertEquals("", result, "Should return empty string when no input available");
    }
    
    @Test
    void test_readLine_exhaustedStream_returnsEmptyString() {
        Scanner testScanner = new Scanner(new StringReader("first\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.readLine("1: "); 
        String result = handler.readLine("2: ");
        
        assertEquals("", result, "Should return empty string when stream exhausted");
    }
    
    @Test
    void test_readLine_inputWithSpacesInMiddle() {
        Scanner testScanner = new Scanner(new StringReader("build road 1 2\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Command: ");
        
        assertEquals("build road 1 2", result, "Should preserve internal spaces");
    }
    
    @Test
    void test_readLine_inputWithNumbers() {
        Scanner testScanner = new Scanner(new StringReader("12345\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Number: ");
        
        assertEquals("12345", result, "Should handle numeric input as string");
    }
    
    @Test
    void test_readLine_inputWithMixedCase() {
        Scanner testScanner = new Scanner(new StringReader("CamelCaseInput\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Input: ");
        
        assertEquals("CamelCaseInput", result, "Should preserve case");
    }
    
    
    @Test
    void test_displayThenRead_bothWork() {
        Scanner testScanner = new Scanner(new StringReader("response\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.displayMessage("Question?");
        String response = handler.readLine("Answer: ");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Question?"), "Display should work");
        assertEquals("response", response, "Read should work");
    }
    
    @Test
    void test_multipleDisplayAndRead_sequence() {
        Scanner testScanner = new Scanner(new StringReader("ans1\nans2\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        handler.displayMessage("Message 1");
        String r1 = handler.readLine("Q1: ");
        handler.displayMessage("Message 2");
        String r2 = handler.readLine("Q2: ");
        
        assertEquals("ans1", r1);
        assertEquals("ans2", r2);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Message 1"));
        assertTrue(output.contains("Message 2"));
    }
    
    
    @Test
    void test_readLine_veryLongInput() {
        String longInput = "a".repeat(1000);
        Scanner testScanner = new Scanner(new StringReader(longInput + "\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Prompt: ");
        
        assertEquals(longInput, result, "Should handle very long input");
    }
    
    @Test
    void test_readLine_onlyWhitespace_returnsTrimmedEmpty() {
        Scanner testScanner = new Scanner(new StringReader("     \n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("Prompt: ");
        
        assertEquals("", result, "Whitespace-only input should trim to empty");
    }
    
    @Test
    void test_displayMessage_nullMessage_doesNotCrash() {
        Scanner testScanner = new Scanner(new StringReader(""));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        assertDoesNotThrow(() -> handler.displayMessage(null));
    }
    
    @Test
    void test_readLine_emptyPrompt() {
        Scanner testScanner = new Scanner(new StringReader("input\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
        String result = handler.readLine("");
        
        assertEquals("input", result, "Should work with empty prompt");
    }
    
    @Test
    void test_readLine_nullPrompt_doesNotCrash() {
        Scanner testScanner = new Scanner(new StringReader("input\n"));
        ConsoleInputHandler handler = new ConsoleInputHandler(testScanner);
        
      
        assertDoesNotThrow(() -> handler.readLine(null));
    }
}
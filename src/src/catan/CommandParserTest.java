package catan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import catan.CommandParser.CommandType;
import catan.CommandParser.ParsedCommand;
import catan.CommandParser.ConsoleCommandParser;

/**
 * Test suite for CommandParser: R2.1 Regex Parser
 * 
 * Tests demonstrate correctness of regex-based parsing:
 * - Valid commands
 * - Invalid commands
 * - Spacing variations
 * - Case insensitivity
 * - Boundary conditions
 * 
 * @author Komal Khan
 */

public class CommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new ConsoleCommandParser();
    }


    /**
     * Test 1: Accepted Simple Commands
     * Test that basic commands (roll, go, list) are parsed correctly
     */
    @Test
    void testValidSimpleCommands() {
        // Test "roll"
        ParsedCommand rollCmd = parser.parse("roll");
        assertEquals(CommandType.ROLL, rollCmd.type, "Should parse 'roll' command");

        // Test "go"
        ParsedCommand goCmd = parser.parse("go");
        assertEquals(CommandType.GO, goCmd.type, "Should parse 'go' command");

        // Test "list"
        ParsedCommand listCmd = parser.parse("list");
        assertEquals(CommandType.LIST, listCmd.type, "Should parse 'list' command");
    }
    
    /**
     * Test 2: Valid Build Commands
     * Test that build commands with node IDs are parsed correctly.
     * This tests regex patterns with capture groups.
     */
    @Test
    void testValidBuildCommands() {
    	
        // Test "build settlement <nodeId>"
        ParsedCommand settlementCmd = parser.parse("build settlement 5");
        assertEquals(CommandType.BUILD_SETTLEMENT, settlementCmd.type, "Should parse 'build settlement' command");
        assertEquals(5, settlementCmd.nodeA, "Should extract node ID 5");

        // Test "build city <nodeId>"
        ParsedCommand cityCmd = parser.parse("build city 10");
        assertEquals(CommandType.BUILD_CITY, cityCmd.type, "Should parse 'build city' command");
        assertEquals(10, cityCmd.nodeA, "Should extract node ID 10");

        // Test "build road <fromNodeId> <toNodeId>"
        ParsedCommand roadCmd = parser.parse("build road 3 15");
        assertEquals(CommandType.BUILD_ROAD, roadCmd.type, "Should parse 'build road' command");
        assertEquals(3, roadCmd.nodeA, "Should extract first node ID 3");
        assertEquals(15, roadCmd.nodeB, "Should extract second node ID 15");
    }

    
    /**
     * Test 3: Spacing Variations
     * Test that commands with varying whitespace are handled correctly.
     * This will test the regex handling of \\s+ and \\s* patterns.
     */
    @Test
    void testSpacingVariations() {
    	
        
        ParsedCommand cmd1 = parser.parse("  roll  ");
        assertEquals(CommandType.ROLL, cmd1.type, "Should handle leading/trailing spaces");

        // Multiple spaces between words
        ParsedCommand cmd2 = parser.parse("build    settlement    8");
        assertEquals(CommandType.BUILD_SETTLEMENT, cmd2.type, "Should handle multiple spaces");
        assertEquals(8, cmd2.nodeA, "Should extract node ID despite extra spaces");

        //Mixed
        ParsedCommand cmd3 = parser.parse("  build\t\troad  \t5\t\t12  ");
        assertEquals(CommandType.BUILD_ROAD, cmd3.type, "Should handle tabs");
        assertEquals(5, cmd3.nodeA, "Should extract first node ID with tabs");
        assertEquals(12, cmd3.nodeB, "Should extract second node ID with tabs");

        // Single space
        ParsedCommand cmd4 = parser.parse("build city 3");
        assertEquals(CommandType.BUILD_CITY, cmd4.type, "Should handle single spaces");
        assertEquals(3, cmd4.nodeA, "Should extract node ID with single spaces");
    }

    
    /**
     * Test 4: Cases Insensitivity
     * Test that commands are case-insensitive.
     * This tests the Pattern.CASE_INSENSITIVE flag in regex.
     */
    @Test
    void testCaseInsensitivity() {
    	

        ParsedCommand cmd1 = parser.parse("ROLL");
        assertEquals(CommandType.ROLL, cmd1.type, "Should handle UPPERCASE");

        ParsedCommand cmd2 = parser.parse("Go");
        assertEquals(CommandType.GO, cmd2.type, "Should handle MixedCase");

        ParsedCommand cmd3 = parser.parse("LiSt");
        assertEquals(CommandType.LIST, cmd3.type, "Should handle CrAzYcAsE");
 
        ParsedCommand cmd4 = parser.parse("BUILD SETTLEMENT 7");
        assertEquals(CommandType.BUILD_SETTLEMENT, cmd4.type, "Should handle uppercase BUILD");
        assertEquals(7, cmd4.nodeA, "Should extract node ID from uppercase command");
        
        ParsedCommand cmd5 = parser.parse("build road 2 9");
        assertEquals(CommandType.BUILD_ROAD, cmd5.type, "Should handle lowercase");
    }
    
    /**
     * Test 5: Invalid Commands
     * Test that invalid/malformed commands return UNKNOWN.
     * This tests regex pattern non-matching behavior.
     */
    @Test
    void testInvalidCommands() {
      

        ParsedCommand cmd1 = parser.parse("attack");
        assertEquals(CommandType.UNKNOWN, cmd1.type, "Should return UNKNOWN for 'attack'");

       
        ParsedCommand cmd2 = parser.parse("roil");
        assertEquals(CommandType.UNKNOWN, cmd2.type, "Should return UNKNOWN for 'roil'");

      
        ParsedCommand cmd3 = parser.parse("build");
        assertEquals(CommandType.UNKNOWN, cmd3.type, 
            "Should return UNKNOWN for incomplete 'build'");

       
        ParsedCommand cmd4 = parser.parse("build settlement");
        assertEquals(CommandType.UNKNOWN, cmd4.type, 
            "Should return UNKNOWN for 'build settlement' without node ID");

   
        ParsedCommand cmd5 = parser.parse("build settlement 5 10");
        assertEquals(CommandType.UNKNOWN, cmd5.type, 
            "Should return UNKNOWN for 'build settlement' with too many args");

    
        ParsedCommand cmd6 = parser.parse("build road 5");
        assertEquals(CommandType.UNKNOWN, cmd6.type, 
            "Should return UNKNOWN for 'build road' with only one node");

   
        ParsedCommand cmd7 = parser.parse("");
        assertEquals(CommandType.UNKNOWN, cmd7.type, "Should return UNKNOWN for empty string");


        ParsedCommand cmd8 = parser.parse(null);
        assertEquals(CommandType.UNKNOWN, cmd8.type, "Should return UNKNOWN for null input");


        ParsedCommand cmd9 = parser.parse("   ");
        assertEquals(CommandType.UNKNOWN, cmd9.type, 
            "Should return UNKNOWN for whitespace-only input");
    }

    
    /**
     * Test 6: Boundary Testing with the Node IDs
     * Test boundary conditions for node IDs.
     * This tests regex \\d+ pattern with edge values.
     */
    @Test
    void testNodeIdBoundaries() {
    	
        // Zero node ID
        ParsedCommand cmd1 = parser.parse("build settlement 0");
        assertEquals(CommandType.BUILD_SETTLEMENT, cmd1.type, "Should parse node ID 0");
        assertEquals(0, cmd1.nodeA, "Should extract node ID 0");

        // Maximum Catan node ID
        ParsedCommand cmd2 = parser.parse("build city 53");
        assertEquals(CommandType.BUILD_CITY, cmd2.type, "Should parse node ID 53");
        assertEquals(53, cmd2.nodeA, "Should extract node ID 53");

        // Large node ID
        ParsedCommand cmd3 = parser.parse("build settlement 999");
        assertEquals(CommandType.BUILD_SETTLEMENT, cmd3.type,"Should parse large node ID");
        assertEquals(999, cmd3.nodeA, "Should extract large node ID 999");

      
        ParsedCommand cmd4 = parser.parse("build road 52 53");
        assertEquals(CommandType.BUILD_ROAD, cmd4.type, "Should parse multi-digit nodes");
        assertEquals(52, cmd4.nodeA, "Should extract first multi-digit node");
        assertEquals(53, cmd4.nodeB, "Should extract second multi-digit node");
    }

    
    /**
     * Test 7: Invalid Node ID of different formats
     * Test that invalid node ID formats are rejected.
     * This tests regex validation of numeric patterns.
     */
    @Test
    void testInvalidNodeIdFormats() {
      

        ParsedCommand cmd1 = parser.parse("build settlement -5");
        assertEquals(CommandType.UNKNOWN, cmd1.type, "Should reject negative node ID");

        ParsedCommand cmd2 = parser.parse("build city 5.5");
        assertEquals(CommandType.UNKNOWN, cmd2.type,"Should reject decimal node ID");

        ParsedCommand cmd3 = parser.parse("build settlement abc");
        assertEquals(CommandType.UNKNOWN, cmd3.type, "Should reject non-numeric node ID");

        ParsedCommand cmd4 = parser.parse("build road 5a 12");
        assertEquals(CommandType.UNKNOWN, cmd4.type, "Should reject alphanumeric node ID");
    }
    
    /**
     * Test 8: Extra Text Before or After Commands
     * Test that extra text surrounding commands is rejected.
     * This tests the ^ and $ anchors in regex patterns.
     */
    @Test
    void testExtraTextRejection() {

        ParsedCommand cmd1 = parser.parse("please roll");
        assertEquals(CommandType.UNKNOWN, cmd1.type, "Should reject command with prefix text");

        ParsedCommand cmd2 = parser.parse("roll now");
        assertEquals(CommandType.UNKNOWN, cmd2.type,"Should reject command with suffix text");

        ParsedCommand cmd3 = parser.parse("build settlement at 5");
        assertEquals(CommandType.UNKNOWN, cmd3.type, "Should reject build command with extra words");

        ParsedCommand cmd4 = parser.parse("I want to build city 3 please");
        assertEquals(CommandType.UNKNOWN, cmd4.type, "Should reject command embedded in sentence");
    }

}
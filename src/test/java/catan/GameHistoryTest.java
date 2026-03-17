package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for GameHistory class.
 * Tests undo/redo functionality
 */
class GameHistoryTest {
    
    private GameHistory history;
    
    @BeforeEach
    void setUp() {
        history = new GameHistory();
    }
    
  
    
    /**
     * Simple mock command that tracks execute/undo calls
     */
    private static class MockCommand implements Command {
        private boolean executed = false;
        private boolean undone = false;
        private final String description;
        
        public MockCommand(String description) {
            this.description = description;
        }
        
        @Override
        public void execute() {
            executed = true;
            undone = false;
        }
        
        @Override
        public void undo() {
            undone = true;
            executed = false;
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        public boolean wasExecuted() {
            return executed;
        }
        
        public boolean wasUndone() {
            return undone;
        }
    }
    

    
    @Test
    void test_undo_emptyStack_returnsFalse() {
       
        boolean result = history.undo();
        
        assertFalse(result, "Should return false when nothing to undo");
    }
    
    @Test
    void test_undo_withCommand_popsCallsUndoPushesToRedo() {
     
        MockCommand cmd = new MockCommand("Test command");
        history.executeCommand(cmd);
        
        boolean result = history.undo();
        
        assertTrue(result, "Should return true when undo succeeds");
        assertTrue(cmd.wasUndone(), "Command's undo() should have been called");
        assertTrue(history.canRedo(), "Command should be in redo stack");
        assertFalse(history.canUndo(), "Command should not be in undo stack");
    }
    
    @Test
    void test_undo_multipleCommands_undoesInOrder() {
        MockCommand cmd1 = new MockCommand("Command 1");
        MockCommand cmd2 = new MockCommand("Command 2");
        
        history.executeCommand(cmd1);
        history.executeCommand(cmd2);
        
        assertTrue(history.undo());
        assertTrue(cmd2.wasUndone());
        assertFalse(cmd1.wasUndone());
        

        assertTrue(history.undo());
        assertTrue(cmd1.wasUndone());
    }
    

    
    @Test
    void test_redo_emptyStack_returnsFalse() {
        
        boolean result = history.redo();
        
        assertFalse(result, "Should return false when nothing to redo");
    }
    
    @Test
    void test_redo_afterUndo_popsCallsExecutePushesToUndo() {
       
        MockCommand cmd = new MockCommand("Test command");
        history.executeCommand(cmd);
        history.undo();
        
        boolean result = history.redo();
        
        assertTrue(result, "Should return true when redo succeeds");
        assertTrue(cmd.wasExecuted(), "Command's execute() should have been called again");
        assertTrue(history.canUndo(), "Command should be back in undo stack");
        assertFalse(history.canRedo(), "Command should not be in redo stack");
    }
    
    @Test
    void test_redo_multipleCommands_redoesInOrder() {
        MockCommand cmd1 = new MockCommand("Command 1");
        MockCommand cmd2 = new MockCommand("Command 2");
        
        history.executeCommand(cmd1);
        history.executeCommand(cmd2);
        history.undo(); 
        history.undo(); 

        assertTrue(history.redo());
        assertTrue(cmd1.wasExecuted());
        

        assertTrue(history.redo());
        assertTrue(cmd2.wasExecuted());
    }
    

    @Test
    void test_canUndo_emptyStack_returnsFalse() {

        assertFalse(history.canUndo(), "Should return false when undo stack is empty");
    }
    
    @Test
    void test_canUndo_withCommand_returnsTrue() {

        MockCommand cmd = new MockCommand("Test");
        history.executeCommand(cmd);
        
        assertTrue(history.canUndo(), "Should return true when undo stack has commands");
    }
    
    @Test
    void test_canUndo_afterUndo_returnsFalse() {
        MockCommand cmd = new MockCommand("Test");
        history.executeCommand(cmd);
        history.undo();
        
        assertFalse(history.canUndo(), "Should return false after undoing all commands");
    }
    

    
    @Test
    void test_canRedo_emptyStack_returnsFalse() {
       
        assertFalse(history.canRedo(), "Should return false when redo stack is empty");
    }
    
    @Test
    void test_canRedo_afterUndo_returnsTrue() {
       
        MockCommand cmd = new MockCommand("Test");
        history.executeCommand(cmd);
        history.undo();
        
        assertTrue(history.canRedo(), "Should return true when redo stack has commands");
    }
    
    @Test
    void test_canRedo_afterRedo_returnsFalse() {
        MockCommand cmd = new MockCommand("Test");
        history.executeCommand(cmd);
        history.undo();
        history.redo();
        
        assertFalse(history.canRedo(), "Should return false after redoing all commands");
    }
    
   
    
    @Test
    void test_getLastCommandDescription_emptyStack_returnsNone() {
        
        String description = history.getLastCommandDescription();
        
        assertEquals("None", description, "Should return 'None' when no commands");
    }
    
    @Test
    void test_getLastCommandDescription_withCommand_returnsDescription() {
        
        MockCommand cmd = new MockCommand("Build Settlement at 5");
        history.executeCommand(cmd);
        
        String description = history.getLastCommandDescription();
        
        assertEquals("Build Settlement at 5", description, "Should return command's description");
    }
    
    @Test
    void test_getLastCommandDescription_multipleCommands_returnsLastOne() {
        MockCommand cmd1 = new MockCommand("First command");
        MockCommand cmd2 = new MockCommand("Second command");
        
        history.executeCommand(cmd1);
        history.executeCommand(cmd2);
        
        String description = history.getLastCommandDescription();
        
        assertEquals("Second command", description, "Should return most recent command's description");
    }
    
    @Test
    void test_getLastCommandDescription_afterUndo_returnsRemainingLast() {
        MockCommand cmd1 = new MockCommand("First command");
        MockCommand cmd2 = new MockCommand("Second command");
        
        history.executeCommand(cmd1);
        history.executeCommand(cmd2);
        history.undo();
        
        String description = history.getLastCommandDescription();
        
        assertEquals("First command", description, "Should return description of new last command");
    }
    
    @Test
    void test_getLastCommandDescription_afterUndoAll_returnsNone() {
        MockCommand cmd = new MockCommand("Test command");
        history.executeCommand(cmd);
        history.undo();
        
        String description = history.getLastCommandDescription();
        
        assertEquals("None", description, "Should return 'None' after undoing all commands");
    }
    
    
    
    @Test
    void test_executeCommand_clearsRedoStack() {
        MockCommand cmd1 = new MockCommand("Command 1");
        MockCommand cmd2 = new MockCommand("Command 2");
        
        history.executeCommand(cmd1);
        history.undo();
        
        assertTrue(history.canRedo(), "Should be able to redo after undo");
        
        history.executeCommand(cmd2);
        
        assertFalse(history.canRedo(), "Redo stack should be cleared after new command");
    }
    
    @Test
    void test_undoRedoSequence_maintainsState() {
        MockCommand cmd1 = new MockCommand("Command 1");
        MockCommand cmd2 = new MockCommand("Command 2");
        MockCommand cmd3 = new MockCommand("Command 3");
        
        // Execute all
        history.executeCommand(cmd1);
        history.executeCommand(cmd2);
        history.executeCommand(cmd3);
        
        // Undo all
        assertTrue(history.undo());
        assertTrue(history.undo()); 
        assertTrue(history.undo()); 
        assertFalse(history.undo()); 
        
        // Redo all
        assertTrue(history.redo()); 
        assertTrue(history.redo()); 
        assertTrue(history.redo()); 
        assertFalse(history.redo());
        
        // Verify final state
        assertEquals("Command 3", history.getLastCommandDescription());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }
    
    @Test
    void test_undoPartialThenExecute_clearsRedo() {
        MockCommand cmd1 = new MockCommand("Command 1");
        MockCommand cmd2 = new MockCommand("Command 2");
        MockCommand cmd3 = new MockCommand("Command 3");
        
        history.executeCommand(cmd1);
        history.executeCommand(cmd2);
        history.undo(); // Undo cmd2
        
        assertTrue(history.canRedo());
        
        history.executeCommand(cmd3);
        
        assertFalse(history.canRedo());
        assertEquals("Command 3", history.getLastCommandDescription());
    }
    
    @Test
    void test_emptyHistory_allChecksFalse() {
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertFalse(history.undo());
        assertFalse(history.redo());
        assertEquals("None", history.getLastCommandDescription());
    }
    
    @Test
    void test_singleCommand_lifecycle() {
        MockCommand cmd = new MockCommand("Single command");
        

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        

        history.executeCommand(cmd);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals("Single command", history.getLastCommandDescription());
        

        assertTrue(history.undo());
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
        assertEquals("None", history.getLastCommandDescription());
        
 
        assertTrue(history.redo());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals("Single command", history.getLastCommandDescription());
    }
}
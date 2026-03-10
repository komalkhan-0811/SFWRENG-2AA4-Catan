package catan;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite that groups all JUnit test classes together.
 * Method to run all tests at once:
 * 
 * @author Komal Khan
 * 
 */
@Suite
@SelectClasses({
    PlayerCardLogicTest.class,
    BoardBuildingTest.class,
    GameFlowTest.class,
    PlayerInitializationTest.class,
    TurnLimitTest.class,
    ConfigParsingTest.class,
    TurnEngineTest.class,
    RobberTest.class,
    CommandParserTest.class
})
public class AllTests {
    
}

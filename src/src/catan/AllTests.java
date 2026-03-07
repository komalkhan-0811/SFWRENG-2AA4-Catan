package catan;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;





import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite that groups all JUnit test classes together.
 * Method to run all tests at once:
 * Right-click AllTests.java → Run As → JUnit Test
 * 
 */
@Suite
@SelectClasses({
    PlayerCardLogicTest.class,
    BoardBuildingTest.class,
    GameFlowTest.class,
    PlayerInitializationTest.class,
    TurnLimitTest.class,
    ConfigParsingTest.class
})
public class AllTests {
    
}

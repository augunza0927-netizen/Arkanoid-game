package se233.project2;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Complete All In One Project Test Suite")
@SelectClasses({
        BallBrickCollisionTest.class,
        ExceptionHandlingTest.class,
        ScoringAndLivesTest.class,
        SlowMotionAbilityTest.class,
        UpgradeSystemTest.class
})

public class AllInOneTestSuite {
}

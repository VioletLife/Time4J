package net.time4j;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses(
    {
        DateComparisonTest.class,
        DateCreationTest.class,
        EpochDaysTest.class,
        GregorianTransformationTest.class,
        LeapYearOrdinalDateTest.class,
        LeapYearRangeArithmeticTest.class,
        LeapYearTest.class,
        LengthOfMonthTest.class,
        MiscDatePropertiesTest.class,
        MonthRangeArithmeticTest.class,
        SpecialUnitTest.class,
        StdDayArithmeticTest.class,
        StdYearOrdinalDateTest.class,
        StdYearRangeArithmeticTest.class,
        YearMonthArithmeticTest.class
    }
)
public class DateSuite {

}
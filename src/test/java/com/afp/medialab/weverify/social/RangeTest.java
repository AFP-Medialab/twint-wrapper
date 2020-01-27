package com.afp.medialab.weverify.social;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.util.DateRange;
import com.afp.medialab.weverify.social.util.RangeDeltaToProcess;

@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan("com.afp.medialab.weverify.social.twint")
public class RangeTest {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String date1 = "2020-01-24 01:00:00";
	private String date2 = "2020-01-24 03:00:00";

	private String date3 = "2020-01-24 06:00:00";
	private String date4 = "2020-01-24 08:00:00";

	private String date5 = "2020-01-24 10:00:00";
	private String date6 = "2020-01-24 12:00:00";
	

	private List<DateRange> rangesMulti, rangesSimple;

	@Autowired
	private RangeDeltaToProcess rangeDeltaToProcess;

	@Before
	public void init() throws ParseException {
		rangesMulti = new LinkedList<DateRange>();
		
		rangesSimple = new LinkedList<DateRange>();

		DateRange range1 = new DateRange(dateFormat.parse(date1), dateFormat.parse(date2));
		DateRange range2 = new DateRange(dateFormat.parse(date3), dateFormat.parse(date4));
		DateRange range3 = new DateRange(dateFormat.parse(date5), dateFormat.parse(date6));

		rangesMulti.add(range1);
		rangesMulti.add(range2);
		rangesMulti.add(range3);

		rangesSimple.add(range3);
	}

	@Test
	@Order(1)
	public void testOutOfRange() throws ParseException {
		System.out.println("test 1");
		String dateRQ1 = "2020-01-24 00:30:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 4);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 00:30:00-2020-01-24 01:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 03:00:00-2020-01-24 06:00:00");
		assertEquals(ranges.get(2), "Range 3 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		assertEquals(ranges.get(3), "Range 4 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(2)
	public void testStartOutOfRange() throws ParseException {
		System.out.println("test 2");
		String dateRQ1 = "2020-01-24 04:30:00";
		String dateRQ2 = "2020-01-24 11:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 04:30:00-2020-01-24 06:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(3)
	public void testEndOutOfRange() throws ParseException {
		System.out.println("test 3");
		String dateRQ1 = "2020-01-24 06:30:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(4)
	public void testStartOutOfRange1() throws ParseException {
		System.out.println("test 4");
		String dateRQ1 = "2020-01-24 00:30:00";
		String dateRQ2 = "2020-01-24 02:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 00:30:00-2020-01-24 01:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(5)
	public void testEndOutOfRange1() throws ParseException {
		System.out.println("test 5");
		String dateRQ1 = "2020-01-24 10:30:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(6)
	public void testStartEndIn2Ranges() throws ParseException {
		System.out.println("test 6");
		String dateRQ1 = "2020-01-24 01:30:00";
		String dateRQ2 = "2020-01-24 11:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 03:00:00-2020-01-24 06:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(7)
	public void testStartEndOutRange1() throws ParseException {
		System.out.println("test 7");
		String dateRQ1 = "2020-01-23 01:30:00";
		String dateRQ2 = "2020-01-24 00:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-23 01:30:00-2020-01-24 00:00:00");
		System.out.println("#################");
	}
	
	@Test()
	@Order(8)
	public void testStartEndOutRangeN() throws ParseException {
		System.out.println("test 8");
		String dateRQ1 = "2020-01-24 17:30:00";
		String dateRQ2 = "2020-01-25 00:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 17:30:00-2020-01-25 00:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(9)
	public void testStartEndInRange() throws ParseException {
		System.out.println("test 9");
		String dateRQ1 = "2020-01-24 06:30:00";
		String dateRQ2 = "2020-01-24 07:30:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 0);
		System.out.println("#################");
	}
	@Test
	@Order(10)
	public void testSameRange() throws ParseException {
		System.out.println("test 10");
		String dateRQ1 = "2020-01-24 06:00:00";
		String dateRQ2 = "2020-01-24 08:00:00";
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 0);
		System.out.println("#################");
	}
	
	@Test
	@Order(11)
	public void testSimpleEndOutOfRange() throws ParseException {
		System.out.println("test 11");
		String dateRQ1 = "2020-01-24 10:00:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		List<String> ranges = result(rangesSimple, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	private List<String> result(List<DateRange> range, String dateRQ1, String dateRQ2) throws ParseException {
		DateRange rqDateRange = new DateRange(dateFormat.parse(dateRQ1), dateFormat.parse(dateRQ2));

		List<DateRange> newRanges = rangeDeltaToProcess.rangeToProcess(range, rqDateRange);
		List<String> results = new LinkedList<String>();
		int i = 1;
		for (DateRange dateRange : newRanges) {
			String result = "Range " + i + " : " + dateFormat.format(dateRange.getStartDate()) + "-"
					+ dateFormat.format(dateRange.getEndDate());
			System.out.println(result);
			results.add(result);
			i++;
		}
		System.out.println();
		
		return results;
	}

	private List<DateRange> createOutOfRangeResults() throws ParseException {
		// Test out of range;

		String res1date1 = "2020-01-24 00:30:00";
		String res1date2 = "2020-01-24 01:00:00";

		String res1date3 = "2020-01-24 03:00:00";
		String res1date4 = "2020-01-24 06:00:00";

		String res1date5 = "2020-01-24 08:00:00";
		String res1date6 = "2020-01-24 10:00:00";

		String res1date7 = "2020-01-24 12:00:00";
		String res1date8 = "2020-01-24 14:00:00";
		
		List<DateRange> results = new LinkedList<DateRange>();
		DateRange range1 = new DateRange(dateFormat.parse(res1date1), dateFormat.parse(res1date2));
		DateRange range2 = new DateRange(dateFormat.parse(res1date3), dateFormat.parse(res1date4));
		DateRange range3 = new DateRange(dateFormat.parse(res1date5), dateFormat.parse(res1date6));
		DateRange range4 = new DateRange(dateFormat.parse(res1date7), dateFormat.parse(res1date8));

		results.add(range1);
		results.add(range2);
		results.add(range3);
		results.add(range4);
		return results;
	}
}

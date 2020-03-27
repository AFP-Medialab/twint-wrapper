package com.afp.medialab.weverify.social;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
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
	
	
	private String date7 = "2020-01-26 00:00:00";
	private String date7bis = "2020-01-27 00:00:00";
	private String date8 = "2020-01-29 00:00:00";
	private String date9 = "2020-01-30 10:00:00";
	
	
	private String date10 = "2020-03-01 00:00:00";
	private String date11 = "2020-03-03 00:00:00";
	
	private String date12 = "2020-03-05 00:00:00";
	private String date13 = "2020-03-06 00:00:00";
	
	private String date14 = "2020-03-07 00:00:00";
	private String date15 = "2020-03-08 00:00:00";
	
	private String date16  = "2020-03-10 00:00:00";
	private String date17  = "2020-03-12 00:00:00";

	private List<DateRange> rangesMulti, rangesSimple, rangeContinous, rangeContinous2, rangesMulti2, rangeContinous3;

	@Autowired
	private RangeDeltaToProcess rangeDeltaToProcess;

	@Before
	public void init() throws ParseException {
		rangesMulti = new LinkedList<DateRange>();
		
		rangesSimple = new LinkedList<DateRange>();
		
		rangeContinous = new LinkedList<DateRange>();
		
		rangeContinous2 = new LinkedList<DateRange>();
		
		rangesMulti2 = new LinkedList<DateRange>();
		
		rangeContinous3 = new LinkedList<DateRange>();

		DateRange range1 = new DateRange(dateFormat.parse(date1), dateFormat.parse(date2));
		DateRange range2 = new DateRange(dateFormat.parse(date3), dateFormat.parse(date4));
		DateRange range3 = new DateRange(dateFormat.parse(date5), dateFormat.parse(date6));

		
		rangesMulti.add(range1);
		rangesMulti.add(range2);
		rangesMulti.add(range3);

		rangesSimple.add(range3);
		
		DateRange range4 = new DateRange(dateFormat.parse(date7), dateFormat.parse(date8));
		DateRange range5 = new DateRange(dateFormat.parse(date8), dateFormat.parse(date9));
		
		rangeContinous.add(range4);
		rangeContinous.add(range5);
		
		DateRange range6 = new DateRange(dateFormat.parse(date7),  dateFormat.parse(date7bis));
		DateRange range7 = new DateRange(dateFormat.parse(date7bis),  dateFormat.parse(date8));
		DateRange range8 = new DateRange(dateFormat.parse(date8),  dateFormat.parse(date9));
		
		rangeContinous2.add(range6);
		rangeContinous2.add(range7);
		rangeContinous2.add(range8);
		
		DateRange range9 = new DateRange(dateFormat.parse(date10),  dateFormat.parse(date11));
		DateRange range10 = new DateRange(dateFormat.parse(date12),  dateFormat.parse(date13));
		
		rangesMulti2.add(range9);
		rangesMulti2.add(range10);
		
		
		rangesMulti.sort(Comparator.comparing(DateRange::getStartDate).thenComparing(DateRange::getEndDate));
		rangesSimple.sort(Comparator.comparing(DateRange::getStartDate).thenComparing(DateRange::getEndDate));
		rangeContinous.sort(Comparator.comparing(DateRange::getStartDate).thenComparing(DateRange::getEndDate));
		rangeContinous2.sort(Comparator.comparing(DateRange::getStartDate).thenComparing(DateRange::getEndDate));
		rangesMulti2.sort(Comparator.comparing(DateRange::getStartDate).thenComparing(DateRange::getEndDate));
		
		DateRange range11 = new DateRange(dateFormat.parse(date13),  dateFormat.parse(date14));
		DateRange range12 = new DateRange(dateFormat.parse(date14),  dateFormat.parse(date15));
		DateRange range13 = new DateRange(dateFormat.parse(date16),  dateFormat.parse(date17));
		
		rangeContinous3.add(range9);
		rangeContinous3.add(range10);
		rangeContinous3.add(range11);
		rangeContinous3.add(range12);
		rangeContinous3.add(range13);
		
	}

	@Test
	@Order(1)
	public void testOutOfRange() throws ParseException {
		String dateRQ1 = "2020-01-24 00:30:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		System.out.println("test 1 : " + dateRQ1 +"-"+ dateRQ2);
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
		String dateRQ1 = "2020-01-24 04:30:00";
		String dateRQ2 = "2020-01-24 11:00:00";
		System.out.println("test 2 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 04:30:00-2020-01-24 06:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(3)
	public void testEndOutOfRange() throws ParseException {
		String dateRQ1 = "2020-01-24 06:30:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		System.out.println("test 3 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(4)
	public void testStartOutOfRange1() throws ParseException {
		String dateRQ1 = "2020-01-24 00:30:00";
		String dateRQ2 = "2020-01-24 02:00:00";
		System.out.println("test 4 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 00:30:00-2020-01-24 01:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(5)
	public void testEndOutOfRange1() throws ParseException {
		String dateRQ1 = "2020-01-24 10:30:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		System.out.println("test 5 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(6)
	public void testStartEndIn2Ranges() throws ParseException {
		String dateRQ1 = "2020-01-24 01:30:00";
		String dateRQ2 = "2020-01-24 11:00:00";
		System.out.println("test 6 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 03:00:00-2020-01-24 06:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-01-24 08:00:00-2020-01-24 10:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(7)
	public void testStartEndOutRange1() throws ParseException {
		String dateRQ1 = "2020-01-23 01:30:00";
		String dateRQ2 = "2020-01-24 00:00:00";
		System.out.println("test 7 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-23 01:30:00-2020-01-24 00:00:00");
		System.out.println("#################");
	}
	
	@Test()
	@Order(8)
	public void testStartEndOutRangeN() throws ParseException {
		String dateRQ1 = "2020-01-24 17:30:00";
		String dateRQ2 = "2020-01-25 00:00:00";
		System.out.println("test 8 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 17:30:00-2020-01-25 00:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(9)
	public void testStartEndInRange() throws ParseException {
		String dateRQ1 = "2020-01-24 06:30:00";
		String dateRQ2 = "2020-01-24 07:30:00";
		System.out.println("test 9 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 0);
		System.out.println("#################");
	}
	@Test
	@Order(10)
	public void testSameRange() throws ParseException {
		String dateRQ1 = "2020-01-24 06:00:00";
		String dateRQ2 = "2020-01-24 08:00:00";
		System.out.println("test 10 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 0);
		System.out.println("#################");
	}
	
	@Test
	@Order(11)
	public void testSimpleEndOutOfRange() throws ParseException {
		String dateRQ1 = "2020-01-24 10:00:00";
		String dateRQ2 = "2020-01-24 14:00:00";
		System.out.println("test 11 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesSimple, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-24 12:00:00-2020-01-24 14:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(12)
	public void testcontinousRange() throws ParseException {
		String dateRQ1 = "2020-01-28 00:00:00";
		String dateRQ2 = "2020-01-30 15:10:00";
		System.out.println("test 12 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangeContinous, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-01-30 10:00:00-2020-01-30 15:10:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(13)
	public void testcontinousSameRange() throws ParseException {
		String dateRQ1 = "2020-01-26 00:00:00";
		String dateRQ2 = "2020-01-30 10:00:00";
		System.out.println("test 12 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangeContinous, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 0);
		System.out.println("#################");
	}
	
	@Test
	@Order(14)
	public void testcontinousSameRange2() throws ParseException {
		String dateRQ1 = "2020-01-26 00:00:00";
		String dateRQ2 = "2020-01-30 10:00:00";
		System.out.println("test 13 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangeContinous2, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 0);
		System.out.println("#################");
	}
	
	@Test
	@Order(15)
	public void testEndOutOfRange2() throws ParseException {
		String dateRQ1 = "2020-03-01 00:00:00";
		String dateRQ2 = "2020-03-09 00:00:00";
		System.out.println("test 3 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangesMulti2, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 2);
		assertEquals(ranges.get(0), "Range 1 : 2020-03-03 00:00:00-2020-03-05 00:00:00");
		assertEquals(ranges.get(1), "Range 2 : 2020-03-06 00:00:00-2020-03-09 00:00:00");
		System.out.println("#################");
	}
	
	@Test
	@Order(16)
	public void testContinious() throws ParseException {
		String dateRQ1 = "2020-03-04 00:00:00";
		String dateRQ2 = "2020-03-06 00:00:00";
		System.out.println("test 16 : " + dateRQ1 +"-"+ dateRQ2);
		List<String> ranges = result(rangeContinous3, dateRQ1, dateRQ2);
		assertEquals(ranges.size(), 1);
		assertEquals(ranges.get(0), "Range 1 : 2020-03-04 00:00:00-2020-03-05 00:00:00");
		
		System.out.println("#################");
	}
	
	@Test
	@Order(17)
	public void testMergeRange(){
		List<DateRange> merge1 = rangeDeltaToProcess.mergeExistingDateRange(rangesMulti);
		assertEquals(merge1.size(), 3);
		List<DateRange> merge2 = rangeDeltaToProcess.mergeExistingDateRange(rangesSimple);
		assertEquals(merge2.size(), 1);
		List<DateRange> merge3 = rangeDeltaToProcess.mergeExistingDateRange(rangeContinous);
		assertEquals(merge3.size(), 1);
		List<DateRange> merge4 = rangeDeltaToProcess.mergeExistingDateRange(rangeContinous2);
		assertEquals(merge4.size(), 1);
		List<DateRange> merge5 = rangeDeltaToProcess.mergeExistingDateRange(rangesMulti2);
		assertEquals(merge5.size(), 2);
		List<DateRange> merge6 = rangeDeltaToProcess.mergeExistingDateRange(rangeContinous3);
		assertEquals(merge6.size(), 3);
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

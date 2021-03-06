package com.afp.medialab.weverify.social.util;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.afp.medialab.weverify.social.dao.entity.Request;
import com.afp.medialab.weverify.social.model.CollectRequest;

@Component
public class RangeDeltaToProcess {

	private static Logger Logger = LoggerFactory.getLogger(RangeDeltaToProcess.class);

	/**
	 * Date Range list should be sort in asc; Date Ranges are not overlap;
	 * 
	 * @param existingRange
	 * @param requestRange
	 * @return
	 */
	public List<DateRange> rangeToProcess(List<DateRange> existingRange, DateRange requestRange) {

		if (requestRange == null)
			return null;
		List<DateRange> dateRangeToProcess = new LinkedList<DateRange>();

		Date rqStartDate = requestRange.getStartDate();
		Date rqEndDate = requestRange.getEndDate();
		DateRange inter = null;
		boolean sameRange = false;
		existingRange = mergeExistingDateRange(existingRange);
		for (DateRange dateRange : existingRange) {
			Date startDate = dateRange.getStartDate();
			Date endDate = dateRange.getEndDate();
			if (inter != null && (startDate.after(rqEndDate) && endDate.after(rqEndDate)))
				continue;

			if (isInRange(startDate, endDate, rqStartDate) && isInRange(startDate, endDate, rqEndDate)
					|| (isSameDate(startDate, rqStartDate) && isSameDate(endDate, rqEndDate))) {
				// Request range already exist nothing to do
				return dateRangeToProcess;
			} else if (!isInRange(startDate, endDate, rqStartDate) && isInRange(startDate, endDate, rqEndDate)) {
				// Start date out of range
				// End date is in the current range
				Date rgEndDate = startDate;
				Date rgStartDate = null;
				if (inter != null) {
					if (isInRange(startDate, endDate, inter.getStartDate())
							&& isInRange(startDate, endDate, inter.getEndDate())) {
						// inter = current dateRange
						inter = null;
						sameRange = true;
						continue;
					} else {
						rgStartDate = inter.getStartDate();
						inter = null;
					}
				} else {
					rgStartDate = rqStartDate;
				}
				DateRange procdateRange = new DateRange(rgStartDate, rgEndDate);
				dateRangeToProcess.add(procdateRange);

			} else if (isInRange(startDate, endDate, rqStartDate) && !isInRange(startDate, endDate, rqEndDate)) {
				// Start date is on the current range
				// End date out of range
				Date rgStartDate = endDate;
				Date rgEndDate = rqEndDate;
				inter = new DateRange(rgStartDate, rgEndDate);

			} else {
				if (inter != null) {
					if (inter.getStartDate().before(startDate)) {
						DateRange procdateRange = new DateRange(inter.getStartDate(), startDate);
						inter.setStartDate(endDate);
						dateRangeToProcess.add(procdateRange);
					} else if (isSameDate(inter.getStartDate(), startDate)) {
						inter.setStartDate(endDate);
					}
				} else if (rqStartDate.before(endDate) && dateRangeToProcess.isEmpty() && rqEndDate.after(endDate)) {
					// Create first range at the beginning
					Date rgStartDate = rqStartDate;
					Date rgEndDate = startDate;
					DateRange procdateRange = new DateRange(rgStartDate, rgEndDate);
					dateRangeToProcess.add(procdateRange);
					inter = new DateRange(endDate, rqEndDate);
				} else if (rqStartDate.before(endDate) && rqEndDate.before(endDate) && dateRangeToProcess.isEmpty()) {
					// request range is before all ranges
					DateRange procdateRange = new DateRange(rqStartDate, rqEndDate);
					dateRangeToProcess.add(procdateRange);
					return dateRangeToProcess;

				}
				// both date out of range
			}

		}
		if (inter != null) {
			dateRangeToProcess.add(inter);
		}
		if (dateRangeToProcess.isEmpty() && !sameRange) {
			dateRangeToProcess.add(requestRange);
		}
		return dateRangeToProcess;
	}

	/**
	 * Clean requests that are not in the request range
	 * 
	 * @param requests
	 * @param collectRequest
	 */
	public Set<Request> requestfromDateRange(Set<Request> requests, CollectRequest collectRequest) {
		Set<Request> machingRequests = new HashSet<Request>();

		if (requests != null && !requests.isEmpty()) {
			machingRequests.addAll(requests);
			Date rqStart = collectRequest.getFrom();
			Date rqEnd = collectRequest.getUntil();
			for (Request request : requests) {
				Date start = request.getSince();
				Date end = request.getUntil();
				if (isInRange(rqStart, rqEnd, start) || isInRange(rqStart, rqEnd, end)) {
					Logger.debug("request is in range");
				} else {
					machingRequests.remove(request);
				}

			}
		}
		return machingRequests;
	}

	/**
	 * Merge following dateRanges
	 * 
	 * @param existingRanges
	 * @return
	 */
	public List<DateRange> mergeExistingDateRange(List<DateRange> existingRanges) {
		existingRanges.sort(Comparator.comparing(DateRange::getStartDate).thenComparing(DateRange::getEndDate));
		List<DateRange> mergeDateRanges = new LinkedList<DateRange>();

		DateRange mergeDateRange = null;

		for (DateRange dateRange : existingRanges) {
			Date start = dateRange.getStartDate();
			Date end = dateRange.getEndDate();
			if (mergeDateRange == null) {
				mergeDateRange = dateRange;
				mergeDateRanges.add(dateRange);
			} else {
				Date prevStart = mergeDateRange.getStartDate();
				Date prevEnd = mergeDateRange.getEndDate();

				if (prevEnd.equals(start)) {
					if (mergeDateRange.getEndDate().equals(start)) {
						mergeDateRanges.remove(mergeDateRange);
						DateRange mergeDateRange2 = new DateRange(prevStart, end);
						mergeDateRange = mergeDateRange2;

					} else {
						mergeDateRange = new DateRange(prevStart, end);
					}
					mergeDateRanges.add(mergeDateRange);
				} else {
					mergeDateRange = dateRange;
					mergeDateRanges.add(dateRange);
				}
			}

		}
//		if(!isMerge)
//			mergeDateRanges.add(previousRange);
		return mergeDateRanges;
	}

	/**
	 * Compare if date are the same
	 * 
	 * @param date
	 * @param date2
	 * @return
	 */
	private boolean isSameDate(Date date, Date date2) {
		if (date.compareTo(date2) == 0)
			return true;
		else
			return false;
	}

	/**
	 * Test if a date belongs to a range
	 * 
	 * @param start
	 * @param end
	 * @param testDate
	 * @return
	 */
	private boolean isInRange(Date start, Date end, Date testDate) {
		if (start.compareTo(testDate) == 0 || end.compareTo(testDate) == 0)
			return true;
		return testDate.after(start) && testDate.before(end);
	}

}

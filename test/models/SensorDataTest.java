package models;

import java.util.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.*;

import org.h2.jdbc.JdbcSQLException;
import org.junit.*;

import static org.junit.Assert.*;
import play.test.WithApplication;
import static play.test.Helpers.*;

public class SensorDataTest extends WithApplication {
	static User user  = null;
	static User user1 = null;
	
	final static int INIT_HOUR   = 22;
	final static int INIT_MINUTE = 40;
	final static int TEST_DATAS  = 10;
	final static int TEST_FACT   =  5;
	
	@Before
	public void setUp() {
//		start(fakeApplication(inMemoryDatabase()));
		user  = UserTest.newKurahashi();		
		user1 = UserTest.newKurahashi1();		
	}

	public static int hour   = INIT_HOUR;
	public static int minute = INIT_MINUTE;
	public static void newSensorDatasWithOwner(User inOwner) {
		Calendar cal = Calendar.getInstance();		
		for (int i = 0; i < TEST_DATAS; i++) {
			cal.set(2015,1,1,hour,minute + i,0);
			cal.set(Calendar.MILLISECOND, 0);
			SensorData sd = new SensorData(cal.getTime(), i * TEST_FACT, (i % 5) == 0 ? TEST_FACT : 0, (i % 2) == 0 ? null : "", inOwner);
			sd.save();
		}
	}
	
	@Test
	public void normalWriteOne() {
		Calendar cal = Calendar.getInstance();		
		cal.set(2015,1,1,hour,minute,0);
		cal.set(Calendar.MILLISECOND, 0);
		SensorData sd = new SensorData(cal.getTime(), 22, 11, "", user);
		sd.save();
		
		List<SensorData> sdFetch = SensorData.find.all();
		assertNotNull("fetched but null", sdFetch);
		assertTrue("fetch size is differenet", sdFetch.size() == 1);
		SensorData sd0 = sdFetch.get(0);
		
		assertNotNull("pirCount is null", sd0.pirCount);
		assertTrue("pirCount is not equal", sd0.pirCount == 22);
		
		assertNotNull("logDate is null", sd0.logDate);
		assertTrue("logDate is wrong", sd0.logDate.equals(cal.getTime()));
		
		assertNotNull("dataOwner is null", sd0.getDataOwner());
		assertTrue("dataOwner's email is wrong", sd0.getDataOwner().email.equals(UserTest.email));
		
		int sdHourMinute  = sd0.iHourMinute;
		int calHourMinute = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
		assertTrue(sdHourMinute == calHourMinute);
	}
	
	@Test
	public void doNotRejectSameHourMinuteButDifferentUer() {
		Calendar cal = Calendar.getInstance();		
		cal.set(2014,1,1,hour,minute,0);
		cal.set(Calendar.MILLISECOND, 0);
		SensorData sd = new SensorData(cal.getTime(), 22, 11, "", user);
		sd.save();
		
		cal.set(2015,1,1,hour,minute,0);
		cal.set(Calendar.MILLISECOND, 0);
		sd = new SensorData(cal.getTime(), 22, 11, "", user1);
		sd.save();
	}
	
	@Test
	public void normalDataWriteAndRead() {
		newSensorDatasWithOwner(user);
		
		List<SensorData> sds = SensorData.findByUser(user);
		assertNotNull("fetched but null", sds);
		assertEquals("fetched size is wrong", sds.size(), TEST_DATAS);
		Calendar cal = Calendar.getInstance();		
		for (int i = 0; i < TEST_DATAS; i++) {
			SensorData sd = sds.get(i);
			cal.set(2015,1,1,hour,minute + i,0);
			cal.set(Calendar.MILLISECOND, 0);
			assertTrue("logDate is not equal", sd.logDate.equals(cal.getTime()));
			assertTrue("pirCount is not equal", sd.pirCount == i * TEST_FACT);
			
			assertNotNull("sd's dataOwner is null", sd.getDataOwner());
			assertEquals("dataOwner's email is wrong", sd.getDataOwner().email, user.email);
			
			assertEquals("iHourMinute is wrong", 
					Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY)*100+cal.get(Calendar.MINUTE)), 
					Integer.valueOf(sd.iHourMinute));
		}
	}

	@Test
	public void fetchDataForGraphRender() {
		newSensorDatasWithOwner(user);
		newSensorDatasWithOwner(user1);
		
		Calendar windowFrom = Calendar.getInstance();
		windowFrom.set(2015,1,1,hour,minute + TEST_DATAS/2, 0);
		windowFrom.set(Calendar.MILLISECOND, 0);
		
		Calendar windowTo = Calendar.getInstance();
		windowTo.set(2015,1,1,hour + 12,minute + TEST_DATAS/2, 0);
		windowTo.set(Calendar.MILLISECOND, 0);
		
		assertNotEquals("from/to should not equal", windowFrom, windowTo);
		
		List<SensorData> sds = SensorData.findByUserSinceDateForHours(user, windowFrom, 12);
		assertNotNull("fetched but null", sds);
		assertEquals("fetch size wrong", TEST_DATAS/2, sds.size());
		
		Calendar dt = Calendar.getInstance();
		for (SensorData sd : sds) {
			dt.setTime(sd.logDate);
			dt.set(Calendar.MILLISECOND, 0);
			
			assertTrue("date out of range", dt.compareTo(windowFrom) >= 0 && dt.compareTo(windowTo) <= 0);
		}
	}
	
	
	@Test
	public void fetchDataForGraphRenderTestOutOfRange() {
		newSensorDatasWithOwner(user);
		newSensorDatasWithOwner(user1);
		
		Calendar windowFrom = Calendar.getInstance();
		windowFrom.set(2015,1,1,hour,minute + TEST_DATAS + 1, 0);
		windowFrom.set(Calendar.MILLISECOND, 0);
		
		Calendar windowTo = Calendar.getInstance();
		windowTo.set(2015,1,1,hour + 12,minute + TEST_DATAS + 1, 0);
		windowTo.set(Calendar.MILLISECOND, 0);
		
		assertNotEquals("from/to should not equal", windowFrom, windowTo);
		
		List<SensorData> sds = SensorData.findByUserSinceDateForHours(user, windowFrom, 12);
		assertNotNull("fetched but null", sds);
		assertEquals("fetch should be none", 0, sds.size());
	}
	
	@Test
	public void fetchDataForGraphRenderTestRange1() {
		newSensorDatasWithOwner(user);
		newSensorDatasWithOwner(user1);
		
		Calendar windowFrom = Calendar.getInstance();
		windowFrom.set(2015,1,1,hour-1,minute + 5, 0);
		windowFrom.set(Calendar.MILLISECOND, 0);
		
		List<SensorData> sds = SensorData.findByUserSinceDateForHours(user, windowFrom, 1);
		assertNotNull("fetched but null", sds);
		assertEquals("fetch size wrong ", 5, sds.size());
	}
	
	@Test
	public void fetchDataForGraphRenderTestRange2() {
		newSensorDatasWithOwner(user);
		newSensorDatasWithOwner(user1);
		
		Calendar windowFrom = Calendar.getInstance();
		windowFrom.set(2015,1,1,hour, minute + TEST_DATAS / 2, 0);
		windowFrom.set(Calendar.MILLISECOND, 0);
		
		List<SensorData> sds = SensorData.findByUserSinceDateForHours(user, windowFrom, 1);
		assertNotNull("fetched but null", sds);
		assertEquals("fetch size wrong ", TEST_DATAS / 2, sds.size());
	}
	
	@Test
	public void hasSameDataTest() {
		newSensorDatasWithOwner(user);
		
		Calendar rangeFrom = Calendar.getInstance();
		rangeFrom.set(2015, 1,1,hour, minute,0);
		rangeFrom.set(Calendar.MILLISECOND, 0);
		
		Calendar rangeFor  = Calendar.getInstance();
		rangeFor.set(2015, 1, 1, hour, minute + TEST_DATAS -1, 0);
		rangeFor.set(Calendar.MILLISECOND, 0);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		
		for (int h = 0; h < 24;  h ++) {
			for (int m = 0; m < 60; m++) {
				cal.set(2015,1,1, h, m, 0);
				if (cal.compareTo(rangeFrom) >= 0 && cal.compareTo(rangeFor) <= 0) {
					assertTrue("should have same data", SensorData.findSameData(user, cal) != null);
				} else {
					assertTrue("should not have same data", SensorData.findSameData(user, cal) == null);
				}
			}
		}
	}
	
	@Test
	public void hourMinuteWithCalendarTest() {
		for (int h = 0; h < 24; h ++) {
			for (int m = 0; m < 60; m++) {
				Calendar cal = Calendar.getInstance();
				cal.set(2015, 1, 3, h, m, 0);
				cal.set(Calendar.MILLISECOND, 0);
//				System.out.println(String.valueOf(hour) + " " + String.valueOf(minute));
				assertEquals("hourMinute returns wrong result", h*100+m, SensorData.hourMinuteWithCalendar(cal));
			}
		}
	}
}



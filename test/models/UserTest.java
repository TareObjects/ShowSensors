package models;

import models.*;

import org.h2.jdbc.JdbcSQLException;
import org.junit.*;

import static org.junit.Assert.*;
import play.test.WithApplication;
import static play.test.Helpers.*;

public class UserTest extends WithApplication {
	@Before
	public void setUp() {
//		start(fakeApplication(inMemoryDatabase()));
		newKurahashi();		
	}

	public static final String email = "kkurahashi@me.com";
	public static final String pass  = "secret";
	public static final String name  = "koichi kurahashi";
	
	public static final String email1 = "kkurahashi1@me.com";
	public static final String pass1  = "secret1";
	public static final String name1  = "koichi kurahashi 1";
	
	public static User newKurahashi() {
		User user = new User(email, pass, name);
		user.save();
		return user;
	}
	
	public static User newKurahashi1() {
		User user = new User(email1, pass1, name1);
		user.save();
		return user;
	}
	
	@Test
	public void testUserCreateWithoutRequiredData1() {
		try {
			new User(null, pass1, name1).save();
			fail("should be error");
		} catch (Exception ex) {
			
		}
	}
	
	@Test
	public void testUserCreateWithoutRequiredData2() {
		try {
			new User(email1, null, name1).save();
			fail("should be error");
		} catch (Exception ex) {
			
		}
	}
	
	@Test
	public void testUserCreateWithoutRequiredData3() {
		try {
			new User(email1, pass1, null).save();
			fail("should be error");
		} catch (Exception ex) {
			
		}
	}

	@Test
	public void testUserCreateDuplicatEmail() {
		try {
			newKurahashi();
			fail("should be error");
		} catch (Exception ex) {
			
		}
	}

	

	@Test
	public void testUserAuthenticate() {
		User kurahashi = User.authenticate(email, pass);
		assertNotNull(kurahashi);
		assertEquals(name,  kurahashi.name);
		assertEquals(email, kurahashi.email);
		assertEquals(pass,  kurahashi.password);
	}
	
	@Test
	public void testUserAuthenticateWithNull1() {
		User kurahashi = User.authenticate(null, pass);
		assertNull(kurahashi);
	}
	
	@Test
	public void testUserAuthenticateWithNull2() {
		User kurahashi = User.authenticate(email, null);
		assertNull(kurahashi);
	}
	
	@Test
	public void testUserAuthenticateWithNull3() {
		User kurahashi = User.authenticate(null, null);
		assertNull(kurahashi);
	}
	
	@Test
	public void userCreateAndFailToRetrive() {
		User kurahashi = User.authenticate(email, pass);
		assertNotNull(kurahashi);
		assertEquals(name,  kurahashi.name);
		assertEquals(email, kurahashi.email);
		assertEquals(pass,  kurahashi.password);
	}
	
	@Test
	public void testFindByEmail() {
		newKurahashi1();
		
		User kurahashi = User.findByEmail(email);
		assertNotNull(kurahashi);
		assertEquals(name,  kurahashi.name);
		assertEquals(email, kurahashi.email);
		assertEquals(pass,  kurahashi.password);
		
		User kurahashi1 = User.findByEmail(email1);
		assertNotNull(kurahashi1);
		assertEquals(name1,  kurahashi1.name);
		assertEquals(email1, kurahashi1.email);
		assertEquals(pass1,  kurahashi1.password);
		
		User kurahashiNull = User.findByEmail(null);
		assertNull(kurahashiNull);
		
		User kurahashiWrong = User.findByEmail("detarame");
		assertNull(kurahashiWrong);
	}
	
	
}



package models;

import models.*;

import org.h2.jdbc.JdbcSQLException;
import org.junit.*;

import static org.junit.Assert.*;
import play.test.WithApplication;
import static play.test.Helpers.*;

public class TestModels extends WithApplication {
	@Before
	public void setUp() {
//		start(fakeApplication(inMemoryDatabase()));
		newKurahashi();		
	}

	static final String email = "kkurahashi@me.com";
	static final String pass  = "secret";
	static final String name  = "koichi kurahashi";
	
	public static void newKurahashi() {
		new User(email, pass, name).save();
	}
		
}



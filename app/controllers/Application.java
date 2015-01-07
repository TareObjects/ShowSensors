package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import play.*;
import play.data.*;
import static play.data.Form.*;
import play.api.libs.json.*;
import play.libs.Json;
import play.mvc.*;
import views.html.*;
import models.*;


public class Application extends Controller {

	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/*
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:30", "count": 12, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:31", "count":  4, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:32", "count":  4, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:33", "count":  0, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:34", "count": 23, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:35", "count": 21, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:36", "count": 12, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:37", "count":  5, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:38", "count":  1, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
curl --header "Content-type: application/json" --request POST --data '{"date": "2015-01-07 01:39", "count":  1, "email": "kkurahashi@me.com"}' http://localhost:9000/receive
*/
	public static Result receive() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			String strDate = json.findPath("date").textValue();
			System.out.println("[" + strDate + "]");
//			String strCount = json.findPath("count").textValue();
//			System.out.println("[" + strCount + "]");
//			Integer pirCount = Integer.valueOf(strCount);
			Integer pirCount = json.findPath("count").intValue();
			String email = json.findPath("email").textValue();
			System.out.println("[" + email + "]");
			if (strDate == null || pirCount == null || email == null) {
				return badRequest("Missing parameter");
			} else {
				User user = User.findByEmail(email);
				if (user == null)
					return badRequest("email address not found");
				
				Date date = null;
				SensorData sd = null;
				try {
					date = sdf.parse(strDate);
				} catch (ParseException e) {
					e.printStackTrace();
					return badRequest("bad date format");
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.MILLISECOND, 0);
				if (date != null && user != null) {
					sd = SensorData.findSameData(user, cal);
					if (sd != null) {
						sd.logDate  = date;
						sd.pirCount = pirCount;
						sd.update();
					} else {
						sd = new SensorData(date, pirCount, user);
						sd.save();
					}
					return ok("received and saved.");
				} else {
					return badRequest("could not create SensorData");
				}
			}
		}
	}
	
	// curl --header "Content-type: application/json" --request POST --data '{"email": "kkurahashi@me.com", "password": "secret", "name": "Koichi KURAHASHI"}' http://localhost:9000/user/new
	public static Result newUser() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			String strEmail    = json.findPath("email").textValue();
			String strPassword = json.findPath("password").textValue();
			String strName     = json.findPath("name").textValue();

			if (strEmail == null || strPassword == null || strName == null) {
				return badRequest("Missing parameter");
			} else {
				User user = User.findByEmail(strEmail);
				if (user == null) {
					user = new User(strEmail, strPassword, strName);
				}
				if (user != null) {
					user.save();
					return ok("received and saved.");
				} else {
					return badRequest("could not create User");
				}
			}
		}
	}
	

	//curl --header "Content-type: application/json" --request POST --data '{"from": "2015-01-03 00:00", "hours": 12, "email": "kkurahashi@me.com"}' http://localhost:9000/fetchFromHours
	@Security.Authenticated(Secured.class)
	public static Result fetchFromHours() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			String strEmail = json.findPath("email").textValue();
			String strFrom  = json.findPath("from").textValue();
			String strHours   = json.findPath("hours").textValue();
			
			User owner = null;
			owner = User.findByEmail(strEmail == null ? "kkurahashi@me.com" : strEmail);

			Date dateFrom = null;
			try {
				dateFrom = strFrom == null ? new Date() : sdf.parse(strFrom);
			} catch (ParseException e) {
				return badRequest("bad date format");
			}
			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(dateFrom);
			calFrom.set(Calendar.MILLISECOND, 0);

			Integer hours = strHours == null ? 24 : Integer.valueOf(strHours);
			
			List<SensorData> sds = SensorData.findByUserSinceDateForHours(owner, calFrom, hours);
			if (sds == null || sds.size() == 0) {
				return badRequest("no data found");
			}
			
			List<SensorDataRest> sdr = new ArrayList<SensorDataRest>();
			for (SensorData sd : sds) {
				sdr.add(new SensorDataRest(sd));
			}
			
			return ok(Json.toJson(sdr));
		}
	}
	
	// curl --header "Content-type: application/json" --request GET http://localhost:9000/fetchFromHours
	@Security.Authenticated(Secured.class)
	public static Result fetchFromHoursGet(String inEmail) {
		User owner = User.findByEmail(inEmail);
		System.out.println(inEmail);
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(new Date());
		calFrom.add(Calendar.HOUR_OF_DAY, -24);
		calFrom.set(Calendar.MILLISECOND, 0);

		Integer hours = 24;
		
		List<SensorData> sds = SensorData.findByUserSinceDateForHours(owner, calFrom, hours);
		if (sds == null || sds.size() == 0) {
			return badRequest("no data found");
		}
		
		List<SensorDataRest> sdr = new ArrayList<SensorDataRest>();
		for (SensorData sd : sds) {
			sdr.add(new SensorDataRest(sd));
		}
		
		return ok(Json.toJson(sdr));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result graph() {
		return ok(graph.render("dummy"));
	}
	
	
	
	public static Result login() {
        return ok(
            login.render(form(Login.class))
        );
    }
	
	public static Result logout() {
	    session().clear();
	    flash("success", "You've been logged out");
	    return redirect(
	        routes.Application.login()
	    );
	}
	
	public static Result authenticate() {
	    Form<Login> loginForm = form(Login.class).bindFromRequest();
	    if (loginForm.hasErrors()) {
	        return badRequest(login.render(loginForm));
	    } else {
	        session().clear();
	        session("email", loginForm.get().email);
	        return redirect(
	            routes.Application.graph()
	        );
	    }
	}	
	public static class Login {

	    public String email;
	    public String password;

	    public String validate() {
	        if (User.authenticate(email, password) == null) {
	          return "Invalid user or password";
	        }
	        return null;
	    }
	}
	
}

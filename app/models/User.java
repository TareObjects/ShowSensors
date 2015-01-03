package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.avaje.ebean.annotation.CreatedTimestamp;

import play.db.ebean.*;
import play.data.validation.*;



@Entity
@Table(uniqueConstraints = @UniqueConstraint(name="UserConstraint", columnNames = { "email" }))
public class User extends Model {

	@Id
 	public Long   id;
 	
 	@NotNull
 	@Constraints.Email
 	public String email;

 	@NotNull
 	public String password;
 	
 	@NotNull
 	public String name;
 	
 	@OneToMany(cascade=CascadeType.ALL, mappedBy = "dataOwner")
 	List<SensorData> sesnsorDatas = new ArrayList<SensorData>();
 	
	@CreatedTimestamp
	public Date createdDate;
	@Version
	public Date updatedDate;
	
	
 	public User(String inEmail, String inPassword, String inName) {
 		this.email    = inEmail;
 		this.password = inPassword;
 		this.name     = inName;
 	}
 	
 	public static Finder<Long, User> find = new Finder<Long, User> (
 		Long.class, User.class
 	);
 	
 	public static User findByEmail(String inEmail) {
 		User user = User.find.where()
 					.eq("email", inEmail)
 					.findUnique();
 		return user;
 	}
 	
 	public static User authenticate(String inEmail, String inPassword) {
 		return find.where().eq("email",  inEmail).eq("password", inPassword).findUnique();
 	}
	
}
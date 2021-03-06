package models;

//  SensorData.java
//
//  センサーからのデータを記録する
//  記録するデータは毎分毎、1ユーザに付き24時間分。時と分で制約を設ける

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import play.db.ebean.*;

import com.avaje.ebean.annotation.CreatedTimestamp;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "SensorDataConstraint", 
                                             columnNames = {"i_hour_minute", "data_owner_id" }))
public class SensorData extends Model {

    @Id
    @NotNull
    public Long id;

    @NotNull
    public Date logDate;

    @NotNull
    public Integer iHourMinute;

    @NotNull
    public Integer pirCount;
    
    @NotNull
    public Integer doorCount;
    
    public String otherInfo;

    //  many to oneはアクセサで書かないと正しいSQLが生成されない
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private User dataOwner;

    @CreatedTimestamp
    public Date createdDate;
    @Version
    public Date updatedDate;

    public SensorData(Date inDate, Integer inPirCount, Integer inDoorCount, String inOtherInfo, User inUser) {
        this.logDate   = inDate;
        this.pirCount  = inPirCount;
        this.doorCount = inDoorCount;
        this.otherInfo = inOtherInfo;
        this.dataOwner = inUser;

        Calendar cal = Calendar.getInstance();
        cal.setTime(inDate);
        cal.set(Calendar.MILLISECOND, 0);
        this.iHourMinute = hourMinuteWithCalendar(cal);
    }

    public User getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(User inUser) {
        dataOwner = inUser;
    }

    public static Finder<Long, SensorData> find = new Finder<Long, SensorData>(
            Long.class, SensorData.class);

    public static List<SensorData> findByUser(User inUser) {
        return SensorData.find.where().eq("dataOwner.email", inUser.email)
                .orderBy().asc("logDate").findList();
    }

    // 同一ユーザ、同一時分のデータを探す。無ければnull
    public static SensorData findSameData(User inUser, Calendar inCalendar) {
        int hourMinute = inCalendar.get(Calendar.HOUR_OF_DAY) * 100
                + inCalendar.get(Calendar.MINUTE);
        List<SensorData> sds = SensorData.find.where()
                .eq("dataOwner.email", inUser.email)
                .eq("iHourMinute", hourMinute).findList();
        if (sds != null && sds.size() == 1) {
            return sds.get(0);
        } else {
            return null;
        }
    }

    // hh * 100 + mmを返す
    public static int hourMinuteWithCalendar(Calendar inCalendar) {
        return inCalendar.get(Calendar.HOUR_OF_DAY) * 100
                + inCalendar.get(Calendar.MINUTE);
    }

    // あるユーザの指定された時刻から指定された時間分のデータを取得する
    public static List<SensorData> findByUserSinceDateForHours(User inOwner,
            Calendar inFrom, int inDuration) {
        Date from = inFrom.getTime();
        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(from);
        toCalendar.add(Calendar.HOUR_OF_DAY, inDuration);
        toCalendar.set(Calendar.MILLISECOND, 0);
        Date to = toCalendar.getTime();

        return SensorData.find.where().eq("dataOwner.email", inOwner.email)
                                      .ge("logDate", from)
                                      .lt("logDate", to)
                                      .orderBy().asc("logDate")
                                      .findList();
    }
}
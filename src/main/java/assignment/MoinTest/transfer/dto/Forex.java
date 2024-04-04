package assignment.MoinTest.transfer.dto;

import lombok.Getter;

import java.sql.Time;
import java.util.Date;

@Getter
public class Forex {

    private String code;
    private String currencyCode;
    private String currencyName;
    private String country;
    private String name;
    private Date date;
    private Time time;
    private double recurrenceCount;
    private double basePrice;
    private double openingPrice;
    private double highPrice;
    private double lowPrice;
    private String  change;
    private double changePrice;
    private double cashBuyingPrice;
    private double cashSellingPrice;
    private double ttBuyingPrice;
    private double ttSellingPrice;
    private double tcBuyingPrice;
    private double fcSellingPrice;
    private double exchangeCommission;
    private double usDollarRate;
    private double high52wPrice;
    private Date high52wDate;
    private double low52wPrice;
    private Date low52wDate;
    private double currencyUnit;
    private String provider;
    private long timestamp;
    private long id;
    private Date createdAt;
    private Date modifiedAt;
    private double signedChangePrice;
    private double signedChangeRate;
    private double changeRate;

    public Forex() {

    }
}

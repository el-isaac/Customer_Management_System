package Pages;

import java.math.BigDecimal;
import java.sql.Date;


import javax.swing.*;

public class Project {
    private Integer pid;
    private String pname;
    private Integer cid;
    private String cname;
    private String ptype;
    private String location;
    private Date start_date;
    private Date end_date;
    private BigDecimal payment;
    private BigDecimal grossPay;
   // private String description;

    public Project(Integer pid, String pname, Integer cid, String cname,String ptype, String location, Date start_date, Date end_date,  BigDecimal payment, BigDecimal grossPay) {
        this.pid = pid;
        this.pname = pname;
        this.cid = cid;
        this.cname = cname;
        this.ptype = ptype;
        this.location = location;
        this.start_date = start_date;
        this.end_date = end_date;
        this.grossPay = grossPay;
        this.payment = payment;
        //this.description = description;
    }

    public Integer getPid(){return pid;}
    public String getPname(){return pname;}
    public Integer getCid(){return cid;}
    public String getCname(){return cname;}
    public String getPtype(){return ptype;}
    public String getLocation(){return location;}
    public Date getStart_date(){return start_date;}
    public Date getEnd_date(){return end_date;}
    public BigDecimal getPayment(){return payment;}
    public BigDecimal getGrossPay(){return grossPay;}
}

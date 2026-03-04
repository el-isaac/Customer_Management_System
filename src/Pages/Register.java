package Pages;

public class Register {

    private final Integer id;
    private final String name;
    private final String lastName;
    private final String street;
    private final String houseNumber;
    private final String city;
    private final String zip;
    private final String email;
    private final String contact;

    public Register(Integer id, String name, String lastName, String street, String houseNumber, String city, String zip, String contact, String email){
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.zip = zip;
        this.email = email;
        this.contact = contact;
    }
    public Integer getId(){return id;}
    public String getName(){return name;}
    public String getLastName(){return lastName;}
    public String getStreet(){return street;}
    public String getHouseNumber(){return houseNumber;}
    public String getCity(){return city;}
    public String getZip(){return zip;}
    public String getContact(){return contact;}
    public String getEmail(){return email;}

    public String getAddress(){
        return street + " " + houseNumber + ", " + city + " " + zip;
    }

}

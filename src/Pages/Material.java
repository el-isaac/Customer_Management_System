package Pages;

import java.math.BigDecimal;

public class Material {
    private final Integer id;
    private final String name;
    private final String type;
    private final Integer quantity;
    private final BigDecimal price;

    public Material(Integer id, String name, String type, Integer quantity, BigDecimal price){
        this.id = id;
        this.name=name;
        this.type=type;
        this.quantity=quantity;
        this.price=price;
    }
    public Integer getId(){ return id;}
    public String getName(){return name;}
    public String getType(){return type;}
    public Integer getQuantity(){return quantity;}
    public BigDecimal getPrice(){return price;}
}

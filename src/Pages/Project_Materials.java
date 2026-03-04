package Pages;

import java.math.BigDecimal;

public class Project_Materials {
    private final Integer id;
    private final String material_name;
    private final Integer quantity;
    private final BigDecimal price;
    private final BigDecimal total;

    public Project_Materials(int id, String material_name, Integer quantity, BigDecimal price, BigDecimal total){
        this.id = id;
        this.material_name = material_name;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
    }
    public Integer getId(){return id;}
    public String getMaterial_name(){return material_name;}
    public Integer getQuantity(){return quantity;}
    public BigDecimal getPrice(){return price;}
    public BigDecimal getTotal(){return total;}
}

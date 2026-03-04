package Pages;

import javafx.beans.property.*;

import java.math.BigDecimal;

public class Item{
    private int id;
    private final String name;
    private final BigDecimal price;
    private int quantity;

    public Item(int id, String name, BigDecimal price, int quantity){
        this.id = id;
        this.name = name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Properties for TableView
    public int getId(){return id;}
    public  String getName(){return name;}
    public BigDecimal getPrice(){return price;}
    public int getQuantity(){return quantity;}
    public String toString(){
        return name + " - $" + price + " Stock: " + quantity;
    }
}

class SelectedItem{
    private Item item;
    private int quantity;

    public SelectedItem(Item item, int quantity){
        this.item = item;
        this.quantity = quantity;
    }
    public Item getItem(){return item;}
    public int getQuantity(){return quantity;}

    public BigDecimal getTotalPrice(){
        return item.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public String toString(){
        return item.getName() + " x " + quantity + " = $" + getTotalPrice();
    }

}
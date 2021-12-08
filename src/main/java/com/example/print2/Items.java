package com.example.print2;

public class Items {

    private String product;
    private int count;
    private float price;
    //private String cash;

    public Items(){

    }
    public Items(String product,int count,float price){
        this.product = product;
        this.count = count;
        this.price = price;
        //this.cash = cash;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }


}

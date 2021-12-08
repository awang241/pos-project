package com.example.print2;

import java.time.LocalDateTime;


public class Transaction {
    private int TransactionID;
    private LocalDateTime dateTime;

    private float payment;
    private String paymentmethod;

    public Transaction(){

    }
    public Transaction(int ID,LocalDateTime dateTime,float payment,String paymentmethod){
        TransactionID = ID;
        this.dateTime = dateTime;
        this.payment=payment;
        this.paymentmethod = paymentmethod;
    }

    public int getID() {
        return TransactionID;
    }

    public void setID(int ID) {
        TransactionID = ID;
    }
    public LocalDateTime getDateTime(){
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /*
        public Time getTime() {
            return time;
        }

        public void setTime(Time time) {
            this.time = time;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = this.date;
        }
        */
    public String getPaymentmethod() {
        return paymentmethod;
    }

    public void setPaymentmethod(String paymentmethod) {
        this.paymentmethod = paymentmethod;
    }

    public float getPayment() {
        return payment;
    }

    public void setPayment(float payment) {
        this.payment = payment;
    }


}

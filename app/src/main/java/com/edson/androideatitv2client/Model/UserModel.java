package com.edson.androideatitv2client.Model;

public class UserModel {
    private String uid, name, address, phone;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserModel(String uid, String name, String address, String phone) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public UserModel() {
    }
}

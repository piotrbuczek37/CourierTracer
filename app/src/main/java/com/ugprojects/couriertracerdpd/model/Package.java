package com.ugprojects.couriertracerdpd.model;

import java.util.Objects;

/**
 * Model for package object includes all important information about package, allows to get and set them
 */
public class Package {
    private String packageNumber;
    private String address;
    private String postCode;

    public Package() {
    }

    public Package(String packageNumber, String address, String postCode) {
        this.packageNumber = packageNumber;
        this.address = address;
        this.postCode = postCode;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Package aPackage = (Package) o;
        return Objects.equals(packageNumber, aPackage.packageNumber) &&
                Objects.equals(address, aPackage.address) &&
                Objects.equals(postCode, aPackage.postCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageNumber, address, postCode);
    }
}

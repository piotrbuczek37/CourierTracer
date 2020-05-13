package com.ugprojects.couriertracerdpd.model;

import java.util.Objects;

public class Package {
    private String packageNumber;
    private String address;
    private String postCode;

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

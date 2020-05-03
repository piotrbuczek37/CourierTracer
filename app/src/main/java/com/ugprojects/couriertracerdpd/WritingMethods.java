//package com.ugprojects.couriertracerdpd;
//
//import android.graphics.Color;
//import android.view.View;
//
//import com.ugprojects.couriertracerdpd.model.Client;
//import com.ugprojects.couriertracerdpd.model.Courier;
//import com.ugprojects.couriertracerdpd.model.Package;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class WritingMethods {
//    public void addPackage(View view){
//        writeNewPackage("000051216124U","CL12345","FA34","KMEGDN");
//        writeNewPackage("000051216154U","CL12345","FA32","KMEGDN");
//
//        writeNewCourier("KMEGDN","10:00","18:00","Opel", Color.BLUE,1234);
//        writeNewClient("CL12345","Piotr","Kowalski",0,0,0);
//    }
//
//    public void writeNewPackage(String packageNumber,String clientID,String pinCode,String courierID){
//        Package pack = new Package(packageNumber,clientID,pinCode,courierID);
//        Map<String,Object> packagesValues = pack.toMap();
//
//
//        Map<String,Object> childrenUpdates = new HashMap<>();
//        childrenUpdates.put("/packages/"+packageNumber,packagesValues);
//        childrenUpdates.put("courier-packages/"+courierID+"/"+packageNumber+"/",packagesValues);
//        childrenUpdates.put("client-packages/"+clientID+"/"+packageNumber+"/",packagesValues);
//        mDatabase.updateChildren(childrenUpdates);
//    }
//
//    public void writeNewCourier(String courierID, String startTime, String endTime, String car, int carColor, int hhPin){
//        Courier courier = new Courier(courierID,startTime,endTime,car,carColor,hhPin);
//
//        mDatabase.child("couriers").child(courierID).child("firstName").setValue(null);
//        mDatabase.child("couriers").child(courierID).child("startTime").setValue(startTime);
//        mDatabase.child("couriers").child(courierID).child("endTime").setValue(endTime);
//        mDatabase.child("couriers").child(courierID).child("car").setValue(car);
//        mDatabase.child("couriers").child(courierID).child("carColor").setValue(carColor);
//        mDatabase.child("couriers").child(courierID).child("hhPin").setValue(hhPin);
//    }
//
//    public void writeNewClient(String clientID, String firstName, String lastName, long phoneNumber,double latitude, double longitude){
//        Client client = new Client(clientID,firstName,lastName,phoneNumber,latitude,longitude);
//
//        mDatabase.child("clients").child(clientID).child("firstName").setValue(firstName);
//        mDatabase.child("clients").child(clientID).child("lastName").setValue(lastName);
//        mDatabase.child("clients").child(clientID).child("phoneNumber").setValue(phoneNumber);
//        mDatabase.child("clients").child(clientID).child("latitude").setValue(latitude);
//        mDatabase.child("clients").child(clientID).child("longitude").setValue(longitude);
//    }
//}

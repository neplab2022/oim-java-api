package com.neplab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import com.thortech.xl.vo.ddm.RootObject;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Operations.tcExportOperationsIntf;

import oracle.iam.platform.OIMClient;

/**
 *
 * CLI Utility to export OIM components. Similar functionality to OIM Deployment Manager Export.
 *
 * @author Aravind.Suresh
 *
 */
public class OIMExporter {

//    public String OIMUSERNAME = "xelsysadm";
//    public String OIMPASSWORD = "Passw0rd1";
//    public String OIMURL = "t3://172.16.30.206:14000";
//    public String OIMCONFIGPATH = "D:/Softwares/DesignConsole11G/designconsoleLCCDev/config/authwl.conf";

    OIMClient client;
    tcExportOperationsIntf exportOps;
    Config config = new Config();

    public HashMap<Integer , String> exportObjects;
    public HashMap<Integer , RootObject> searchedObjects;
    public HashMap<Integer , RootObject> selectedObjects;
    public HashMap<RootObject , Collection<RootObject>> childObjectMap = new HashMap<RootObject, Collection<RootObject>>();
    public HashMap<RootObject , Collection<RootObject>> dependentObjectMap = new HashMap<RootObject, Collection<RootObject>>();
    public Collection<RootObject> finalSelection = new ArrayList<RootObject>();

    @SuppressWarnings("unchecked")
    public int getObjectSelection(){
        int selection = 0;
        try {
            System.out.println("Select the object type to be exported : ");
            Collection<String> objects = exportOps.retrieveCategories();
            int count= 1;
            exportObjects = new HashMap<Integer, String>();
            for(String object : objects){
                System.out.println(count + " : " + object);
                exportObjects.put(count, object);
                count++;
            }
            System.out.println("Enter a number [1 to " + (count -1) + "] :");
            selection = new Scanner(System.in).nextInt();
        } catch (tcAPIException e) {
            e.printStackTrace();
        }
        return selection;
    }

    @SuppressWarnings("unchecked")
    public void searchObjects(String objectType, String searchString) {
        try {
            System.out.println("Select the object to be exported : ");
            Collection<RootObject> objects = exportOps.findObjects(objectType, searchString);
            int count= 1;
            searchedObjects = new HashMap<Integer, RootObject>();
            for(RootObject object : objects){
                System.out.println(count + " : " + object.getName() + " [" + object.getPhysicalType() + "]");
                searchedObjects.put(count, object);
                count++;
            }
            System.out.println("Enter objects you want export separated by commas [1 to " + (count -1) + " , 0 to select all] :");
            String selection = new Scanner(System.in).next();
            selectedObjects = new HashMap<Integer, RootObject>();
            String[] selectionList = selection.split(",");
            count= 1;
            for(String item : selectionList){
                int itemId = Integer.parseInt(item.trim());
                if(itemId == 0) {
                    selectedObjects.putAll(searchedObjects);
                } else {
                    selectedObjects.put(count, searchedObjects.get(itemId));
                    count++;
                }
            }
        } catch (tcAPIException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void getChildren(RootObject object){
        Collection<RootObject> objects = new ArrayList<RootObject>();
        objects.add(object);
        try {
            Collection<RootObject> childObjects = exportOps.retrieveChildren(objects);
            System.out.println("Children of object : " + object.getName());
            for(RootObject obj : childObjects){
                if(obj != object){
                    System.out.println("==> " + obj.getName() + " [" + object.getPhysicalType() + "]");
                }
            }
            System.out.println("Do you want to export the child objects? (y/n) : ");
            if(new Scanner(System.in).next().equals("y")){
                childObjectMap.put(object, childObjects);
            }
        } catch (tcAPIException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void getDependencies(RootObject object){
        Collection<RootObject> objects = new ArrayList<RootObject>();
        HashMap<Integer, RootObject> dependentMap = new HashMap<Integer, RootObject>();
        HashMap<Integer, RootObject> delectedDependentMap = new HashMap<Integer, RootObject>();
        objects.add(object);
        try {
            Collection<RootObject> dependentObjects = exportOps.getDependencies(objects);
            System.out.println("Dependents of object : " + object.getName());
            int count= 1;
            for(RootObject obj : dependentObjects){
                if(obj != object){
                    System.out.println("==> " + count + " : " + obj.getName() + " [" + obj.getPhysicalType() + "]");
                    dependentMap.put(count, obj);
                    count++;
                }
            }
            System.out.println("Enter objects you want export separated by commas [1 to " + (count -1) + " , 0 to select all, x to select none] :");
            String selection = new Scanner(System.in).next();
            String[] selectionList = selection.split(",");
            count= 1;
            for(String item : selectionList){
                String itemId = item.trim();
                if(itemId.equals("0")) {
                    delectedDependentMap.putAll(dependentMap);
                } else if(itemId.equalsIgnoreCase("x")){
                    // Do nothing
                } else {
                    delectedDependentMap.put(count, dependentMap.get(itemId));
                    count++;
                }
            }
            dependentObjectMap.put(object, delectedDependentMap.values());
        } catch (tcAPIException e) {
            e.printStackTrace();
        }
    }

    public String exportObjects(){
        String xml = "";
        System.out.println("Enter export description : ");
        String description = new Scanner(System.in).next();
        try {
            xml = exportOps.getExportXML(finalSelection, description);
        } catch (tcAPIException e) {
            e.printStackTrace();
        }
        return xml;
    }

    public void writeToFile(String xml, String filePath) {
        BufferedWriter bw = null;
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(xml);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute() {
        Scanner inputScanner = new Scanner(System.in);
        config.getOIMConnection();
        while(true){
            int selection = getObjectSelection();
            String selectedObjectType = exportObjects.get(selection);
            System.out.println("Selection : " + selection + " [" + exportObjects.get(selection) + "]");
            System.out.println("Enter your search criteria : ");
            String searchString = inputScanner.next();
            while (true) {
                searchObjects(selectedObjectType, searchString);
                System.out.println("Selection : ");
                for (RootObject object : selectedObjects.values()) {
                    System.out.println(object.getName() + " [" + object.getPhysicalType() + "]");
                }
                System.out.println("Confirm selection [c] or Review selection again [r] : ");
                if (inputScanner.next().equals("c")) {
                    break;
                }
            }

            for (RootObject object : selectedObjects.values()) {
                getChildren(object);
                while(true){
                    getDependencies(object);
                    System.out.println("Selection : ");
                    for (RootObject obj : dependentObjectMap.get(object)) {
                        System.out.println(obj.getName() + " [" + obj.getPhysicalType() + "]");
                    }
                    System.out.println("Confirm selection [c] or Review selection again [r] : ");
                    if (inputScanner.next().equals("c")) {
                        break;
                    }
                }
                finalSelection.add(object);
                if(childObjectMap.containsKey(object)){
                    finalSelection.addAll(childObjectMap.get(object));
                }
                if(dependentObjectMap.containsKey(object)){
                    finalSelection.addAll(dependentObjectMap.get(object));
                }
            }

            System.out.println("Do you want to export another object? (y/n) : ");
            if(!inputScanner.next().equalsIgnoreCase("y")){
                break;
            }

        }
        System.out.println("Enter the file path where you want to save : ");
        String filePath = inputScanner.next();
        writeToFile(exportObjects(), filePath);
        System.out.println("Exported file saved to : " + filePath);

        System.out.println("Exiting...");
        config.disconnect();
    }

    public static void main(String[] args) {
        OIMExporter oim = new OIMExporter();
        oim.execute();
    }
}

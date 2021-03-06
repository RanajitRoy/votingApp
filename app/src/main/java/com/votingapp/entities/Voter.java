/*
 * Author: Ranajit Roy, Abhishek Mukherjee
 */

package com.votingapp.entities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.votingapp.entities.Candidate;

import java.sql.*;
import java.io.*;
import java.io.Serializable;
import java.util.Base64;

public class Voter implements Serializable {
    private String name;
    private String father;
    private String epic_no;
    private String dob;
    private String address;
    private String cid;
    private String sex;
    private String password;
    private Boolean alreadyVoted;
    private Candidate selectedCand;
    private Connection con=null;
    private Statement stmt=null;
    private Boolean secondTry = false;

    public void setVoter(String epic_no, String name, String father, String dob, String address, String sex, String cid, String password) {
        this.name = name;
        this.father = father;
        this.epic_no = epic_no;
        this.dob = dob;
        this.cid = cid;
        this.sex = sex;
        this.address = address;
        this.password = password;
        this.alreadyVoted= true;
        this.selectedCand = null;
    }

    public void setSecondTry(Boolean secondTry) {
        this.secondTry = secondTry;
    }

    public Candidate getSelectedCand() {
        return selectedCand;
    }

    public void setSelectedCand(Candidate selectedCand) {
        this.selectedCand = selectedCand;
    }

    public String getName() {
        return name;
    }

    public String getFather() {
        return father;
    }

    public String getEpic_no() {
        return epic_no;
    }

    public String getDob() {
        return dob;
    }

    public String getCid() {
        return cid;
    }

    public String getSex() {
        return sex;
    }

    public Boolean getAlreadyVoted() {
        return alreadyVoted;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFather(String father) {
        this.father = father;
    }

    public void setEpic_no(String epic_no) {
        this.epic_no = epic_no;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setAlreadyVoted(Boolean alreadyVoted) {
        this.alreadyVoted = alreadyVoted;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String  execute(String EPIC, String pass, int CASE){
        String sendString="";
        String originalPass="";
        ResultSet rs=null;
        try{
            switch(CASE){

                case 1://at login
                    con = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/voterdb", "root", "");



                    stmt=con.createStatement();
                    System.out.println(EPIC);
                    rs=stmt.executeQuery("select * from voter where epic = '"+EPIC+"'");

                    int countRows=0;
                    while(rs.next())
                    {
                        for(int i=1;i<8;i++)
                        {
                            sendString+=rs.getString(i)+"$";
                        }

                        originalPass=rs.getString(8);
                        sendString+=originalPass;

                        countRows++;
                    }


                    if(countRows!=1){
                        //Invalid Login
                        sendString="INVALID";
                    }else {
                        if(!originalPass.equals(pass))
                            sendString="INVALID";

                    }
                    rs.close();
                    stmt.close();
                    con.close();

                    break;


                case 2: // fetching already voted or not from ElectionDB
                    con = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/electiondb", "root", "");

                    stmt=con.createStatement();

                    rs=stmt.executeQuery("select * from voter where epic = '"+EPIC+"'");

                    while(rs.next())
                    {
                        for(int i=1;i<4;i++)
                            sendString+=rs.getString(i)+" ";
                    }
                    rs.close();
                    stmt.close();
                    con.close();
                    break;



                case 4: // verify Vote
                    con = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/electiondb", "root", "");
                    stmt=con.createStatement();

                    rs=stmt.executeQuery("select PARTYID from vote where EPIC = '"+EPIC+"'");

                    String PartyID = null;
                    String CID = null;
                    if(rs.next())
                    {
                        PartyID = rs.getString(1);
                    }

                    rs.close();
                    rs=stmt.executeQuery("select CID from voter where EPIC = '"+EPIC+"'");

                    if(rs.next())
                    {
                        CID = rs.getString(1);
                    }

                    rs.close();
                    rs=stmt.executeQuery("select NAME from candidate where CID = '"+CID+"' and PARTYID = '"+PartyID+"'");

                    if(rs.next())
                    {
                        sendString+=rs.getString(1)+"$"+PartyID;
                    }
                    if(sendString.equals(""))
                    {
                        sendString = "INVALID";
                    }
                    rs.close();
                    stmt.close();
                    con.close();
                    break;


                case 5:
                    boolean resultStr = true;
                    String knownStr = null;
                    File sentPic = new File("sentPic.jpg");
                    File knownPic = new File("knownPic.jpg");

                    con = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/voterdb", "root", "");

                    stmt=con.createStatement();

                    rs=stmt.executeQuery("select image from photographs where epic = '"+EPIC+"'");

                    while(rs.next())
                    {
                        knownStr = rs.getString(1);
                    }
                    rs.close();
                    stmt.close();
                    con.close();

                    try {
                        byte[] byteArray1 = Base64.getDecoder().decode(pass);
                        byte[] byteArray2 = Base64.getDecoder().decode(knownStr);
                        if (!sentPic.exists()) {
                            if (!sentPic.createNewFile()){
                                resultStr = false;
                                sendString = "INVALID";
                                sentPic.delete();
                                break;
                            }
                        }
                        else {
                            resultStr = false;
                            sendString = "INVALID";
                            sentPic.delete();
                            break;
                        }
                        if (!knownPic.exists()) {
                            if (!knownPic.createNewFile()) {
                                resultStr = false;
                                sendString = "INVALID";
                                knownPic.delete();
                                break;
                            }
                        }
                        else {
                            resultStr = false;
                            knownPic.delete();
                            sendString = "INVALID";
                            break;
                        }
                        FileOutputStream fos = new FileOutputStream(sentPic);
                        fos.write(byteArray1);
                        fos.close();
                        fos = new FileOutputStream(knownPic);
                        fos.write(byteArray2);
                        fos.close();
                        System.out.println("Image Received");

                        Runtime rt = Runtime.getRuntime();
                        String[] commands = {"py", "script.py"};
                        Process proc = rt.exec(commands);

                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(proc.getInputStream()));

                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(proc.getErrorStream()));

                        String s = null;
                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                            if(s.equals("False"))
                            {
                                resultStr = false;
                            }
                        }

                        while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                            resultStr = false;
                        }

                        sentPic.delete();
                        knownPic.delete();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        resultStr = false;
                        sentPic.delete();
                        knownPic.delete();
                    }
/*                    FileWriter myWriter = new FileWriter("pic.txt");
                    myWriter.write(pass);
                    myWriter.close();*/
                    if( resultStr )
                    {
                        sendString = "VALID";
                    }
                    else {
                        sendString = "INVALID";
                    }
                    break;

                case 6: // Insert the user vote into the database
                    con = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/electiondb", "root", "");
                    stmt=con.createStatement();
                    int partyID= Integer.parseInt(pass);
                    try{
                        stmt.executeUpdate("INSERT INTO vote ( EPIC , PARTYID ) VALUES ( '"+EPIC+"' , '"+partyID+"' )");
                        stmt.executeUpdate("UPDATE voter SET VOTED = '"+1+"' WHERE EPIC = '"+EPIC+"' " );

                    }
                    catch(Exception e){


                        sendString="INVALID";


                    }

            }//switch


        }
        catch(Exception e){
            System.out.println(e);
        }

        return sendString;

    }

}
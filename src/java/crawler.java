/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author anu
 */
public class crawler {
String html="";
URL oracle = new URL("http://..........gr");
BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));;
String inputLine;
while ((inputLine = in.readLine()) != null)
                html+=inputLine;
in.close(); 
}

package com.erikpartridge;
import spark.Request;
import spark.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;

import static spark.Spark.*;

/**
 * Created by erik on 10/08/16.
 */
public class Application {

    private static final String[] cardMaster = {"AS", "2S", "3S", "4S", "5S", "6S", "7S", "8S", "9S", "10S", "JS", "QS", "KS", "AD", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "10D", "JD", "QD", "KD", "AC", "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "10C", "JC", "QC", "KC", "AH", "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "10H", "JH", "QH", "KH"};

    private static LinkedList<String> deck = new LinkedList<>(Arrays.asList(cardMaster));

    public static void main(String[] args){
        for(int i = 0; i < 100; i++){
            Collections.shuffle(deck, new SecureRandom());
        }
        get("/", (request, response) -> {
            Scanner scanner = new Scanner(Application.class.getClassLoader().getResourceAsStream("index.html"));
            String contents =  "";
            while(scanner.hasNextLine()){
                contents = contents + scanner.nextLine();
            }
            scanner.close();
            contents = contents.replace("CONTENT", "");
            response.body(contents);
            return contents;
        });
        post("/", Application::processRequest);

    }

    private static String processRequest(Request req, Response res) throws FileNotFoundException {
        Scanner scanner = new Scanner(Application.class.getClassLoader().getResourceAsStream("index.html"));
        String contents =  "";
        while(scanner.hasNextLine()){
            contents = contents + scanner.nextLine();
        }
        scanner.close();
        //
        Scanner dataScan = new Scanner(new File("/home/root/TeaParty/src/main/resources/data.csv"));
        ArrayList<String> data = new ArrayList<>();
        while(dataScan.hasNextLine()){
            data.add(dataScan.nextLine());
        }
        dataScan.close();
        String request = req.queryParams("command").toUpperCase();
        //
        if(request.startsWith("DEAL")){
            String cid = request.split(" ")[1];
            if(!StringUtils.isNumeric(cid)){
                halt(402);
            }
            for(int i = 0; i < data.size(); i++) {
                String entry = data.get(i);
                if (entry.split(" ")[0].equals(cid)) {
                    if (entry.split(" ").length > 5) {
                        contents = contents.replace("CONTENT", "FAILURE: HAND FULL");
                        res.body(contents);
                        return contents;
                    } else {
                        data.remove(i);
                        String card = getCard();
                        contents = contents.replace("CONTENT", "SUCCESS: DEALT " + card);
                        res.body(contents);
                        entry = entry + " " + card;
                        data.add(entry);
                        writeToDisk(data);
                        return contents;
                    }
                }
            }
            String c1 = getCard();

            String c2 = getCard();
            data.add(cid + " " + c1 + " " + c2);
            writeToDisk(data);
            contents = contents.replace("CONTENT", "SUCCESS: NEW USER DEALT " + c1 + "," + c2);
            return contents;
        }else if(request.startsWith("DROP")){
            String cid = request.split(" ")[2].toUpperCase();
            String card = request.split(" ")[1].toUpperCase();
            if(!org.apache.commons.lang3.StringUtils.isNumeric(cid)){
                halt();
            }
            for(int i = 0; i < data.size(); i++) {
                String entry = data.get(i);
                if (entry.split(" ")[0].equals(cid)) {
                    if (entry.split(" ").length > 0) {
                        for(int j = 1; j < entry.split(" ").length; j++){
                            if(entry.split(" ")[j].equals(card)){
                                data.remove(i);
                                entry = entry.replace(" " + card, "");
                                data.add(entry);
                                writeToDisk(data);
                                contents = contents.replace("CONTENT", "SUCCESS: REMOVED " + card);
                                res.body(contents);
                                return contents;
                            }
                        }
                        contents = contents.replace("CONTENT", "FAILURE: THIS PLAYER DOES NOT HAVE THE SPECIFIED CARD");
                        res.body(contents);
                        return contents;
                    } else {
                        contents = contents.replace("CONTENT", "FAILURE: THIS PLAYER HAS NO CARDS");
                        res.body(contents);
                        return contents;
                    }
                }
            }
        }else if(request.startsWith("LIST")){
            String result = "";
            for(String line : data){
                result += line + "<br>";
            }
            contents = contents.replace("CONTENT", "SUCCESS: LIST BELOW<br>" + result);
            res.body(contents);
            return contents;
        }else if(request.startsWith("SHOW")){
            String result = "Your hand presently contains the following cards: ";
            String cid = request.split(" ")[1];
            if(!StringUtils.isNumeric(cid)){
                halt(402);
            }
            for(String line : data){
                if(line.startsWith(cid)){
                    contents = contents.replace("CONTENT", result + line.replace(cid + " ", ""));
                    res.body(contents);
                    return contents;
                }
            }
        }
        res.body(contents);
        return contents;
    }

    private static void writeToDisk(ArrayList<String> data) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File("/home/root/TeaParty/src/main/resources/data.csv"));
        for(String line : data){
            writer.write(line + "\n");
        }
        writer.flush();
        writer.close();
    }

    private static String getCard(){
        if(deck.size() > 0){
        }else{
            deck = new LinkedList<>(Arrays.asList(cardMaster));
            for(int i = 0; i < 100; i++){
                Collections.shuffle(deck, new SecureRandom());
            }
        }
        String card = deck.get(0);
        deck.removeFirst();
        return card;
    }
}

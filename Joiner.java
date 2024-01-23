package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Joiner {
    String tableFirst, tableSecond, columnFirst, columnSecond;


    Joiner(List<String> words, String fileName) throws SyntaxException{
        tableFirst = fileName;
        tableSecond = words.get(0);
        if(!(words.get(1).equals("on") ||
             words.get(2).equals(tableFirst) ||
             words.get(4).equals("="))){
            throw new SyntaxException("error near JOIN");
        }
        columnFirst = words.get(3);
        tableSecond = words.get(5);
        columnSecond = words.get(6);
    }
    
    public List<Map<String, String>> join(Map<String, String> prevData) throws SyntaxException, FileNotFoundException{
        if(!prevData.containsKey(columnFirst))
            throw new SyntaxException("error in JOIN: no such column " + columnFirst);
        String[] columns = null;
        List<Map<String, String>> newData = new ArrayList<>();
        File FILE = new File(tableSecond+ ".txt");
        try (Scanner myReader = new Scanner(FILE)) {
            columns = myReader.nextLine().split(",");
            while (myReader.hasNextLine()) {
                String[] values = myReader.nextLine().split(",");
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    row.put(columns[i], values[i]);
                }
                newData.add(row);
            }
        }
        List<Map<String, String>> outputData = new ArrayList<>();
        for (Map<String, String> m : newData) {
            if (m.get(columnSecond).equals(prevData.get(columnFirst))) {
                Map<String, String> tmp = new LinkedHashMap<>(prevData); 
                tmp.putAll(m);
                outputData.add(tmp);
            }
        }
        return outputData;
    }
}

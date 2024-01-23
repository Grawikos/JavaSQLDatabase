package project;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser {
    private Scanner scanner = new Scanner(System.in);
    private File FILE;
    private List<String> words = new ArrayList<>();
    private boolean debugMode = false;

    public Parser() {};
    public Parser(boolean debugging){
        debugMode = debugging;
    }

    public boolean input(String in){
        if(in.equals("q"))
            return false;
        tokenizeCommand(in);
        if(debugMode)
            System.out.println(words); 
        return true;
    }

    public boolean userInput() {
        String input = scanner.nextLine();
        if(input.equals("q"))
            return false;
        tokenizeCommand(input);
        if(debugMode)
            System.out.println(words); 
        return true;
    }


    private void tokenizeCommand(String input) {
        String specialCharPattern = "[=<>]+|>=|<=|!=|<>";
        String commaWordPattern = "[a-zA-Z_]+,?";
        String parenthesesPattern = "\\([^\\)]+\\)";
        String singleQuotesPattern = "'[^']*'";
        String numericValuePattern = "\\d+(\\.\\d+)?";
        String singleStar = "\\*";


        String combinedPattern = String.format("(%s|%s|%s|%s|%s|%s)", singleStar, specialCharPattern, commaWordPattern, parenthesesPattern, singleQuotesPattern, numericValuePattern);

        Pattern regex = Pattern.compile(combinedPattern);
        Matcher matcher = regex.matcher(input);

        while (matcher.find()) {
            String word = matcher.group();
            if(!((word.startsWith("'") && word.endsWith("'")) ||
                 (word.startsWith("(") && word.endsWith(")"))))
                word = word.toLowerCase();
            words.add(word);
        }
    }

    public void parse() throws IOException{
        if(words.size() == 0)
            return;
        try{
        switch (words.remove(0)) {
            case "select":
                parseSelect();
                break;
            case "insert":
                parseInsert();
                break;
            case "update":
                parseUpdate();
                break;
            case "delete":
                parseDelete();
                break;
            case "create":
                parseCreate();
                break;
            default:
                throw new SyntaxException("undefined command");
        }
        }catch(SyntaxException e){
            System.err.println(e);
        }finally{
            words.clear();
        }
    }

    private void wordCheck(String next) throws SyntaxException{
        if(words.size() == 0)
            throw new SyntaxException("expected: " + next.toUpperCase());
        if(words.remove(0).equals(next))
            return;
        throw new SyntaxException("expected: " + next.toUpperCase());
    }

    private void checkWordsLength(int n, String message) throws SyntaxException{
        if(words.size() < n){
            throw new SyntaxException(message);
        }
    }

    private void parseCreate() throws IOException, SyntaxException{
        wordCheck("table");
        checkWordsLength(2, "error near CREATE");
        FILE = new File(words.remove(0) + ".txt");
        FILE.createNewFile();
        String wordWithCols = words.get(0);
        if(!(wordWithCols.startsWith("(") && wordWithCols.endsWith(")")))
            throw new SyntaxException("argument should be in ()");
        String[] columns = wordWithCols.substring(1, wordWithCols.length() - 1).split(", *");
        try (FileWriter fWriter = new FileWriter(FILE, true)) {
            String output = "";
            for (String s : columns) {
                output += s;
                output += ",";
            }
            output = (output.substring(0, output.length() - 1) + "\n");
            fWriter.write(output);
        }
    }

    private List<String> getWordsComma() throws SyntaxException{
        List<String> wantedWords = new ArrayList<>();
        while (true) {
            checkWordsLength(1, "error near column names");
            String word = words.get(0);
            if (word.endsWith(",")) {
                wantedWords.add(word.substring(0, word.length() - 1));
            } else {
                wantedWords.add(word);
                words.remove(0);
                break;
            }
            words.remove(0);
        }
        return wantedWords;
    }


    private void parseSelect() throws SyntaxException, FileNotFoundException {
        List<String> wantedCols = getWordsComma();
        wordCheck("from");
        checkWordsLength(1, "error near FROM");
        String fileName = words.remove(0) + ".txt"; 
        FILE = new File(fileName);
        List<Map<String, String>> data = populateFromFile();
        data = addJoins(data, fileName);
        String[] columns = data.get(0).keySet().toArray(new String[0]);
        if(wantedCols.get(0).equals("*")){
            wantedCols = Arrays.asList(columns);
        }
        Condition contition = new Condition(words, columns);
        Grouper grouper = new Grouper(words, columns);
        grouper.groupBy(data);
        printHeader(wantedCols);
        printData(data, wantedCols, contition);
    }

    private List<Map<String, String>> addJoins(List<Map<String, String>> data, String fileName) throws SyntaxException, FileNotFoundException{
        List<Joiner> otherFiles = new ArrayList<>();
        for(int i = 0; i < words.size(); i++){
            String word = words.get(i);
            if(word.equals("join")){
                checkWordsLength(8, "error near: JOIN");
                Joiner joiner = new Joiner(words.subList(i + 1, i + 8), fileName);
                otherFiles.add(joiner);
                i += 7;
            }
        }
        
        for(Joiner j : otherFiles){ 
            List<Map<String, String>> newData = new ArrayList<>();   
            for(Map<String, String> map : data){
                newData.addAll(j.join(map));
            }
            data = newData;
        }
        return data;
    }


    private void printHeader(List<String> cols) {
        for (String s : cols) {
            System.out.print(String.format("%20s", s));
        }
        System.out.println("\n");
    }


    private void printData(List<Map<String, String>> data, List<String> wantedCols, Condition condition) throws SyntaxException {
        while (data.isEmpty() == false) {
            Map<String, String> row = data.remove(0);
            if(condition.evaluate(row)){
                for(String s : wantedCols){
                    System.out.print(String.format("%20s", row.get(s)));
                }
                System.out.println();

            }
        }
    }

    private void parseInsert() throws SyntaxException, FileNotFoundException {
        wordCheck("into");
        checkWordsLength(3, "error near INSERT");
        FILE = new File(words.remove(0) + ".txt");
        String[] columns = null, wantedCols = null;

        try (Scanner myReader = new Scanner(FILE)) {
            columns = myReader.nextLine().split(",");
            wantedCols = words.get(0).substring(1, words.get(0).length() - 1).split(", *");
        }
        words.remove(0);
        wordCheck("values");
        checkWordsLength(1, "error near VALUES");
        String valuesWord  = words.get(0);
        if(!(valuesWord.startsWith("(") && valuesWord.endsWith(")"))){
            throw new SyntaxException("error near VALUES, expected: ()");
        }
        String[] values = valuesWord.substring(1, valuesWord.length() - 1).split(", *");
        writeData(columns, wantedCols, values);
    }

    private void writeData(String[] columns, String[] wantedCols, String[] values) {
        String output = "";
        for(int i = 0; i < values.length; i++){

            values[i] = values[i].substring(1, values[i].length() - 1);
        }
        List<String> listWantedCols = Arrays.asList(wantedCols);
        try (FileWriter fWriter = new FileWriter(FILE, true)) {
            for (String s : columns) {
                if (listWantedCols.contains(s)) {
                    int id = listWantedCols.indexOf(s);
                    output += values[id] += ",";
                } else {
                    output += "NULL,";
                }
            }
            output = (output.substring(0, output.length() - 1) + "\n");
            fWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error in writing");
        }
    }

    private void parseUpdate() throws SyntaxException, FileNotFoundException {
        checkWordsLength(2, "error near UPDATE");
        FILE = new File(words.remove(0) + ".txt");
        wordCheck("set");
        Map<String, String> toUpdate = new HashMap<>();
        int wordNum = getToUpdateMap(toUpdate);
        for (int i = 0; i < wordNum; i++) {
            words.remove(0);
        }
        List<Map<String, String>> data = populateFromFile();
        String[] columns = data.get(0).keySet().toArray(new String[0]);
        Condition condition = new Condition(words, columns);
        updateData(toUpdate, data, condition);
        writeUpdatedData(columns, data);
    }

    private List<Map<String, String>> populateFromFile() throws FileNotFoundException{
        List<Map<String, String>> data = new ArrayList<Map<String,String>>();
        String[] columns = null;
        try (Scanner myReader = new Scanner(FILE)) {
            columns = myReader.nextLine().split(",");
            while (myReader.hasNextLine()) {
                String[] values = myReader.nextLine().split(",");
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    row.put(columns[i], values[i]);
                }
                data.add(row);
            }
        }
        return data;
    }

    private int getToUpdateMap(Map<String, String> toUpdate) {
        int wordNum = 0;
        for (int i = 0; i < words.size() - 1; i++) {
            if (words.get(i).equals("where")) {
                break;
            }
            if (words.get(i).equals("=") && wordNum % 3 == 1) {
                String val = words.get(i + 1);
                if (val.endsWith(","))
                    toUpdate.put(words.get(i - 1), val.substring(0, val.length() - 1));
                else
                    toUpdate.put(words.get(i - 1), val);
            }
            wordNum++;
        }
        return wordNum;
    }

    private void updateData(Map<String, String> toUpdate, List<Map<String, String>> data, Condition condition) throws SyntaxException {
        for (Map<String, String> row : data) {
            if(!condition.evaluate(row.values().toArray())){
                continue;
            }
            else{
                for (Map.Entry<String, String> entry : toUpdate.entrySet()) {
                    String columnName = entry.getKey();
                    String updatedValue = entry.getValue();
                    if(updatedValue.endsWith("'") && updatedValue.startsWith("'"))
                        updatedValue = updatedValue.substring(1, updatedValue.length() - 1);
                    else
                        throw new SyntaxException("expected: '");
                    if(!row.containsKey(columnName))
                        throw new SyntaxException("no such column: " + columnName);
                    row.put(columnName, updatedValue);
                }
            }
        }
    }

    private void writeUpdatedData(String[] columns, List<Map<String, String>> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE))) {
            writer.write(String.join(",", columns));
            writer.newLine();

            for (Map<String, String> row : data) {
                List<String> rowValues = new ArrayList<>();
                for (String column : columns) {
                    rowValues.add(row.get(column));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
            }

            System.out.println("File updated successfully.");
        } catch (IOException e) {
            System.err.println("Error in writing to the file.");
            e.printStackTrace();
        }
    }


    private void parseDelete() throws SyntaxException, FileNotFoundException{
        wordCheck("from");
        checkWordsLength(1, "error near DELETE");
        FILE = new File(words.remove(0) + ".txt");
        
        String[] columns = null;
        List<Map<String, String>> data = new ArrayList<>();
        try (Scanner myReader = new Scanner(FILE)) {
            columns = myReader.nextLine().split(",");
            Condition condition = new Condition(words, columns);
            while (myReader.hasNextLine()) {
                String[] values = myReader.nextLine().split(",");
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    row.put(columns[i], values[i]);
                }
                if(!condition.evaluate(row.values().toArray()))
                    data.add(row);
            }
        }
        writeUpdatedData(columns, data);
    }
}

package project;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Condition {
    boolean isJustTrue;
    String compare;
    int index;
    String columnName;
    String type;
    
    Condition(List<String> words, String[] columns) throws SyntaxException {
        int whereIndex = words.indexOf("where");
        if (whereIndex == -1) {
            isJustTrue = true;
        } else {
            if (!((words.size() - whereIndex >= 4) && words.get(whereIndex + 3).startsWith("'") &&
                    words.get(whereIndex + 3).endsWith("'"))) {
                throw new SyntaxException("condition is invalid");
            }
            columnName = words.get(whereIndex + 1);
            type = words.get(whereIndex + 2);
            if (!Arrays.asList(columns).contains(columnName)) {
                throw new SyntaxException("condition is invalid - wrong column name");
            }

            index = Arrays.asList(columns).indexOf(columnName);
            compare = words.get(whereIndex + 3).substring(1, words.get(whereIndex + 3).length() - 1);
        }
    }

    private boolean compareDiff(int num1, int num2){
        switch (type) {
            case ">":
                return num1 > num2;
            case "<":
                return num1 < num2;
            case ">=":
                return num1 >= num2;
            case "<=":
                return num1 <= num2;
            default:
                return false;
        }
    }


    public boolean evaluate(String[] data) throws SyntaxException
    {
        if(isJustTrue)
            return true;
        int num1 = 0, num2 = 0;
        switch (type) {
            case "=":
            case "is":
                return data[index].equals(compare);
            case ">":
            case "<":
            case ">=":
            case "<=":
                num1 = Integer.parseInt(data[index]);
                num2 = Integer.parseInt(compare);
                break;
            default:
                throw new SyntaxException("condition unrecognized: " + type);
        }
        
        return compareDiff(num1, num2);
    }

    public boolean evaluate(Object[] data) throws SyntaxException
    {
        if(isJustTrue)
            return true;
        int num1 = 0, num2 = 0;
        switch (type) {
            case "=":
            case "is":
                return Array.get(data, index).equals(compare);
            case ">":
            case "<":
            case ">=":
            case "<=":
                num1 = Integer.parseInt(Array.get(data, index).toString());
                num2 = Integer.parseInt(compare);
                break;
            default:
                throw new SyntaxException("condition unrecognized: " + type);
        }
        return compareDiff(num1, num2);
    }

    
    public boolean evaluate(Map<String, String> data) throws SyntaxException
    {
        if(isJustTrue)
            return true;
        int num1 = 0, num2 = 0;
        switch (type) {
            case "=":
            case "is":
                return data.get(columnName).equals(compare);
            case ">":
            case "<":
            case ">=":
            case "<=":
                num1 = Integer.parseInt(data.get(columnName));
                num2 = Integer.parseInt(compare);
                break;
            default:
                throw new SyntaxException("condition unrecognized: " + type);
        }
        return compareDiff(num1, num2);
    }
}

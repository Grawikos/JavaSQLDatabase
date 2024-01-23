package project;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Grouper {
    boolean noGruoping = false;
    int index = -1;
    String columnName;

    Grouper(List<String> words, String[] columns) throws SyntaxException {
        int groupIndex = words.indexOf("group"), byIndex = words.indexOf("by");
        if (groupIndex == -1) {
            noGruoping = true;
        } else {
            if (!(byIndex == groupIndex + 1 &&
                 words.size() - byIndex >= 2)) {
                throw new SyntaxException("group condition is invalid");
            }

            columnName = words.get(byIndex + 1);
            if (!Arrays.asList(columns).contains(columnName)) {
                throw new SyntaxException("group condition is invalid - wrong column name");
            }

            index = Arrays.asList(columns).indexOf(columnName);
        }
    }
    
    public void groupBy(List<Map<String, String>> data){
        if(noGruoping)
            return;
        Collections.sort(data, Comparator.comparing(m -> m.get(columnName)));
    }
}

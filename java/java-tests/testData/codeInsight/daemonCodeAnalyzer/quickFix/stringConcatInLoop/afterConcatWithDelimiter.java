// "Change type of 'res' to StringBuilder" "true"

public class Main {
    String test(String[] strings) {
        /*within*/
        StringBuilder res = new StringBuilder();
        for (String s : strings) {
            if (/*before*/res.length() > 0) {
                res.append(", ");
            }
            res.append(s);
        }
        return res.toString();
    }
}

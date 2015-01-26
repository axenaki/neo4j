package helper;

import java.util.Iterator;

public class Utilities {

    public static <T> boolean isEmpty(Iterable<T> lista) {
        Iterator foo = lista.iterator();
        return !foo.hasNext();
    }
}

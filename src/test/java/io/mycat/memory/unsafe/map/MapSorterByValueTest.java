package io.mycat.memory.unsafe.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Created by znix on 2016/7/4.
 */
public class MapSorterByValueTest {
    @Test
    public void testMapSorterByValue() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("q", 23);
        map.put("b", 4);
        map.put("c", 5);
        map.put("d", 6);

        Map<String, Integer> resultMap = mapSorterByValue(map); //order by Value

        for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    private Map<String, Integer> mapSorterByValue(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

        List<Map.Entry<String, Integer>> entryList = new ArrayList<
                Map.Entry<String, Integer>>(
                map.entrySet());

        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();
        Map.Entry<String, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }
}



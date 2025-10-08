package kiaatix.polygoncutout.util;

import org.openstreetmap.josm.data.osm.*;
import kiaatix.polygoncutout.polygon.MultiPolygon;

import java.util.*;
import java.util.stream.Collectors;

public class MultipolygonUtils {
    /**
     * Prüft, ob zwei Multipolygone mindestens einen gemeinsamen Way oder zwei gemeinsame Nodes teilen.
     */
    public static boolean multipolygonsTouch(MultiPolygon mp1, MultiPolygon mp2) {
        return haveCommonWay(mp1, mp2); // || haveAtLeastTwoCommonNodes(mp1, mp2);
    }

    private static Collection<Way> getWaysFromMultipolygon(MultiPolygon mp) {
        List<Way> ways = new ArrayList<>();
        ways.add(mp.getOuterWay());
        ways.addAll(mp.getInnerWays());
        return ways;
    }

    private static boolean haveCommonWay(MultiPolygon mp1, MultiPolygon mp2) {
        Set<Way> ways1 = new HashSet<>(getWaysFromMultipolygon(mp1));
        for (Way w : getWaysFromMultipolygon(mp2)) {
            if (ways1.contains(w)) {
                return true;
            }
        }
        return false;
    }

    private static boolean haveAtLeastTwoCommonNodes(MultiPolygon mp1, MultiPolygon mp2) {
        Set<Node> nodes1 = getWaysFromMultipolygon(mp1).stream()
                .flatMap(w -> w.getNodes().stream())
                .collect(Collectors.toSet());

        int count = 0;
        for (Way w : getWaysFromMultipolygon(mp2)) {
            for (Node n : w.getNodes()) {
                if (nodes1.contains(n)) {
                    count++;
                    if (count >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

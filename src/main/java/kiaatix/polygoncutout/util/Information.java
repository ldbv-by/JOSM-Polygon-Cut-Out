package kiaatix.polygoncutout.util;

import kiaatix.polygoncutout.polygon.MultiPolygon;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

import javax.swing.*;
import java.util.Map;

public class Information {
    public static void informUser(MultiPolygon polygon, String messagePart) {
        long id = getId(polygon);

        String oar = getObjektart(polygon);
        String message = oar + " '" + id + "' nicht vollständig geladen. -> Kein " + messagePart + " möglich!";
        JOptionPane.showMessageDialog(null, message, "Achtung", JOptionPane.WARNING_MESSAGE);
    }

    public static long getId(MultiPolygon polygon) {
        long id;
        if (polygon.hasRelation()){
            Relation relation = polygon.getRelation();
            id = relation.getUniqueId();
        }
        else{
            Way way = polygon.getOuterWay();
            id = way.getUniqueId();
        }
        return id;
    }

    public static String getObjektart(MultiPolygon polygon){
        Map<String, String> tags = polygon.getTags();
        return tags.get("object_type");
    }
}

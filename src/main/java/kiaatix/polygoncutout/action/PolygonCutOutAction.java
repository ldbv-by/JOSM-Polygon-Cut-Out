package kiaatix.polygoncutout.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.logging.Logger;

import kiaatix.polygoncutout.util.Information;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Geometry.PolygonIntersection;
import org.openstreetmap.josm.tools.Shortcut;

import kiaatix.polygoncutout.BetterPolygonSplitter;
import kiaatix.polygoncutout.polygon.MultiPolygon;
import kiaatix.polygoncutout.util.Commands;
import kiaatix.polygoncutout.util.QueryUtils;
import kiaatix.polygoncutout.util.TagSettings;

public class PolygonCutOutAction extends AreaAction {

	private static final Logger LOGGER = Logger.getLogger(PolygonCutOutAction.class.getName());

	private static final TagSettings tagSettings;

	static {
	    tagSettings = new TagSettings();
	    tagSettings.allowTags("natural", Arrays.asList( 
				"wood", 
				"scrub", 
				"heath", 
				"moor", 
				"grassland", 
				"fell", 
				"bare_rock", 
				"scree", 
				"shingle", 
				"sand", 
				"mud", 
				"water", 
				"wetland", 
				"glacier", 
				"beach")
		);

	    tagSettings.allowTags("landuse", Arrays.asList( 
				"allotments", 
				"basin", 
				"brownfield", 
				"farmland", 
				"forest", 
				"grass", 
				"greenfield", 
				"meadow", 
				"orchard", 
				"plant_nursery", 
				"village_green", 
				"vineyard")
		);

		// TODO: An AX_Objektarten anpassen:
		tagSettings.allowTags("object_type", Arrays.asList(
				"A_41001_Wohnbauflaeche",
				"A_41002_Deponie (untertägig)",
				"A_41002_Autobahnmeisterei",
				"A_41002_Straßenmeisterei",
				"A_41002_Versorgungsanlage",
				"A_41002_Tankstelle",
				"A_41002_Industrie und Gewerbe",
				"A_41002_Förderanlage",
				"A_41002_Parken",
				"A_41002_Nicht abgrenzbar",
				"A_41002_Deponie (oberirdisch)",
				"A_41002_Abfallbehandlungsanlage",
				"A_41002_Kläranlage, Klärwerk (groß, mit Klärbecken)",
				"A_41002_Kläranlage, Klärwerk (klein, ohne Klärbecken)",
				"A_41002_Heizwerk",
				"A_41002_Funk- und Fernmeldeanlage",
				"A_41002_Spielbank",
				"A_41002_Beherbergung",
				"A_41002_Lagerfläche",
				"A_41002_Stadtwerke",
				"A_41002_Wertstoffhof",
				"A_41002_Bauhof",
				"A_41002_Handel- und Dienstleistung",
				"A_41002_Industrie allgemein",
				"A_41002_Werft",
				"A_41002_Ausstellung, Messe",
				"A_41002_Gärtnerei",
				"A_41002_Wasserwerk",
				"A_41002_Kraftwerk",
				"A_41002_Umspannstation",
				"A_41002_Raffinerie",
				"A_41003_Halde",
				"A_41004_Bergbaubetrieb",
				"A_41005_TagebauGrubeSteinbruch",
				"A_41006_FlGemischterNutzung",
				"A_41007_FlBesFunkPraegung",
				"A_41008_Freizeitflaeche",
				"A_41009_Friedhof",
				"A_41009_Parkfriedhof",
				"A_41009_Parken",
				"A_42001_Strassenverkehr",
				"A_42009_Platz",
				"A_42010_Bahnverkehr",
				"A_42015_Flugverkehr",
				"A_42016_Schiffsverkehr",
				"A_43001_Landwirtschaft",
				"A_43002_Wald",
				"A_43003_Gehoelz",
				"A_43004_Heide",
				"A_43005_Moor",
				"A_43006_Sumpf",
				"A_43007_Naturnahe Fläche",
				"A_43007_Gewässerbegleitfläche",
				"A_43007_Vegetationslose Fläche",
				"A_44001_FliessgewKanal",
				"A_44001_FliessgewWasserlauf",
				"A_44005_Hafenbecken",
				"A_44006_StehendesGewaesser",
				"AX_Bahnverkehr",
				"AX_Bergbaubetrieb",
				"AX_FlaecheBesondererFunktionalerPraegung",
				"AX_FlaecheGemischterNutzung",
				"AX_Fliessgewaesser",
				"AX_Flugverkehr",
				"AX_Friedhof",
				"AX_Gehoelz",
				"AX_Hafenbecken",
				"AX_Halde",
				"AX_Heide",
				"AX_IndustrieUndGewerbeflaeche",
				"AX_Landwirtschaft",
				"AX_Meer",
				"AX_Moor",
				"AX_Platz",
				"AX_Schiffsverkehr",
				"AX_SportFreizeitUndErholungsflaeche",
				"AX_StehendesGewaesser",
				"AX_Strassenverkehr",
				"AX_Sumpf",
				"AX_TagebauGrubeSteinbruch",
				"AX_UnlandVegetationsloseFlaeche",
				"AX_Wald",
				"AX_Wohnbauflaeche")
		);

	    tagSettings.allowTag("area", "yes");
	    tagSettings.allowKey("area:highway");

	    tagSettings.disAllowKeys(Arrays.asList(
		    "building",
		    "boundary",
		    "leisure",
		    "man_made",
		    "highway",
		    "railway",
		    "public_transport"
		));
	}

	public PolygonCutOutAction() {
		super(tr("Cut Out Overlapping Polygons"), "cutout.png", tr("Cut Out Overlapping Polygons"),
				Shortcut.registerShortcut("tools:AreaUtils:Cut_Out", "Cut_Out", KeyEvent.VK_3, Shortcut.CTRL_SHIFT),
				false, true);
	}

	@Override
	public void actionPerformed(ActionEvent e, DataSet data) {
		displaceSelectedPolygons(data);
	}

	private void displaceSelectedPolygons(DataSet data) {
		// Get all selected polygons as a list
		List<MultiPolygon> selectedPolygons = QueryUtils.getSelectedMultiPolygons(data);

		if (selectedPolygons.isEmpty()) {
			showNoitifcation(tr("No polygons selected"));
		}
		
		// For each selected polygon, do displace action
		for (MultiPolygon selectedMultiPolygon : selectedPolygons) {
			if(!selectedMultiPolygon.isValid()){
				Information.informUser(selectedMultiPolygon, "Ausstanzen");
				continue;
			}

			displacePolygon(data, selectedMultiPolygon);
		}
	}

	private static void displacePolygon(DataSet data, MultiPolygon selectedMultiPolygon) {
		// Get all background polygons with allowed tags
		List<MultiPolygon> backgroundPolygons = QueryUtils.getUnselectedMultiPolygons(data, p -> {
			return tagSettings.isValid(p);
		});

		LOGGER.info("Found " + backgroundPolygons.size() + " background polygon candidates");

		// For each background polygon...
		for (MultiPolygon backgroundPolygon : backgroundPolygons) {

//			if (backgroundPolygon.canWayBeInnerWay(selectedMultiPolygon.getOuterWay())) {
//				doCreateMultiPolygon(data, selectedMultiPolygon, backgroundPolygon);
//			}
			// If that background polygon intersects the selected polygon
			if (selectedMultiPolygon.intersectsMultiPolygon(backgroundPolygon)) {

				if (!backgroundPolygon.isValid()){
					Information.informUser(backgroundPolygon, "Ausstanzen");
					continue;
				}

				// If they do not share same outer way
				if (!selectedMultiPolygon.getOuterWay().equals(backgroundPolygon.getOuterWay())) {

					LOGGER.info("Found overlapping polygon");
					// Do displacement of the selected polygon and the background polygon
					doDisplacePolygon(data, selectedMultiPolygon, backgroundPolygon);
				}
			}
		}
	}

	private static void doDisplacePolygon(DataSet data, MultiPolygon foreground, MultiPolygon background) {
		// Actually do the displacing and get a list of all new polygons.
		Commands c = new Commands(data);
		BetterPolygonSplitter m = new BetterPolygonSplitter(data);
		List<MultiPolygon> newPolygons = m.cutOutPolygon(foreground, background, c);

		LOGGER.info(
				"Split of background polygon done. Polygon was split into " + newPolygons.size() + " smaller polygons");

		// Add all new polygons to the dataset
        switch (newPolygons.size()) {
            case 0:
                // If the background polygon is contained within the foreground polygon.
                // No multipolygon must be created.
                if (!foreground.canWayBeInnerWay(background.getOuterWay())) {
                    doCreateMultiPolygon(data, c, foreground, background);
                }
                break;
            default:

                // Add all new polygons
                for (MultiPolygon p : newPolygons) {
                    c.addMultiPolygon(p);
                }


                if (background.hasRelation()) {
                    c.removeRelation(background.getRelation());
                }

                for (Way oldWay : background) {
                    boolean shouldDelete = true;

                    // If it is part of a new polygon, do not delete.
                    for (MultiPolygon newPolygon : newPolygons) {
                        for (Way newWay : newPolygon) {

                            if (newWay.equals(oldWay)) {
                                shouldDelete = false;
								break;
                            }
                        }
                    }

                    // If still part of other relations, do not delete
                    Set<Relation> parentRelations = OsmPrimitive.getParentRelations(Collections.singleton(oldWay));
                    if (!parentRelations.isEmpty()) {
                        // Does oldWay have inner as role for all parent relations
                        if (parentRelations.stream().allMatch(pr -> pr.getMembers().stream().anyMatch(rm -> rm.getMember() == oldWay && rm.hasRole("inner")))) {
                            if (Geometry.polygonIntersection(oldWay.getNodes(), foreground.getOuterWay().getNodes()) == PolygonIntersection.SECOND_INSIDE_FIRST) {
                                c.removeTags(oldWay);
                            }
                        }
                        shouldDelete = false;
                    }

                    // If inner way and is itself a polygon
                    if (background.isInner(oldWay) && oldWay.hasAreaTags()) {
                        shouldDelete = false;
                    }

                    if (shouldDelete) {
                        c.removeWay(oldWay);
                    }
                }
                break;
        }

		c.makeCommandSequence("Cutout polygon");
	}

	private static void doCreateMultiPolygon(DataSet data, Commands c, MultiPolygon inner, MultiPolygon outer) {
		
		if (outer.hasRelation()) {
			c.addInnerWayToPolygon(outer.getRelation(), inner.getOuterWay());
		} else {
			MultiPolygon multiPolygon = new MultiPolygon();
			multiPolygon.setOuter(outer.getOuterWay());
			multiPolygon.addInnerWay(inner.getOuterWay());
			multiPolygon.addTags(outer.getTags());
			
			c.addMultiPolygon(multiPolygon);
			
			Way w = new Way(outer.getOuterWay());
			w.removeAll();
			c.addCommand(new ChangeCommand(data, outer.getOuterWay(), w));
		}
	}
	
	@Override
	protected void updateEnabledState() {
		updateEnabledStateOnCurrentSelection();
	}

	@Override
	protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
		setEnabled(OsmUtils.isOsmCollectionEditable(selection)
				&& selection.stream().anyMatch(o -> {
					if (o.isIncomplete()) {
						return false;
					}
					
					if (o instanceof Way) {
//						Way w = (Way) o;
//						if (w.isClosed()) {
							return true; 
//						}
					}
					
					if (o instanceof Relation) {
						Relation r = (Relation) o;
						if (r.hasTag("type", "multipolygon")) {
							return true;
						}
					}
					
					return false;
				}));
	}

	private static final long serialVersionUID = -4666864264518649294L;
}

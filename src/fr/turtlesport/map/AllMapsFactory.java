package fr.turtlesport.map;

import fr.turtlesport.Configuration;
import fr.turtlesport.ui.swing.model.AddDeleteMapEvent;
import fr.turtlesport.ui.swing.model.AddDeleteMapListener;
import fr.turtlesport.ui.swing.model.ModelMapkitManager;
import fr.turtlesport.util.Location;
import org.jdesktop.swingx.mapviewer.TileCache;

import javax.swing.ImageIcon;
import java.io.File;
import java.util.LinkedHashMap;

/**
 * @author Denis Apparicio
 * 
 */
public final class AllMapsFactory implements AddDeleteMapListener {

  private static final File                                  DIR_CACHE = new File(Location
                                                                                      .userLocation(),
                                                                                  "openstreetmap");

  private static AllMapsFactory                              singleton = new AllMapsFactory();

  private LinkedHashMap<String, AbstractTileFactoryExtended> hashMap   = new LinkedHashMap<String, AbstractTileFactoryExtended>();

  private String[]                                           names;

  /**
   * @return
   */
  public static AllMapsFactory getInstance() {
    return singleton;
  }

  private AllMapsFactory() {
    ModelMapkitManager.getInstance().addAddDeleteMapListener(this);

    // mercator
    //hashMap.put(TurtleEmptyTileFactory.NAME, new TurtleEmptyTileFactory());
    // mapnik
    addOpenStreetMap("http://tile.openstreetmap.org", "mapnik", null, null);
    // opencyclemap
    addOpenStreetMap("http://tile.opencyclemap.org/cycle",
                     "cyclemap",
                     null,
                     null);
    // MapQuest map
    //addOpenStreetMap("http://otile1.mqcdn.com/tiles/1.0.0/osm",
    //                 "MapQuest",
    //                 new ImageIcon(getClass().getResource("mapquest14.png")),
    //                 new ImageIcon(getClass().getResource("maquestLogo.png")));
    // MapQuest sat
    //addOpenStreetMap("http://otile1.mqcdn.com/tiles/1.0.0/sat",
    //                 "MapQuest sat",
    //                 new ImageIcon(getClass().getResource("mapquest14.png")),
    //                 new ImageIcon(getClass().getResource("maquestLogo.png")));

    // Transport
    addOpenStreetMap("http://tile2.opencyclemap.org/transport",
                     "Transport",
                     null,
                     null);
    // Landscape
    addOpenStreetMap("http://tile3.opencyclemap.org/landscape",
                     "Landscape",
                     null,
                     null);

    // User map define
    for (DataMap map : MapConfiguration.getConfig().getMaps().getMaps()) {
      addUserMap(map);
    }

    names = new String[hashMap.size()];
    hashMap.keySet().toArray(names);
  }

  private void addUserMap(DataMap map) {
    UserDefineMapTileProviderInfo tileInfo = new UserDefineMapTileProviderInfo(map);
    UserDefineMapTileFactory tileFactory = new UserDefineMapTileFactory(tileInfo,
                                                                        map.getUrl());

    File dir = new File(DIR_CACHE, tileFactory.getInfo().getName());
    tileFactory.setTileCache(new DiskTitleCache(dir, tileFactory.getInfo()));

    hashMap.put(map.getName(), tileFactory);
  }

  private void addOpenStreetMap(String url,
                                String name,
                                ImageIcon smallIcon,
                                ImageIcon bigIcon) {
    OpenStreetMapTileFactory tileFactory = new OpenStreetMapTileFactory(url,
                                                                        name,
                                                                        smallIcon);
    tileFactory.setBigIcon(bigIcon);
    File dir = new File(DIR_CACHE, tileFactory.getInfo().getName());
    tileFactory.setTileCache(new DiskTitleCache(dir, tileFactory.getInfo()));

    hashMap.put(name, tileFactory);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.turtlesport.ui.swing.model.AddDeleteMapListener#deleteMap(fr.turtlesport
   * .ui.swing.model.AddDeleteMapEvent)
   */
  @Override
  public void deleteMap(AddDeleteMapEvent e) {
    hashMap.remove(e.getMapName());
    names = new String[hashMap.size()];
    hashMap.keySet().toArray(names);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.turtlesport.ui.swing.model.AddDeleteMapListener#addMap(fr.turtlesport
   * .ui.swing.model.AddDeleteMapEvent)
   */
  @Override
  public void addMap(AddDeleteMapEvent e) {
    if (!hashMap.containsKey(e.getMapName())) {
      addUserMap(e.getDataMap());
      names = new String[hashMap.size()];
      hashMap.keySet().toArray(names);
    }
  }

  /**
   * Restitue les noms des maps.
   * 
   * @return les noms des maps
   */
  public String[] getTileNames() {
    return names;
  }

  /**
   * Restitue les noms des maps.
   * 
   * @return les noms des maps
   */
  public AbstractTileFactoryExtended[] getTileDisplayInfo() {
    AbstractTileFactoryExtended[] tab = new AbstractTileFactoryExtended[hashMap
        .values().size()];
    return hashMap.values().toArray(tab);
  }

  /**
   * Restitue la factory et met a jour la configuration.
   * 
   * @return la factory.
   */
  public AbstractTileFactoryExtended getTileFactory(String name) {
    AbstractTileFactoryExtended tileFactory = hashMap.get(name);

    Configuration.getConfig().addProperty("map", "tile", name);
    return tileFactory;
  }

  /**
   * Restitue la factory.
   * 
   * @return la factory.
   */
  public AbstractTileFactoryExtended retreiveTileFactory(String name) {
    return hashMap.get(name);
  }

  /**
   * Restitue la factory par d&eacute;faut.
   * 
   * @return la factory par d&eacute;faut.
   */
  public AbstractTileFactoryExtended getDefaultTileFactory() {
    String key = Configuration.getConfig().getProperty("map", "tile", "mapnik");

    AbstractTileFactoryExtended tileFactory = getTileFactory(key);
    if (tileFactory == null) {
      tileFactory = new TurtleEmptyTileFactory();
    }

    setDefaultTileFactory(tileFactory);
    return tileFactory;
  }

  /**
   * Sauvegarde la factory par d&eacute;faut.
   * 
   * @return la factory par d&eacute;faut.
   */
  public void setDefaultTileFactory(AbstractTileFactoryExtended tileFactory) {
    Configuration.getConfig().addProperty("map",
                                          "tile",
                                          tileFactory.getInfo().getName());
  }

  /**
   * Efface le cache.
   */
  public void cleanCache() {
    for (String key : hashMap.keySet()) {
      hashMap.get(key).getTileCache().needMoreMemory();
    }

    // Effacement des residus de la version 0.1.12
    File dirCache112 = new File(Location.userLocation(), "openstreeetmap");
    if (dirCache112.exists()) {
      File[] files = dirCache112.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isFile()) {
            f.delete();
          }
        }
      }
    }
    dirCache112.delete();
  }

  /**
   * Restitue la taille du cache.
   * 
   * @return la taille du cache.
   */
  public long cacheSize() {
    long length = 0;
    for (String key : getTileNames()) {
      TileCache cache = hashMap.get(key).getTileCache();
      if (cache != null && cache instanceof DiskTitleCache) {
        length += ((DiskTitleCache) cache).length();
      }
    }
    return length;
  }

}

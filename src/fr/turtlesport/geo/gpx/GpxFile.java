package fr.turtlesport.geo.gpx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import fr.turtlesport.db.DataRun;
import fr.turtlesport.db.DataRunLap;
import fr.turtlesport.db.DataRunTrk;
import fr.turtlesport.db.RunLapTableManager;
import fr.turtlesport.db.RunTrkTableManager;
import fr.turtlesport.geo.GeoConvertException;
import fr.turtlesport.geo.GeoLoadException;
import fr.turtlesport.geo.IGeoConvertCourse;
import fr.turtlesport.geo.IGeoConvertRun;
import fr.turtlesport.geo.IGeoFile;
import fr.turtlesport.geo.IGeoRoute;
import fr.turtlesport.lang.LanguageManager;
import fr.turtlesport.log.TurtleLogger;
import fr.turtlesport.protocol.data.D1006CourseType;
import fr.turtlesport.protocol.data.D304TrkPointType;
import fr.turtlesport.util.GeoUtil;
import fr.turtlesport.util.Location;
import fr.turtlesport.util.XmlUtil;

/**
 * @author Denis apparicio
 * 
 */
public class GpxFile implements IGeoFile, IGeoConvertRun, IGeoConvertCourse {
  private static TurtleLogger  log;
  static {
    log = (TurtleLogger) TurtleLogger.getLogger(GpxFile.class);
  }

  /** Extensions. */
  public static final String[] EXT = { "gpx" };

  /**
   * 
   */
  public GpxFile() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.geo.IGeoFile#description()
   */
  public String description() {
    return "GPS eXchange (*.gpx)";
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.geo.IGeoFile#extension()
   */
  public String[] extension() {
    return EXT;
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.geo.IGeoConvertRun#convert(fr.turtlesport.db.DataRun,
   * java.io.File)
   */
  public File convert(DataRun data, File file) throws GeoConvertException,
                                              SQLException {
    log.debug(">>convert");

    if (data == null) {
      throw new IllegalArgumentException("dataRun est null");
    }
    if (file == null) {
      throw new IllegalArgumentException("file est null");
    }

    // Recuperation des points des tours intermediaires.
    DataRunLap[] laps = RunLapTableManager.getInstance().findLaps(data.getId());
    if (laps != null && laps.length < 1) {
      return null;
    }

    List<DataRunTrk> trks = RunTrkTableManager.getInstance().getTrks(data
        .getId());
    if (trks != null && trks.size() < 1) {
      return null;
    }

    long startTime = System.currentTimeMillis();

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(file));
      SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

      // begin
      writeBegin(file, startTime, writer);

      // Ecriture des tours intermediaires.
      for (DataRunLap l : laps) {
        writeLap(writer, data, l, timeFormat);
      }

      // end
      writeEnd(writer);
    }
    catch (IOException e) {
      log.error("", e);
      throw new GpxGeoConvertException(e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          log.error("", e);
        }
      }
    }

    long endTime = System.currentTimeMillis();
    log.info("Temps pour ecrire gpx : " + (endTime - startTime) + " ms");

    log.debug("<<convert");
    return file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.geo.IGeoConvertRun#convert(fr.turtlesport.db.DataRun)
   */
  public File convert(DataRun data) throws GeoConvertException, SQLException {
    if (data == null) {
      throw new IllegalArgumentException();
    }

    // construction du nom du fichier
    String name = LanguageManager.getManager().getCurrentLang()
        .getDateTimeFormatterWithoutSep().format(data.getTime())
                  + ".gpx";
    File file = new File(Location.googleEarthLocation(), name);

    // conversion
    return convert(data, file);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.turtlesport.geo.IGeoConvertCourse#convert(fr.turtlesport.protocol.data
   * .D1006CourseType, java.io.File)
   */
  public File convert(D1006CourseType data, File file) throws GeoConvertException {
    log.debug(">>convert");

    if (data == null) {
      throw new IllegalArgumentException("data est null");
    }
    if (file == null) {
      throw new IllegalArgumentException("file est null");
    }

    long startTime = System.currentTimeMillis();

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(file));

      // begin
      writeBegin(file, startTime, writer);

      // Ecriture des points
      SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      for (D304TrkPointType trk : data.getListTrkPointType()) {
        writeTrkPoint(writer, trk, timeFormat);
      }

      // end
      writeEnd(writer);
    }
    catch (IOException e) {
      log.error("", e);
      throw new GpxGeoConvertException(e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          log.error("", e);
        }
      }
    }

    long endTime = System.currentTimeMillis();
    log.info("Temps pour ecrire gpx : " + (endTime - startTime) + " ms");

    log.debug("<<convert");
    return file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.turtlesport.geo.IGeoConvertCourse#convert(fr.turtlesport.protocol.data
   * .D1006CourseType)
   */
  public File convert(D1006CourseType data) throws GeoConvertException {
    if (data == null) {
      throw new IllegalArgumentException();
    }

    // construction du nom du fichier
    File file = new File(Location.googleEarthLocation(), data.getCourseName()
                                                         + ".gpx");
    // conversion
    convert(data, file);
    return file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.turtlesport.geo.IGeoFile#load(java.io.File)
   */
  public IGeoRoute[] load(File file) throws GeoLoadException,
                                    FileNotFoundException {
    log.debug(">>load");

    IGeoRoute[] rep;

    // Lecture
    FileInputStream fis = new FileInputStream(file);

    GpxHandler handler = null;
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();

      handler = new GpxHandler();
      parser.parse(fis, handler);

      if (log.isDebugEnabled()) {
        log.debug("handler.nbTrk=" + handler.nbTrk);
        log.debug("handler.nbTrkseg=" + handler.nbTrkseg);
        log.debug("handler.nbTrkpt=" + handler.nbTrkpt);
      }

      // construction de la reponse
      ArrayList<IGeoRoute> list = new ArrayList<IGeoRoute>();
      if (handler.listRte != null) {
        for (Rte r : handler.listRte) {
          list.add(r);
        }
      }
      if (handler.listTrk != null) {
        for (Trk t : handler.listTrk) {
          list.add(t);
        }
      }

      rep = new IGeoRoute[list.size()];
      if (list.size() > 0) {
        return list.toArray(rep);
      }
    }
    catch (Exception e) {
      log.error("", e);
      throw new GeoLoadException(e);
    }

    log.debug("<<load");
    return rep;
  }

  private void writeBegin(File file, long startTime, BufferedWriter writer) throws IOException {
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writeln(writer);

    // <gpx>
    // --------------------------------------------------
    writer
        .write("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"TurtleSport\">");
    writeln(writer);

    // <metadata> fils de <gpx>
    // --------------------------------------------------
    writer.write("<metadata>");
    writeln(writer);

    writer.write("<name>" + file.getName() + "</name>");
    writeln(writer);

    writer.write("<time>");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    writer.write(dateFormat.format(new Date(startTime)));
    writer.write("</time>");
    writeln(writer);

    writer.write("</metadata>");
    writeln(writer);

    // <trk> fils de <gpx>
    // --------------------------------------------------
    writer.write("<trk>");
    writeln(writer);
    writer.write("<name>" + file.getName() + "</name>");
    writeln(writer);
  }

  private void writeLap(BufferedWriter writer,
                        DataRun data,
                        DataRunLap l,
                        SimpleDateFormat timeFormat) throws IOException,
                                                    SQLException {
    log.debug(">>writeLap");

    // recuperation des points du tour
    Date dateEnd = new Date(l.getStartTime().getTime() + l.getTotalTime() * 10);
    DataRunTrk[] trks = RunTrkTableManager.getInstance()
        .getTrks(data.getId(), l.getStartTime(), dateEnd);

    if (trks.length == 0) {
      log.warn("pas de points pour ce tour");
      return;
    }

    // <trkseg> fils de <trk>
    // --------------------------------------------------
    writer.write("<trkseg>");

    for (DataRunTrk t : trks) {
      writeln(writer);
      writeTrkPoint(writer, t, timeFormat);
    }

    writer.write("</trkseg>");
    writeln(writer);

    log.debug("<<writeLap");
  }

  private void writeTrkPoint(BufferedWriter writer,
                             DataRunTrk point,
                             SimpleDateFormat timeFormat) throws IOException {
    double latitude = GeoUtil.makeLatitudeFromGarmin(point.getLatitude());
    double longitude = GeoUtil.makeLatitudeFromGarmin(point.getLongitude());

    writer.write("<trkpt ");
    writer.write("lat=\"" + Double.toString(latitude) + "\" ");
    writer.write("lon=\"" + Double.toString(longitude) + "\">");
    writer.write("<ele>" + Float.toString(point.getAltitude()) + "</ele>");
    writer.write("<time>" + timeFormat.format(point.getTime()) + "</time>");

    writer.write("</trkpt>");
    writeln(writer);
  }

  private void writeTrkPoint(BufferedWriter writer,
                             D304TrkPointType point,
                             SimpleDateFormat timeFormat) throws IOException {
    double latitude = GeoUtil.makeLatitudeFromGarmin(point.getPosn()
        .getLatitude());
    double longitude = GeoUtil.makeLatitudeFromGarmin(point.getPosn()
        .getLongitude());

    writer.write("<trkpt ");
    writer.write("lat=\"" + Double.toString(latitude) + "\" ");
    writer.write("lon=\"" + Double.toString(longitude) + "\">");
    writer.write("<el>" + Float.toString(point.getAltitude()) + "</el>");
    writer.write("<time>" + timeFormat.format(point.getTime()) + "</time>");

    writer.write("</trkpt>");
    writeln(writer);
  }

  private void writeEnd(BufferedWriter writer) throws IOException {
    writeln(writer);
    writer.write("</trk>");

    writeln(writer);
    writer.write("</gpx>");
  }

  private void writeln(BufferedWriter writer) throws IOException {
    writer.write("\n");
  }

  /**
   * @author Denis Apparicio
   * 
   */
  private class GpxHandler extends DefaultHandler {
    private ArrayList<Trk> listTrk;

    private ArrayList<Rte> listRte;

    // private ArrayList<Wpt> listWpt;

    private StringBuffer   stBuffer;

    private boolean        checkGpx = true;

    // private Wpt currentWpt;

    private Rte            currentRte;

    private Wpt            currentRtept;

    private Trk            currentTrk;

    private Trkseg         currentTrkseg;

    private Wpt            currentTrkpt;

    private boolean        isRte    = false;

    private boolean        isRtept  = false;

    private boolean        isTrk    = false;

    private boolean        isTrkseg = false;

    private boolean        isTrkpt  = false;

    // private boolean isWpt = false;

    private int            nbTrk    = 0;

    private int            nbTrkseg = 0;

    private int            nbTrkpt  = 0;

    /**
     * 
     */
    public GpxHandler() {
      super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attrs) throws SAXParseException {
      log.debug(">>startElement uri=" + uri + " localName=" + localName
                + " qName=" + qName);

      // verification que la racine est gpx
      if (checkGpx) {
        if (!qName.equals("gpx")) {
          throw new SAXParseException("qName != gpx", (Locator) null);
        }
        checkGpx = false;
      }

      // rte
      // --------------------------
      if (qName.equals("rte")) {
        currentRte = new Rte();
        isRte = true;
      }

      // rtept
      if (qName.equals("rtept") && isRte) {
        isRtept = true;

        // Latitude
        String attr = attrs.getValue("lat");
        double lat = (attr != null) ? Double.valueOf(attr) : Double.NaN;

        // Longitude
        attr = attrs.getValue("lon");
        double lon = (attr != null) ? Double.valueOf(attr) : Double.NaN;

        log.debug("Trkpt lat=" + lat + ", lon=" + lon);
        currentRtept = new Wpt(lat, lon);
      }

      // trk
      // ------------------------------
      if (qName.equals("trk")) {
        currentTrk = new Trk();
        nbTrk++;
        isTrk = true;
      }

      // trkseg
      if (qName.equals("trkseg") && isTrk) {
        currentTrkseg = new Trkseg(currentTrk.getSegmentSize());
        nbTrkseg++;
        isTrkseg = true;
      }

      // trkpt
      if (qName.equals("trkpt") && isTrkseg) {
        nbTrkpt++;
        isTrkpt = true;

        // Latitude
        String attr = attrs.getValue("lat");
        double lat = (attr != null) ? Double.valueOf(attr) : Double.NaN;

        // Longitude
        attr = attrs.getValue("lon");
        double lon = (attr != null) ? Double.valueOf(attr) : Double.NaN;

        log.debug("Trkpt lat=" + lat + ", lon=" + lon);
        currentTrkpt = new Wpt(lat, lon);
      }

      log.debug("<<startElement");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) {
      log.debug(">>endElement uri=" + uri + " localName=" + localName
                + " qName=" + qName);

      // elevation
      if (qName.equals("ele")) {
        if (isTrkpt) { // de trkpt
          currentTrkpt.setElevation(Double.valueOf(stBuffer.toString()));
        }
        else if (isRtept) { // de rtept
          currentRtept.setElevation(Double.valueOf(stBuffer.toString()));
          log.debug("Rtept ele: " + currentRtept.getElevation());
        }
      }

      // time
      if (qName.equals("time")) {
        if (isTrkpt) { // de trkpt
          currentTrkpt.setDate(XmlUtil.getTime(stBuffer.toString()));
          if (log.isDebugEnabled()) {
            log.debug("Trkpt Time: " + currentTrkpt.getDate());
          }
        }
        else if (isRtept) { // de rtept
          currentRtept.setDate(XmlUtil.getTime(stBuffer.toString()));
          if (log.isDebugEnabled()) {
            log.debug("Rtept Time: " + currentRtept.getDate());
          }
        }
      }

      // name
      if (qName.equals("name") && stBuffer != null) {
        String st = stBuffer.toString();

        if (st.startsWith("![CDATA[") && st.endsWith("]]")) {
          st = st.substring(8, st.length() - 2);
        }

        if (isRte && !isRtept) {// de rte
          currentRte.setName(st);
          log.debug("Rte name: " + st);
        }
        else if (isRte && isRtept) {// de rtept
          currentRtept.setName(st);
          log.debug("Rtept name: " + st);
        }
        else if (isTrk && !isTrkseg && !isTrkpt) { // de trk
          log.debug("Trk name: " + st);
        }
        else if (isTrk && isTrkseg && isTrkpt) { // de trkpt
          currentTrkpt.setName(st);
          log.debug("Trkpt name: " + st);
        }
      }

      // desc
      if (qName.equals("desc") && stBuffer != null) {
        String st = stBuffer.toString();

        if (st.startsWith("![CDATA[") && st.endsWith("]]")) {
          st = st.substring(8, st.length() - 2);
        }

        if (isRte && !isRtept) { // de rte
          currentRte.setDesc(st);
          log.debug("Rte desc: " + st);
        }
        else if (isRte && isRtept) {// de rtept
          currentRtept.setDesc(st);
          log.debug("Rtept desc: " + st);
        }
        else if (isTrk && !isTrkpt && !isTrkseg) {
          log.debug("Trk desc: " + st);
        }
        else if (isTrk && isTrkpt && isTrkseg) {
          currentTrkpt.setDesc(st);
          log.debug("Trkpt desc: " + st);
        }
      }

      stBuffer = null;

      // rte
      // -------------------------
      if (qName.equals("rte")) {
        isRte = false;
        log.debug("currentRte.getRteptSize()=" + currentRte.getRteptSize());
        if (currentRte.getRteptSize() > 0) {
          addRte(currentRte);
        }
      }

      // rtept
      // -------------------------
      if (qName.equals("rtept")) {
        isRtept = false;
        currentRte.addRtept(currentRtept);
      }

      // trk
      // -------------------------
      if (qName.equals("trk")) {
        isTrk = false;
        if (currentTrk.getTrkSize() > 0) {
          addTrk(currentTrk);
        }
      }

      // trkpt
      // ------------
      if (qName.equals("trkpt")) {
        isTrkpt = false;
        currentTrkseg.addTrk(currentTrkpt);
      }

      // trkseg
      // -----------------
      if (qName.equals("trkseg")) {
        isTrkseg = false;
        log.debug("currentTrkseg.getTrkSize()=" + currentTrkseg.getTrkSize());
        if (currentTrkseg.getTrkSize() > 0) {
          currentTrk.addTrkseg(currentTrkseg);
        }
      }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) {
      String st = new String(ch, start, length).trim();
      log.debug(">>characters " + st);

      if (st.length() > 0) {
        if (stBuffer == null) {
          stBuffer = new StringBuffer(st);
        }
        else {
          stBuffer.append(st);
        }
      }

      log.debug("<<characters ");
    }

    /**
     * Ajoute une route.
     */
    private void addRte(Rte rte) {
      if (listRte == null) {
        listRte = new ArrayList<Rte>();
      }
      listRte.add(rte);
    }

    /**
     * Ajoute une piste.
     */
    private void addTrk(Trk trk) {
      if (listTrk == null) {
        listTrk = new ArrayList<Trk>();
      }
      listTrk.add(trk);
    }

    // /**
    // * Ajoute un wpt.
    // */
    // private void addWpt(Wpt wpt) {
    // if (listWpt == null) {
    // listWpt = new ArrayList<Wpt>();
    // }
    // listWpt.add(wpt);
    // }
  }

  // /**
  // * @author Denis Apparicio
  // *
  // */
  // private class WptGeoRoute extends AbstractGeoRoute {
  // private GpxGeoSegment segment;
  //
  // private List<IGeoSegment> list;
  //
  // public WptGeoRoute(List<Wpt> listWpt) {
  // super();
  // segment = new GpxGeoSegment(listWpt);
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoRoute#getAllPoints()
  // */
  // public List<IGeoPositionWithAlt> getAllPoints() {
  // return segment.getPoints();
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoRoute#getName()
  // */
  // public String getName() {
  // return null;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoRoute#getSegment(int)
  // */
  // public IGeoSegment getSegment(int index) {
  // if (index != 0) {
  // throw new IndexOutOfBoundsException("size 0, index " + index);
  // }
  // return segment;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoRoute#getSegmentSize()
  // */
  // public int getSegmentSize() {
  // return 1;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoRoute#getSegments()
  // */
  // public List<IGeoSegment> getSegments() {
  // if (list == null) {
  // synchronized (WptGeoRoute.class) {
  // list = new ArrayList<IGeoSegment>();
  // list.add(segment);
  // }
  // }
  // return list;
  // }
  //
  // }

  // /**
  // * @author Denis Apparicio
  // *
  // */
  // private class GpxGeoSegment extends AbstractGeoSegment {
  // private List<IGeoPositionWithAlt> list;
  //
  // /**
  // * @param listWpt
  // */
  // public GpxGeoSegment(List<Wpt> listWpt) {
  // list = new ArrayList<IGeoPositionWithAlt>();
  // for (Wpt w : listWpt) {
  // list.add(w);
  // }
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoSegment#getPoints()
  // */
  // public List<IGeoPositionWithAlt> getPoints() {
  // return list;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see fr.turtlesport.geo.IGeoSegment#index()
  // */
  // public int index() {
  // return 0;
  // }
  //
  // }

}
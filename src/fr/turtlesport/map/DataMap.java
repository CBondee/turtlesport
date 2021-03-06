//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.3-hudson-jaxb-ri-2.2-70- 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.01.11 at 12:23:38 AM CET 
//

package fr.turtlesport.map;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for mapType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="mapType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="zoomMin" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="zoomMax" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="x" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="editable" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mapType", propOrder = { "name", "url", "zoomMin", "zoomMax" })
public class DataMap {

  @XmlElement(required = true)
  protected String                    name;

  @XmlElement(required = true)
  protected String                    url;

  protected int                       zoomMin;

  protected int                       zoomMax;

  @XmlAttribute(name = "editable", required = true)
  protected boolean                   editable;

  @XmlTransient
  private AbstractTileFactoryExtended tileMap;

  public DataMap() {
    super();
  }

  public DataMap(String name, boolean isEditable) {
    super();
    this.name = name;
    this.editable = isEditable;
    this.zoomMin = 1;
    this.zoomMax = 15;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Gets the value of the name property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the value of the url property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the value of the url property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setUrl(String value) {
    this.url = value;
  }

  /**
   * Gets the value of the zoomMin property.
   * 
   */
  public int getZoomMin() {
    return zoomMin;
  }

  /**
   * Sets the value of the zoomMin property.
   * 
   */
  public void setZoomMin(int value) {
    this.zoomMin = value;
  }

  /**
   * Gets the value of the zoomMax property.
   * 
   */
  public int getZoomMax() {
    return zoomMax;
  }

  /**
   * Sets the value of the zoomMax property.
   * 
   */
  public void setZoomMax(int value) {
    this.zoomMax = value;
  }

  /**
   * Gets the value of the editable property.
   * 
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * Sets the value of the editable property.
   * 
   */
  public void setEditable(boolean value) {
    this.editable = value;
  }

  public AbstractTileFactoryExtended getTileMap() {
    return tileMap;
  }

  public void setTileMap(AbstractTileFactoryExtended tileMap) {
    this.tileMap = tileMap;
  }

  public boolean hasSameFields(DataMap map) {
    try {
      return (equals(map) && url.equals(map.url) && zoomMax == map.zoomMax && zoomMin == map.zoomMin);
    }
    catch (Throwable e) {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (name == null || obj == null || !(obj instanceof DataMap)) {
      return false;
    }
    return name.equals(((DataMap) obj).name);
  }

  /**
   * @return D&eacute;termine si la map est valide
   */
  public boolean isValidMap() {
    if (url == null) {
      return false;
    }
    try {
      new URL(url);
    }
    catch (MalformedURLException e) {
      return false;
    }
    if (zoomMin < 0 || zoomMax < 0 || zoomMin == zoomMax) {
      return false;
    }
    if (zoomMin > zoomMax) {
      int tmp = zoomMax;
      zoomMax = zoomMin;
      zoomMin = tmp;
    }
    return true;
  }

}

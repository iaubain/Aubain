/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Owner
 */

//defining my session bean
@Entity
@Table(name="location")
@XmlRootElement(name="LOCATION")
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQuery(name="findlocation",query = "select loc from MainLocation loc")
public class MainLocation implements Serializable {
    
    
    @Id
    @Column(name="locationid")
    @XmlElement(name="LOCATIONID") 
    private int locationId;
    
    @Column(name="locationname")
    @XmlElement(name="LOCATIONNAME")
    private String locationName;
    
    @XmlElement(name="SUBLOCATION")
    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL})
    @JoinColumn(name = "locationid")
    private List<SubLocation> subLocation;
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    /**
     * @return the locationId
     */
    public int getLocationId() {
        return locationId;
    }

    /**
     * @param locationId the locationId to set
     */
    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    /**
     * @return the locationName
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * @param locationName the locationName to set
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * @return the subLocation
     */
    public List<SubLocation> getSubLocation() {
        return subLocation;
    }

    /**
     * @param subLocation the subLocation to set
     */
    public void setSubLocation(List<SubLocation> subLocation) {
        this.subLocation = subLocation;
    }

    
    
}

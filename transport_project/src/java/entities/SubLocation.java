/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.ejb.Stateless;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Entity
@Table(name="sublocation")
@XmlRootElement(name="SUBLOCATION")
@XmlAccessorType(XmlAccessType.FIELD)
@SequenceGenerator(name="seq", initialValue=1, allocationSize=1000000000)
public class SubLocation implements Serializable {
    
    //declaring properties of my entity bean
    
    @Column(name="locationid")
    @XmlElement(name="LOCATIONID") 
    private int locationId;
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
    @Column(name="sublocationid")
    //@XmlElement(name="SUBLOCATIONID") 
    private int subLocationId;
    
    @Column(name="sublocationname")
    @XmlElement(name="SUBLOCATIONNAME")
    private String sublocationName;
    
    

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
     * @return the subLocationId
     */
    public int getSubLocationId() {
        return subLocationId;
    }

    /**
     * @param subLocationId the subLocationId to set
     */
    public void setSubLocationId(int subLocationId) {
        this.subLocationId = subLocationId;
    }

    /**
     * @return the sublocationName
     */
    public String getSublocationName() {
        return sublocationName;
    }

    /**
     * @param sublocationName the sublocationName to set
     */
    public void setSublocationName(String sublocationName) {
        this.sublocationName = sublocationName;
    }

    
    
    
  
    
}

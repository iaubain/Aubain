package entities;

import entities.SubLocation;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-10-17T16:50:37")
@StaticMetamodel(MainLocation.class)
public class MainLocation_ { 

    public static volatile ListAttribute<MainLocation, SubLocation> subLocation;
    public static volatile SingularAttribute<MainLocation, Integer> locationId;
    public static volatile SingularAttribute<MainLocation, String> locationName;

}
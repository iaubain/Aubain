/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb_session;

import entities.MainLocation;
import entities.SubLocation;
import java.io.StringReader;
import static java.lang.Math.max;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.xml.sax.InputSource;

/**
 *
 * @author Owner
 */
@Stateless
public class TransportSessionBean {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    //Persist data to entities
    @PersistenceContext 
    EntityManager em;
    @PersistenceUnit
    private EntityManagerFactory emf;
    Log log;
    private String encodedInput,input;
    
    //Insert data into database
    public void createLocation(MainLocation ml){
   
     em.persist(ml); 
     log.debug("Data saved "+ml.getLocationName());
    }
    
    //fetch main Location and their sublocation repectively going to an enetered location ID
    public MainLocation getLocator(int locId){
        MainLocation mn=em.find(MainLocation.class, locId);
    return mn;  
    }
    
    //Get a sublocation name going from its id
    public List<SubLocation> getSubLoc(int subLocId){
         em = emf.createEntityManager();
         List<SubLocation> list = (List<SubLocation>)em.createQuery("SELECT s FROM SubLocation s where s.subLocationId=:subLocId")
                 .setParameter("subLocId", subLocId).getResultList();

    return list;
    }
    
    //Get a sublocation list from an entered location name
    public List<MainLocation> getSubLocList(String locName){
         em = emf.createEntityManager();
         List<MainLocation> list = (List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m where m.locationName=:locName")
                 .setParameter("locName", locName).getResultList();

    return list;
    }
    
    //select all main locations
    public List<MainLocation> selectMainLocation(){
         em = emf.createEntityManager();
         List<MainLocation> list = (List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m").getResultList();
            
        return list;  
        }
    
    //select all main locations
    public org.w3c.dom.Document compareMatch(String encodedInput) throws Exception{
        //Decoding the data
         Base64.Decoder decoder = Base64.getDecoder();
         byte[] decodedByteArray = decoder.decode(encodedInput);
         //Verify the decoded string
         input=new String(decodedByteArray);
         
         em = emf.createEntityManager();
         List<MainLocation> list = (List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m").getResultList();
         Iterator i=list.iterator();
         MainLocation mn=new MainLocation();
         SubLocation sbln=new SubLocation();
         String locName="",subLocName="",xmlHolder="";
         String xmlData="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><MAINLOCATION>";
         
         while(i.hasNext()){
        mn=(MainLocation) i.next();
        locName=mn.getLocationName();
        
        //get list of sub location of the entered main location
        List<SubLocation>subLoc=mn.getSubLocation();
        Iterator itr=subLoc.iterator();
        while(itr.hasNext()){
        sbln=(SubLocation)itr.next();
        subLocName=sbln.getSublocationName();
        double distance=distance(input,subLocName);
        double percent=percentage(distance,input,subLocName);
        xmlHolder+=decision(percent,locName,subLocName);
        }
        } 
         //if no data match in sublocation
         if("".equals(xmlHolder)){
         list = (List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m").getResultList();
         i=list.iterator();
         while(i.hasNext()){
         mn=(MainLocation) i.next();
         locName=mn.getLocationName();
         double distance=distance(input,locName);
         double percent=percentage(distance,input,locName);
         xmlHolder+=decision(percent,locName,locName);
         }
        }
         
         if("".equals(xmlHolder)){
            xmlHolder+="<LOCATIONNAME>"+input.toUpperCase()+"</LOCATIONNAME>"+"<SUBLOCATIONNAME>NO_MATCH_FOUND</SUBLOCATIONNAME>";
            }
         
         xmlData+=xmlHolder+"</MAINLOCATION>";
         //Dom document
         org.w3c.dom.Document doc=loadXML(xmlData);
        return doc;  
        }
    
    //select all main locations but not is not used
    public List<MainLocation> selectMainLocationAndSubLocation(){
         em = emf.createEntityManager();
         List<MainLocation> list = (List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m JOIN m.subLocation s WHERE m.locationId= s.locationId").getResultList();
            
        return list;  
        }
    
     //select all sub locations
    public List<SubLocation> subLocationFinder(){
        
         em = emf.createEntityManager();
         List<SubLocation> list = (List<SubLocation>)em.createQuery("SELECT s FROM SubLocation s").getResultList();

        return list;
        }
    
    //select all sub locations from an entered location name
    public List<SubLocation> subLocationName(int locId){
        
         em = emf.createEntityManager();
         List<SubLocation> list = (List<SubLocation>)em.createQuery("SELECT s FROM SubLocation s where s.locationId=:locId")
                 .setParameter("locId", locId).getResultList();

        return list;
        }
    
     //select main location going from an entered subLocation
    public org.w3c.dom.Document placementFinder(String subLocName) throws Exception{
        
        String xmlData="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><MAINLOCATION>",xmlHolder="";
        
        List<MainLocation> list=(List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m JOIN m.subLocation s WHERE m.locationId= s.locationId and ((s.sublocationName =:subLocName)or(m.locationName=:subLocName))")
        .setParameter("subLocName",subLocName).getResultList();
        
            Iterator i=list.iterator();
            MainLocation ml = null;
            while (i.hasNext()) {
                ml = (MainLocation) i.next();
            }
            
             xmlHolder+="<LOCATION>"+
                    "<LOCATIONNAME>"+ml.getLocationName()+
                    "</LOCATIONNAME>"+"<SUBLOCATIONNAME>"+subLocName.toUpperCase()+"</SUBLOCATIONNAME>"+
                    "</LOCATION>";
    
         xmlData+=xmlHolder+"</MAINLOCATION>";    
         //Dom document
         org.w3c.dom.Document doc=loadXML(xmlData);
         return doc;
    }
    
    //Delete main location
    public String deleteLocation(int locId){
    String msg="";
    boolean chk=false;
    List<MainLocation> lis =(List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m  WHERE m.locationId=:locId")
                .setParameter("locId",locId).getResultList();
    if(lis.isEmpty()){
        msg+="Location not deleted or it may not exist";
    }
    else{
        MainLocation mn=new MainLocation();
        mn=em.find(MainLocation.class, locId);
        msg+=mn.getLocationName()+". Successfully deleted";
        em.remove(mn);
    }    
    return msg;
    }
    
    //Update record to the database
    public void updateData(MainLocation ml){
    
        em.merge(ml);
        
    }
    
   //Get sublocation By ID
    public List<MainLocation> getSubLocListById(int locId){
    em = emf.createEntityManager();
    List<MainLocation> list = (List<MainLocation>)em.createQuery("SELECT m FROM MainLocation m where m.locationId=:locId")
    .setParameter("locId", locId).getResultList();
    
    return list;
    }
    
    //Insert data into database
    public void mappingSubLocation(SubLocation sbl){
    
     em.persist(sbl);
    }
    
            //XML builder for xml String
            public org.w3c.dom.Document loadXML(String xml) throws Exception
        {
               DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));

                org.w3c.dom.Document doc = db.parse(is);
                return doc;
        }
            
        //Distance calculation
        public int distance(String input, String subLocName) {
        input = input.toLowerCase();
        subLocName = subLocName.toLowerCase();
        // i == 0
        int [] costs = new int [subLocName.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= input.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= subLocName.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), input.charAt(i - 1) == subLocName.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[subLocName.length()];
    }
 
     //percentage calculation
    public double percentage(double distance,String input,String subLocName){
        double percent = 0; 
        percent=100-(distance*100)/max(input.length(),subLocName.length());
        return percent; 
    }
    
    //decision taking
    public String decision(double percentage,String locName,String subLocName){
        String xml="";
        if(percentage>=60){
        xml="<LOCATIONNAME>"+locName+"</LOCATIONNAME>"+"<SUBLOCATIONNAME>"+subLocName+"</SUBLOCATIONNAME>";
        }
        return xml;
    }
             
}

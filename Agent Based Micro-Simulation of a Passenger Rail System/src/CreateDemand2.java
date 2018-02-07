import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.io.MatsimNetworkReader;
//import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
//import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


public class CreateDemand2 {

	private static final String NETWORKFILE = "//jupiter.inf.brad.ac.uk/oamakind/SOIProfile/Desktop/Traffic Simulation/Workspace/pt2matsim/input/NYmappedNTW.xml";
	private static final String KREISE = "./input/new-york_new-york-shapefiles/new-york_new-york_osm_buildings.shp";
	
	private static final String PLANSFILEOUTPUT = "./input/20TESTPLAN.xml";
	
	private static final String pusTripsFile = "./input/random1%_of_travelSurvey_trips.txt";
	private static final String SHOPS = "input/shops.txt";
	//private static final String KINDERGARTEN = "input/kindergaerten.txt";
	
	
	private Scenario scenario;
	private Map<String,Geometry> shapeMap;
	private static double SCALEFACTOR = 0.1;
	
	private Map<String,Coord> Home;
	private Map<String,Coord> Work;
	private Map<String,Coord> kindergartens;
	private Map<String,Coord> shops;
	
	private Map<String,Coord> HomefromGeometry = new HashMap<>();
	private Map<String,Coord> WorkfromGeometry = new HashMap<>();
	
	
	CreateDemand2 (){
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		Network net = scenario.getNetwork();
		MatsimNetworkReader onr = new MatsimNetworkReader(net);
		onr.readFile(NETWORKFILE);
		
		
		//new MatsimNetworkReader.readFile(NETWORKFILE);	
				
	}
	public static void main(String[] args) {

		CreateDemand2 cd = new CreateDemand2();
		cd.run();
	}
	
	private void run() {
		this.shapeMap = readShapeFile(KREISE, "type");
		//this.Home = readFacilityLocations(pusTripsFile, "Home");
		//this.Work = readFacilityLocations(pusTripsFile, "Work");
		//this.shops = readFacilityLocations(SHOPS);
		//this.kindergartens = readFacilityLocations(KINDERGARTEN);
		
		//CB-CB
		double commuters = 200*SCALEFACTOR;
		double carcommuters = 0.55 * commuters;
		//Achtung! 3 Nullen extra hier ggÃ¼. ISIS Daten
		System.out.println(this.shapeMap.keySet());
		//Geometry home = this.shapeMap.get("apartments");
		//Geometry work = this.shapeMap.get("commercial");
		
		//this is my code
		for(String key:this.shapeMap.keySet()){
			 if (key.equals("apartments") || key.equals("apartment") || key.equals("residential") || key.equals("house")){
				 
				 Geometry home = this.shapeMap.get(key);
				 Coord homecoordinate = drawRandomPointFromGeometry(home);
				 this.HomefromGeometry.put(key, homecoordinate);
				 
			 }
			 
			 if (key.equals("commercial") || key.equals("industrial") || key.equals("office") || key.equals("comapny") || key.equals("brewery")){
				 
				 Geometry work = this.shapeMap.get(key);
				 Coord workcoordinate = drawRandomPointFromGeometry(work);
				 this.WorkfromGeometry.put("work", workcoordinate);
				 
			 }
				 
		}
		
		System.out.println(this.shapeMap.size()+" "+this.shapeMap.get("commercial") );
		System.out.println(this.HomefromGeometry.keySet()+" "+this.HomefromGeometry.size());
		
		
		//for (int i = 0; i<=commuters;i++){
					
		  try{  
			  BufferedReader bufferedReader = new BufferedReader(new FileReader(pusTripsFile));
  			bufferedReader.readLine(); //skip header
  			
  			int index_personId = 0;
  			int index_xCoordOrigin = 11;
  			int index_yCoordOrigin = 10;
  			int index_xCoordDestination = 13;
  			int index_yCoordDestination = 12;
  			int index_activityDuration = 8;
  			int index_mode = 14;
  			int index_activityType = 2;
  			
  			String line;
			while((line = bufferedReader.readLine()) != null){
				String parts[] = line.split(",");
				
			  
			  int i = 0;	
			  String mode = "car";
			  mode = "pt"; //if (i>carcommuters) mode = "pt";
			  Coord homepoint = createPoint(Double.parseDouble(parts[index_xCoordOrigin]), Double.parseDouble(parts[index_yCoordOrigin]));
			  //Coord homec = drawRandomPointFromGeometry(home);
			  //System.out.println(homec); //+" : closest kg: "+this.findClosestCoordFromMap(homec, this.kindergartens)+"closest shop:"+this.findClosestCoordFromMap(homec, this.shops));
//				System.out.println(this.findClosestCoordFromMap(homec, this.kindergartens));
			 // Coord workc = drawRandomPointFromGeometry(work);
			  Coord workpoint = createPoint(Double.parseDouble(parts[index_xCoordDestination]), Double.parseDouble(parts[index_yCoordDestination]));
			  
			  Coord closestHomepoint = this.findClosestCoordFromMap(homepoint, HomefromGeometry);
			  Coord closestWorkpoint = this.findClosestCoordFromMap(workpoint, WorkfromGeometry); 
			  
			  //find random points closent to home and work
			  System.out.println("id "+ parts[index_personId]+" " +parts[index_xCoordOrigin]+" " +parts[index_yCoordOrigin]+" Home point "+ closestHomepoint);
			  System.out.println("id "+ parts[index_personId]+" "+parts[index_xCoordDestination]+" " +parts[index_yCoordDestination]+" Work point "+ closestWorkpoint);
			  
			  createOnePerson(Integer.parseInt(parts[index_personId]), closestHomepoint, closestWorkpoint, mode, "CB_CB");
			 //i++;
		    }
			bufferedReader.close();
			
		  }catch (IOException e) {
    			e.printStackTrace();
        	}
		//}
	
		PopulationWriter pw = new PopulationWriter(scenario.getPopulation(),scenario.getNetwork());
		pw.write(PLANSFILEOUTPUT);
	}
	
	
	private void createOnePerson(int i, Coord coord, Coord coordWork, String mode, String toFromPrefix) {
		Id<Person> personId = Id.createPersonId(toFromPrefix+i);
		Person person = scenario.getPopulation().getFactory().createPerson(personId);
 
		Plan plan = scenario.getPopulation().getFactory().createPlan();
 
 
		Activity home = scenario.getPopulation().getFactory().createActivityFromCoord("home", coord);
		home.setEndTime(9*60*60);
		plan.addActivity(home);
 
		Leg hinweg = scenario.getPopulation().getFactory().createLeg(mode);
		plan.addLeg(hinweg);
 
		Activity work = scenario.getPopulation().getFactory().createActivityFromCoord("work", coordWork);
		work.setEndTime(17*60*60);
		plan.addActivity(work);
 
		Leg rueckweg = scenario.getPopulation().getFactory().createLeg(mode);
		plan.addLeg(rueckweg);
 
		Activity home2 = scenario.getPopulation().getFactory().createActivityFromCoord("home", coord);
		plan.addActivity(home2);
 
		person.addPlan(plan);
		scenario.getPopulation().addPerson(person);
	}
	
	private  Coord drawRandomPointFromGeometry(Geometry g) {
		   Random rnd = MatsimRandom.getLocalInstance();
		   Point p;
		   double x, y;
		   do {
		      x = g.getEnvelopeInternal().getMinX() +  rnd.nextDouble() * (g.getEnvelopeInternal().getMaxX() - g.getEnvelopeInternal().getMinX());
		      y = g.getEnvelopeInternal().getMinY() + rnd.nextDouble() * (g.getEnvelopeInternal().getMaxY() - g.getEnvelopeInternal().getMinY());
		      p = MGC.xy2Point(x, y);
		   } while (!g.contains(p));
		   Coord coord = new Coord(p.getX(), p.getY());
		   return coord;
		}
	
	/**************This is my method for geting point from coordinates***********/
	private  Coord createPoint(double X, double Y) {
		   
		   Point p;
		   double x, y;
		  
		      x = X;
		      y = Y;
		      p = MGC.xy2Point(x, y);
		  
		   Coord coord = new Coord(p.getX(), p.getY());
		   return coord;
		}


	
	
	public Map<String,Geometry> readShapeFile(String filename, String attrString){
		//attrString: FÃ¼r Brandenburg: Nr
		//fÃ¼r OSM: osm_id
		
		Map<String,Geometry> shapeMap = new HashMap<String, Geometry>();
		 
		for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {

				GeometryFactory geometryFactory= new GeometryFactory();
				WKTReader wktReader = new WKTReader(geometryFactory);
				Geometry geometry;

				try {
					geometry = wktReader.read((ft.getAttribute("type")).toString());
					shapeMap.put(ft.getAttribute(attrString).toString(),geometry);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} 
		return shapeMap;
	}	
	
	private Map<String,Coord> readFacilityLocations (String fileName, String home_or_work){
		
		FacilityParser fp = new FacilityParser(home_or_work);
		TabularFileParserConfig config = new TabularFileParserConfig();
		config.setDelimiterRegex("\t");
		config.setCommentRegex("#");
		config.setFileName(fileName);
		new TabularFileParser().parse(config, fp);
		return fp.getFacilityMap();
		
	}
	
	private Coord findClosestCoordFromMap(Coord location, Map<String,Coord> coordMap){
		Coord closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (Coord coord : coordMap.values()){
			double distance = CoordUtils.calcEuclideanDistance(coord, location);
			if (distance<closestDistance) {
				closestDistance = distance;
				closest = coord;
			}
		}
		return closest;
	}
	
	
	
}

	class FacilityParser implements TabularFileHandler{
		public String home_or_work;
		
	FacilityParser(String workhome){
		home_or_work = workhome;
		
	}
		
	private Map<String,Coord> facilityMap = new HashMap<String, Coord>();	
	CoordinateTransformation ct = new GeotoolsTransformation("EPSG:4326", "EPSG:32633"); 
    
	@Override
	public void startRow(String[] row) {
		Double x;
		Double y;
		try{
			if (home_or_work.equals("Home")){
				 x = Double.parseDouble(row[2]);
				 y = Double.parseDouble(row[1]);
			}else{
				 x = Double.parseDouble(row[2]);
				 y = Double.parseDouble(row[1]);
			}
		Coord coords = new Coord(x,y);
		this.facilityMap.put(row[0],ct.transform(coords));
		}
		catch (NumberFormatException e){
			//skips line
		}
	}

	public Map<String, Coord> getFacilityMap() {
		return facilityMap;
	}
	
	
}



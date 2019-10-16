package IotDomain;

import be.kuleuven.cs.som.annotate.*;
import org.jxmapviewer.viewer.GeoPosition;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * A class representing a map of the environment.
 */
public class Environment implements Serializable {

    private static final long serialVersionUID = 1L;
     /**
     * The coordinates of the point [0,0] on the map.
     */
    private final GeoPosition mapOrigin;

    /**
     * The max x-coordinate allowed on the map
     */
    private final int maxXpos;
    /**
     * The max y-coordinate allowed on the map
     */
    private final int maxYpos;
    /**
     * A list containing all motes currently active on the map.
     */
    private LinkedList<Mote> motes = new LinkedList<>();

    /**
     * A list containing all gateways currently active on the map.
     */
    private LinkedList<Gateway> gateways = new LinkedList<>();

    private MQTTServer MQTTServer;

    /**
     * The actual map containing the characteristics of the environment.
     */
    private Characteristic[][] characteristics;
    /**
     * A clock to represent time in the environment.
     */
    private LocalTime clock;
    /**
     * The number of zones in the configuration.
     */
    private int numberOfZones = 36;
    /**
     * The WayPoints in the configurations.
     */
    private LinkedHashSet<GeoPosition> wayPoints;
    /**
     * The number of runs with this configuration.
     */
    private int numberOfRuns;

    /**
     * A constructor generating a new environment with a given map with characteristics.
     * @param mapOrigin coordinates of the point [0,0] on the map.
     * @param characteristics   The map with the characteristics of the current environment.
     * @param wayPoints The WayPoints of the configuration.
     * @Post    Sets the max x-coordinate to the x size of the map if the map is valid.
     * @Post    Sets the max y-coordinate to the y size of the map if the map is valid.
     * @Post    Sets the characteristics to the given map if the map is valid.
     * @Post    Sets the max x-coordinate to 0 if the map is  not valid.
     * @Post    Sets the max y-coordinate to 0 if the map is not valid.
     * @Post    Sets the characteristics to an empty list if the map is not valid.
     */
    public Environment(Characteristic[][] characteristics, GeoPosition mapOrigin, LinkedHashSet<GeoPosition> wayPoints){
        if (areValidCharacteristics(characteristics)) {
            maxXpos = characteristics.length-1;
            maxYpos = characteristics[0].length-1;
            this.characteristics = characteristics;
            this.numberOfZones = (maxXpos + 1) * (maxYpos + 1);
        } else {
            // FIXME this is buggy -> maxXpos of 0 would mean that index 0 is still valid, whilst it shouldn't be
            maxXpos = 0;
            maxYpos = 0;
            this.characteristics = new Characteristic[0][0];
            this.numberOfZones = 0;
        }
        clock = LocalTime.of(0,0);
        this.mapOrigin = mapOrigin;
        this.MQTTServer = new MQTTServer();
        this.wayPoints = wayPoints;
        numberOfRuns = 1;
    }

    /**
     * Returns the MQTT server used in this environment.
     * @return the MQTT server used in this environment.
     */
    public MQTTServer getMQTTServer() {
        return MQTTServer;
    }

    /**
     * Gets the number of zones.
     * @return The number of zones.
     */
    public int getNumberOfZones() {
        return numberOfZones;
    }

    /**
     * Sets the number of zones.
     * @param numberOfZones the number of zones.
     */
    public void setNumberOfZones(int numberOfZones) {
        this.numberOfZones = numberOfZones;
    }

    /**
     * Adds a waypoint to the configuration
     * @param wayPoint The waypoint to add
     */
    public void addWayPoint(GeoPosition wayPoint){
        this.wayPoints.add(wayPoint);
    }

    /**
     * Gets the paths.
     * @return The paths.
     */
    public LinkedHashSet<GeoPosition> getWayPoints() {
        return wayPoints;
    }

    /**
     * Determines if a x-coordinate is valid on the map
     * @param x The x-coordinate to check
     * @return true if the coordinate is not bigger than the max coordinate
     */
    public boolean isValidXpos(int x) {
        return x >= 0 && x <= getMaxXpos();
    }

    /**
     *
     * @return the max x-coordinate
     */
    @Basic
    public int getMaxXpos() {
        return maxXpos;
    }

    /**
     * Determines if a y-coordinate is valid on the map
     * @param y The y-coordinate to check
     * @return true if the coordinate is not bigger than the max coordinate
     */
    public boolean isValidYpos(int y){
        return y >= 0 && y <= getMaxYpos();
    }

    /**
     *
     * @return the max y-coordinate
     */
    @Basic
    public int getMaxYpos() {
        return maxYpos;
    }

    /**
     * Returns all the gateways on the map.
     * @return A list with all the gateways on the map.
     */
    @Basic
    public LinkedList<Gateway> getGateways() {
        return gateways;
    }

    /**
     * Adds a gateway to the list of gateways if it is located in this environment.
     * @param gateway  the node to add
     * @Post    If the gateway is in this environment, it is added to the list.
     */
    @Basic
    public void addGateway(Gateway gateway) {
        if(gateway.getEnvironment() == this){
            gateways.add(gateway);
        }
    }

    /**
     *
     * @return A list with all the motes on the map.
     */
    @Basic
    public LinkedList<Mote> getMotes() {
        return motes;
    }

    /**
     * Adds a mote to the list of motes if it is located in this environment.
     * @param mote  the mote to add
     * @Post    If the mote is in this environment, it is added to the list.
     */
    @Basic
    public void addMote(Mote mote) {
        if(mote.getEnvironment() == this){
            motes.add(mote);
        }
    }

    /**
     * Determines if a given map of characteristics is valid.
     * @param characteristics The map to check.
     * @return  True if the Map is square.
     */
    public boolean areValidCharacteristics(Characteristic[][] characteristics){
        if (characteristics.length == 0) {
            return false;
        } else if (characteristics[0].length == 0) {
            return false;
        }

        // Make sure that each row has the same length
        int ySize = characteristics[0].length;

        for (Characteristic[] row : characteristics) {
            if (row.length != ySize) {
                return false;
            }
        }

        return true;
    }

    /**
     * returns the characteristics of a given position
     * @param xPos  The x-coordinate of the position.
     * @param yPos  The y-coordinate of the position.
     * @return  the characteristic of the position if the position is valid.
     */
    public Characteristic getCharacteristic(int xPos, int yPos) {
        if (isValidXpos(xPos) && isValidYpos(yPos)) {
            return characteristics[xPos][yPos];
        }
        else
            return null;
    }

    /**
     * Sets the characteristic to the given characteristic on the given location.
     * @param characteristic the given characteristic.
     */
    public void setCharacteristics(Characteristic characteristic, int xPos, int yPos) {
        this.characteristics[xPos][yPos] = characteristic;
    }

    /**
     * Returns the current time in the simulation.
     * @return The current time in the simulation.
     */
    public LocalTime getTime() {
        return clock;
    }

    /**
     * Increases the time with a given amount of miliseconds.
     * @param milliSeconds
     * @Post Increases the time with a given amount of miliseconds.
     */
    public void tick(long milliSeconds) {
        this.clock= this.clock.plus(milliSeconds, ChronoUnit.MILLIS);
    }

    public void resetClock(){
        this.clock =  LocalTime.of(0,0);
    }

    /**
     * Returns the coordinates of the point [0,0] on the map.
     * @return The coordinates of the point [0,0] on the map.
     */
    public GeoPosition getMapOrigin() {
        return mapOrigin;
    }

    /**
     * Returns the geoPosition of the center of the map.
     * @return The geoPosition of the center of the map.
     */
    public GeoPosition getMapCenter() {

        return new GeoPosition(toLatitude(getMaxYpos()/2),toLongitude(getMaxXpos()/2));
    }

    /**
     * A function to calculate the longitude from a given x-coordinate on the map.
     * @param xPos  The x-coordinate of the entity.
     * @return The longitude of the given x-coordinate
     */
    public double toLongitude(int xPos){
        double longitude;
        if(xPos> 0) {
            longitude = (double) xPos;
            longitude = longitude / 1000;
            longitude = longitude / 1.609344;
            longitude = longitude / (60 * 1.1515);
            longitude = Math.toRadians(longitude);
            longitude = Math.cos(longitude);
            longitude = longitude - Math.sin(Math.toRadians(getMapOrigin().getLatitude())) * Math.sin(Math.toRadians(getMapOrigin().getLatitude()));
            longitude = longitude / (Math.cos(Math.toRadians(getMapOrigin().getLatitude())) * Math.cos(Math.toRadians(getMapOrigin().getLatitude())));
            longitude = Math.acos(longitude);
            longitude = Math.toDegrees(longitude);
            longitude = longitude + getMapOrigin().getLongitude();
        }
        else{
            longitude = getMapOrigin().getLongitude();
        }
        return longitude;


    }
    /**
     * A function to calculate the latitude from a given y-coordinate on the map.
     * @param yPos  The y-coordinate of the entity.
     * @return The latitude of the given y-coordinate.
     */
    public double toLatitude(int yPos){
        double latitude = (double) yPos;
        latitude = latitude /1000 ;
        latitude = latitude/ 1.609344;
        latitude = latitude / (60 * 1.1515);
        latitude = latitude + getMapOrigin().getLatitude();
        return latitude;

    }

    /**
     * A function for calculating distances from geographical positions.
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }

    /**
     * A function that moves a mote to a geoposition 1 step and returns if the note has moved.
     * @param position The position to move towards.
     * @param mote The mote to move.
     * @return If the node has moved.
     */
    public boolean moveMote(Mote mote, GeoPosition position){
        int xPos = toMapXCoordinate(position);
        int yPos = toMapYCoordinate(position);
        if(Integer.signum(xPos - mote.getXPos()) != 0 || Integer.signum(yPos - mote.getYPos()) != 0){
            if(Math.abs(mote.getXPos() - xPos) >= Math.abs(mote.getYPos() - yPos)){
                mote.setXPos(mote.getXPos()+ Integer.signum(xPos - mote.getXPos()));

            }
            else{
                mote.setYPos(mote.getYPos()+ Integer.signum(yPos - mote.getYPos()));
            }
            return true;
        }
        return false;
    }

    /**
     * Converts a GeoPostion to an x-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The x-coordinate on the map of the GeoPosition.
     */
    public int toMapXCoordinate(GeoPosition geoPosition){
        return (int)Math.round(1000*distance(getMapOrigin().getLatitude(), getMapOrigin().getLongitude(), getMapOrigin().getLatitude(), geoPosition.getLongitude()));
    }
    /**
     * Converts a GeoPostion to an y-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The y-coordinate on the map of the GeoPosition.
     */
    public int toMapYCoordinate(GeoPosition geoPosition){
        return (int)Math.round(1000*distance(getMapOrigin().getLatitude(), getMapOrigin().getLongitude(),geoPosition.getLatitude(), getMapOrigin().getLongitude()));
    }

    /**
     * reset all entities in the configuration.
     */
    public void reset(){
        for(Mote mote: getMotes()){
            mote.reset();
        }
        for(Gateway gateway: getGateways()){
            gateway.reset();
        }
        numberOfRuns = 1;
    }

    /**
     * Adds a run to all entities in the configuration.
     */
    public void addRun(){
        for (Mote mote : getMotes()) {
            mote.addRun();
        }
        for (Gateway gateway : getGateways()) {
            gateway.addRun();
        }
        numberOfRuns ++;
    }

    /**
     * Returns the number of runs of this configuration.
     * @return The number of runs of this configuration.
     */
    @Basic
    public int getNumberOfRuns(){
        return numberOfRuns;
    }


}


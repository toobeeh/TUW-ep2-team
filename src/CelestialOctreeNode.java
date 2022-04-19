import java.util.Iterator;

/**
 * Iterator to loop over all bodies in a sector
 */
class CONIterator implements Iterator<Body>{

    CelestialOctreeNode[] nodes;
    int currentIndex;
    Body nextBody;
    Iterator<Body> currentIterator;

    /**
     * Constructor of a sector iteration if the sector contains multiple bodies (and subsectors)
     * @param subSectors
     */
    public CONIterator(CelestialOctreeNode[] subSectors){
        this.nodes = subSectors;
        this.currentIndex = 0;
        this.currentIterator = null;
        this.getNextBody();
    }

    /**
     * Costructor of a sector iteration if the sector consists only of a single body (and has no subsectors)
     * @param sectorBody
     */
    public CONIterator(Body sectorBody){
        this.nextBody = sectorBody;
        this.nodes = new CelestialOctreeNode[0];
        this.currentIterator = null;
        this.currentIndex = 0;
    }

    /**
     * Get the next body beyond all nodes
     */
    private void getNextBody() {

        // reset current body
        this.nextBody = null;

        // find next node with iterator that has a next
        while(this.currentIndex < this.nodes.length && this.nextBody == null){

            // if current index points to an instance
            if (this.nodes[this.currentIndex] != null){

                // if current iterator isnt set, get next node and its iterator
                if(this.currentIterator == null) this.currentIterator = this.nodes[this.currentIndex].iterator();

                // get from iterator and check if it's empty now
                this.nextBody = currentIterator.next();
                if(!this.currentIterator.hasNext()){
                    this.currentIterator = null;
                    currentIndex++;
                }
            }

            // else just try to go to a further node
            else this.currentIndex++;
        }
    }

    /**
     * implementation of iterator interface
     * @return if this sector iteration has a body left
     */
    @Override
    public boolean hasNext() {
        return this.nextBody != null;
    }

    /**
     * implementation of iterator interface
     * @return the next body of this sector iteration
     */
    @Override
    public Body next() {
        Body currentNext = this.nextBody;
        this.getNextBody();
        return currentNext;
    }
}

/**
 * Node of an octree used for the barnes-hut-algorithm
 */
public class CelestialOctreeNode implements Iterable {

    /*
        Sector Coordinate Indexing / Translation:
        index as decimal -> sector coordinates as bit array [x, y, z]

        e.g. index 3 -> [0, 1, 1]
        translates to x: 0, y: 1, z: 1

        find a sketch here: https://imgur.com/a/DEG0Ur1
     */

    /** Other sectors/bodies contained in this sector */
    private CelestialOctreeNode[] subNodes;

    /** Element that holds next iteration eleemnt */
    private CelestialOctreeNode next = null;

    /** Approximated body of this sector */
    private Body sectorApproximation;

    /** size of the sector, in meters (length of square sides) */
    private double sectorSize;

    /** center coordinates of the sector */
    private Vector3 sectorCenter;

    /** Indicator whether this sector is an actual body, not approximated data of bodies in a sector */
    private boolean isBody;

    /**
     * Creates a new 3d subsector
     * @param sectorApproximation the body approximation which this sector equals - can also be an actual body
     * @param sectorSize the square side length of this sector in meters
     * @param sectorCenter the sector center coordinates in 3d space
     */
    public CelestialOctreeNode(Body sectorApproximation, double sectorSize, Vector3 sectorCenter){
        this.subNodes = new CelestialOctreeNode[8];
        this.sectorApproximation = sectorApproximation;
        this.isBody = true;
        this.sectorSize = sectorSize;
        this.sectorCenter = sectorCenter;
    }

    /**
     * implements the iterable interface
     * @return an iterator that is used to loop over all bodies in this sector
     */
    public Iterator iterator() {
        if(this.isBody == false) return new CONIterator(this.subNodes);
        else return new CONIterator(this.sectorApproximation);
    }

    /**
     * Adds a body to this sector
     * @param body the body to add
     */
    public void addBody(Body body){

        // if the sector contained only one body, move the body to a sector slot
        if(this.isBody){

            // get the sector index where sector body belongs to
            int subSectorIndex = this.sectorApproximation.getBarnesHutSubsectorIndex(this.sectorCenter);

            // add to subsector
            this.addBodyToSubSector(subSectorIndex, this.sectorApproximation);

            // mark that this sector doesn't just contain of a single body anymore
            this.isBody = false;
        }

        // get sector index of the actual to-add body
        int subSectorIndex = body.getBarnesHutSubsectorIndex(this.sectorCenter);

        // add body there
        this.addBodyToSubSector(subSectorIndex, body);

        // update sector approximation
        this.sectorApproximation = this.sectorApproximation.merge(body);
    }

    /**
     * Adds a body to a subsector or inits a new subsector if none was present at that index
     * @param subindex the subsector index coordinates
     * @param body the body to add
     */
    private void addBodyToSubSector(int subindex, Body body){

        // if subsector is not initialized, create new with body as body approximation
        if(this.subNodes[subindex] == null) {
            this.subNodes[subindex] = new CelestialOctreeNode(
                    body,
                    this.sectorSize / 2,
                    this.calcSubSectorCenter(subindex)
            );
        }

        // else add body to the existing sector
        else this.subNodes[subindex].addBody(body);
    }

    /**
     * calculates the center position of a subsector
     * @param vectorCoordinates the coordinates to address the target subsector in this octree
     * @return the sector center coordinates relative to the middle of this tree's coordinate system middle
     */
    private Vector3 calcSubSectorCenter(int vectorCoordinates) {

        /*
        // check if vector coordinates are valid
        if(vectorCoordinates > 8 || vectorCoordinates < 0){
            throw new Error("Coordinates for subsector were out of bounds");
        }
        */

        // mask bits and get subsector
        boolean x = (vectorCoordinates & 4) == 4;
        boolean y = (vectorCoordinates & 2) == 2;
        boolean z = (vectorCoordinates & 1) == 1;

        // calc vector components
        double distanceToSubCenters = this.sectorSize / 4;
        Vector3 subCenter = this.sectorCenter.plus(new Vector3(
                x ? distanceToSubCenters : -distanceToSubCenters,
                y ? distanceToSubCenters : -distanceToSubCenters,
                z ? distanceToSubCenters : -distanceToSubCenters
        ));
        return subCenter;
    }

}

public class CelestialOctreeNode {

    /*
        Sector Coordinate Indexing / Translation:
        index as decimal -> sector coordinates as bit array [x, y, z]

        e.g. index 3 -> [0, 1, 1]
        translates to x: 0, y: 1, z: 1

        find a sketch here: https://imgur.com/a/DEG0Ur1
     */

    /** Other sectors/bodies contained in this sector */
    private CelestialOctreeNode[] subNodes;

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
     * Adds a body to this sector
     * @param body the body to add
     */
    public void addBody(Body body){

        // if the sector contained only one body, move the body to a sector slot
        if(this.isBody){

            // get the sector index where sector body belongs to
            int subindex = this.calcSectorIndexOf(this.sectorApproximation);

            // add to subsector
            this.addBodyToSubsector(subindex, this.sectorApproximation);

            // mark that this sector doesn't just contain of a single body anymore
            this.isBody = false;
        }

        // get sector index of the actual to-add body
        int subindex = this.calcSectorIndexOf(body);

        // add body there
        this.addBodyToSubsector(subindex, body);

        // update sector approximation
        this.sectorApproximation = this.sectorApproximation.merge(body);
    }

    /**
     * Adds a body to a subsector or inits a new subsector if none was present at that index
     * @param subindex the subsector index coordinates
     * @param body the body to add
     */
    private void addBodyToSubsector(int subindex, Body body){

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
     * Gets a sub-sector of this sector at a specified index
     * @param nodePosition position of the sector node
     * @return a sector / body at specified index if present, else null
     */
    public CelestialOctreeNode subNodeAt(int nodePosition){
        return subNodes[nodePosition];
    }

    /**
     * Calculates in which of this sectors sub-sectors a body would fit
     * @param body the body to check on
     * @return sector index from 0-7 or -1 if body is out of this sector
     */
    private int calcSectorIndexOf(Body body){

        // calculate body relative position to sector center
        double centerSideNormDistance = this.sectorSize / 2;
        double[] relativeBodyPosition = body.getMassCenter().minus(this.sectorCenter).asComponentArray();

        // check if mass center is in sector bounds
        if(Math.abs(relativeBodyPosition[0]) > centerSideNormDistance
                || Math.abs(relativeBodyPosition[1]) > centerSideNormDistance
                || Math.abs(relativeBodyPosition[2]) > centerSideNormDistance) {
            throw new Error("Body center was out of sector bounds");
        }

        // else calculate coordinates in octree cube
        int x = relativeBodyPosition[0] < 0 ? 0 : 1;
        int y = relativeBodyPosition[1] < 0 ? 0 : 1;
        int z = relativeBodyPosition[2] < 0 ? 0 : 1;
        int sector = Integer.parseInt(x + "" + y + "" + z, 2);

        return sector;
    }

    private Vector3 calcSubSectorCenter(int vectorCoordinates) {

        // check if vector coordinates are valid
        if(vectorCoordinates > 8 || vectorCoordinates < 0) throw new Error("Coordinates for subsector were out of bounds");

        // get binary representation ~ add 8 to get a 4-digit code, then cut off first index
        String binaryCoords = Integer.toBinaryString(vectorCoordinates + 8).substring(1);

        // calc vector components
        double distanceToSubCenters = this.sectorSize / 4;
        Vector3 subCenter = this.sectorCenter.plus(new Vector3(
                binaryCoords.charAt(0) == '0' ? -distanceToSubCenters : distanceToSubCenters,
                binaryCoords.charAt(1) == '0' ? -distanceToSubCenters : distanceToSubCenters,
                binaryCoords.charAt(2) == '0' ? -distanceToSubCenters : distanceToSubCenters
        ));
        return subCenter;
    }

}

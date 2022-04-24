import java.util.Iterator;

/**
 * Iterator to loop over all bodies in a sector (celestial octree node)
 */
public class CONIterator implements Iterator<Body> {

    CelestialOctreeNode[] nodes;
    int currentIndex;
    Body nextBody;
    Iterator<Body> currentIterator;

    /**
     * Constructor of a sector iteration if the sector contains multiple bodies (and subsectors)
     * @param subSectors teh subsectors of a sectors which have to be iterated over
     */
    public CONIterator(CelestialOctreeNode[] subSectors){
        this.nodes = subSectors;
        this.currentIndex = 0;
        this.currentIterator = null;
        this.getNextBody();
    }

    /**
     * Costructor of a sector iteration if the sector consists only of a single body (and has no subsectors)
     * @param sectorBody the body approximation of the sector
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

            // if current index points to an instance (subsector has planet(s) )
            if (this.nodes[this.currentIndex] != null){

                // if current iterator isn't set, get next node and its iterator
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

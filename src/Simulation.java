import codedraw.CodeDraw;

import java.util.Random;

import static java.lang.System.nanoTime;

public class Simulation {

    // gravitational constant
    public static final double G = 6.6743e-11;

    // one astronomical unit (AU) is the average distance of earth to the sun.
    public static final double AU = 150e9; // meters

    // one light year
    public static final double LY = 9.461e15; // meters

    // some further constants needed in the simulation
    public static final double SUN_MASS = 1.989e30; // kilograms
    public static final double SUN_RADIUS = 696340e3; // meters
    public static final double EARTH_MASS = 5.972e24; // kilograms
    public static final double EARTH_RADIUS = 6371e3; // meters

    // set some system parameters
    public static final double SECTION_SIZE = 2 * AU; // the size of the square region in space
    public static final int NUMBER_OF_BODIES = 100000;
    public static final double OVERALL_SYSTEM_MASS = 20 * SUN_MASS; // kilograms

    // all quantities are based on units of kilogram respectively second and meter.

    public static void main(String[] args) {

        //CodeDraw cd = new CodeDraw();
        Random random = new Random(2022);

        // store bodies in an array
        Body[] bodies = new Body[NUMBER_OF_BODIES];

        // generate bodies
        for (int i = 0; i < NUMBER_OF_BODIES; i++) {
            bodies[i] = new Body(
                    Math.abs(random.nextGaussian()) * OVERALL_SYSTEM_MASS / NUMBER_OF_BODIES, // kg
                    new Vector3(
                            0.2 * random.nextGaussian() * AU,
                            0.2 * random.nextGaussian() * AU,
                            0.2 * random.nextGaussian() * AU
                    ),
                    new Vector3(
                            0 + random.nextGaussian() * 5e3,
                            0 + random.nextGaussian() * 5e3,
                            0 + random.nextGaussian() * 5e3
                    )
            );
        }


        double nano = nanoTime();
        for(int h = 0; h < 100; h++){

            // add bodies to celestial octree
            CelestialOctree tree = new CelestialOctree(SECTION_SIZE * 2);
            for(int i = 0; i < NUMBER_OF_BODIES; i++)
                tree.addBody(bodies[i]);
        }
        nano = nanoTime() - nano;
        System.out.println(nano / 100 / 1e6);

    }

}

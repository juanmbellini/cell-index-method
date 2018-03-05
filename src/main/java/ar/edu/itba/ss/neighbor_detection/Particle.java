package ar.edu.itba.ss.neighbor_detection;

/**
 * Represents a particle of the simulation.
 */
public class Particle {

    /**
     * The actual position of this particle.
     */
    private Point position;

    /**
     * The radius of this particle.
     */
    private final double radius;

    /**
     * Constructor.
     *
     * @param initialXPosition The initial position in the x axis for this particle.
     * @param initialYPosition The initial position in the y axis for this particle.
     * @param radius           The radius of this particle (can be zero in case this is a point-like particle).
     * @throws IllegalArgumentException If the {@code radius} is negative.
     */
    public Particle(final double initialXPosition, final double initialYPosition, final double radius)
            throws IllegalArgumentException {
        if (Double.compare(radius, 0.0) < 0) {
            throw new IllegalArgumentException("The radius must not be negative");
        }
        this.position = new Point(initialXPosition, initialYPosition);
        this.radius = radius;
    }

    /**
     * @return The actual position of this particle.
     */
    public Point getPosition() {
        return position;
    }

    /**
     * @return The radius of this particle.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Checks if this particle is a point-like particle (i.e with radius equals to zero).
     *
     * @return {@code true} if this particle is point-like (i.e radius equals to zero), or {@code false} otherwise.
     */
    public boolean isPointLike() {
        return Double.compare(radius, 0.0) == 0;
    }

    /**
     * Bean class representing a point in the plane.
     */
    public final static class Point {

        /**
         * The 'x' value of this point.
         */
        private final double x;

        /**
         * The 'y' value of this point.
         */
        private final double y;

        /**
         * Constructor.
         *
         * @param x The 'x' value of this point.
         * @param y The 'y' value of this point.ø
         */
        private Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return The 'x' value of this point.
         */
        public double getX() {
            return x;
        }

        /**
         * @return The 'y' value of this point.
         */
        public double getY() {
            return y;
        }
    }
}

package ar.edu.itba.ss.neighbor_detection;

import org.springframework.util.Assert;

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
     * Calculates the distance between {@code this} {@link Particle}, and the given {@code anotherParticle}.
     *
     * @param anotherParticle The {@link Particle} to which the distance to it must be calculated.
     * @return The calculated distance.
     * @implNote This method takes into account the radius of both {@link Particle}s.
     */
    public double distanceTo(Particle anotherParticle) {
        Assert.notNull(anotherParticle, "Must set another particle to calculate distance");

        final Point point = this.getPosition();
        final Point anotherPoint = anotherParticle.getPosition();
        final double x = point.getX() - anotherPoint.getX();
        final double y = point.getY() - anotherPoint.getY();
        final double centerDistance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        return centerDistance - (this.getRadius() + anotherParticle.getRadius());
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

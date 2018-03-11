package ar.edu.itba.ss.neighbor_detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Main class
 */
public class NeighborDetector {

    /**
     * The {@link Logger} object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NeighborDetector.class);

    /**
     * Entry point.
     *
     * @param args Execution arguments.
     */
    public static void main(String[] args) {
        LOGGER.info("Hello, Cell Index Method!");
        final int integerSide = new Random().nextInt(90) + 10;
        final double percentage = new Random().nextDouble();
        final double sideLength = percentage * integerSide;
        final int amountOfParticles = new Random().nextInt(30000 - 10000) + 10000;
        final Space space = Space.randomSpace(sideLength, amountOfParticles, false);
        final double interactionRadius = new Random().nextDouble() * 8.0 + 1.0;


        final int maxM = (int) Math.floor(sideLength / interactionRadius);
        final int M = maxM <= 1 ? 1 : new Random().nextInt(maxM) + 1;

        LOGGER.info("Starting algorithm with values: L = {}, M = {}, r = {}, n = {}.",
                sideLength, M, interactionRadius, amountOfParticles);
        final long startingTime = System.currentTimeMillis();
        final Map<Particle, List<Particle>> result = getParticles(space, interactionRadius, M);
        LOGGER.info("Finished program. Elapsed time: {} secs.", (System.currentTimeMillis() - startingTime) / 1000.0);
    }

    private static Map<Particle, List<Particle>> getParticles(Space space, double interactionRadius, int M) {
        final double spaceSideLength = space.getSideLength();
        if (M <= 0) {
            throw new IllegalArgumentException("There must be at least one grid per side");
        }
        if (Double.compare(interactionRadius, 0) < 0) {
            // TODO: check interaction radius == 0 (particles in the exact same position)
            throw new IllegalArgumentException("The interaction radius must be positive");
        }
        if (M != 1 && Double.compare((spaceSideLength / M), interactionRadius) <= 0) {
            throw new IllegalArgumentException("The interaction radius must be lower than " +
                    "the space side length divided by the amount of grids per side. " +
                    "Values were: L = " + spaceSideLength + ", M = " + M + ", r = " + interactionRadius + ".");
        }

        LOGGER.info("Splitting space into a grid....");
        // Split space particles into a grid
        final double factor = M / spaceSideLength;
        final GridCellFactory gridCellFactory = GridCellFactory.getFactory(M);
        final Map<GridCell, List<Particle>> grid = space.getParticles().stream()
                .collect(Collectors.groupingBy(p -> getGridPosition(p, factor, gridCellFactory)));
        LOGGER.info("Finished splitting space.");

        LOGGER.info("Calculating related particles...");
        // Transform each grid cell in the List of Particles belonging to the grid,
        // mapped to the List of Particles in the nearby cells
        final Map<List<Particle>, List<Particle>> relatedParticles = grid.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> nearParticles(e.getKey(), grid, M, gridCellFactory)));
        LOGGER.info("Finished calculating related particles.");

        LOGGER.info("Calculating neighbors...");
        final Map<Particle, List<Particle>> result = new HashMap<>();
        // For each <List,List> tuple, perform the following...
        for (Map.Entry<List<Particle>, List<Particle>> related : relatedParticles.entrySet()) {
            // A Set to save those particles in the same grid that their distances have been already calculated
            final Set<Particle> alreadyCalculated = new HashSet<>();

            // For each Particle in the Key List, perform the following...
            for (Particle particle : related.getKey()) {
                // Get the Particle's List of neighbors.
                final List<Particle> neighbors = result.getOrDefault(particle, new LinkedList<>());
                // Get the Particle's new neighbors, calculating the distance to the possible new neighbors,
                // and filtering only those whose distance is lower or equal to the interaction radius.
                final List<Particle> newNeighbors = related.getValue()
                        .stream()
                        .parallel() // Perform this operation using parallelism to increase performance
                        .filter(another -> Double.compare(particle.distanceTo(another), interactionRadius) <= 0)
                        .collect(Collectors.toList());
                // Calculate distances to the same grid's particles
                final List<Particle> sameGridNewNeighbors = related.getKey()
                        .stream()
                        .parallel() // Perform this operation using parallelism to increase performance
                        .filter(another -> !particle.equals(another))
                        .filter(another -> !alreadyCalculated.contains(another))
                        .filter(another -> Double.compare(particle.distanceTo(another), interactionRadius) <= 0)
                        .collect(Collectors.toList());
                alreadyCalculated.add(particle); // Save this particle in the already calculated set

                // Add all new neighbors.
                neighbors.addAll(newNeighbors);
                neighbors.addAll(sameGridNewNeighbors);

                // For each new Neighbor, add the Particle to their list of neighbors
                for (Particle another : newNeighbors) {
                    final List<Particle> anotherNeighbors = result.getOrDefault(another, new LinkedList<>());
                    anotherNeighbors.add(particle);
                    result.put(another, anotherNeighbors);
                }

                // Save the List in the result map
                result.put(particle, neighbors);
            }
        }
        LOGGER.info("Finished calculating neighbors.");
        return result;
    }


    /**
     * Method that calculates to which cell a particle belongs to.
     *
     * @param particle        The particle to which the calculation must be done.
     * @param factor          A factor used to calculate the position
     *                        (i.e space side length / amount of grids per side).
     * @param gridCellFactory The {@link GridCellFactory} from where {@link GridCell}s will be taken from.
     * @return The {@link GridCell} to which the particle belongs to.
     * @implNote The origin of the grid is the lower left corner.
     */
    private static GridCell getGridPosition(Particle particle, double factor, GridCellFactory gridCellFactory) {
        final int row = (int) (particle.getPosition().getY() * factor);
        final int column = (int) (particle.getPosition().getX() * factor);

        return gridCellFactory.getGridCell(row, column);
    }


    /**
     * Calculates which {@link Particle}s are related to a given {@link GridCell}.
     *
     * @param grid             The {@link GridCell} to which the related {@link Particle}s will be calculated.
     * @param particlesPerCell A {@link Map} holding, for each {@link GridCell},
     *                         a {@link List} of {@link Particle} that belongs to the said {@link GridCell}.
     * @param M                Amount of {@link GridCell}s in a space side
     *                         (used to know how to take into account periodic boundary conditions).
     * @param gridCellFactory  The {@link GridCellFactory} from where {@link GridCell}s will be taken from.
     * @return A {@link List} holding those {@link Particle}s related to the given {@code grid}.
     */
    private static List<Particle> nearParticles(GridCell grid, Map<GridCell, List<Particle>> particlesPerCell, int M,
                                                GridCellFactory gridCellFactory) {
        final Set<GridCell> neighborCells = neighborGridCell(grid, M, gridCellFactory);
        return particlesPerCell.entrySet().stream()
                .filter(entry -> neighborCells.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Calculates which {@link GridCell}s are related (are neighbors) with the given {@code gridCell}.
     *
     * @param gridCell        The {@link GridCell} to which the neighbor cells will be calculated.
     * @param M               Amount of {@link GridCell}s in a space side
     *                        (used to know how to take into account periodic boundary conditions)
     * @param gridCellFactory The {@link GridCellFactory} from where {@link GridCell}s will be taken from.
     * @return A {@link Set} holding the neighbor {@link GridCell}s of the given {@code gridCell}.
     */
    private static Set<GridCell> neighborGridCell(GridCell gridCell, int M, GridCellFactory gridCellFactory) {
        // Upper grid cell
        final int upperRow = Math.floorMod(gridCell.getRow() + 1, M);
        final int upperColumn = Math.floorMod(gridCell.getColumn(), M);

        // Upper-right grid cell coordinates
        final int upperRightRow = Math.floorMod(gridCell.getRow() + 1, M);
        final int upperRightColumn = Math.floorMod(gridCell.getColumn() + 1, M);

        // Right grid cell coordinates
        final int rightRow = Math.floorMod(gridCell.getRow(), M);
        final int rightColumn = Math.floorMod(gridCell.getColumn() + 1, M);

        // Lower-right grid cell coordinates
        final int lowerRightRow = Math.floorMod(gridCell.getColumn() - 1, M);
        final int lowerRightColumn = Math.floorMod(gridCell.getColumn() + 1, M);

        return Stream.of(
                gridCellFactory.getGridCell(upperRow, upperColumn),
                gridCellFactory.getGridCell(upperRightRow, upperRightColumn),
                gridCellFactory.getGridCell(rightRow, rightColumn),
                gridCellFactory.getGridCell(lowerRightRow, lowerRightColumn)
        ).collect(Collectors.toSet());
    }


    /**
     * Bean class representing a position in the grid (i.e a corresponding cell).
     */
    private static final class GridCell {

        /**
         * The row for this cell.
         */
        private final int row;

        /**
         * The column for this cell.
         */
        private final int column;

        /**
         * Constructor.
         *
         * @param row    The row for this cell.
         * @param column The column for this cell.
         */
        private GridCell(int row, int column) {
            if (row < 0 || column < 0) {
                throw new IllegalArgumentException("Row and Column must be positive.");
            }
            this.row = row;
            this.column = column;
        }

        /**
         * @return The row for this cell.
         */
        private int getRow() {
            return row;
        }

        /**
         * @return The column for this cell.
         */
        private int getColumn() {
            return column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GridCell)) {
                return false;
            }

            final GridCell gridCell = (GridCell) o;

            return row == gridCell.row && column == gridCell.column;
        }

        @Override
        public int hashCode() {
            return 31 * row + column;
        }
    }

    /**
     * Factory class for {@link GridCell}s, in order to avoid creating a new {@link GridCell} each time it is needed.
     */
    private static final class GridCellFactory {

        /**
         * {@link Map} holding, for each value of {@code M}, the corresponding {@link GridCellFactory}.
         */
        private final static Map<Integer, GridCellFactory> factories = new HashMap<>();

        /**
         * An 2-D array holding each grid cell,
         * ordered using the {@code row} and {@code column} of the stored {@link GridCell}s.
         */
        private final GridCell[][] grid;

        /**
         * The amount of cells per side.
         */
        private final int M;


        /**
         * Constructor.
         *
         * @param M The amount of cells per side.
         */
        private GridCellFactory(int M) {
            this.M = M;
            // Create all GridCells when this factory is created.
            this.grid = IntStream.range(0, M)
                    .mapToObj(row ->
                            IntStream.range(0, M)
                                    .mapToObj(column -> new GridCell(row, column))
                                    .toArray(GridCell[]::new))
                    .toArray(GridCell[][]::new);
        }

        /**
         * Retrieves the {@link GridCell} for the given {@code row} and {@code column.}
         *
         * @param row    The row of the {@link GridCell}.
         * @param column The column of the {@link GridCell}.
         * @return The {@link GridCell} with the given {@code row} and {@code column.}.
         */
        private GridCell getGridCell(int row, int column) {
            if (row < 0 || row >= M || column < 0 || column >= M) {
                throw new IllegalArgumentException("Row and Column must be values between 0 and " + M + ".");
            }
            return grid[row][column];
        }

        /**
         * Gets a {@link GridCellFactory} for the given {@code M}.
         *
         * @param M The amount of cells per side.
         * @return The {@link GridCellFactory}.
         * @implNote This is not synchronized but it does not matter because,
         * in case there are write interferences at most we will have instantiated twice the same factory,
         * which is not a problem, as the Map will override with the same value for a given key.
         */
        private static GridCellFactory getFactory(int M) {
            GridCellFactory factory = factories.get(M);
            if (factory == null) {
                factory = new GridCellFactory(M);
                factories.put(M, factory);
            }
            return factory;
        }
    }
}

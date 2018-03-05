package ar.edu.itba.ss.neighbor_detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

/**
 * Main class
 */
@SpringBootApplication
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Entry point.
     *
     * @param args Execution arguments.
     */
    public static void main(String[] args) {
        LOGGER.info("Hello, Systems Simulations!");
    }

    public static Map<Particle, List<Particle>> getParticles(Space space, double interactionRadius, int M) {
        // TODO: implement
        return null;
    }
}

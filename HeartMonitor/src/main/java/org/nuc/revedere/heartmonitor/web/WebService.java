package org.nuc.revedere.heartmonitor.web;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;

/**
 * See implementation <a
 * href="http://docs.oracle.com/cd/E26576_01/doc.312/e24932/embedded-server-guide.htm#gjrcs">http://docs.oracle.com/cd/E26576_01/doc.312/e24932/embedded-server-guide.htm#gjrcs</a>
 * 
 * @author Nuc
 *
 */
public class WebService {
    private static final Logger LOGGER = Logger.getLogger(WebService.class);
    final GlassFishProperties glassFishProperties;
    final GlassFish glassFish;
    final Deployer deployer;

    public WebService(int port) throws GlassFishException, URISyntaxException, IOException {
        glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort("http-listener", port);
        glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
        glassFish.start();

        deployer = glassFish.getDeployer();
        final File rootDir = new File(this.getClass().getResource("/WebContent/").toURI());
        final ScatteredArchive archive = new ScatteredArchive("heartmonitor", Type.WAR, rootDir);
        archive.addClassPath(new File("target", "classes"));

        deployer.deploy(archive.toURI(), "--contextroot=heart");
    }

    public void stop() throws GlassFishException {
        glassFish.stop();
    }

    public static void main(String[] args) {
        try {
            BasicConfigurator.configure();
            new WebService(8080);
        } catch (Exception e) {
            LOGGER.error("Failed to start web service", e);
        }
    }
}

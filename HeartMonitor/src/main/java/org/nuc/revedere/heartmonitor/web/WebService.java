package org.nuc.revedere.heartmonitor.web;

import java.io.File;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;

/**
 * See implementation <a href="http://docs.oracle.com/cd/E26576_01/doc.312/e24932/embedded-server-guide.htm#gjrcs">http://docs.oracle.com/cd/E26576_01/doc.312/e24932/embedded-server-guide.htm#gjrcs</a>
 * @author Nuc
 *
 */
public class WebService {
    final GlassFishProperties glassFishProperties;
    final GlassFish glassFish;
    final Deployer deployer;

    public WebService(int port) throws Exception {
        glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort("http-listener", port);
        glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
        glassFish.start();

        deployer = glassFish.getDeployer();
        final File rootDir = new File(this.getClass().getResource("/WebContent/").toURI());
        final ScatteredArchive archive = new ScatteredArchive("heartmonitor", Type.WAR, rootDir);
        archive.addClassPath(new File("target", "classes"));

        deployer.deploy(archive.toURI(), "--contextroot=hello");
    }

    public void stop() throws Exception {
        glassFish.stop();
    }
    
    public static void main(String[] args) throws Exception {
        new WebService(8080);
    }
}
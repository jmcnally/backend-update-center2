package org.jvnet.hudson.update_center;

import hudson.util.VersionNumber;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * {@link MavenRepository} that limits the plugins to those listed in 
 * the specifiedPlugins file.
 *
 * @author John McNally
 */
public class SpecifiedMavenRepository extends MavenRepository {
    private final MavenRepository base;
    private final Set<String> specifiedPlugins;
    
    public SpecifiedMavenRepository(MavenRepository base, File plugins) {
        this.base = base;
        this.specifiedPlugins = new HashSet<String>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(plugins));
            String s;
            while ((s = r.readLine()) != null) {
                this.specifiedPlugins.add(s.trim());
            }
            r.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Override
    public TreeMap<VersionNumber, HudsonWar> getHudsonWar() throws IOException, AbstractArtifactResolutionException {
        return base.getHudsonWar();
    }

    @Override
    public File resolve(ArtifactInfo a, String type, String classifier) throws AbstractArtifactResolutionException {
        return base.resolve(a, type, classifier);
    }

    @Override
    public Collection<PluginHistory> listHudsonPlugins() throws PlexusContainerException, ComponentLookupException, IOException, UnsupportedExistingLuceneIndexException, AbstractArtifactResolutionException {
        Collection<PluginHistory> result = new ArrayList<PluginHistory>(specifiedPlugins.size());
	Collection<PluginHistory> existingPlugins = base.listHudsonPlugins();
	for (PluginHistory ph : existingPlugins) {
	    if (this.specifiedPlugins.contains(ph.artifactId)) {
		result.add(ph);
	    }
	}
        return result;
    }
}

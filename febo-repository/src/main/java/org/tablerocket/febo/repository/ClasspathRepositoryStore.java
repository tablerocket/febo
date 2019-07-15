package org.tablerocket.febo.repository;

import org.tablerocket.febo.api.Dependency;
import org.tablerocket.febo.api.RepositoryStore;
import org.tablerocket.febo.api.ResolvedDependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClasspathRepositoryStore implements RepositoryStore
{
    public static final String BLOB_FILENAME = "febo-blobs.properties";
    private final Properties index;

    public Dependency dependency(String s)
    {
        return new ResolvedDependency(s,ResolvedDependency.parseLocation( index.getProperty( s )));
    }

    public ClasspathRepositoryStore()
    {
        this("/" + BLOB_FILENAME);
    }

    public ClasspathRepositoryStore(Properties p)
    {
        index = p;
    }

    public ClasspathRepositoryStore( String path )
    {
        index = new Properties(  );
        try
        {
            index.load( ClasspathRepositoryStore.class.getResourceAsStream( path ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException("Problem loading store from " + path,e);
        }
    }

    @Override public Dependency resolve( String s )
    {
        return new ResolvedDependency(s,ResolvedDependency.parseLocation( index.getProperty( s )));
    }

    @Override
    public Dependency[] platform() {
        List<Dependency> deps = new ArrayList<>();
        for(String key : index.stringPropertyNames()) {
            deps.add(resolve(key));
        }
        return deps.toArray(new Dependency[0]);
    }
}

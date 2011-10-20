package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Map;

public class Environment
{
    private final Map<String, Pair<Class<? extends Provisioner>, Map<String, String>>> provisioners = Maps.newConcurrentMap();
    private final Map<String, Pair<Class<? extends Installer>, Map<String, String>>>   installers   = Maps.newConcurrentMap();

    private final Map<String, Base>   bases      = Maps.newConcurrentMap();
    private final Map<String, String> properties = Maps.newConcurrentMap();

    public Environment()
    {
        this(Collections.<String, Pair<Class<? extends Provisioner>, Map<String, String>>>emptyMap(),
             Collections.<String, Pair<Class<? extends Installer>, Map<String, String>>>emptyMap(),
             Collections.<String, Base>emptyMap(),
             Collections.<String, String>emptyMap());
    }

    public Environment(Map<String, Pair<Class<? extends Provisioner>, Map<String, String>>> provisioners,
                       Map<String, Pair<Class<? extends Installer>, Map<String, String>>> installers,
                       Map<String, Base> bases,
                       Map<String, String> properties)
    {
        this.provisioners.putAll(provisioners);
        this.installers.putAll(installers);
        this.bases.putAll(bases);
        this.properties.putAll(properties);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public Maybe<Base> findBase(final String base)
    {
        if (bases.containsKey(base)) {
            return Maybe.definitely(bases.get(base));
        }
        else {
            return Maybe.unknown();
        }
    }

    public Map<String, String> getProperties()
    {
        return ImmutableMap.copyOf(this.properties);
    }

    public ActualDeployment planDeploymentFor(SystemMap map, Space state)
    {
        return new ActualDeployment(map, this, state);
    }

    public Maybe<Provisioner> findProvisioner(Uri<Provisioner> provisioner)
    {
        if (provisioners.containsKey(provisioner.getScheme())) {
            Pair<Class<? extends Provisioner>, Map<String, String>> pair = provisioners.get(provisioner.getScheme());
            try {
                return Maybe.definitely(Instantiator.create(pair.getLeft(), pair.getRight()));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate provisioner", e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }

    public Maybe<Installer> findInstaller(Uri<Installer> uri)
    {
        if (installers.containsKey(uri.getScheme())) {
            Pair<Class<? extends Installer>, Map<String, String>> pair = installers.get(uri.getScheme());
            try {
                return Maybe.definitely(Instantiator.create(pair.getLeft(), pair.getRight()));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate provisioner", e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }

    public Provisioner resolveProvisioner(Uri<Provisioner> uri)
    {
        return findProvisioner(uri).otherwise(new ErrorProvisioner());
    }

    public Installer resolveInstaller(Uri<Installer> uri)
    {
        return findInstaller(uri).otherwise(new ErrorInstaller(Collections.<String, String>emptyMap()));
    }
}

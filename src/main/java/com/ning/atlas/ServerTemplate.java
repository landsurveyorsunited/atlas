package com.ning.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerTemplate extends Template
{
    private List<String> installations = new ArrayList<String>();
    private String base;

    public ServerTemplate(String name)
    {
        super(name);
    }

    @Override
    public final Iterable<BoundTemplate> _normalize(Environment env)
    {
        final List<BoundTemplate> rs = new ArrayList<BoundTemplate>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {
            rs.add(new BoundServer(this, node_name, env, installations));
        }
        return rs;
    }

    public Iterable<? extends Template> getChildren()
    {
        return Collections.emptyList();
    }

    public List<String> getInstallations()
    {
        return installations;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    public String getBase()
    {
        return base;
    }

    /**
     * called by jruby template parser
     */
    public void setInstall(List<String> installs)
    {
        this.installations = new ArrayList<String>(installs);
    }

}

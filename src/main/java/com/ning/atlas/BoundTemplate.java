package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.tree.Tree;
import com.ning.atlas.upgrade.UpgradePlan;

import java.util.Collection;
import java.util.concurrent.Executor;

public abstract class BoundTemplate implements Tree
{
    private final String type;
    private final String name;
    private final My     my;

    protected BoundTemplate(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public My getMy()
    {
        return my;
    }

    public abstract Collection<? extends BoundTemplate> getChildren();

    public abstract ListenableFuture<? extends ProvisionedElement> provision(ErrorCollector collector, Executor exec);

    public abstract UpgradePlan upgradeFrom(InstalledElement initialState);
}

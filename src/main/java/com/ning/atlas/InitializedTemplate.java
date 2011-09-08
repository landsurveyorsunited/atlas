package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;

import java.util.Collection;
import java.util.concurrent.Executor;

public abstract class InitializedTemplate implements Thing
{
    private final Identity id;
    private final String type;
    private final String name;
    private final My     my;

    public InitializedTemplate(Identity id, String type, String name, My my)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.my = my;
    }

    public Identity getId()
    {
        return id;
    }

    public My getMy()
    {
        return my;
    }

    public final String getName()
    {
        return name;
    }

    public final String getType()
    {
        return type;
    }

    @Override
    public abstract Collection<? extends Thing> getChildren();

    public final ListenableFuture<? extends InstalledElement> install(ErrorCollector ec, Executor exec)
    {
        return install(ec, exec, this);
    }

    protected abstract ListenableFuture<? extends InstalledElement> install(ErrorCollector ec, Executor exec, InitializedTemplate root);
}

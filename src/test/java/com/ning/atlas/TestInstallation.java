package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestInstallation
{
    @Test
    public void testFoo() throws Exception
    {
        final AtomicBoolean installed = new AtomicBoolean(false);
        Installer installer = new Installer()
        {
            @Override
            public void install(Server server, String fragment, InitializedTemplate root)
            {
                installed.set(true);
                assertThat(fragment, equalTo("waffles-1.2"));
            }
        };

        Environment env = new Environment("env");
        env.addInstaller("ugx", installer);
        Base base = new Base("server", env, "fake", Collections.<Initialization>emptyList(), Collections.<String, String>emptyMap());

        InitializedServer child = new InitializedServer("server",
                                                        "0",
                                                        new My(),
                                                        new Server("10.0.0.1", "10.0.0.1"),
                                                        Arrays.asList("ugx:waffles-1.2"),
                                                        base);

        InitializedSystem root = new InitializedSystem("top", "0", new My(), asList(child));

        ListenableFuture<? extends InstalledElement> f = root.install(MoreExecutors.sameThreadExecutor());
        InstalledElement it = f.get();
        assertThat(it, notNullValue());
        assertThat(installed.get(), equalTo(true));
    }
}
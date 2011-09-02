package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.tree.Trees;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestInitialization
{
    @Test
    public void testInitialize() throws Exception
    {

        final AtomicBoolean initialized = new AtomicBoolean(false);
        Initializer initializer = new Initializer()
        {
            @Override
            public void initialize(Server server, String arg, ProvisionedElement root,
                                     ProvisionedServer node)
            {
                initialized.set(true);
                assertThat(arg, equalTo("meow"));
            }
        };

        Environment env = new Environment("env");
        env.addInitializer("woof", initializer);

        Base base = new Base("server", env, "fake", ImmutableList.<Initialization>of(Initialization.parseUriForm("woof:meow")), Collections
            .<String, String>emptyMap());

        List<? extends ProvisionedElement> children = asList(
            new ProvisionedServer("server", "0", new My(),
                                  new Server("10.0.0.1", "10.0.0.1"), Collections.<String>emptyList(), base));

        ProvisionedSystem root = new ProvisionedSystem("root", "0", new My(), children);
        InitializedTemplate initialized_root = root.initialize(MoreExecutors.sameThreadExecutor()).get();

        assertThat(initialized.get(), equalTo(true));

        initialized_root.getChildren();
    }

    @Test
    public void testMyAttributesProp() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/test_initialization.rb"));
        Template t = p.parseSystem(new File("src/test/ruby/test_initialization.rb"));
        ExecutorService s = MoreExecutors.sameThreadExecutor();

        BoundTemplate bound = t.normalize(e);
        List<BoundServer> lbs = Trees.findInstancesOf(bound, BoundServer.class);
        BoundServer bs = lbs.get(0);
        assertThat(String.valueOf(bs.getMy().get("waffles")), equalTo("pancakes"));

        ProvisionedElement ps = bound.provision(s).get();
        ProvisionedServer pss = Trees.findInstancesOf(ps, ProvisionedServer.class).get(0);
        assertThat(String.valueOf(pss.getMy().get("waffles")), equalTo("pancakes"));

        InitializedTemplate is = ps.initialize(s).get();
        InitializedServer iss = Trees.findInstancesOf(is, InitializedServer.class).get(0);
        assertThat(String.valueOf(iss.getMy().get("waffles")), equalTo("pancakes"));

        InstalledElement ins = is.install(s).get();
        InstalledServer inss = Trees.findInstancesOf(ins, InstalledServer.class).get(0);
        assertThat(String.valueOf(inss.getMy().get("waffles")), equalTo("pancakes"));
    }


}
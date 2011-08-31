package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestBase
{
    @Test
    public void testFoo() throws Exception
    {
        final List<String> inits = Lists.newArrayList();
        Environment env = new Environment("unit-test");
        env.addInitializer("waffle", new Initializer()
        {
            @Override
            public Server initialize(Server server, String arg, ProvisionedElement root, ProvisionedServer node)
            {
                inits.add("waffle+" + arg);
                return server;
            }
        });

        env.addInitializer("pancake", new Initializer()
        {
            @Override
            public Server initialize(Server server, String arg, ProvisionedElement root,
                                     ProvisionedServer node)
            {
                inits.add("pancake+" + arg);
                return server;
            }
        });

        Base base = new Base("test", env, "hello",
                             ImmutableList.<Initialization>of(Initialization.parseUriForm("waffle:hut"),
                                                              Initialization.parseUriForm("pancake:house")),
                             Collections.<String, String>emptyMap());

        Server s = new Server("10.0.0.1", "10.0.0.1");
        base.initialize(s,
                        new ProvisionedSystem("root", "0", new My(), Lists.<ProvisionedElement>newArrayList()),
                        new ProvisionedServer("server", "waffle", new My(), s, Collections.<String>emptyList(), base));

        assertThat(inits, equalTo(asList("waffle+hut", "pancake+house")));


    }


}

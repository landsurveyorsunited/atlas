package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.chef.StubServer;
import org.junit.Test;

import javax.naming.ldap.InitialLdapContext;
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
            public Server initialize(Server server, String arg, ProvisionedTemplate root, ProvisionedServerTemplate node)
            {
                inits.add("waffle+" + arg);
                return server;
            }
        });

        env.addInitializer("pancake", new Initializer()
        {
            @Override
            public Server initialize(Server server, String arg, ProvisionedTemplate root,
                                     ProvisionedServerTemplate node)
            {
                inits.add("pancake+" + arg);
                return server;
            }
        });

        Base base = new Base("test", env);
        base.addInit("waffle:hut");
        base.addInit("pancake:house");

        Server s = new StubServer("10.0.0.1");
        base.initialize(s,
                        new ProvisionedSystemTemplate("root", "0", new My(), Lists.<ProvisionedTemplate>newArrayList()),
                        new ProvisionedServerTemplate("server", "waffle", new My(), s, Collections.<String>emptyList()));

        assertThat(inits, equalTo(asList("waffle+hut", "pancake+house")));


    }


}

package com.ning.atlas.components.packages;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;

import java.util.Map;
import java.util.concurrent.Future;

public class GemInstaller extends ConcurrentComponent
{
    private static final Logger log = Logger.get(GemInstaller.class);
    private final String credentialName;

    public GemInstaller(Map<String, String> attributes)
    {
        this.credentialName = attributes.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        SSH ssh = new SSH(host, d.getSpace(), credentialName);
        try {
            String out = ssh.exec("sudo gem install " + uri.getFragment().replaceAll(",", " ") + " --no-ri --no-rdoc");
            log.info(out);
            return out;
        }
        finally {
            ssh.close();
        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        log.info("unwinding %s on %s", uri, hostId);
        SSH ssh = new SSH(hostId, credentialName, d.getSpace());
        try {
            String out = ssh.exec("sudo gem uninstall " + uri.getFragment().replaceAll(",", " "));
            log.info(out);
            return out;
        }
        finally {
            ssh.close();
        }
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture(String.format("install gems " + uri.getFragment()));
    }
}

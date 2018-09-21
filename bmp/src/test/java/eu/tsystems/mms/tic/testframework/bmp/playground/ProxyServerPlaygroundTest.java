/*
 * (C) Copyright T-Systems Multimedia Solutions GmbH 2018, ..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Peter Lehmann <p.lehmann@t-systems.com>
 *     pele <p.lehmann@t-systems.com>
 */
package eu.tsystems.mms.tic.testframework.bmp.playground;

import eu.tsystems.mms.tic.testframework.bmp.AbstractTest;
import eu.tsystems.mms.tic.testframework.bmp.BMProxyManager;
import eu.tsystems.mms.tic.testframework.bmp.ProxyServer;
import eu.tsystems.mms.tic.testframework.utils.TestUtils;
import net.lightbody.bmp.core.har.Har;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.testng.annotations.Test;

/**
 * Created by pele on 20.10.2014.
 */
public class ProxyServerPlaygroundTest extends AbstractTest {

    @Test
    public void testProxyServer() {
        HttpHost proxyHost = new HttpHost("proxy.mms-dresden.de", 8080);
        UsernamePasswordCredentials credentials = null;
        ProxyServer proxyServer = new ProxyServer(9999, proxyHost, credentials);

        proxyServer.startCapture();
        //...
        Har har = proxyServer.stopCapture();
    }

    @Test
    public void testMultipleInstances() throws Exception {

        HttpHost proxyHost = new HttpHost("proxy.mms-dresden.de", 8080);
        UsernamePasswordCredentials credentials = null;
        ProxyServer proxyServer1 = new ProxyServer(9991, proxyHost, credentials);
        ProxyServer proxyServer2 = new ProxyServer(9992, proxyHost, credentials);
        ProxyServer proxyServer3 = new ProxyServer(9993, proxyHost, credentials);

        TestUtils.sleep(2000);

        proxyServer1.stopProxy();
        proxyServer2.stopProxy();
        proxyServer3.stopProxy();
    }

    @Test
    public void testBMPManger() throws Exception {
        final ProxyServer proxyServer = BMProxyManager.getProxyServer();

        TestUtils.sleep(3000);

        BMProxyManager.shutDownProxyServer();
    }
}

package com.cadit.main;


import com.cadit.data.DocumentRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * A sample program which acts a remote client for a EJB deployed on JBoss EAP server. This program shows how to lookup stateful and
 * stateless beans via JNDI and then invoke on them
 *

 */
public class RemoteEJBClient {

    private static final String HTTP = "http";

    public static void main(String[] args) throws Exception {
        // Invoke a stateless bean
        invokeStatelessBean();
    }

    /**
     * Looks up a stateless bean and invokes on it
     *
     * @throws NamingException
     */
    private static void invokeStatelessBean() throws NamingException {
        // Let's lookup the remote stateless ejb
        final DocumentRemote statelessRemoteDocumentCreator = lookupRemoteStatelessBean();
        System.out.println("Obtained a remote stateless calculator for invocation");
        statelessRemoteDocumentCreator.pdfCreator();
    }


    /**
     * Looks up and returns the proxy to remote stateless bean
     *
     * @return
     * @throws NamingException
     */
    private static DocumentRemote lookupRemoteStatelessBean() throws NamingException {
        final Hashtable<String, Object> jndiProperties = new Hashtable<>();

        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, org.jboss.naming.remote.client.InitialContextFactory.class.getName());
        if (Boolean.getBoolean(HTTP)) {
            //use HTTP based invocation. Each invocation will be a HTTP request
            jndiProperties.put(Context.PROVIDER_URL, "http://localhost:8080/wildfly-services");
        } else {
            //use HTTP upgrade, an initial upgrade requests is sent to upgrade to the remoting protocol
            jndiProperties.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        }

        jndiProperties.put("jboss.naming.client.ejb.context", true);
        final Context context = new InitialContext(jndiProperties);

        // The JNDI lookup name for a stateless session bean has the syntax of:
        // ejb:<appName>/<moduleName>/<distinctName>/<beanName>!<viewClassName>
        //
        // <appName> The application name is the name of the EAR that the EJB is deployed in
        // (without the .ear). If the EJB JAR is not deployed in an EAR then this is
        // blank. The app name can also be specified in the EAR's application.xml
        //
        // <moduleName> By the default the module name is the name of the EJB JAR file (without the
        // .jar suffix). The module name might be overridden in the ejb-jar.xml
        //
        // <distinctName> : EAP allows each deployment to have an (optional) distinct name.
        // This example does not use this so leave it blank.
        //
        // <beanName> : The name of the session been to be invoked.
        //
        // <viewClassName>: The fully qualified classname of the remote interface. Must include
        // the whole package name.

        // let's do the lookup
        return (DocumentRemote) context.lookup("ejb:CQ-EV-ear/CQ-EV-business//DocumentCreatorBean!"
                + DocumentRemote.class.getName());
    }
}


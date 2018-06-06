package com.cadit.main;


import com.cadit.data.DocumentRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * A sample program which acts a remote client for a EJB deployed on JBoss EAP server.
 * is program shows how to lookup
 * stateless beans via JNDI and then invoke on them
 * TODO errore che mi da (stesso errore se chiamato dal StartupTriggerEvents tramite l'annotazione @ejb, gli standalone-full.xml sono in wildfly-configuations , i server sono avviati
 * TODO sempre con standalone.bat ma con l'opzione -server-config=standalone-full.xml
 *
 * Jun 06, 2018 4:26:00 PM org.xnio.Xnio <clinit>
 INFO: XNIO version 3.4.0.Final
 Jun 06, 2018 4:26:00 PM org.xnio.nio.NioXnio <clinit>
 INFO: XNIO NIO Implementation Version 3.4.0.Final
 Jun 06, 2018 4:26:01 PM org.jboss.remoting3.EndpointImpl <clinit>
 INFO: JBoss Remoting version 4.0.21.Final
 Jun 06, 2018 4:26:01 PM org.jboss.ejb.client.EJBClient <clinit>
 INFO: JBoss EJB Client version 2.1.4.Final
 Obtained a remote stateless calculator for invocation
 Exception in thread "main" java.lang.IllegalStateException: EJBCLIENT000025: No EJB receiver available for handling [appName:CQ-EV-ear, moduleName:CQ-EV-business, distinctName:] combination for invocation context org.jboss.ejb.client.EJBClientInvocationContext@1e88b3c
 at org.jboss.ejb.client.EJBClientContext.requireEJBReceiver(EJBClientContext.java:798)
 at org.jboss.ejb.client.ReceiverInterceptor.handleInvocation(ReceiverInterceptor.java:128)
 at org.jboss.ejb.client.EJBClientInvocationContext.sendRequest(EJBClientInvocationContext.java:186)
 at org.jboss.ejb.client.EJBInvocationHandler.sendRequestWithPossibleRetries(EJBInvocationHandler.java:255)
 at org.jboss.ejb.client.EJBInvocationHandler.doInvoke(EJBInvocationHandler.java:200)
 at org.jboss.ejb.client.EJBInvocationHandler.doInvoke(EJBInvocationHandler.java:183)
 at org.jboss.ejb.client.EJBInvocationHandler.invoke(EJBInvocationHandler.java:146)
 at com.sun.proxy.$Proxy2.pdfCreator(Unknown Source)
 at com.cadit.main.RemoteEJBClient.invokeStatelessBean(RemoteEJBClient.java:36)
 at com.cadit.main.RemoteEJBClient.main(RemoteEJBClient.java:24)

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
            jndiProperties.put(Context.PROVIDER_URL, "http://127.0.0.1:8080/wildfly-services");
        } else {
            //use HTTP upgrade, an initial upgrade requests is sent to upgrade to the remoting protocol
            jndiProperties.put(Context.PROVIDER_URL, "http-remoting://127.0.0.1:8080");
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
        DocumentRemote lookup = (DocumentRemote) context.lookup("ejb:CQ-EV-ear/CQ-EV-business//DocumentCreatorBean!"
                + DocumentRemote.class.getName());
//        context.close();
        return lookup;
    }
}


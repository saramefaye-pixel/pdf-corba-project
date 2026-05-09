package com.pdfcorba;

import PDFServiceModule.PDFServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class CORBAServer {

    public static void main(String[] args) {
        try {
            // 1. Démarrer le health check HTTP sur port 8090
            startHealthCheck();

            System.out.println("Démarrage du serveur CORBA...");

            // 2. Initialiser l'ORB
            ORB orb = ORB.init(args, null);

            // 3. Obtenir le POA
            POA rootPOA = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA")
            );
            rootPOA.the_POAManager().activate();

            // 4. Créer et activer notre implémentation
            PDFServiceImpl impl = new PDFServiceImpl();
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(impl);

            // 5. Enregistrer dans le Name Service
            org.omg.CORBA.Object nameServiceObj =
                orb.resolve_initial_references("NameService");
            NamingContextExt namingContext =
                NamingContextExtHelper.narrow(nameServiceObj);

            NameComponent[] path = namingContext.to_name("PDFService");
            namingContext.rebind(path, ref);

            System.out.println("✅ Serveur CORBA prêt - PDFService enregistré !");

            // 6. Attendre les requêtes
            orb.run();

        } catch (Exception e) {
            System.err.println("❌ Erreur serveur CORBA : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Serveur HTTP minimal pour les health checks de Render
    private static void startHealthCheck() throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8090), 0
        );
        server.createContext("/health", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("✅ Health check HTTP démarré sur port 8090");
    }
}

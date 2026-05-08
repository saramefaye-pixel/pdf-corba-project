package com.pdfcorba;

import PDFServiceModule.PDFServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class CORBAServer {

    public static void main(String[] args) {
        try {
            System.out.println("Démarrage du serveur CORBA...");

            // 1. Initialiser l'ORB (Object Request Broker)
            ORB orb = ORB.init(args, null);

            // 2. Obtenir le POA (Portable Object Adapter)
            POA rootPOA = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA")
            );
            rootPOA.the_POAManager().activate();

            // 3. Créer et activer notre implémentation
            PDFServiceImpl impl = new PDFServiceImpl();
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(impl);

            // 4. Enregistrer dans le Name Service
            org.omg.CORBA.Object nameServiceObj =
                orb.resolve_initial_references("NameService");
            NamingContextExt namingContext =
                NamingContextExtHelper.narrow(nameServiceObj);

            NameComponent[] path = namingContext.to_name("PDFService");
            namingContext.rebind(path, ref);

            System.out.println("✅ Serveur CORBA prêt - PDFService enregistré !");

            // 5. Attendre les requêtes
            orb.run();

        } catch (Exception e) {
            System.err.println("❌ Erreur serveur CORBA : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.pdfcorba.service;

import PDFServiceModule.PDFService;
import PDFServiceModule.PDFServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Service
public class CORBAClientService {

    @Value("${corba.host}")
    private String corbaHost;

    @Value("${corba.port}")
    private String corbaPort;

    private PDFService pdfService;

    // Appelé automatiquement au démarrage de Spring
    @PostConstruct
    public void init() {
        try {
            System.out.println("Connexion au serveur CORBA : " + corbaHost + ":" + corbaPort);

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialHost", corbaHost);
            props.put("org.omg.CORBA.ORBInitialPort", corbaPort);

            ORB orb = ORB.init(new String[]{}, props);

            org.omg.CORBA.Object nameServiceObj =
                orb.resolve_initial_references("NameService");
            NamingContextExt namingContext =
                NamingContextExtHelper.narrow(nameServiceObj);

            org.omg.CORBA.Object ref = namingContext.resolve_str("PDFService");
            pdfService = PDFServiceHelper.narrow(ref);

            System.out.println("✅ Connecté au serveur CORBA !");

        } catch (Exception e) {
            System.err.println("❌ Erreur connexion CORBA : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PDFService getPdfService() {
        return pdfService;
    }
}

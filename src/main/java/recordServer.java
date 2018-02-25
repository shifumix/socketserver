import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class recordServer implements Runnable {
    String address=null;
    String myIp=null;
    Boolean connected=false;
    Long dtLastConnexion=0L;
    ScheduledExecutorService scheduler=null;
    private static Logger logger = Logger.getLogger(String.valueOf(recordServer.class));

    public recordServer(String address, String myIp,ScheduledExecutorService scheduler) {
        this.address = address;
        this.myIp = myIp;
        this.scheduler=scheduler;
        logger.info("Preparation demande de connexion pour "+address);
    }

    @Override
    public void run() {
        logger.info("Tache d'enregistrement du server");
        this.dtLastConnexion=System.currentTimeMillis();
        if(this.connected==false){
            logger.info("Demande d'ajout a "+address);
            String s=Tools.rest(address+"/_ah/api/shifumix/v1/addserver?server="+ java.net.URLEncoder.encode(myIp)+"&type=socket&sc=hh4271");
            if(s==null)logger.warning("Demande non reçu par le serveur");
            scheduler.schedule(this,30, TimeUnit.SECONDS);
        }
    }
}

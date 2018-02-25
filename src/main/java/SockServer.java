import org.eclipse.jetty.websocket.api.Session;
import spark.Spark;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static spark.Spark.*;

public class SockServer {
    // this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    static Map<Session, User> userUsernameMap =new ConcurrentHashMap<Session, User>();
    static Map<Session, Screen> screensMap =new ConcurrentHashMap<Session, Screen>();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Logger logger = Logger.getLogger(String.valueOf(SockServer.class));
    static recordServer rs=null;

    static int nextUserNumber = 1; //Used for creating the next username

    public static void main(String[] args) {
        logger.info("Lancement du SocketServer. Syntax:<Server_address> <port> <local_adresse>");

        String serverAddress="https://shifumixweb.appspot.com";
        if(args.length>0)serverAddress=args[0];
        if(!serverAddress.startsWith("http"))serverAddress="http://"+serverAddress;

        String port="4567";
        if(args.length>1)port=args[1];

        String myIp=Tools.rest("http://checkip.amazonaws.com/").replace("\n","")+":"+port;
        if(args.length>2)myIp=args[2]+":"+port;
        if(!myIp.startsWith("http"))myIp="http://"+myIp;

        logger.info("Adresse IP="+myIp+", port="+port);

        webSocket("/chat", ChatWebSocketHandler.class);
        port(Integer.valueOf(port));

        if(args.length>3 && args[3].equals("secure")){
            myIp=myIp.replace("http:","https:");
            secure("hhoareau.jks","hh4271!!",null,null);
        }

        rs= new recordServer(serverAddress, myIp,scheduler);

        get("/state", (request, response) -> {
            String rc="Users connected:"+userUsernameMap.size();
            rc+="<br>Screens connected:"+screensMap.size();
            rc+="<br>MyIp="+rs.myIp+"<br>Shifumix server="+rs.address+" - Connected:"+rs.connected+"<br>";
            rc+="LastConnexion:"+new SimpleDateFormat("HH:mm").format(new Date(rs.dtLastConnexion));

            if(userUsernameMap.size()>0){
                rc+="<br>Users:";for(User u:userUsernameMap.values())rc+=u.toHTML();
            }

            return rc+"<br><br>" +
                    "<a target='_blank' href='"+rs.myIp+"/reconnect'>Reconnect</a><br>" +
                    "<a target='_blank' href='https://console.cloud.google.com/datastore/entities/query?project=shifumixweb&ns=&kind=ShifuServer'>Servers</a>" +
                    "<a target='_blank' href='"+rs.myIp+"/sendToAll?message=close'>Close client</a>";
        });


        post("/order/:event", (request, response) -> {
            rs.connected=true;
            SockServer.broadcastMessage("server",request.params(":event"),request.body());
            return "ok";
        });

        post("/refresh", (request, response) -> {
            rs.connected=true;
            SockServer.broadcastMessage("server",null,"refresh");
            return "ok";
        });

        post("/close", (request, response) -> {
            rs.connected=false;
            rs.run();
            return "ok";
        });

        get("/reconnect", (request, response) -> {
            rs.connected=false;
            rs.run();
            return "in progress";
        });

        get("/sendtoall", (request, response) -> {
            return broadcastMessage(request.host(),"all",request.params("message"));
        });


        post("/join", (request, response) -> {
            String from=request.params("from");
            if(from==null)from=request.host();
            logger.info(from+" adresse un join");

            rs.connected=true;
            return System.currentTimeMillis();
        });

        init();

        scheduler.schedule(rs, 30, TimeUnit.SECONDS);
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static String broadcastMessage(String sender, String event,String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                if(event!=null){
                    if(event.equals(userUsernameMap.get(session).idEvent) || event.equals("all")){
                        session.getRemote().sendString(message);
                        //rc=rc+session.getRemoteAddress().getHostString()+";";
                    }

                }

                if(event==null && userUsernameMap.get(session).idEvent.length()==0){
                    session.getRemote().sendString(message);
                    //rc[0] +=session.getRemoteAddress().getHostString()+";";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return "Done";
    }

}

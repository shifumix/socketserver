import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.logging.Logger;

@WebSocket
public class ChatWebSocketHandler {
    private static Logger logger = Logger.getLogger(String.valueOf(SockServer.class));
    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session s) throws Exception {
        logger.info("onConnect:"+s.getRemoteAddress().getHostString());
        SockServer.userUsernameMap.put(s, new User());
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        logger.info("onClose:"+user.getRemoteAddress().getHostString());
        User u= SockServer.userUsernameMap.get(user);
        SockServer.userUsernameMap.remove(user);
    }

    @OnWebSocketError
    public void methodName(Session session, Throwable error){
        logger.info("onError:"+session.getRemoteAddress().getHostString());
        SockServer.userUsernameMap.remove(session);
    }


    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        logger.info("onMessage:"+user.getRemoteAddress().getHostString()+":"+message);
        SockServer.userUsernameMap.get(user).idUser=message.split(",")[0];
        if(message.split(",")[1].startsWith("Screen"))
            SockServer.userUsernameMap.get(user).idScreen=message.split(",")[1];
        else
            SockServer.userUsernameMap.get(user).idEvent=message.split(",")[1];
    }
}

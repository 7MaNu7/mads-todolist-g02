import play.libs.ws.*;

 public class WSUtils {
    //metodo helper que devuelve el token de sesion de un usuario
    public static String getSessionCookie(String login,String password) {
        //si no logea bien, devolvera un token null
        String sessionCookie=null;
        //hacemos el post de login
        WSResponse wsResponse = WS.url("http://localhost:3333/login")
            .setFollowRedirects(false)
            .setContentType("application/x-www-form-urlencoded")
            .post("login=" + login + "&password=" + password)
            .get(20000);
        //si esta la cookie, la sacamos
        if(wsResponse.getCookie("PLAY_SESSION")!=null) {
            sessionCookie = "PLAY_SESSION=" + wsResponse.getCookie("PLAY_SESSION").getValue();
        }

        return sessionCookie;
    }
}

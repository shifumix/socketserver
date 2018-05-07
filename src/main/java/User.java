public class User {
    public String idUser="";
    public String idEvent="";
    public String idScreen="";

    public User() {}

    public String toHTML() {
        return "<br>idUser="+idUser+" - idEvent="+idEvent+" - idScreen="+idScreen;
    }
}

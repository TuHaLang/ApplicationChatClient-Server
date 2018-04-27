package Server;

public class Messages {
    private String username="";
    private String content="";
    private int idSocket;

    public Messages(String username, String content, int idSocket) {
        this.username = username;
        this.content = content;
        this.idSocket = idSocket;
    }

    public Messages(){
        super();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIdSocket() {
        return idSocket;
    }

    public void setIdSocket(int idSocket) {
        this.idSocket = idSocket;
    }
}

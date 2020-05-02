package co.za.gmapssolutions.beatraffic.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private long id;
    private String type;
    public User(long id, String type){
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
    public static JSONObject userToJson(User user) throws JSONException {
        JSONObject jsonUser =  new JSONObject();
        jsonUser.put("id",user.getId());
        jsonUser.put("type",user.getType());
        return jsonUser;
    }
}

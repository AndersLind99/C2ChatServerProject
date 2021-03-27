package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class UserService {
    private final HashSet<String> users = new HashSet<String>();

    UserService() {
        this.users.add("Anders");
        this.users.add("Lukas");
        this.users.add("Nicklas");
        this.users.add("Simon");
    }


    public boolean usernameExists(String name) {
        return this.users.contains(name);
    }
}

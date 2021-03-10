package server;

import java.util.ArrayList;
import java.util.List;

 class UserLogin {

    public List<String> listOfUsers() {
      //  private ArrayList<User> users = new ArrayList<User>();

        List<String> usersList = new ArrayList<String>();

        usersList.add("Anders");
        usersList.add("Lukas");
        usersList.add("Nicklas");
        usersList.add("Simon");


        return (usersList);
    }
}

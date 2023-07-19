//@Charlie Dean
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

public class FrontEnd implements Auction{





    public static Replicas server;
    public static int i = 0;

    //A function that fetches the servers currently operating at registry localhost
    public static ArrayList<Replicas> getServerList()
    {
      //Creates an empty array 
      ArrayList<Replicas> serverList = new ArrayList<Replicas>();
      try{
        //Creates a registry of servers at registry localhost
        Registry registry2= LocateRegistry.getRegistry("localhost");
        //For all servers found in the registry, it adds them to serverlist
        for(String name : registry2.list())
        {
          try{
            Replicas serverStub = (Replicas) registry2.lookup(name);
            serverList.add(serverStub);
          }
          catch (Exception exc) {
          }
        }
      }
      catch (Exception exc) {
        System.err.println("Client exception: Second " + exc.toString());
        exc.printStackTrace();
      }
        //returns serverList
        return serverList;
    }

    //A function that connects the user to a random Replica found in the serverList
    public static void connection(){
      try {
        boolean d = true;
        //Creates a Array list containing all running Severs.
        ArrayList<Replicas> servers = getServerList();
        //Runs a while loop while d is equal to true
        while (d){
          Random rand = new Random();
          boolean a = true;
          //Runs a while loop creating a random number
          //This random number is between 1 and the server size and picks the server the FrontEnd Will innitially use
          while (a){
            i = rand.nextInt(servers.size());
            if (i == 0){
              i = rand.nextInt(servers.size());
              a=false;
            }
            if (i!=0){
              a =false;
            }
          }
          //Defines the Primary server as the random server chosen above
          server = servers.get(i);
          try {  
            //Informs the User of the current ID they are connected to. 
            System.out.println("Connected to server ID: " + server.getPrimaryReplicaID());  
            //If a server is found it goes out of the while loop.
            d=false;
          }
          catch (Exception e) {
            //If the server found doesnt exist it reruns the server list and finds another server
            servers = getServerList();
          }
        } 
        }
        catch (Exception e) {
          //If it runs into a problem, this means the FrontEnd couldnt find a replica
          System.out.println("Cant Find a Replica");
        }
    }

     public static void main(String[] args) throws RemoteException {
      //Creates a RMI Service advertised with the name "FrontEnd"
      FrontEnd s = new FrontEnd();
      String name = "FrontEnd";
      Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
      Registry registry = LocateRegistry.getRegistry("localhost");
      registry.rebind(name, stub);
      //Runs Connection to connect to the initial server
      connection();
    }

    //THis function runs every time an action is called
    //It checks that the FrontEnd is still connected to the same Replica as Before
    //If not it reruns the connection function and connects to a new Replica
    public static void actionCalled() throws RemoteException{
      try {   
        System.out.println("Connected to server ID: " + server.getPrimaryReplicaID()); 
      }
      catch (Exception e) {
        System.out.println("Reconnecting");
        connection();
      }
    }

    //Creates the Function created in the Auction Interface.
    @Override
    public NewUserInfo newUser(String email) throws RemoteException {
      //Every time a function in Auction Interface is called actioncalled()
      //is run. This checks for replica failure
      actionCalled();
      //A variable is created running server aka the Primary Replicas Function
      NewUserInfo User = server.newUser(email);
      //After Information has been changed the FrontEnd runs the Primary Replicas Function called UpdateReplicas
      server.UpdateReplicas(i);
      //Returns the updated info
      return User;
    }

    //All functions below are structured the same as the above function.
    @Override
    public byte[] challenge(int userID) throws RemoteException {
      actionCalled();
      byte[] challenge = server.challenge(userID);
      server.UpdateReplicas(i);
      return challenge;
    }


    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
      actionCalled();
      boolean auth = server.authenticate(userID,signature);
      server.UpdateReplicas(i);
      return auth;
    }


    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
      actionCalled();
      AuctionItem Spec = server.getSpec(itemID);
      server.UpdateReplicas(i);
      return Spec;
    }


    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
      actionCalled();
      int newAuct = server.newAuction(userID, item);
      server.UpdateReplicas(i);
      return newAuct;
    }


    @Override
    public AuctionItem[] listItems() throws RemoteException {
      actionCalled();
      AuctionItem[] list = server.listItems();
      server.UpdateReplicas(i);
      return list;
    }


    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
      actionCalled();
      AuctionCloseInfo close = server.closeAuction(userID, itemID);
      server.UpdateReplicas(i);
      return close;
    }


    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
      actionCalled();
      boolean bid = server.bid(userID, itemID, price);
      server.UpdateReplicas(i);
      return bid;
    }


    @Override
    public int getPrimaryReplicaID() throws RemoteException {
      return server.getPrimaryReplicaID();
    }
    }